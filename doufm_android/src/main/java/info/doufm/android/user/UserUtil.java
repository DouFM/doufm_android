package info.doufm.android.user;

import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.doufm.android.info.MusicInfo;
import info.doufm.android.network.RequestManager;

/**
 * 用户帮助类
 * Created on 2014-12-07
 */

/*
     获取当前用户：
     UserUtil userUtil = new UserUtil();
     userUtil.getIsLogin();//获取登录状态
     int result = userUtil.login();//若未登录需先登录
     int result = userUtil.getCurrent();//若已登录可直接获取 //result用于检测是否操作成功
     User currentUser = userUtil.getCurrentUser();
*/

public class UserUtil {

    private static final String USER_URL = "http://doufm.info/api/user/";
    private static final String CURRENT_USER_URL = "http://doufm.info/api/user/current/";
    private static final String USER_HISTORY_URL = "http://doufm.info/api/user/current/history/";
    private static final String USER_FAVOR_URL = "http://doufm.info/api/user/current/favor";

    public static final int STATE_INIT = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_WRONG = 2;
    public static final int STATE_ERROR = -1;
    public static final int STATE_OTHER = -2;
    private int state;
    private User mCurrentUser;
    private boolean isLogin;

    public UserUtil() {
        mCurrentUser = new User();
        state = STATE_INIT;
        isLogin = false;
    }

    public boolean getIsLogin(){
        return isLogin;
    }
    public User getCurrentUser() {
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
                                getUserInfo(mCurrentUser, jsonObject);
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
    public int getCurrent() {
        state = STATE_INIT;
        RequestManager.getRequestQueue().add(
                new JsonObjectRequest(Request.Method.GET, CURRENT_USER_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                //已登录
                                getUserInfo(mCurrentUser, jsonObject);
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
    public int login(String name, String password) {
        final String LOGIN_URL = CURRENT_USER_URL + "?name=" + name + "&password=" + password;
        state = STATE_INIT;
        RequestManager.getRequestQueue().add(
                new JsonObjectRequest(Request.Method.POST, CURRENT_USER_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                getUserInfo(mCurrentUser, jsonObject);
                                state = STATE_SUCCESS;
                                isLogin = true;
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
                            getUserInfo(mCurrentUser, jsonObject);
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

    private void getUserInfo(User user, JSONObject jsonObject) throws JSONException {

        user.setKey(jsonObject.getString("key"));
        user.setName(jsonObject.getString("name"));
        user.setLevel(jsonObject.getString("level"));
        user.setRegist_date(jsonObject.getString("regist_date"));
        user.setListened(jsonObject.getInt("listened"));
        user.setFavor(jsonObject.getInt("favor"));
        user.setDislike(jsonObject.getInt("dislike"));
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
    public int updateOperation(String op, String music_key) {
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
        //判断是否已登录
        if (!isLogin) {

        }
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
}
