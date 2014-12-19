package info.doufm.android.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import info.doufm.android.R;
import info.doufm.android.adapter.UserHistoryListAdapter;
import info.doufm.android.user.UserHistoryInfo;
import info.doufm.android.utils.Constants;
import io.realm.Realm;
import io.realm.RealmResults;

public class UserHistoryActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private int themeNum;
    private UserHistoryListAdapter adapter;
    private RealmResults<UserHistoryInfo> userHistoryInfoList;
    private ListView lvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        findViews();
        initViews();
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        lvHistory = (ListView) findViewById(R.id.lvUserHistory);
    }

    private void initViews() {
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        mToolbar.setTitle("听歌历史");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        setSupportActionBar(mToolbar);
        LoadingHistory();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int musicId = userHistoryInfoList.get(position).getHistory_id();
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_CHOOSE_MUSIC);
                intent.putExtra(Constants.EXTRA_LIST_TYPE, Constants.HISTORY_TYPE);
                intent.putExtra(Constants.EXTRA_MUSIC_ID, musicId);
                sendBroadcast(intent);
                setResult(RESULT_OK);
                finish();
            }
        });
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

    private void LoadingHistory() {
        Realm realm = Realm.getInstance(this);
        userHistoryInfoList = realm.where(UserHistoryInfo.class).findAll();
        adapter = new UserHistoryListAdapter(UserHistoryActivity.this, userHistoryInfoList);
        lvHistory.setAdapter(adapter);
    }
}
