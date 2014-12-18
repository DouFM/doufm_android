package info.doufm.android.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import info.doufm.android.R;
import info.doufm.android.network.JsonObjectPostRequest;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.UserUtil;
import info.doufm.android.utils.Constants;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private EditText etUserName, etUserPassword;
    private Button btnLogin;
    private TextWatcher mTextWatcher;
    private int themeNum;
    private StateListDrawable mStateListDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        findViews();
        initViews();

    }

    private void initViews() {
        //etUserName.set
        mStateListDrawable = new StateListDrawable();
        mStateListDrawable.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.btn_disable));
        mStateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum])));
        mStateListDrawable.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(Color.parseColor(Constants.BACKGROUND_COLORS[themeNum])));
        if (Build.VERSION.SDK_INT >= 16) {
            btnLogin.setBackground(mStateListDrawable);
        } else {
            btnLogin.setBackgroundDrawable(mStateListDrawable);
        }
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        mToolbar.setTitle("登录");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        btnLogin.setOnClickListener(this);
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String userName = etUserName.getText().toString();
                String userPassWord = etUserPassword.getText().toString();
                if (userName.equals("") || userPassWord.equals("")) {
                    btnLogin.setEnabled(false);
                } else {
                    btnLogin.setEnabled(true);
                }

            }
        };
        etUserName.addTextChangedListener(mTextWatcher);
        etUserPassword.addTextChangedListener(mTextWatcher);
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        etUserName = (EditText) findViewById(R.id.et_activity_login_user_name);
        etUserPassword = (EditText) findViewById(R.id.et_activity_login_user_password);
        btnLogin = (Button) findViewById(R.id.btn_activity_login);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_activity_login:
                //执行Login操作
                Login();
                break;
        }
    }

    private void Login() {
        if (checkUserInputInfo()) {
            String userName = etUserName.getText().toString().trim();
            String userPassword = etUserPassword.getText().toString().trim();
            //生成MD5
            userPassword = UserUtil.toLowerCaseMD5(userPassword);
            //转成成UTF-8
            try {
                userName = URLEncoder.encode(userName, "UTF-8");
                userPassword = URLEncoder.encode(userPassword, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HashMap<String, String> mMap = new HashMap<String, String>();
            mMap.put("user_name", userName);
            mMap.put("password", userPassword);
            RequestManager.getRequestQueue().add(new JsonObjectPostRequest(Constants.LOGIN_URL, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        Log.w("LOG", jsonObject.getString("status"));
                        Log.w("LOG", jsonObject.getString("user_id"));
                        if (jsonObject.get("status").equals("success")) {
                            //登录成功
                            Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            //登录失败
                            Toast.makeText(LoginActivity.this, "账号或者密码错误！", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            }, mMap));
            /*//发起POST请求
            RequestManager.getRequestQueue().add(
                    new JsonObjectRequest(Request.Method.POST, "http://http:115.29.140.122:5001/api/app_auth?name=" + userName + "&password=" + userPassword, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    Log.i("Volley", jsonObject.toString());
                                    //登录成功
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Log.i("Volley", volleyError.toString());
                                    //登录失败
                                    Toast.makeText(LoginActivity.this, "账号或者密码错误！", Toast.LENGTH_LONG).show();
                                }
                            }));*/

        }
    }

    private boolean checkUserInputInfo() {
        if (etUserName.getText().toString().equals("")) {
            Toast.makeText(LoginActivity.this, "用户名不能为空!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etUserPassword.getText().toString().equals("")) {
            Toast.makeText(LoginActivity.this, "密码不能为空!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
