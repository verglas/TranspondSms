package com.tim.tsms.transpondsms.utils.sender;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.tim.tsms.transpondsms.utils.SettingUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.tim.tsms.transpondsms.SenderActivity.NOTIFY;

public class SenderDingdingMsg {

    static String TAG = "SenderDingdingMsg";

    public static void sendMsg(String msg) throws Exception {

        String webhook_token = SettingUtil.get_using_dingding_token();
        String webhook_secret = SettingUtil.get_using_dingding_secret();
        if (webhook_token.equals("")) {
            return;
        }
        if (!webhook_secret.equals("")) {
            Long timestamp = System.currentTimeMillis();

            String stringToSign = timestamp + "\n" + webhook_secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhook_secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64.encode(signData, Base64.NO_WRAP)), "UTF-8");
            webhook_token += "&timestamp=" + timestamp + "&sign=" + sign;
            Log.i(TAG, "webhook_token:" + webhook_token);

        }

        final String msgf = msg;
        String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"" + msg + "\"}}";
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                textMsg);

        final Request request = new Request.Builder()
                .url(webhook_token)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure：" + e.getMessage());
                SendHistory.addHistory("钉钉转发:" + msgf + "onFailure：" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                Log.d(TAG, "Code：" + String.valueOf(response.code()) + responseStr);
                SendHistory.addHistory("钉钉转发:" + msgf + "Code：" + String.valueOf(response.code()) + responseStr);
            }
        });
    }

    public static void sendMsg(final Handler handError, String token, String secret,String atMobiles,Boolean atAll, String msg) throws Exception {
        Log.i(TAG, "sendMsg token:"+token+" secret:"+secret+" atMobiles:"+atMobiles+" atAll:"+atAll+" msg:"+msg);

        if (token == null || token.isEmpty()) {
            return;
        }
        if (secret != null && !secret.isEmpty()) {
            Long timestamp = System.currentTimeMillis();

            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64.encode(signData, Base64.NO_WRAP)), "UTF-8");
            token += "&timestamp=" + timestamp + "&sign=" + sign;
            Log.i(TAG, "webhook_token:" + token);

        }

        Map textMsgMap =new HashMap();
        textMsgMap.put("msgtype","text");
        Map textText=new HashMap();
        textText.put("content",msg);
        textMsgMap.put("text",textText);
        if(atMobiles != null || atAll !=null){
            Map AtMap=new HashMap();
            if(atMobiles!=null){
                String[] atMobilesArray = atMobiles.split(",");
                List<String> atMobilesList=new ArrayList<>();
                for (String atMobile:atMobilesArray
                     ) {
                    if(TextUtils.isDigitsOnly(atMobile)){
                        atMobilesList.add(atMobile);
                    }
                }
                if(!atMobilesList.isEmpty()){
                    AtMap.put("atMobiles",atMobilesList);

                }
            }

            AtMap.put("isAtAll",false);
            if(atAll !=null){
                AtMap.put("isAtAll",atAll);

            }

            textMsgMap.put("at",AtMap);
        }

        String textMsg = JSON.toJSONString(textMsgMap);
        Log.i(TAG, "textMsg:" + textMsg);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                textMsg);

        final Request request = new Request.Builder()
                .url(token)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.d(TAG, "onFailure：" + e.getMessage());

                if(handError != null){
                    android.os.Message msg = new android.os.Message();
                    msg.what = NOTIFY;
                    Bundle bundle = new Bundle();
                    bundle.putString("DATA","发送失败：" + e.getMessage());
                    msg.setData(bundle);
                    handError.sendMessage(msg);
                }


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                Log.d(TAG, "Code：" + String.valueOf(response.code()) + responseStr);

                if(handError != null){
                    android.os.Message msg = new android.os.Message();
                    msg.what = NOTIFY;
                    Bundle bundle = new Bundle();
                    bundle.putString("DATA","发送状态：" + responseStr);
                    msg.setData(bundle);
                    handError.sendMessage(msg);
                    Log.d(TAG, "Coxxyyde：" + String.valueOf(response.code()) + responseStr);
                }

            }
        });
    }


}
