package info.doufm.android.activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import info.doufm.android.R;
import info.doufm.android.network.JsonObjectPostRequest;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.User;
import info.doufm.android.user.UserUtil;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.CyptoUtils;
import info.doufm.android.utils.SharedPreferencesUtils;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int LOGIN_SUCCESS = 100;
    private static final int LOGIN_ERROR = 200;
    private static final int DISSMIS_LOADING_DLG = 1000;
    private Toolbar mToolbar;
    private EditText etUserName, etUserPassword;
    private Button btnLogin;
    private TextWatcher mTextWatcher;
    private int themeNum;
    private StateListDrawable mStateListDrawable;
    private boolean isLogin = false;
    private CheckBox cbSavePassword;
    private String originPassword;
    private String originName;
    private ImageView ivLoginLogo;
    private Map<String, String> sendHeader = new HashMap<String, String>();

    private SweetAlertDialog loadingDialog;
    private Context context;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DISSMIS_LOADING_DLG:
                    loadingDialog.dismissWithAnimation();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = this;
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        findViews();
        initViews();
    }

    private void initViews() {
        ivLoginLogo.setImageDrawable(UserUtil.getCircleImage(this, R.drawable.default_artist_300));
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
        if (SharedPreferencesUtils.getBoolean(context, Constants.SAVE_USER_LOGIN_INFO_FLAG, false)) {
            etUserName.setText(SharedPreferencesUtils.getString(context, Constants.LOGIN_USR_NAME, ""));
            etUserPassword.setText(CyptoUtils.decode(CyptoUtils.KEY, SharedPreferencesUtils.getString(context, Constants.LOGIN_USR_PASSWORD, "")));
        }
        SharedPreferencesUtils.putBoolean(context, Constants.SAVE_USER_LOGIN_INFO_FLAG, true);
    }


    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        etUserName = (EditText) findViewById(R.id.et_activity_login_user_name);
        etUserPassword = (EditText) findViewById(R.id.et_activity_login_user_password);
        btnLogin = (Button) findViewById(R.id.btn_activity_login);
        cbSavePassword = (CheckBox) findViewById(R.id.cbSavePassword);
        cbSavePassword.setOnCheckedChangeListener(this);
        ivLoginLogo = (ImageView) findViewById(R.id.iv_login_logo);
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
                if (!isLogin) {
                    Login();
                }
                break;
        }
    }

    private void Login() {
        if (checkUserInputInfo()) {
            loadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            loadingDialog.setCancelable(false);
            loadingDialog.setTitleText("努力登录中...");
            loadingDialog.show();
            String userName = etUserName.getText().toString().trim();
            String userPassword = etUserPassword.getText().toString().trim();
            originPassword = userPassword;
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
            JsonObjectPostRequest jsonObjectPostRequest = new JsonObjectPostRequest(Constants.LOGIN_URL, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    //从response的jsonObject中取出cookie的值，存入sharePreference
                    Log.w("LOG", "jsonObject from onResponse " + jsonObject.toString());
                    try {
                        if(!jsonObject.getString("Cookie").equals("")){
                            SharedPreferencesUtils.putString(LoginActivity.this, Constants.COOKIE, jsonObject.getString("Cookie"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (jsonObject.get("status").equals("success")) {
                            //登录成功
                            //登录时不分区用户名大小写，但是后台记录的用户名是有区分大小写的
                            originName = jsonObject.getString("user_name");
                            User.getInstance().setUserName(originName);
                            User.getInstance().setUserPassword(originPassword);
                            User.getInstance().setUserID(jsonObject.getString("user_id"));
                            User.getInstance().setLogin(true);
                            if (cbSavePassword.isChecked()) {
                                //记住用户名、密码、
                                SharedPreferencesUtils.putString(context, Constants.LOGIN_USR_NAME, originName);
                                SharedPreferencesUtils.putString(context, Constants.LOGIN_USR_PASSWORD, CyptoUtils.encode(CyptoUtils.KEY, originPassword));
                            }

                            if (!isLogin) {
                                Message msg = new Message();
                                msg.what = DISSMIS_LOADING_DLG;
                                handler.sendMessage(msg);
                                Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                                LoginActivity.this.setResult(LOGIN_SUCCESS);
                                LoginActivity.this.finish();
                                isLogin = true;
                            }
                        } else if (jsonObject.get("status").equals("already login")) {
                            //登录失败
                            Toast.makeText(LoginActivity.this, "该用户已登录", Toast.LENGTH_SHORT).show();
                            Message msg = new Message();
                            msg.what = DISSMIS_LOADING_DLG;
                            handler.sendMessage(msg);
                        } else if (jsonObject.get("status").equals("can not get rs response")) {
                            //登录失败
                            Toast.makeText(LoginActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                            Message msg = new Message();
                            msg.what = DISSMIS_LOADING_DLG;
                            handler.sendMessage(msg);
                        } else {
                            Toast.makeText(LoginActivity.this, "啊哦，服务器出错了", Toast.LENGTH_SHORT).show();
                            Message msg = new Message();
                            msg.what = DISSMIS_LOADING_DLG;
                            handler.sendMessage(msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.w("LOG", volleyError.getMessage(), volleyError);
                    Message msg = new Message();
                    msg.what = DISSMIS_LOADING_DLG;
                    handler.sendMessage(msg);
                    Toast.makeText(LoginActivity.this, "网络错误，登录失败！", Toast.LENGTH_SHORT).show();
                }
            }, mMap);
            //发起登录请求时不向服务器发送cookie，就可以多台设备登录一个账号了
            /*String localCookieStr = SharedPreferencesUtils.getString(LoginActivity.this, Constants.COOKIE, "");
            if (!localCookieStr.equals("")) {
                jsonObjectPostRequest.setSendCookie(localCookieStr);
            }*/
            RequestManager.getRequestQueue().add(jsonObjectPostRequest);
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (cbSavePassword.isChecked()) {
            SharedPreferencesUtils.putBoolean(context, Constants.SAVE_USER_LOGIN_INFO_FLAG, true);
        } else {
            SharedPreferencesUtils.putBoolean(context, Constants.SAVE_USER_LOGIN_INFO_FLAG, false);
        }
    }
}
