package info.doufm.android.Activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import info.doufm.android.Info.ChannelInfo;
import info.doufm.android.Info.MusicInfo;
import info.doufm.android.Info.PlaylistInfo;
import info.doufm.android.Play.PlayMusic;
import info.doufm.android.R;
import info.doufm.android.ResideMenu.ResideMenu;
import info.doufm.android.ResideMenu.ResideMenuItem;

/**
 * Created with Android Studio.
 * Date 2014-04-26
 * <p/>
 * Reside Menu的初始化和设置
 *
 * @author Qichao Chen
 * @version 1.0
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private MainActivity mContext;
    private ResideMenu mResideMenu;
    private ResideMenuListener mReisdeMenulistener;
    //左侧菜单项
    private List<ResideMenuItem> mLeftResideMenuItemList;
    private List<String> mLeftResideMenuItemTitleList;
    private List<Integer> mLeftResideMenuItemIconList;

    //右侧菜单项
    private List<ResideMenuItem> mRightResideMenuItemList;
    private List<String> mRightResideMenuItemTitleList;
    private List<Integer> mRightResideMenuItemIconList;

    //播放和下一首按钮
    private Button btnPlayMusic, btnNextSong;

    //Test Music URL
    private String URL = "http://abv.cn/music/%E5%85%89%E8%BE%89%E5%B2%81%E6%9C%88.mp3";
    private MediaPlayer mediaPlayer;


    private PlayMusic player; //播放器
    private String musicURL = "";

    private static final String CHANNEL_URL = "http://doufm.info/api/channel/?start=0";
    private static final String MUSIC_URL = "http://doufm.info/api/channel/?start=0";
    private static final String PLAYLIST_URL = "http://doufm.info/api/channel/?start=0";

    private List<MusicInfo> mMusicInfoList = new ArrayList<MusicInfo>();
    private List<ChannelInfo> mChannelInfoList = new ArrayList<ChannelInfo>();
    private List<PlaylistInfo> mPlaylistInfoList = new ArrayList<PlaylistInfo>();

    //Volley请求
    private RequestQueue mRequstQueue;
    private List<Integer> randomChannel = new ArrayList<Integer>();
    private static final int CHANNEL_MENU_NUM = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mRequstQueue = Volley.newRequestQueue(this);
        initView();
        InitResideMenu();
        initPlayer();
    }


    private void initView() {
        btnPlayMusic = (Button) findViewById(R.id.btnPlayMusic);
        btnNextSong = (Button) findViewById(R.id.btnNextSong);
        btnPlayMusic.setOnClickListener(this);
        btnNextSong.setOnClickListener(this);
    }

    private void InitResideMenu() {
        //初始化Reside Menu风格
        mResideMenu = new ResideMenu(this);
        mResideMenu.setBackground(R.drawable.reside_menu_background);
        mResideMenu.attachToActivity(this);
        mReisdeMenulistener = new ResideMenuListener();
        mResideMenu.setMenuListener(mReisdeMenulistener);

        //初始化左侧ResideMenu Item
        mLeftResideMenuItemList = new ArrayList<ResideMenuItem>();
        mLeftResideMenuItemTitleList = new ArrayList<String>();
        mLeftResideMenuItemIconList = new ArrayList<Integer>();

        findViewById(R.id.btn_open_left_reside_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });

        JsonArrayRequest jaq = new JsonArrayRequest(CHANNEL_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                //请求channel列表成
                JSONObject jo = new JSONObject();
                try {
                    Log.w("MainActivity", jsonArray.toString(1));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jo = jsonArray.getJSONObject(i);
                        ChannelInfo channelInfo = new ChannelInfo();
                        channelInfo.setKey(jo.getString("key"));
                        channelInfo.setUpload_date(jo.getString("upload_date"));
                        channelInfo.setMusic_list(jo.getString("music_list"));
                        channelInfo.setName(jo.getString("name"));
                        channelInfo.setPlayable(jo.getString("playable"));
                        mChannelInfoList.add(channelInfo);
                    }
                    //随机产生Channel
                    Random random = new Random();
                    for (int i = 0; i < CHANNEL_MENU_NUM; i++) {
                        int num = random.nextInt(mChannelInfoList.size());
                        if (!randomChannel.contains(num)) {
                            randomChannel.add(num);
                            mLeftResideMenuItemTitleList.add(mChannelInfoList.get(num).getName());
                        }
                    }
                    //添加左侧列表菜单项
                    for (int i = 0; i < CHANNEL_MENU_NUM; i++) {
                        //图标需要改变
                        mLeftResideMenuItemList.add(new ResideMenuItem(MainActivity.this, R.drawable.channel_logo, mLeftResideMenuItemTitleList.get(i)));
                    }

                    //添加监听事件
                    for (int i = 0; i < CHANNEL_MENU_NUM; i++) {
                        //图标需要改变
                        mLeftResideMenuItemList.get(i).setOnClickListener(MainActivity.this);
                    }
                    //禁用右侧ResideMenu
                    mResideMenu.setDirectionDisable(ResideMenu.DIRECTION_RIGHT);
                    mResideMenu.setMenuItems(mLeftResideMenuItemList, ResideMenu.DIRECTION_LEFT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        mRequstQueue.add(jaq);
    }

    private void initPlayer() {
        player = new PlayMusic();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnPlayMusic:
                try {
                    musicURL = URLEncoder.encode(URL, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                player.PlayOnline("http://doufm.info/api/fs/535b35aa1d41c866ca673f85");
                break;
            case R.id.btnNextSong:
                player.pause();
                break;
        }

        for (int i = 0; i < CHANNEL_MENU_NUM; i++) {
            if (view == mLeftResideMenuItemList.get(i)) {
                Toast.makeText(this, mChannelInfoList.get(randomChannel.get(i)).getName(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class ResideMenuListener implements ResideMenu.OnMenuListener {

        @Override
        public void openMenu() {
            //打开Reside Menu
        }

        @Override
        public void closeMenu() {
            //关闭Reside Menu
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev) || mResideMenu.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRequstQueue.cancelAll(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRequstQueue.cancelAll(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRequstQueue.cancelAll(this);
        if (player != null) {
            player.stop();
            player = null;
        }
    }

    public ResideMenu getResideMenu() {
        return mResideMenu;
    }
}
