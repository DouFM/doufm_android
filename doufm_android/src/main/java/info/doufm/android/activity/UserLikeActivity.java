package info.doufm.android.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import info.doufm.android.R;
import info.doufm.android.adapter.UserLoveListAdapter;
import info.doufm.android.user.UserLoveMusicInfo;
import info.doufm.android.utils.Constants;
import io.realm.Realm;
import io.realm.RealmResults;

public class UserLikeActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private int themeNum;
    private UserLoveListAdapter adapter;
    private RealmResults<UserLoveMusicInfo> userLoveInfoList;
    private ListView lvLove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_like);
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        findViews();
        initViews();
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        lvLove = (ListView) findViewById(R.id.lvLove);
    }

    private void initViews() {
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        mToolbar.setTitle("我的收藏");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        setSupportActionBar(mToolbar);
        LoadingLoveMusic(userLoveInfoList);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void LoadingLoveMusic(RealmResults<UserLoveMusicInfo> userLoveInfoList) {
        Realm realm = Realm.getInstance(this);
        userLoveInfoList = realm.where(UserLoveMusicInfo.class).findAll();
        adapter = new UserLoveListAdapter(UserLikeActivity.this, userLoveInfoList);
        adapter.notifyDataSetChanged();
        lvLove.setAdapter(adapter);

    }
}
