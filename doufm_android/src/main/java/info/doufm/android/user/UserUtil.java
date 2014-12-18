package info.doufm.android.user;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.doufm.android.info.MusicInfo;
import info.doufm.android.network.RequestManager;
import info.doufm.android.utils.CacheUtil;

/**
 * 用户帮助类
 * Created on 2014-12-07
 */

/*
     类用法说明：
         要获取用户信息,需先创建一个UserUtil对象，由UserUtil对象获取User对象
         int state;        //用于标识操作返回状态
         User mCurrentUser;//用于保存当前登录用户信息，若未登录则user.key = null;
         boolean isLogin;  //判断是否已登录
     对外提供API：
         boolean getIsLogin() 获取登录状态
         User getCurrentUser() 获取当前用户对象，若当前无登录用户则返回空
         以下是需要与服务端对接的方法：通过返回值(int)判断操作完成状态
         int regist(String name,String password) 注册用户
         int login(String name, String password) 登录用户 将登录状态置为已登录
         int current() 从服务端获取当前用户信息，并保存在mCurrentUser中
         int logout() 登出用户 并将mCurrentUser信息置空 将登录状态置为未登录
         int updateUserInfo(String password) 修改用户信息 通过给HTTP头部传入"x-http-method-override"参数，达成对HTTP请求方法的重写
         int insertHistory(String op,String music_key) 添加用户操作日志
         int getFavorList() 获取喜欢歌曲列表 并将列表信息存储在mCurrentUser中的favorList中

*/

public class UserUtil {
    private static final String TAG = "UserUtil";

    private static final String USER_URL = "http://115.29.140.122:5001/api/user/";
    private static final String TEST_LOGIN_URL = "http://115.29.140.122:5001/api/app_auth/";
    private static final String CURRENT_USER_URL = "http://115.29.140.122:5001/api/user/current/";
    private static final String USER_HISTORY_URL = "http://115.29.140.122:5001/api/user/current/history/";
    private static final String USER_FAVOR_URL = "http://115.29.140.122:5001/api/user/current/favor";

    //操作状态常数
    public static final int STATE_INIT = 0;  //初始状态
    public static final int STATE_SUCCESS = 1; //操作成功
    public static final int STATE_WRONG = 2;   //操作失败
    public static final int STATE_ERROR = -1;  //网络出错
    public static final int STATE_OTHER = -2;  //系统出现异常
    private int state;        //标识操作返回状态
    private User mCurrentUser;//用于保存当前登录用户信息，若未登录则user.key = null;
    private boolean isLogin;  //判断是否已登录

    public UserUtil() {
        mCurrentUser = new User();
        state = STATE_INIT;
        isLogin = false;
    }

    public boolean getIsLogin() {
        return isLogin;
    }

    public User getCurrentUser() {
        if (!isLogin) return null;
        return mCurrentUser;
    }

    /*
        Method GET: 获取用户列表
        URL: /api/user/
        客户端无需实现该API
     */
    public void getUserList(int start, int end) {
    }

    /*
        Method POST: 用户注册
        URL: /api/user/
        level: None
        Arguments:
            name: 用户名
            password: 密码
        Response: 若注册失败返回None，否则返回用户信息:
            key: 用户key
            name: 用户名
            level: 权限
            regist_date: 注册时间
            listened: 听过歌曲数
            favor: 喜欢歌曲数
            dislike: 不喜欢歌曲数
        @return : boolean 表示是否注册成功
     */
    public int regist(String name, String password) {
        state = STATE_INIT;
        final String REGIST_URL = USER_URL + "?name=" + name + "&password=" + password;
        RequestManager.getRequestQueue().add(
                new JsonObjectRequest(Request.Method.POST, REGIST_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                //注册成功  返回用户信息
                                getUserInfoByJO(mCurrentUser, jsonObject);
                                state = STATE_SUCCESS;
                                Log.d(TAG, jsonObject.toString());
                            } else state = STATE_WRONG;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            state = STATE_OTHER;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        state = STATE_ERROR;
                    }
                })
        );
        return state;

    }

    /*
        Method GET: 获取当前登录用户信息
        URL: /api/user/current/
        level: None
        Arguments: None
        Response: 若未登录返回None，否则返回用户信息:
            key: 用户key
            name: 用户名
            level: 权限
            regist_date: 注册时间
            favor: 喜欢歌曲数
            listened: 听过歌曲数
            skipped: 跳过歌曲数
            dislike: 不喜欢歌曲数
     */
    public int current() {
        state = STATE_INIT;
        RequestManager.getRequestQueue().add(
                new JsonObjectRequest(Request.Method.GET, CURRENT_USER_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                //已登录
                                getUserInfoByJO(mCurrentUser, jsonObject);
                                state = STATE_SUCCESS;
                            } else state = STATE_WRONG;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            state = STATE_OTHER;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        state = STATE_ERROR;
                    }
                })
        );
        return state;

    }

    /*
        Method POST: 用户登录
        URL: /api/user/current/
        level: None
        Arguments:
            name: 用户名
            password: 密码
            Response:
        Response: 若登录失败返回None，否则返回用户信息:
            key: 用户key
            name: 用户名
            level: 权限
            regist_date: 注册时间
            favor: 喜欢歌曲数
            dislike: 不喜欢歌曲数
            listened: 听过歌曲数
     */
    public class JsonObjectPostRequest extends Request<JSONObject>{
        private Map<String,String> mMap;
        private Response.Listener<JSONObject> mListener;


        public JsonObjectPostRequest(String url,Response.Listener<JSONObject> listener, Response.ErrorListener errorListener,Map map) {
            super(Request.Method.POST, url, errorListener);
            mListener=listener;
            mMap=map;

        }
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {

            return mMap;
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString =
                        new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(new JSONObject(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

        @Override
        protected void deliverResponse(JSONObject response) {
            mListener.onResponse(response);

        }

    }
    public int login(String name, String password) {
        HashMap<String,String> mMap=new HashMap<String,String>();
        mMap.put("user_name",name);
        mMap.put("password", CacheUtil.hashKeyForDisk(password));
        RequestManager.getRequestQueue().add(new JsonObjectPostRequest(TEST_LOGIN_URL,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    Log.w("LOG",jsonObject.getString("status"));
                    Log.w("LOG",jsonObject.getString("user_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                state = STATE_ERROR;
            }
        },mMap));
/*        final String LOGIN_URL = TEST_LOGIN_URL + "?user_name=" + name + "&password=" + CacheUtil.hashKeyForDisk(password);
        state = STATE_INIT;
        Map<String,String> map=new HashMap<String,String>();
        map.put("name", name);
        map.put("password",CacheUtil.hashKeyForDisk(password));
        JSONObject params=new JSONObject(map);
        RequestManager.getRequestQueue().add(
                new JsonObjectRequest(Request.Method.POST, TEST_LOGIN_URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            Log.w("LOG",jsonObject.getString("status"));
                            Log.w("LOG",jsonObject.getString("user_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
*//*                        try {
                            if (jsonObject != null) {
                                getUserInfoByJO(mCurrentUser, jsonObject);
                                state = STATE_SUCCESS;
                                isLogin = true;
                                Log.d(TAG, jsonObject.toString());
                            } else state = STATE_WRONG;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            state = STATE_OTHER;
                        }*//*
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        state = STATE_ERROR;
                    }
                })
        );*/
        return state;
    }

    /*
        Method DELETE: 用户登出
        URL: /api/user/current/
        level: None
        Arguments: None
        Response: None
     */
    public int logout() {
        state = STATE_INIT;
        RequestManager.getRequestQueue().add(
                new JsonObjectRequest(Request.Method.DELETE, CURRENT_USER_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        resetUser(mCurrentUser);
                        state = STATE_SUCCESS;
                        isLogin = false;

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        state = STATE_ERROR;
                    }
                })
        );
        return state;
    }

    /*
        Method PATCH: 修改用户信息
        URL: /api/user/<string:key>/
        level: admin
        Arguments:
            password: 密码
            level: 权限('disable', 'normal', 'admin')
        Response: 返回用户信息:
            key: 用户key
            name: 用户名
            level: 权限
            regist_date: 注册时间
            favor: 喜欢歌曲数
            dislike: 不喜欢歌曲数
            listened: 听过歌曲数
     */
    public int updateUserInfo(String password) {
        state = STATE_INIT;
        final String UPDATE_URL = USER_URL + "?key=" + mCurrentUser.getKey() +
                "&level=" + mCurrentUser.getLevel() + "&password=" + password;
        RequestManager.getRequestQueue().add(
                new JsonObjectRequest(Request.Method.POST, CURRENT_USER_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            getUserInfoByJO(mCurrentUser, jsonObject);
                            state = STATE_SUCCESS;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            state = STATE_OTHER;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        state = STATE_ERROR;
                    }
                }) {
                    //自定义HTTP头部，将POST方法复写为PATCH方法
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("x-http-method-override", "PATCH");
                        return super.getHeaders();
                    }
                }
        );
        return state;
    }

    //通过JSONObject获取用户信息
    private void getUserInfoByJO(User user, JSONObject jsonObject) throws JSONException {
        user.setKey(jsonObject.getString("key"));
        user.setName(jsonObject.getString("name"));
        user.setLevel(jsonObject.getString("level"));
        user.setRegist_date(jsonObject.getString("regist_date"));
        user.setListened(jsonObject.getInt("listened"));
        user.setFavor(jsonObject.getInt("favor"));
        user.setDislike(jsonObject.getInt("dislike"));
    }

    //情况用户信息
    private void resetUser(User user) {
        user.setKey(null);
        user.setName(null);
        user.setLevel(null);
        user.setRegist_date(null);
        user.setListened(0);
        user.setFavor(0);
        user.setDislike(0);
    }


    /*
        Method DELETE: 删除用户
        URL:/api/user/<string:key>/
        客户端无需实现该API
     */
    public void deleteUser() {
    }

    /*
        Method GET: 获取用户操作日志
        URL: /api/user/current/history/
        客户端无需实现该API
     */
    public void getHistory() {
    }

    /*
    Method POST: 添加用户操作
    URL: /api/user/current/history/
    level: normal, admin
    Arguments:
        op: 操作类型（favor, dislike, shared, listened）
        key: 音乐key
    Response: None
     */
    public int insertHistory(String op, String music_key) {
        final String UPDATE_OP_URL = USER_HISTORY_URL + "?op=" + op + "&key=" + music_key;
        state = STATE_INIT;
        RequestManager.getRequestQueue().add(
                new JsonObjectRequest(Request.Method.POST, UPDATE_OP_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        state = STATE_SUCCESS;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        state = STATE_ERROR;
                    }
                })
        );
        return state;
    }

    /*
        Method GET: 获取用户喜欢列表
        URL: /api/user/current/favor/
        level: normal, admin
        Arguments:
            start: 起始
            end: 终止
        Response: 返回喜欢歌曲列表，内容包括:
            key: 歌曲key
            title: 歌曲名
            artist: 艺术家
            album: 专辑
            company: 唱片公司
            public_time: 出版年份
            kbps: 码率
            cover: 专辑封面URL
            audio: 音频URL
     */
    //获取整个喜欢列表
    public void getFavorList() {
        state = STATE_INIT;
        final List<MusicInfo> favorList = mCurrentUser.getFavorList();
        RequestManager.getRequestQueue().add(
                new JsonArrayRequest(USER_FAVOR_URL, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        JSONObject jo = new JSONObject();
                        try {
                            //获取喜欢歌曲列表
                            for (int i = 0; i < mCurrentUser.getFavor(); i++) {
                                jo = jsonArray.getJSONObject(i);
                                MusicInfo musicInfo = new MusicInfo();
                                musicInfo.setKey(jo.getString("key"));
                                musicInfo.setTitle(jo.getString("title"));
                                musicInfo.setArtist(jo.getString("artist"));
                                musicInfo.setAlbum(jo.getString("album"));
                                musicInfo.setAudio(jo.getString("audio"));
                                musicInfo.setCompany(jo.getString("company"));
                                musicInfo.setCover(jo.getString("cover"));
                                musicInfo.setKbps(jo.getString("kbps"));
                                musicInfo.setPublic_time(jo.getString("public_time"));
                                favorList.add(musicInfo);
                            }
                            mCurrentUser.setFavorList(favorList);
                            state = STATE_SUCCESS;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            state = STATE_OTHER;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        state = STATE_ERROR;
                    }
                })
        );
    }

    /**
     * 生成全小写的MD5值
     *
     * @param password
     * @return
     */
    public static String ToLowerCaseMD5(String password) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(" UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString().toLowerCase();
    }
}
