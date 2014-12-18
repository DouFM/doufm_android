package info.doufm.android.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cn.pedant.SweetAlert.SweetAlertDialog;
import info.doufm.android.R;

/**
 * Created with Android Studio.
 * Date 2014-04-24
 * <p/>
 * 启动画面加载和网络状态判断
 *
 * @author Qichao Chen
 * @version 1.0
 */
public class SplashActivity extends Activity {

    private SweetAlertDialog mDialog;
    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what != 1) { // 无法连接服务器
                SetNetwork();
            } else { // 正确连接服务器
                if (mDialog == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }
    };

    private void SetNetwork() {
        if (mDialog == null) {
            mDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("网络连接出错啦...")
                    .setCancelText("设置网络")
                    .setConfirmText("试听一下!")
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            Intent intent = null;
                            if (Build.VERSION.SDK_INT > 11) {
                                //Android 3.0以上版本跳转至Wifi设置界面
                                intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            } else {
                                //Android 3.0以下版本跳转至Wifi设置界面
                                intent = new Intent();
                                ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                                intent.setComponent(componentName);
                                intent.setAction("android.intent.action.VIEW");
                            }
                            startActivity(intent);
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            startActivity(new Intent(SplashActivity.this, TryListenActivity.class));
                            finish();
                        }
                    });
            mDialog.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_actvity);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNetworkAvailable(h, 2000);
        MobclickAgent.onPageStart("Welcome");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Welcome");
        MobclickAgent.onPause(this);
    }

    public void isNetworkAvailable(final Handler handler, final int timeout) {
        new Thread() {
            private boolean responded = false;

            @Override
            public void run() {
                // set 'responded' to TRUE if is able to connect with google mobile (responds fast)
                new Thread() {
                    @Override
                    public void run() {
                        HttpGet requestForTest = new HttpGet("http://doufm.info");
                        try {
                            new DefaultHttpClient().execute(requestForTest); // can last...
                            responded = true;
                        } catch (Exception e) {
                        }
                    }
                }.start();

                try {
                    int waited = 0;
                    while (!responded && (waited < timeout)) {
                        sleep(100);
                        if (!responded) {
                            waited += 100;
                        }
                    }
                } catch (InterruptedException e) {
                } // do nothing
                finally {
                    if (!responded) {
                        handler.sendEmptyMessage(0);
                    } else {
                        handler.sendEmptyMessage(1);
                    }
                }
            }
        }.start();
    }
}
