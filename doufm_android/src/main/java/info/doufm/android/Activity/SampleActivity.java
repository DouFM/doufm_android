package info.doufm.android.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.doufm.android.Info.PlaylistInfo;
import info.doufm.android.PlayView.PlayView;
import info.doufm.android.R;


public class SampleActivity extends Activity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private boolean drawerArrowColor;

    private FrameLayout mContainer;
    private Button mStartBtn;
    private Button mStopBtn;
    private PlayView mPlayView;

    private List<String> mLeftResideMenuItemTitleList = new ArrayList<String>();
    private List<PlaylistInfo> mPlaylistInfoList = new ArrayList<PlaylistInfo>();
    private String PLAYLIST_URL = "http://doufm.info/api/playlist/?start=0";
    private int PLAYLIST_MENU_NUM = 0;

    //Volley请求
    private RequestQueue mRequstQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        mRequstQueue = Volley.newRequestQueue(this);
        mContainer = (FrameLayout) findViewById(R.id.media_container);
        mStartBtn = (Button) findViewById(R.id.btn_start_play);
        mStopBtn = (Button) findViewById(R.id.btn_stop_play);

        mPlayView = new PlayView(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mContainer.addView(mPlayView, lp);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayView.play();
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayView.pause();
            }
        });

        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);


        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,drawerArrow, R.string.drawer_open,R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }

    @Override
    protected void onResume() {
        super.onResume();
        JsonArrayRequest jaq = new JsonArrayRequest(PLAYLIST_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                JSONObject jo = new JSONObject();
                try {
                    PLAYLIST_MENU_NUM = jsonArray.length();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jo = jsonArray.getJSONObject(i);
                        PlaylistInfo playlistInfo = new PlaylistInfo();
                        playlistInfo.setKey(jo.getString("key"));
                        playlistInfo.setName(jo.getString("name"));
                        mLeftResideMenuItemTitleList.add(jo.getString("name"));
                        playlistInfo.setMusic_list(jo.getString("music_list"));
                        mPlaylistInfoList.add(playlistInfo);
                    }
                    //生成播放列表菜单
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SampleActivity.this,
                            android.R.layout.simple_list_item_1, android.R.id.text1, mLeftResideMenuItemTitleList);
                    mDrawerList.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, errorListener) {
            /**
             * 添加自定义HTTP Header
             * @return
             * @throws AuthFailureError
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-Agent", "Android:1.0:2009chenqc@163.com");
                return params;
            }
        };
        mRequstQueue.add(jaq);
    }

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Toast.makeText(SampleActivity.this, "网络异常,无法加载在线音乐,请检查网络配置!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
