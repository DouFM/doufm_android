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
import info.doufm.android.adapter.UserLoveListAdapter;
import info.doufm.android.adapter.UserMusicAdapter;
import info.doufm.android.network.JsonArrayRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.User;
import info.doufm.android.user.UserLoveMusicInfo;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.SharedPreferencesUtils;
import io.realm.RealmResults;

public class UserLikeActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private int themeNum;
    private UserLoveListAdapter adapter;
    private ArrayList<UserLoveMusicInfo> userLoveInfoList;
    private ListView lvLove;
    private Context context;
    private boolean canGetMore = true;
    private int startId = 0;
    private final static int num = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_like);
        context = this;
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
        userLoveInfoList = new ArrayList<UserLoveMusicInfo>();
        lvLove.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if(canGetMore){
                    if(scrollState == SCROLL_STATE_IDLE){
                        if(absListView.getLastVisiblePosition()+1== absListView.getCount()){
                            getMoreLove();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {

            }
        });
        LoadingLoveMusic();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        lvLove.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_CHOOSE_MUSIC);
                intent.putExtra(Constants.EXTRA_LIST_TYPE, Constants.LOVE_TYPE);
                intent.putExtra(Constants.EXTRA_MUSIC_ID, userLoveInfoList.get(position));
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

    private void LoadingLoveMusic() {
        /*Realm realm = Realm.getInstance(this);
        if (realm != null) {
            userLoveInfoList = realm.where(UserLoveMusicInfo.class).findAll();
            adapter = new UserLoveListAdapter(UserLikeActivity.this, userLoveInfoList);
            adapter.notifyDataSetChanged();
            lvLove.setAdapter(adapter);
        }*/

        //如果从服务器获取用户历史记录，则可以显示该账号在多台设备的历史记录。如果处理从realm和从服务器获取历史记录的关系？ 之前写的if else好像不好用
        //else {
        if (userLoveInfoList.isEmpty()) {
            getMoreLove();
        }
        adapter = new UserLoveListAdapter(UserLikeActivity.this, userLoveInfoList);
        lvLove.setAdapter(adapter);
    }

    private void getMoreLove(){
        //从服务器获取喜欢列表信息
        JsonArrayRequestWithCookie jsonArrayRequestWithCookie = new JsonArrayRequestWithCookie(Constants.USER_MUSIC_URL + "?type=favor&start="+startId+"&end="+(startId+num), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                int sum = jsonArray.length();
                if(sum==0){
                    Toast.makeText(context,"已经到底了",Toast.LENGTH_SHORT).show();
                    canGetMore = false;
                }
                else{
                    for(int i=0;i<sum;i++){
                        UserLoveMusicInfo userLoveMusicInfo = new UserLoveMusicInfo();
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            userLoveMusicInfo.setUserID(User.getInstance().getUserID());
                            userLoveMusicInfo.setKey(jsonObject.getString("key"));
                            userLoveMusicInfo.setSinger(jsonObject.getString("artist"));
                            userLoveMusicInfo.setTitle(jsonObject.getString("title"));
                            userLoveMusicInfo.setMusicURL(jsonObject.getString("audio"));
                            userLoveMusicInfo.setCover(jsonObject.getString("cover"));
                            userLoveInfoList.add(userLoveMusicInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                    startId += sum;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("LOG", "show favor history error " + volleyError);
            }
        });
        try {
            String localCookie = SharedPreferencesUtils.getString(context, Constants.COOKIE, "");
            jsonArrayRequestWithCookie.setCookie(localCookie);
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
        RequestManager.getRequestQueue().add(jsonArrayRequestWithCookie);
        // }
    }



}
