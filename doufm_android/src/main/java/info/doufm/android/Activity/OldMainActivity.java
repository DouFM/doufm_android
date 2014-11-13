package info.doufm.android.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import info.doufm.android.Info.PlaylistInfo;
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
public class OldMainActivity extends Activity implements View.OnClickListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private OldMainActivity mContext;
    private ResideMenu mResideMenu;
    private ResideMenuListener mReisdeMenulistener;

    //左侧菜单项
    private List<ResideMenuItem> mLeftResideMenuItemList;
    private List<String> mLeftResideMenuItemTitleList;
    private List<Integer> mLeftResideMenuItemIconList;

    //播放和下一首按钮
    private Button btnPlayMusic, btnNextSong;
    private ImageView ivCover;
    private TextView tvMusicTitle;
    private String mMusicTitle;
    private TextView tvTimeLeft;

    //播放器
    private MediaPlayer mMainMediaPlayer;
    private String PLAYLIST_URL = "http://doufm.info/api/playlist/?start=0";

    private List<PlaylistInfo> mPlaylistInfoList = new ArrayList<PlaylistInfo>();
    private int mPlayListNum = 0;

    //Volley请求
    private RequestQueue mRequstQueue;
    private int PLAYLIST_MENU_NUM = 0;

    //音乐文件和封面路径
    private String MusicURL = "";
    private String CoverURL = "";
    private boolean isPlay = false;

    //加载用户体验处理
    private ProgressDialog progressDialog;
    private boolean isLoadingSuccess = false;

    //Rotation
    private Animation animation;

    private long mMusicDuration;            //音乐总时间
    private long mMusicCurrDuration;        //当前播放时间
    private Timer mTimer = new Timer();     //计时器
    private static final int DISSMISS = 1000;
    private static final int UPDATE_TIME = 2000;

    private boolean isOpenResideMenu = false;

    private SensorManager sensorManager;
    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float xValue = Math.abs(event.values[0]);
            float yValue = Math.abs(event.values[1]);
            float zValue = Math.abs(event.values[2]);
            if (xValue > 15 || yValue > 15 || zValue > 15) {
                if (mMainMediaPlayer != null) {
                    PlayRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //定义Handler对象
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //处理消息
            if (msg.what == DISSMISS) {
                progressDialog.dismiss();
            }
            if (msg.what == UPDATE_TIME) {
                //更新音乐播放状态
                if (mMainMediaPlayer == null) {
                    return;
                }
                mMusicCurrDuration = mMainMediaPlayer.getCurrentPosition();
                mMusicDuration = mMainMediaPlayer.getDuration();
                if (mMusicDuration > 0) {
                    //更新剩余时间
                    tvTimeLeft.setText(FormatTime(mMusicDuration - mMusicCurrDuration));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
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
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        btnPlayMusic = (Button) findViewById(R.id.btnPlayMusic);
        btnNextSong = (Button) findViewById(R.id.btnNextSong);
        btnPlayMusic.setOnClickListener(this);
        btnNextSong.setOnClickListener(this);
        animation = AnimationUtils.loadAnimation(this, R.anim.rotation);
        ivCover.startAnimation(animation);
    }

    private void InitResideMenu() {
        //初始化Reside Menu风格
        mResideMenu = new ResideMenu(this);
        mResideMenu.setBackground(R.drawable.reside_menu_background01);
        mResideMenu.attachToActivity(this);
        mReisdeMenulistener = new ResideMenuListener();
        mResideMenu.setMenuListener(mReisdeMenulistener);

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
        //关闭右侧侧边栏
        mResideMenu.setDirectionDisable(ResideMenu.DIRECTION_RIGHT);

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
                        mLeftResideMenuItemList.add(new ResideMenuItem(OldMainActivity.this, R.drawable.channel_logo, mLeftResideMenuItemTitleList.get(i)));
                    }

                    //添加监听事件
                    for (int i = 0; i < PLAYLIST_MENU_NUM; i++) {
                        //图标需要改变
                        mLeftResideMenuItemList.get(i).setOnClickListener(OldMainActivity.this);
                    }
                    mResideMenu.setMenuItems(mLeftResideMenuItemList, ResideMenu.DIRECTION_LEFT);
                    isLoadingSuccess = true;
                    initPlayer();
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
        mMainMediaPlayer = new MediaPlayer(); //创建媒体播放器
        mMainMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); //设置媒体流类型
        mMainMediaPlayer.setOnCompletionListener(this);
        mMainMediaPlayer.setOnErrorListener(this);
        mMainMediaPlayer.setOnBufferingUpdateListener(this);
        mMainMediaPlayer.setOnPreparedListener(this);
        mTimer.schedule(timerTask, 0, 1000);
        PlayRandomMusic(mPlaylistInfoList.get(0).getKey());
    }

    private void PlayRandomMusic(String playlist_key) {
        progressDialog = ProgressDialog.show(OldMainActivity.this, "提示", "加载中...", true, false);
        tvTimeLeft.setText("00:00");
        final String MUSIC_URL = "http://doufm.info/api/playlist/" + playlist_key + "/?num=1";
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
                    mMusicTitle = jo.getString("title");
                    mMainMediaPlayer.reset();
                    mMainMediaPlayer.setDataSource(MusicURL); //这种url路径
                    mMainMediaPlayer.prepare(); //prepare自动播放
                    isPlay = true;
                    btnNextSong.setEnabled(true);
                    btnNextSong.setEnabled(true);
                    btnPlayMusic.setBackgroundResource(R.drawable.btn_pause_seletor);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
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
                    btnPlayMusic.setBackgroundResource(R.drawable.btn_play_selector);
                    mMainMediaPlayer.pause();
                    ivCover.clearAnimation();
                } else {
                    isPlay = true;
                    btnPlayMusic.setBackgroundResource(R.drawable.btn_pause_seletor);
                    mMainMediaPlayer.start();
                    ivCover.startAnimation(animation);
                }
                break;
            case R.id.btnNextSong:
                PlayRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
                break;
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

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        if (percent < 100) {
            tvMusicTitle.setText(mMusicTitle + " " + percent + "%");
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        } else {
            tvMusicTitle.setText(mMusicTitle);
        }

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        PlayRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        if (mMainMediaPlayer != null) {
            mMainMediaPlayer.stop();
            mMainMediaPlayer.release();
            mMainMediaPlayer = null;
        }
        Toast.makeText(this, "播放器异常!", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMainMediaPlayer.start();
        progressDialog.dismiss();
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
        if (isPlay = false && mMainMediaPlayer != null) {
            mMainMediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRequstQueue.cancelAll(this);
        if (mMainMediaPlayer != null) {
            mMainMediaPlayer.stop();
            mMainMediaPlayer.release();
            mMainMediaPlayer = null;
        }
    }

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Toast.makeText(OldMainActivity.this, "网络异常,无法加载在线音乐,请检查网络配置!", Toast.LENGTH_SHORT).show();
            Message msg = new Message();
            msg.what = DISSMISS;
            handler.sendMessage(msg);
            btnNextSong.setEnabled(false);
            btnNextSong.setEnabled(false);
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
                        mMainMediaPlayer.pause();
                        btnPlayMusic.setBackgroundResource(R.drawable.btn_play_selector);
                        isPlay = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //通话结束
                    if (isPlay == false && mMainMediaPlayer != null) {
                        mMainMediaPlayer.start();
                        btnPlayMusic.setBackgroundResource(R.drawable.btn_pause_seletor);
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
            if (mMainMediaPlayer != null) {
                mMainMediaPlayer.stop();
                mMainMediaPlayer.release();
                mMainMediaPlayer = null;
            }
            finish();
        } else {
            mBackKeyPressedCount++;
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
        }
    }

    //格式化时间
    private String FormatTime(long timeMills) {
        Date date = new Date(timeMills);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.CHINA);
        return simpleDateFormat.format(date);
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (mMainMediaPlayer == null) {
                return;
            }
            if (mMainMediaPlayer.isPlaying()) {
                //处理播放
                Message msg = new Message();
                msg.what = UPDATE_TIME;
                handler.sendMessage(msg);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_app_about:
                startActivity(new Intent(this, About.class));
                break;
        }
        return true;
    }


}
