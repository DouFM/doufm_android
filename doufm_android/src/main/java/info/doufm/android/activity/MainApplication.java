package info.doufm.android.activity;

import android.app.Application;

import info.doufm.android.network.RequestManager;
import info.doufm.android.user.User;

/**
 * Created with Android Studio.
 * Time: 2014-12-12 03:28
 * Info:
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        //初始化Vollay
        RequestManager.init(this);
        //初始化用户类
        User.init(this);
    }
}
