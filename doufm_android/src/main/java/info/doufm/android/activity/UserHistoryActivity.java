package info.doufm.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import info.doufm.android.R;
import info.doufm.android.adapter.UserHistoryListAdapter;
import info.doufm.android.adapter.UserMusicAdapter;
import info.doufm.android.network.JsonArrayRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.User;
import info.doufm.android.user.UserHistoryInfo;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.SharedPreferencesUtils;

public class UserHistoryActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private int themeNum;
    private UserHistoryListAdapter adapter;
    //private RealmResults<UserHistoryInfo> userHistoryInfoList;
    private ArrayList<UserHistoryInfo> userHistoryInfoList;
    private UserMusicAdapter userMusicAdapter;
    private ListView lvHistory;
    private String localCookie;
    private Context context;
    private JSONArray mJsonArray;
    private int startId = 0;
    private final static int num = 10;
    private boolean canGetMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);
        context = this;
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        findViews();
        initViews();
        localCookie = SharedPreferencesUtils.getString(context, Constants.COOKIE, "");
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        lvHistory = (ListView) findViewById(R.id.lvUserHistory);
    }

    private void initViews() {
        userHistoryInfoList = new ArrayList<>();
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        mToolbar.setTitle("听歌历史");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_CHOOSE_MUSIC);
                intent.putExtra(Constants.EXTRA_LIST_TYPE, Constants.HISTORY_TYPE);
                intent.putExtra(Constants.EXTRA_MUSIC_ID, userHistoryInfoList.get(position));
                sendBroadcast(intent);
                setResult(RESULT_OK);
                finish();
            }
        });
        lvHistory.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (canGetMore) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                        if (view.getLastVisiblePosition() + 1 == view.getCount()) {
                            getMoreHistory();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        try {
            LoadingHistory();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
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
        if (userHistoryInfoList.isEmpty()) {
            getMoreHistory();
        }
        adapter = new UserHistoryListAdapter(UserHistoryActivity.this, userHistoryInfoList);
        lvHistory.setAdapter(adapter);
    }

    /*    private void LoadingHistory() throws AuthFailureError {
            Realm realm = Realm.getInstance(this);
            if (realm != null) {
                userHistoryInfoList = realm.where(UserHistoryInfo.class).findAll();
                if(userHistoryInfoList.isEmpty()){
                    getMoreHistory();
                } else{
                    startId = userHistoryInfoList.size();
                }
                adapter = new UserHistoryListAdapter(UserHistoryActivity.this, userHistoryInfoList);
                lvHistory.setAdapter(adapter);
                //解决java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification.
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {

                    }
                });
            }
        }*/
    private void getMoreHistory() {
        JsonArrayRequestWithCookie jsonArrayRequestWithCookie = new JsonArrayRequestWithCookie(Constants.USER_MUSIC_URL + "?type=listened&start=" + String.valueOf(startId) + "&end=" + String.valueOf(startId + num), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {

                int sum = jsonArray.length();
                Log.i("TAG", "sum:" + sum);
                if (sum == 0 && canGetMore) {
                    Toast.makeText(UserHistoryActivity.this, "已经到底了", Toast.LENGTH_SHORT).show();
                    canGetMore = false;
                } else {
 /*                   Realm realm = Realm.getInstance(UserHistoryActivity.this);
                    realm.beginTransaction();*/
                    for (int i = 0; i < sum; i++) {
                        UserHistoryInfo userHistoryInfo = new UserHistoryInfo();
                        try {
                            JSONObject jo = jsonArray.getJSONObject(i);
                            //userHistoryInfo.setHistory_id(startId + i);
                            userHistoryInfo.setUserID(User.getInstance().getUserID());
                            userHistoryInfo.setKey(jo.getString("key"));
                            userHistoryInfo.setTitle(jo.getString("title"));
                            userHistoryInfo.setSinger(jo.getString("artist"));
                            userHistoryInfo.setMusicURL(jo.getString("audio"));
                            userHistoryInfo.setCover(jo.getString("cover"));
                            //int index = MusicIsExist(userHistoryInfo);
                            //if (index != -1) {
                            //    userHistoryInfoList.remove(index);
                            //}
                            userHistoryInfoList.add(userHistoryInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
/*                    realm.commitTransaction();
                    realm.close();*/

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("LOG", "show listen history error " + volleyError);
            }
        });
        startId = startId + num;
        try {
            String localCookie = SharedPreferencesUtils.getString(context, Constants.COOKIE, "");
            jsonArrayRequestWithCookie.setCookie(localCookie);
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
        RequestManager.getRequestQueue().add(jsonArrayRequestWithCookie);
    }

/*    private int MusicIsExist(UserHistoryInfo userHistoryInfo) {
        for (int i = 0; i < userHistoryInfoList.size(); i++) {
            if (userHistoryInfo.getKey()。equals( userHistoryInfoList.get(i).getKey()))
                return i;
        }
        return -1;
    }*/
}
