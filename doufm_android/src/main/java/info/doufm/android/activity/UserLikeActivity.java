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
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import info.doufm.android.R;
import info.doufm.android.adapter.UserLoveListAdapter;
import info.doufm.android.adapter.UserMusicAdapter;
import info.doufm.android.network.JsonArrayRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.UserLoveMusicInfo;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.SharedPreferencesUtils;
import io.realm.RealmResults;

public class UserLikeActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private int themeNum;
    private UserLoveListAdapter adapter;
    private RealmResults<UserLoveMusicInfo> userLoveInfoList;
    private UserMusicAdapter userMusicAdapter;
    private ListView lvLove;
    private Context context;

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
        LoadingLoveMusic();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        lvLove.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int musicId = userLoveInfoList.get(position).getLove_id();
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_CHOOSE_MUSIC);
                intent.putExtra(Constants.EXTRA_LIST_TYPE, Constants.LOVE_TYPE);
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
        JsonArrayRequestWithCookie jsonArrayRequestWithCookie = new JsonArrayRequestWithCookie(Constants.USER_MUSIC_URL + "?type=favor&start=0&end=30", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                userMusicAdapter = new UserMusicAdapter(UserLikeActivity.this, jsonArray);
                lvLove.setAdapter(userMusicAdapter);
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
