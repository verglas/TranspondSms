package com.tim.tsms.transpondsms;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tim.tsms.transpondsms.BroadCastReceiver.RebootBroadcastReceiver;
import com.tim.tsms.transpondsms.model.vo.FeedBackResult;
import com.tim.tsms.transpondsms.utils.HttpI;
import com.tim.tsms.transpondsms.utils.HttpUtil;
import com.tim.tsms.transpondsms.utils.UpdateAppHttpUtil;
import com.tim.tsms.transpondsms.utils.aUtil;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.UpdateCallback;
import com.vector.update_app.listener.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;


public class AboutActivity extends AppCompatActivity {
    private String TAG = "AboutActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Log.d(TAG, "onCreate: "+RebootBroadcastReceiver.class.getName());

        Switch check_with_reboot = (Switch)findViewById(R.id.switch_with_reboot);
        checkWithReboot(check_with_reboot);

        TextView version_now = (TextView)findViewById(R.id.version_now);
        Button check_version_now = (Button)findViewById(R.id.check_version_now);
        try {
            version_now.setText(aUtil.getVersionName(AboutActivity.this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        check_version_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNewVersion();
            }
        });

    }

    //检查重启广播接受器状态并设置
    private void checkWithReboot(Switch withrebootSwitch){
        //获取组件
        final ComponentName cm = new ComponentName(this.getPackageName(), RebootBroadcastReceiver.class.getName());

        final PackageManager pm = getPackageManager();
        int state = pm.getComponentEnabledSetting(cm);
        if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                && state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {

            withrebootSwitch.setChecked(true);
        }else{
            withrebootSwitch.setChecked(false);
        }
        withrebootSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int newState = (Boolean)isChecked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                pm.setComponentEnabledSetting(cm, newState,PackageManager.DONT_KILL_APP);
                Log.d(TAG,"onCheckedChanged:"+isChecked);
            }
        });
    }

    private void checkNewVersion(){
        String geturl = "http://api.allmything.com/api/version/hasnew?versioncode=";

        try {
            geturl+= aUtil.getVersionCode(AboutActivity.this);

            Log.i("SettingActivity",geturl);
            new UpdateAppManager
                    .Builder()
                    //当前Activity
                    .setActivity(AboutActivity.this)
                    //更新地址
                    .setUpdateUrl(geturl)
                    //全局异常捕获
                    .handleException(new ExceptionHandler() {
                        @Override
                        public void onException(Exception e) {
                            Log.e(TAG, "onException: ",e );
                            Toast.makeText(AboutActivity.this, "更新失败："+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    //实现httpManager接口的对象
                    .setHttpManager(new UpdateAppHttpUtil())
                    .build()
                    .checkNewApp(new UpdateCallback(){
                        /**
                         * 没有新版本
                         */
                        protected void noNewApp(String error) {
                            Toast.makeText(AboutActivity.this, "没有新版本", Toast.LENGTH_SHORT).show();
                        }
                    });
//                    .update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void feedbackcommit(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
        View view1 = View.inflate(AboutActivity.this, R.layout.dialog_feedback, null);

        final EditText feedback_et_email = view1.findViewById(R.id.feedback_et_email);
        final EditText feedback_et_text = view1.findViewById(R.id.feedback_et_text);

        builder
                .setTitle(R.string.feedback_input_text)
                .setView(view1)
                .create();
        builder.setPositiveButton("提交反馈",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    Map<String,String> feedBackData=new HashMap<>();
                    feedBackData.put("email",feedback_et_email.getText().toString());
                    feedBackData.put("text",feedback_et_text.getText().toString());
                    new HttpUtil().asyncPost("https://api.sl.willanddo.com/api/tsms/feedBack", feedBackData, new HttpI.Callback() {
                        @Override
                        public void onResponse(String result) {
                            Log.i(TAG, "onResponse: "+result);
                            if(result!=null){
                                FeedBackResult feedBackResult= JSON.parseObject(result, FeedBackResult.class);
                                Log.i(TAG, "feedBackResult: "+feedBackResult);

                                if(feedBackResult!=null){
                                    JSONObject feedBackResultObject= JSON.parseObject(result);
                                    Toast.makeText(AboutActivity.this,feedBackResultObject.getString("message"),Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(AboutActivity.this,"感谢您的反馈，我们将尽快处理！",Toast.LENGTH_LONG).show();

                                }
                            }else {
                                Toast.makeText(AboutActivity.this,"感谢您的反馈，我们将尽快处理！",Toast.LENGTH_LONG).show();

                            }

                        }
                        @Override
                        public void onError(String error) {
                            Log.i(TAG, "onError: "+error);
                            Toast.makeText(AboutActivity.this,error,Toast.LENGTH_LONG).show();

                        }
                    });

                }catch (Exception e){
                    Toast.makeText(AboutActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,"feedback e: "+e.getMessage());
                }


            }
        }).show();
    }

}
