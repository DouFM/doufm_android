package info.doufm.android.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import info.doufm.android.R;
import info.doufm.android.user.User;
import info.doufm.android.user.UserUtil;
import info.doufm.android.utils.Constants;

public class UserActivity extends ActionBarActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private int themeNum;
    private RelativeLayout rlUserMain, rlUserHistory, rlUserLove;
    private ImageView ivUserLogo;
    private TextView tvUserName;
    private Button btnUserQuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
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
                User.getInstance().Quit();
                setResult(100);
                finish();
                break;
            case R.id.rl_activity_user_history:
                Intent intent = new Intent(UserActivity.this, UserHistoryActivity.class);
                intent.putExtra(Constants.EXTRA_THEME, themeNum);
                startActivity(intent);
                break;
            case R.id.rl_activity_user_like:
                Intent i = new Intent(UserActivity.this, UserLikeActivity.class);
                i.putExtra(Constants.EXTRA_THEME, themeNum);
                startActivity(i);
                break;
        }
    }
}
