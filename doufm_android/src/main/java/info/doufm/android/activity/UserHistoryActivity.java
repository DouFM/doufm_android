package info.doufm.android.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import info.doufm.android.R;
import info.doufm.android.adapter.UserHistoryListAdapter;
import info.doufm.android.network.JsonArrayRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.UserHistoryInfo;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.ShareUtil;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class UserHistoryActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private int themeNum;
    private UserHistoryListAdapter adapter;
    private RealmResults<UserHistoryInfo> userHistoryInfoList;
    private ListView lvHistory;
    private ShareUtil shareUtil;
    private String localCookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        findViews();
        initViews();
        shareUtil = new ShareUtil(this);
        localCookie = shareUtil.getLocalCookie();
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
        try {
            LoadingHistory();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
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


    private void LoadingHistory() throws AuthFailureError {
        Realm realm = Realm.getInstance(this);
        userHistoryInfoList = realm.where(UserHistoryInfo.class).findAll();
        adapter = new UserHistoryListAdapter(UserHistoryActivity.this, userHistoryInfoList);
        lvHistory.setAdapter(adapter);
        //解决java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification.
        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                adapter.notifyDataSetChanged();
            }
        });
        /*JsonArrayRequestWithCookie jsonArrayRequestWithCookie = new JsonArrayRequestWithCookie(Constants.USER_HISTORY_URL,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                apiAdapter = new UserHistoryListFromServerAdapter(UserHistoryActivity.this,jsonArray);
                lvHistory.setAdapter(apiAdapter);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(UserHistoryActivity.this, "从服务器加载听歌记录失败", Toast.LENGTH_SHORT).show();
            }
        });
        jsonArrayRequestWithCookie.setCookie(localCookie);
        RequestManager.getRequestQueue().add(jsonArrayRequestWithCookie);*/

    }
}
