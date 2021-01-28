//
//  RCTQQ.m
//  RCTQQ
//
//  Created by Liang Feng on 19 Jan 2021.
//  Copyright © 2021 Liang Feng. All rights reserved.
//

#import "RCTQQ.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTBridge.h>
#import <React/RCTLog.h>
#import <React/RCTImageLoader.h>

#import <TencentOpenAPI/QQApiInterface.h>
#import <TencentOpenAPI/TencentOAuth.h>

// Define error messages
#define NOT_REGISTERED (@"registerApp required.")
#define INVOKE_FAILED (@"QQ API invoke returns false.")

@implementation RCTQQ

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
    
    if ([TencentOAuth HandleOpenURL:aURL])
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

RCT_EXPORT_METHOD(registerQQApp:(NSString *)appid :(NSString *)universalLink
                  :(RCTResponseSenderBlock)callback)
{
    self.appId = appid;
    self.oauth = [[TencentOAuth alloc] initWithAppId:appid andUniversalLink:universalLink andDelegate:self];
    callback(@[self.oauth != nil? [NSNull null] : INVOKE_FAILED]);
}

RCT_EXPORT_METHOD(isQQAppInstalled:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], @([QQApiInterface isSupportShareToQQ])]);
}

RCT_EXPORT_METHOD(share:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    NSString * type = data[@"type"];
    if ([type isEqualToString: @"photo"]) {
        [self sendImage: data callback:callback];
        return;
    } else if ([type isEqualToString: @"audio"]) {
        [self sendAudio:data callback:callback];
        return;
    }
}

- (void)sendImage: (NSDictionary *)data
         callback: (RCTResponseSenderBlock)callback {
    NSString * title = data[@"title"];
    NSString * appName = data[@"appName"];
    NSString * path = data[@"path"];
    NSData *imgData = [NSData dataWithContentsOfFile:[[NSURL URLWithString:path] path]];
    QQApiImageObject *imgObj = [QQApiImageObject objectWithData:imgData
        previewImageData:NULL
        title:title ? title : @"图片"
        description:appName ? appName: @""];
    
    SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:imgObj];
    QQApiSendResultCode ret = [QQApiInterface sendReq:req];
    callback(@[ret==EQQAPISENDSUCESS ? [NSNull null] : INVOKE_FAILED]);
}

- (void)sendAudio: (NSDictionary *)data
         callback: (RCTResponseSenderBlock)callback {
    NSString * title = data[@"title"];
    NSString * appName = data[@"appName"];
    NSString * path = data[@"path"];
    NSData *audioData = [NSData dataWithContentsOfFile:[[NSURL URLWithString:path] path]];
    QQApiFileObject *audioObj = [QQApiFileObject objectWithData:audioData previewImageData:nil title:title description:appName];
    
    SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:audioObj];
    QQApiSendResultCode ret = [QQApiInterface sendReq:req];
    
    callback(@[ret==EQQAPISENDSUCESS ? [NSNull null] : INVOKE_FAILED]);
}

#pragma mark - QQApiInterfaceDelegate
- (void)onReq:(QQBaseReq *)req
{
    
}

- (void)onResp:(QQBaseResp *)resp
{
    switch (resp.type)
    {
        case ESENDMESSAGETOQQRESPTYPE:
        {
            NSMutableDictionary *body = @{@"errCode":@(0)}.mutableCopy;
            body[@"errStr"] = resp.errorDescription;
            body[@"type"] = @"SendMessageToQQ.Resp";
            [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
            break;
        }
        default:
        {
            break;
        }
    }
}


@end
