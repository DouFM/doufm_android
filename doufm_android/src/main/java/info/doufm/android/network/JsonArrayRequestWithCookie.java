package info.doufm.android.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lsc on 2014/12/27.
 */
public class JsonArrayRequestWithCookie extends JsonArrayRequest {
    private Map<String,  String> mHeaders=new HashMap<>(1);

    public JsonArrayRequestWithCookie(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }
    //发送请求时，往Header中添加cookie，可以一并发送
    public void setCookie(String cookie) throws AuthFailureError {
        mHeaders.put("Cookie",cookie);
    }
}
