//
//  RCTQQ.m
//  RCTQQ
//
//  Created by Liang Feng on 19 Jan 2021.
//  Copyright © 2021 Liang Feng. All rights reserved.
//

#import "RCTDingding.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTBridge.h>
#import <React/RCTLog.h>
#import <React/RCTImageLoader.h>

#import <DTShareKit/DTOpenKit.h>

// Define error messages
#define NOT_REGISTERED (@"registerApp required.")
#define INVOKE_FAILED (@"QQ API invoke returns false.")
#define RCTWXEventName @"WeChat_Resp"

@implementation RCTDingding

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

- (instancetype)init
{
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleOpenURL:) name:@"RCTOpenURLNotification" object:nil];
    }
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (BOOL)handleOpenURL:(NSNotification *)aNotification
{
    NSString * aURLString =  [aNotification userInfo][@"url"];
    NSURL * aURL = [NSURL URLWithString:aURLString];
    
    NSLog(@"in RCTDingding handleOpenURL, url:%@", aURLString);
    if ([DTOpenAPI handleOpenURL:aURL delegate:self])
    {
        return YES;
    } else {
        return NO;
    }
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

RCT_EXPORT_METHOD(registerDingdingApp:(NSString *)appid
                  :(RCTResponseSenderBlock)callback)
{
    self.appId = appid;
    callback(@[[DTOpenAPI registerApp:appid] ? [NSNull null] : INVOKE_FAILED]);
}

RCT_EXPORT_METHOD(isDingdingShareAvalable:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], @([DTOpenAPI isDingTalkInstalled] && [DTOpenAPI isDingTalkSupportOpenAPI])]);
}

RCT_EXPORT_METHOD(share:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    NSString * imagePath = data[@"imagePath"];
    if (imagePath != NULL) {
        [self sendImage: imagePath callback:callback];
        return;
    }
}

- (void)sendImage: (NSString *)path
                 callback:(RCTResponseSenderBlock)callback {
    DTSendMessageToDingTalkReq *sendMessageReq = [[DTSendMessageToDingTalkReq alloc] init];
    DTMediaMessage *mediaMessage = [[DTMediaMessage alloc] init];
    DTMediaImageObject *imgObject = [[DTMediaImageObject alloc] init];
    NSData *imgData = [NSData dataWithContentsOfFile:[[NSURL URLWithString:path] path]];
    imgObject.imageData = imgData;
    
    mediaMessage.mediaObject = imgObject;
    sendMessageReq.message = mediaMessage;
    
    BOOL result = [DTOpenAPI sendReq:sendMessageReq];
    callback(@[result ? [NSNull null] : INVOKE_FAILED]);
}

#pragma mark - DTOpenAPIDelegate
-(void) onReq:(DTBaseReq*)req
{
}

-(void) onResp:(DTBaseResp*)resp
{
    NSLog(@"In RCTDingding onResp resp: %@", resp);
    NSMutableDictionary *body = @{@"errCode":@(resp.errorCode)}.mutableCopy;
    body[@"errStr"] = resp.errorMessage;
    body[@"type"] = @"SendMessageToDD.Resp";
    [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
}

@end
