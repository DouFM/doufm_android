package info.doufm.android.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import info.doufm.android.R;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private EditText etUserName, etUserPassword;
    private Button btnLogin;
    private TextWatcher mTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
        initViews();

    }

    private void initViews() {
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
                break;
        }
    }
}
