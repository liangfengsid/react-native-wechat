package com.theweflex.react;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.android.dingtalk.share.ddsharemodule.DDShareApiFactory;
import com.android.dingtalk.share.ddsharemodule.IDDAPIEventHandler;
import com.android.dingtalk.share.ddsharemodule.IDDShareApi;
import com.android.dingtalk.share.ddsharemodule.ShareConstant;
import com.android.dingtalk.share.ddsharemodule.message.BaseReq;
import com.android.dingtalk.share.ddsharemodule.message.BaseResp;
import com.android.dingtalk.share.ddsharemodule.message.DDImageMessage;
import com.android.dingtalk.share.ddsharemodule.message.DDMediaMessage;
import com.android.dingtalk.share.ddsharemodule.message.SendAuth;
import com.android.dingtalk.share.ddsharemodule.message.SendMessageToDD;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by tdzl2_000 on 2015-10-10.
 */
public class DingdingModule extends ReactContextBaseJavaModule implements IDDAPIEventHandler {
    public static String TAG = "RCTDingding";

    private String appId;
    private IDDShareApi mIDDShareApi = null;
    private final static String NOT_REGISTERED = "registerApp required.";
    private final static String INVOKE_FAILED = "API invoke returns false.";
    private final static String INVALID_ARGUMENT = "invalid argument.";

    public DingdingModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "RCTDingding";
    }

    /**
     * fix Native module WeChatModule tried to override WeChatModule for module name RCTWeChat.
     * If this was your intention, return true from WeChatModule#canOverrideExistingModule() bug
     *
     * @return
     */
    public boolean canOverrideExistingModule() {
        return true;
    }

    private static ArrayList<DingdingModule> modules = new ArrayList<>();

    @Override
    public void initialize() {
        super.initialize();
        modules.add(this);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        if (mIDDShareApi != null) {
            mIDDShareApi = null;
        }
        modules.remove(this);
    }

    public static void handleIntent(Intent intent) {
        for (DingdingModule mod : modules) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mod.mIDDShareApi.handleIntent(intent, mod);
        }
    }

    @ReactMethod
    public void registerDingdingApp(String appid, Callback callback) {
        this.appId = appid;
        try {
            //activity的export为true，try起来，防止第三方拒绝服务攻击
            mIDDShareApi = DDShareApiFactory.createDDShareApi(this.getCurrentActivity(), appid, true);
            Intent intent = getCurrentActivity().getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callback.invoke(null, true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG , "e===========>"+e.toString());
            callback.invoke(null, false);
        }
    }

    @ReactMethod
    public void isDingdingShareAvalable(Callback callback) {
        if (mIDDShareApi == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        callback.invoke(null, mIDDShareApi.isDDAppInstalled()&&mIDDShareApi.isDDSupportAPI());
    }


    @ReactMethod
    public void share(ReadableMap data, Callback callback) {
        if (mIDDShareApi == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        String path = data.getString("imagePath");
        sendLocalImage(path, callback);
    }

    /**
     * 通过图片本地路径方式分享图片消息
     */
    private void sendLocalImage(String filePath, Callback callback) {
        //图片本地路径，开发者需要根据自身数据替换该数据
//        String path =  Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.png";

        File file = new File(filePath);
        if (!file.exists()) {
            callback.invoke(INVALID_ARGUMENT);
            return;
        }

        //初始化一个DDImageMessage
        DDImageMessage imageObject = new DDImageMessage();
        imageObject.mImagePath = filePath;

        //构造一个mMediaObject对象
        DDMediaMessage mediaMessage = new DDMediaMessage();
        mediaMessage.mMediaObject = imageObject;

        //构造一个Req
        SendMessageToDD.Req req = new SendMessageToDD.Req();
        req.mMediaMessage = mediaMessage;
        req.mTransaction = UUID.randomUUID().toString();
//        req.transaction = buildTransaction("image");

        //调用api接口发送消息
        callback.invoke(null, mIDDShareApi.sendReq(req));
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.i(TAG, "onReq=========>");
    }

    @Override
    public void onResp(BaseResp baseResp) {
        int errCode = baseResp.mErrCode;
        String errMsg = baseResp.mErrStr;
        WritableMap map = Arguments.createMap();
        map.putInt("errCode", errCode);
        map.putString("errStr", errMsg);
        map.putString("transaction", baseResp.mTransaction);
        Log.d(TAG , "errMsg==========>"+errMsg);
        if(baseResp.getType() == ShareConstant.COMMAND_SENDAUTH_V2 && (baseResp instanceof SendAuth.Resp)){
            SendAuth.Resp authResp = (SendAuth.Resp) baseResp;
            map.putString("type", "SendAuthToDD.Resp");
            map.putString("code", authResp.code);
            map.putString("state", authResp.state);

        }else{
            SendMessageToDD.Resp resp = (SendMessageToDD.Resp) baseResp;
            map.putString("type", "SendMessageToDD.Resp");
        }

        this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("WeChat_Resp", map);
    }
}
