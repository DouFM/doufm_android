package info.doufm.android.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.doufm.android.R;
import info.doufm.android.network.JsonObjectRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.User;
import info.doufm.android.user.UserUtil;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.SharedPreferencesUtils;

public class UserActivity extends ActionBarActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private int themeNum;
    private RelativeLayout rlUserMain, rlUserHistory, rlUserLove;
    private ImageView ivUserLogo;
    private TextView tvUserName;
    private Button btnUserQuit;
    private String localCookieStr;
    private Map<String, String> sendHeader = new HashMap<String, String>();
    private TextView tvLoveNum;
    private TextView tvHistoryNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        localCookieStr = SharedPreferencesUtils.getString(this, Constants.COOKIE, "");
        findViews();
        initViews();
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        ivUserLogo = (ImageView) findViewById(R.id.iv_activity_user_logo);
        tvUserName = (TextView) findViewById(R.id.tv_activity_user_name);
        btnUserQuit = (Button) findViewById(R.id.btn_activity_quit);
        btnUserQuit.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        rlUserMain = (RelativeLayout) findViewById(R.id.rl_activity_user_main);
        rlUserHistory = (RelativeLayout) findViewById(R.id.rl_activity_user_history);
        rlUserLove = (RelativeLayout) findViewById(R.id.rl_activity_user_like);
        rlUserMain.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        tvLoveNum = (TextView)findViewById(R.id.tv_love_num);
        tvHistoryNum = (TextView)findViewById(R.id.tv_history_num);

    }

    private void initViews() {
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        mToolbar.setTitle("个人中心");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ivUserLogo.setImageDrawable(UserUtil.getCircleImage(this, R.drawable.default_artist_300));
        tvUserName.setText(SharedPreferencesUtils.getString(this, Constants.LOGIN_USR_NAME, ""));
        btnUserQuit.setOnClickListener(this);
        rlUserHistory.setOnClickListener(this);
        rlUserLove.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //放在onStart方法中，从从userHistoryActivity或userLikeActivity回到UserActivity时会更新
        try {
            getHistoryAndLoveNum();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
    }

    //总感觉加载速度有点慢
    private void getHistoryAndLoveNum() throws AuthFailureError{
        JsonObjectRequestWithCookie jsonObjectRequestWithCookie = new JsonObjectRequestWithCookie(
                Constants.USER_PROFILE_URL, null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    int favorNum = jsonObject.getInt("favor");
                    int listenedNum = jsonObject.getInt("listened");
                    int shareNum = jsonObject.getInt("share");
                    int dislikeNum = jsonObject.getInt("dislike");
                    tvHistoryNum.setText(String.valueOf(listenedNum));
                    tvLoveNum.setText(String.valueOf(favorNum));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("LOG", volleyError.getMessage(), volleyError);
                Toast.makeText(UserActivity.this, "网络错误，稍后再试", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjectRequestWithCookie.setCookie(localCookieStr);
        RequestManager.getRequestQueue().add(jsonObjectRequestWithCookie);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(200);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_activity_quit:
                try {
                    quit();
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                } finally {
                    /*shareUtil.setLocalCookie("");
                    shareUtil.apply();
                    Log.w("LOG","delete localCookie in sharePreference");*/
                }
                User.getInstance().Quit();
                setResult(100);
                finish();
                break;
            case R.id.rl_activity_user_history:
                Intent intent = new Intent(UserActivity.this, UserHistoryActivity.class);
                intent.putExtra(Constants.EXTRA_THEME, themeNum);
                startActivityForResult(intent, 0);

                break;
            case R.id.rl_activity_user_like:
                Intent i = new Intent(UserActivity.this, UserLikeActivity.class);
                i.putExtra(Constants.EXTRA_THEME, themeNum);
                startActivityForResult(i, 0);
                break;
        }
    }

    //退出登录
    public void quit() throws AuthFailureError {
        JsonObjectRequestWithCookie jsonObjectPostRequestWithCookie = new JsonObjectRequestWithCookie(Constants.LOGOUT_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.w("LOG", "quite response jsonObject " + jsonObject.toString());
                try {
                    if (jsonObject.getString("status").equals("success")) {
                        Toast.makeText(UserActivity.this, "您已退出登录", Toast.LENGTH_SHORT).show();
                        SharedPreferencesUtils.putString(UserActivity.this, Constants.COOKIE, "");
                        Log.w("LOG", "delete localCookie in sharePreference");
                    } else if (jsonObject.getString("status").equals("have not login")) {
                        Toast.makeText(UserActivity.this, "您尚未登录，无法退出", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("LOG", volleyError.getMessage(), volleyError);
                Toast.makeText(UserActivity.this, "网络错误，稍后再试", Toast.LENGTH_SHORT).show();
            }
        });
        //给发给服务器的请求的头里加入cookie
        jsonObjectPostRequestWithCookie.setCookie(localCookieStr);
        RequestManager.getRequestQueue().add(jsonObjectPostRequestWithCookie);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            finish();
        }
    }
}
