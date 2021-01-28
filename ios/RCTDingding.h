//
//  RCTDingding.h
//  RCTWeChat
//
//  Created by liangfengsid on 2021/1/19.
//  Copyright © 2021 WeFlex. All rights reserved.
//

#ifndef RCTDingding_h
#define RCTDingding_h

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import <React/RCTBridgeModule.h>

#import <DTShareKit/DTOpenAPI.h>

#define RCTWXEventName @"WeChat_Resp"

@interface RCTDingding : NSObject <RCTBridgeModule, DTOpenAPIDelegate>

@property NSString* appId;

@end

#endif /* RCTDingding_h */
