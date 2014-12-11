package info.doufm.android.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Vollay请求帮助类
 * Created on 2014-12-07
 */
public class RequestManager {
    private static RequestQueue mRequestQueue;

    private RequestManager() {

    }

    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("No initialized");
        }
    }
}
