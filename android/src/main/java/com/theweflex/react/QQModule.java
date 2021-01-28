package com.theweflex.react;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.dingtalk.share.ddsharemodule.DDShareApiFactory;
import com.android.dingtalk.share.ddsharemodule.IDDAPIEventHandler;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by tdzl2_000 on 2015-10-10.
 */
public class QQModule extends ReactContextBaseJavaModule implements IUiListener {
    public static final String TAG = "RCTQQ";

    private static final String AUDIO_TYPE = "audio";
    private static final String PHOTO_TYPE = "photo";

    private String appId;
    private Tencent mTencent = null;
    private Callback mCallback = null;
    private int mExtarFlag = 0x00;
    private final static String NOT_REGISTERED = "registerApp required.";
    private final static String INVOKE_FAILED = "API invoke returns false.";
    private final static String INVALID_ARGUMENT = "invalid argument.";

    public QQModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "RCTQQ";
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

    private static ArrayList<QQModule> modules = new ArrayList<>();

    @Override
    public void initialize() {
        super.initialize();
        modules.add(this);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        if (mTencent != null) {
            mTencent = null;
        }
        if (mCallback != null) {
            mCallback = null;
        }
        modules.remove(this);
    }

    @ReactMethod
    // authorities Manifest文件中注册FileProvider时设置的authorities属性值
    public void registerQQApp(String appid, String authorities, Callback callback) {
        this.appId = appid;
        mTencent = Tencent.createInstance(appid, this.getReactApplicationContext().getApplicationContext(), authorities);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void isQQAppInstalled(Callback callback) {
        if (mTencent == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        callback.invoke(null, mTencent.isQQInstalled(this.getReactApplicationContext().getApplicationContext()));
    }


    @ReactMethod
    public void share(ReadableMap data, Callback callback) {
        if (mTencent == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        mCallback = callback;
        String type = data.getString("type");
        String title = data.getString("title");
        String appName = data.getString("appName");
        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtarFlag);
        switch (type) {
            case PHOTO_TYPE:
                sendLocalFile(data, params);
                break;
            case AUDIO_TYPE:
                sendLocalFile(data, params);
                break;
        }
    }

    /**
     * 通过图片本地路径方式分享图片消息
     * qq 纯图分享只支持本地图片
     */
    private void sendLocalFile(ReadableMap data, Bundle params) {
        String path = data.getString("path");
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path);
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        mTencent.shareToQQ(this.getCurrentActivity(), params, this);
    }

    private void sendAudio(ReadableMap data, Bundle params) {
        if (data.hasKey("targetUrl")) {
            String targetUrl = data.getString("targetUrl");
            Log.i(TAG, targetUrl);
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        }
        if (data.hasKey("summary")) {
            String summary = data.getString("summary");
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        }
        if (data.hasKey("imageUrl")) {
            String imageUrl = data.getString("imageUrl");
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        }
        if (data.hasKey("audioUrl")) {
            String audioUrl = data.getString("audioUrl");
            params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, audioUrl);
        }
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
        mTencent.shareToQQ(this.getCurrentActivity(), params, this);
    }

    @Override
    public void onComplete(Object o) {
        if (mCallback != null) {
            mCallback.invoke(null, "onComplete");
        }
    }

    @Override
    public void onError(UiError uiError) {
        if (mCallback != null) {
            mCallback.invoke(null, "onError code: "+uiError.errorCode+", msg: "+uiError.errorMessage);
        }
    }

    @Override
    public void onCancel() {
        if (mCallback != null) {
            mCallback.invoke(null, "onCancel");
        }
    }

    @Override
    public void onWarning(int code) {
        if (mCallback != null) {
            mCallback.invoke(null, "onWarning: "+code);
        }
    }
}
