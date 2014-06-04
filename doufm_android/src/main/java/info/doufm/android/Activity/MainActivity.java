package info.doufm.android.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import info.doufm.android.Info.ChannelInfo;
import info.doufm.android.Info.MusicInfo;
import info.doufm.android.Info.PlaylistInfo;
import info.doufm.android.Play.OnPlayListener;
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
public class MainActivity extends Activity implements View.OnClickListener, OnPlayListener {

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
    private ImageView ivCover;
    private TextView tvMusicTitle;
    private TextView tvAuthorTitle;
    private TextView tvTimeLeft;

    //播放器
    private PlayMusic player;
    private String musicURL = "";

    private String CHANNEL_URL = "http://doufm.info/api/channel/?start=0";
    private String PLAYLIST_URL = "http://doufm.info/api/playlist/?start=0";

    private List<MusicInfo> mMusicInfoList = new ArrayList<MusicInfo>();
    private List<ChannelInfo> mChannelInfoList = new ArrayList<ChannelInfo>();
    private List<PlaylistInfo> mPlaylistInfoList = new ArrayList<PlaylistInfo>();
    private int mPlayListNum = 0;

    //Volley请求
    private RequestQueue mRequstQueue;
    private List<Integer> randomChannel = new ArrayList<Integer>();
    private int CHANNEL_MENU_NUM = 6;
    private int PLAYLIST_MENU_NUM = 6;

    private int randomMusicIndex = new Random().nextInt(2000);

    //音乐文件和封面路径
    private String MusicURL = "";
    private String CoverURL = "";
    private boolean isPlay = false;

    //加载用户体验处理
    private ProgressDialog progressDialog;
    private boolean isLoadingSuccess = false;

    //Rotation
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mRequstQueue = Volley.newRequestQueue(this);

        PhoneIncomingListener();
        initView();
        InitResideMenu();
    }

    private void PhoneIncomingListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }


    private void initView() {
        ivCover = (ImageView) findViewById(R.id.ivCover);
        tvMusicTitle = (TextView) findViewById(R.id.tvMusicTitle);
        tvAuthorTitle = (TextView) findViewById(R.id.tvAuthorName);
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        btnPlayMusic = (Button) findViewById(R.id.btnPlayMusic);
        btnNextSong = (Button) findViewById(R.id.btnNextSong);
        btnPlayMusic.setOnClickListener(this);
        btnNextSong.setOnClickListener(this);
        animation = AnimationUtils.loadAnimation(this,R.anim.rotation);
        LinearInterpolator lin = new LinearInterpolator();
        animation.setInterpolator(lin);
        ivCover.startAnimation(animation);
    }

    private void InitResideMenu() {
        //初始化Reside Menu风格
        mResideMenu = new ResideMenu(this);
        mResideMenu.setBackground(R.drawable.reside_menu_background01);
        mResideMenu.attachToActivity(this);
        mReisdeMenulistener = new ResideMenuListener();
        mResideMenu.setMenuListener(mReisdeMenulistener);

        //初始化右侧RESideMenu Item
        mRightResideMenuItemList = new ArrayList<ResideMenuItem>();
        mRightResideMenuItemTitleList = new ArrayList<String>();
        mRightResideMenuItemIconList = new ArrayList<Integer>();
        mRightResideMenuItemTitleList.add("关于");
        mRightResideMenuItemIconList.add(new Integer(R.drawable.icon_about));
        mRightResideMenuItemList.add(new ResideMenuItem(this, mRightResideMenuItemIconList.get(0), mRightResideMenuItemTitleList.get(0)));
        mResideMenu.setMenuItems(mRightResideMenuItemList, ResideMenu.DIRECTION_RIGHT);
        mRightResideMenuItemList.get(0).setOnClickListener(MainActivity.this);

        //初始化左侧RESideMenu Item
        mLeftResideMenuItemList = new ArrayList<ResideMenuItem>();
        mLeftResideMenuItemTitleList = new ArrayList<String>();
        mLeftResideMenuItemIconList = new ArrayList<Integer>();

        //左侧打开ResideMenu按钮响应
        findViewById(R.id.btn_open_left_reside_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
        //右侧打开ResideMenu按钮响应
        findViewById(R.id.btn_open_right_reside_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResideMenu.openMenu(ResideMenu.DIRECTION_RIGHT);
            }
        });

        JsonArrayRequest jaq = new JsonArrayRequest(PLAYLIST_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                btnPlayMusic.setEnabled(true);
                btnNextSong.setEnabled(true);
                //请求channel列表成
                JSONObject jo = new JSONObject();
                try {
                    Log.w("MainActivity", jsonArray.toString(1));
                    PLAYLIST_MENU_NUM = jsonArray.length();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jo = jsonArray.getJSONObject(i);
                        PlaylistInfo playlistInfo = new PlaylistInfo();
                        playlistInfo.setKey(jo.getString("key"));
                        playlistInfo.setName(jo.getString("name"));
                        playlistInfo.setMusic_list(jo.getString("music_list"));
                        mPlaylistInfoList.add(playlistInfo);
                    }
                    //生成播放列表菜单
                    for (int i = 0; i < PLAYLIST_MENU_NUM; i++) {
                        mLeftResideMenuItemTitleList.add(mPlaylistInfoList.get(i).getName());
                    }
                    //添加左侧列表菜单项
                    for (int i = 0; i < PLAYLIST_MENU_NUM; i++) {
                        //图标需要改变
                        mLeftResideMenuItemList.add(new ResideMenuItem(MainActivity.this, R.drawable.channel_logo, mLeftResideMenuItemTitleList.get(i)));
                    }

                    //添加监听事件
                    for (int i = 0; i < PLAYLIST_MENU_NUM; i++) {
                        //图标需要改变
                        mLeftResideMenuItemList.get(i).setOnClickListener(MainActivity.this);
                    }
                    mResideMenu.setMenuItems(mLeftResideMenuItemList, ResideMenu.DIRECTION_LEFT);
                    initPlayer();
                    isLoadingSuccess = true;
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

    private void initPlayer() {
        player = new PlayMusic(mContext, this, progressDialog, tvTimeLeft);
        PlayRandomMusic(mPlaylistInfoList.get(0).getKey());
    }

    private void PlayRandomMusic(int randomNum) {
        progressDialog = ProgressDialog.show(MainActivity.this, "提示", "音乐加载中...", true, false);
        String MUSIC_URL = "http://doufm.info/api/music/?start=" + randomNum + "&" + "end=" + (randomNum + 1);
        JsonArrayRequest jaq = new JsonArrayRequest(MUSIC_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                //请求随机播放音乐文件信息
                try {
                    JSONObject jo = new JSONObject();
                    jo = jsonArray.getJSONObject(0);
                    MusicURL = "http://doufm.info" + jo.getString("audio");
                    CoverURL = "http://doufm.info" + jo.getString("cover");
                    GetCoverImageRequest(CoverURL);
                    tvMusicTitle.setText(jo.getString("title"));
                    tvAuthorTitle.setText(jo.getString("artist"));
                    player.PlayOnline(MusicURL);
                    isPlay = true;
                    btnPlayMusic.setBackgroundResource(R.drawable.ktv_pause_press);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, errorListener);
        mRequstQueue.add(jaq);
    }

    private void PlayRandomMusic(String playlist_key) {
        tvTimeLeft.setText("00:00");
        String MUSIC_URL = "http://doufm.info/api/playlist/" + playlist_key + "/?num=1";
        JsonArrayRequest jaq = new JsonArrayRequest(MUSIC_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                //请求随机播放音乐文件信息
                try {
                    JSONObject jo = new JSONObject();
                    jo = jsonArray.getJSONObject(0);
                    MusicURL = "http://doufm.info" + jo.getString("audio");
                    CoverURL = "http://doufm.info" + jo.getString("cover");
                    GetCoverImageRequest(CoverURL);
                    tvMusicTitle.setText(jo.getString("title"));
                    tvAuthorTitle.setText(jo.getString("artist"));
                    player.PlayOnline(MusicURL);
                    isPlay = true;
                    btnPlayMusic.setBackgroundResource(R.drawable.pause_song);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, errorListener);
        mRequstQueue.add(jaq);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnPlayMusic:
                if (isPlay) {
                    isPlay = false;
                    btnPlayMusic.setBackgroundResource(R.drawable.play_song);
                    player.pause();
                    animation.cancel();
                } else {
                    isPlay = true;
                    btnPlayMusic.setBackgroundResource(R.drawable.pause_song);
                    player.play();
                    ivCover.startAnimation(animation);
                }
                break;
            case R.id.btnNextSong:
                PlayRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
                break;
        }

        for (int i = 0; i < mRightResideMenuItemList.size(); i++) {
            if (view == mRightResideMenuItemList.get(i)) {
                //判断按下那个菜单
                if ("关于".equals(mRightResideMenuItemTitleList.get(i))) {
                    startActivity(new Intent(MainActivity.this, About.class));
                }
            }
        }

        if (isLoadingSuccess) {
            for (int i = 0; i < PLAYLIST_MENU_NUM; i++) {
                if (view == mLeftResideMenuItemList.get(i)) {
                    mPlayListNum = i;
                    PlayRandomMusic(mPlaylistInfoList.get(i).getKey());
                }
            }
        }
    }

    private void GetCoverImageRequest(String coverURL) {
        ImageRequest imageRequest = new ImageRequest(coverURL, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                ivCover.setImageBitmap(bitmap);
            }
        }, 0, 0, null, errorListener);
        mRequstQueue.add(imageRequest);
    }

    public void getChannelMusicJsonData() {
        //获取指定Channel的音乐
    }

    @Override
    public void EndOfMusic() {
        //自动播放同一个播放列表的下一首歌
        PlayRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
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
    protected void onResume() {
        super.onResume();
        if (isPlay = false && player != null) {
            player.play();
        }
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

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Toast.makeText(MainActivity.this, "网络异常,无法加载在线音乐", Toast.LENGTH_SHORT).show();
//            btnPlayMusic.setEnabled(false);
//            btnNextSong.setEnabled(false);
        }
    };

    public ResideMenu getResideMenu() {
        return mResideMenu;
    }

    private class MyPhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //来电
                    if (isPlay) {
                        player.pause();
                        btnPlayMusic.setBackgroundResource(R.drawable.ktv_play_press);
                        isPlay = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //通话结束
                    if (isPlay == false && player != null) {
                        player.play();
                        btnPlayMusic.setBackgroundResource(R.drawable.ktv_pause_press);
                        isPlay = true;
                    }
                    break;
            }
        }
    }

    private int mBackKeyPressedCount = 1;

    @Override
    public void onBackPressed() {
        if (mBackKeyPressedCount == 2) {
            mRequstQueue.cancelAll(this);
            if (player != null) {
                player.stop();
                player = null;
            }
            finish();
        } else {
            mBackKeyPressedCount++;
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
        }
    }
}
