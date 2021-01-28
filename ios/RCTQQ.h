//
//  RCTQQ.h
//  RCTQQ
//
//  Created by liangfengsid on 2021/1/19.
//  Copyright © 2021 Liang Feng All rights reserved.
//

#ifndef RCTQQ_h
#define RCTQQ_h

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import <React/RCTBridgeModule.h>

#import <TencentOpenAPI/TencentOAuth.h>

#define RCTWXEventName @"WeChat_Resp"

@interface RCTQQ : NSObject <RCTBridgeModule, TencentSessionDelegate>

@property NSString* appId;
@property TencentOAuth *oauth;

@end


#endif /* RCTQQ_h */
