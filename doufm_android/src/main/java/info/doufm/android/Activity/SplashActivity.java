package info.doufm.android.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

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

    private static final int LOADING_TIME = 1000; //启动画面显示时间

    private ConnectivityManager connectivityManager;

    private boolean bNetWorkStatus = false; //网络连接状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_actvity);

        LoadingProcess();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadingProcess();
    }

    private void LoadingProcess() {
        bNetWorkStatus = CheckNetworkStatus();
        if (bNetWorkStatus) {
            //如果连接到校园网
            StartMainActivity();
        } else {
            //提示用户设置Wifi
            StartMainActivity();//方便模拟器测试直接跳过Wifi设置
//
//            AlertDialog.Builder aletDialog = new AlertDialog.Builder(this);
//            aletDialog.setTitle("提示");
//            aletDialog.setMessage("当前无法连接到校园网,如果继续使用,请先设置网络");
//            aletDialog.setPositiveButton(R.string.splash_activity_set_wifi, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    Intent intent = null;
//                    if (Build.VERSION.SDK_INT > 11) {
//                        //Android 3.0以上版本跳转至Wifi设置界面
//                        intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                    } else {
//                        //Android 3.0以下版本跳转至Wifi设置界面
//                        intent = new Intent();
//                        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
//                        intent.setComponent(componentName);
//                        intent.setAction("android.intent.action.VIEW");
//                    }
//                    startActivity(intent);
//                }
//            });
//            aletDialog.setNegativeButton(R.string.splash_activity_quit, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    //无法使用网络退出
//                    finish();
//                }
//            });
//            aletDialog.show();
        }
    }

    private boolean CheckNetworkStatus() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //连接WIFI，后期需要再次判断是否是连接Stu-Wlan或者Xd-Wlan或者自建Wifi
                return true;
            }
        }
        return false;
    }

    private void StartMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }, LOADING_TIME);
    }
}
