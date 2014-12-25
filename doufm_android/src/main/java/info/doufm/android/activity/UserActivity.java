package info.doufm.android.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.doufm.android.R;
import info.doufm.android.network.JsonObjectRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.User;
import info.doufm.android.user.UserUtil;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.ShareUtil;

public class UserActivity extends ActionBarActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private int themeNum;
    private RelativeLayout rlUserMain, rlUserHistory, rlUserLove;
    private ImageView ivUserLogo;
    private TextView tvUserName;
    private Button btnUserQuit;
    private ShareUtil shareUtil;
    private String localCookieStr;
    private String cookieKey;
    private String cookieValue;
    private Map<String,String> sendHeader = new HashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        shareUtil=new ShareUtil(this);

        //取出cookie,利用正则表达式将localCookieStr拆分成键和值
        localCookieStr = shareUtil.getLocalCookie();
        //取出“=”前，作为cookieKey
        Pattern pattern=Pattern.compile(".*?=");
        Matcher m=pattern.matcher(localCookieStr);
        if(m.find()){
            cookieKey = m.group();
        }
        cookieKey = cookieKey.substring(0,cookieKey.length()-1);//去掉“=”
        Log.w("LOG","cookieKey "+ cookieKey);
        //取出“=”后，作为cookieValue
        Pattern pattern2 = Pattern.compile("=.*;");
        Matcher m2 = pattern2.matcher(localCookieStr);
        if(m2.find()){
            cookieValue = m2.group();
        }
        cookieValue = cookieValue.substring(1,cookieValue.length()-1);//去掉“=”和";"
        Log.w("LOG","cookieValue "+ cookieValue);
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
    }

    private void initViews() {
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        mToolbar.setTitle("个人中心");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ivUserLogo.setImageDrawable(UserUtil.getCircleImage(this, R.drawable.default_artist_300));
        tvUserName.setText(getSharedPreferences("user", MODE_PRIVATE).getString("username", ""));
        btnUserQuit.setOnClickListener(this);
        rlUserHistory.setOnClickListener(this);
        rlUserLove.setOnClickListener(this);
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
        JsonObjectRequestWithCookie jsonObjectPostRequestWithCookie = new JsonObjectRequestWithCookie(Constants.LOGOUT_URL,null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.w("LOG","quite response jsonObject "+ jsonObject.toString());
                try{
                    if(jsonObject.getString("status").equals("success")){
                        Toast.makeText(UserActivity.this, "您已退出登录", Toast.LENGTH_SHORT).show();
                    }
                    else if(jsonObject.getString("status").equals("have not login")){
                        Toast.makeText(UserActivity.this, "您尚未登录，无法退出", Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("LOG",volleyError.getMessage(),volleyError);
                Toast.makeText(UserActivity.this, "网络错误，稍后再试", Toast.LENGTH_SHORT).show();
            }
        });
        //给发给服务器的请求的头里加入cookie
        jsonObjectPostRequestWithCookie.setCookie(cookieKey,cookieValue);
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
