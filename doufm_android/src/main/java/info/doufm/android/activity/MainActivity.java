package info.doufm.android.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;
import info.doufm.android.R;
import info.doufm.android.adapter.ChannelListAdapter;
import info.doufm.android.info.MusicInfo;
import info.doufm.android.info.PlaylistInfo;
import info.doufm.android.network.JsonArrayRequestWithCookie;
import info.doufm.android.network.JsonObjectRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.playview.MySeekBar;
import info.doufm.android.playview.RotateAnimator;
import info.doufm.android.user.User;
import info.doufm.android.user.UserHistoryInfo;
import info.doufm.android.user.UserLoveMusicInfo;
import info.doufm.android.utils.CacheUtil;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.SharedPreferencesUtils;
import info.doufm.android.utils.TimeFormat;
import io.realm.Realm;
import io.realm.RealmResults;
import libcore.io.DiskLruCache;


public class MainActivity extends ActionBarActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, View.OnClickListener {

    private static final String TAG = "seekBar";
    private ListView mDrawerList;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private File cacheDir;
    private DiskLruCache mDiskLruCache = null;
    private MusicInfo playMusicInfo;
    private MusicInfo nextMusicInfo;
    private MusicInfo preMusicInfo;
    private boolean hasNextCache = false;
    private DownloadMusicThread mDownThread;
    private int bufPercent = 0;
    private boolean seekNow = false;
    private TextView tvTotalTime;
    private TextView tvCurTime;
    private boolean playLoopFlag = false;

    private int colorNum;
    private SweetAlertDialog mErrorDlg;

    //菜单列表监听器
    private ListListener mListListener;

    //播放界面相关
    private Button btnPlay;
    private Button btnNextSong;
    private Button btnPreSong;
    private Button btnPlayMode;
    private Button btnLove;
    private MySeekBar seekBar;
    private ImageView ivNeedle;
    private ImageView ivDisk;
    private RotateAnimator mDiskAnimator;
    private boolean isLoadingSuccess = false;
    private int mMusicDuration;            //音乐总时间
    private int mMusicCurrDuration;        //当前播放时间
    private Animation needleUpAnim;
    private Animation needleDownAnim;
    private Animation needleAnim;
    private Timer mTimer = new Timer();     //计时器
    private List<String> mLeftResideMenuItemTitleList = new ArrayList<String>();
    private List<PlaylistInfo> mPlaylistInfoList = new ArrayList<PlaylistInfo>();
    private int mPlayListNum = 0;
    private int mThemeNum = 0;
    private boolean isFirstLoad = true;
    private boolean needleDownFlag = false;  //是否需要play needledown的动画
    private boolean loveFlag = false;
    private ChannelListAdapter channelListAdapter;
    private LinearLayout llLeftSlideMenu;
    private RelativeLayout rlUserLogin;
    private TextView tvUserLoginTitle;
    private ImageView ivLoginArea;
    private IntentFilter bcFilter;
    private MusicBroadcastReceiver mReceiver;
    private boolean isPlay = false;
    private String localCookie;
    private Context context;
    //定义Handler对象
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //处理消息
            if (msg.what == Constants.DISMISS) {
                btnNextSong.setEnabled(true);
                btnPlay.setEnabled(true);
            }
            if (msg.what == Constants.UPDATE_TIME) {
                //更新音乐播放状态
                if (isPlay) {
                    mMusicCurrDuration = mMainMediaPlayer.getCurrentPosition();
                    tvCurTime.setText(TimeFormat.msToMinAndS(mMusicCurrDuration));
                    seekBar.setProgress(mMusicCurrDuration);
                }
            }
        }
    };
    //来电标志:只当正在播放的情况下有来电时置为true
    private boolean phoneCome = false;
    //播放器
    private MediaPlayer mMainMediaPlayer;
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            if (mMainMediaPlayer == null) {
                return;
            }
            if (mMainMediaPlayer.isPlaying() && !seekNow) {
                //处理播放
                Message msg = new Message();
                msg.what = Constants.UPDATE_TIME;
                handler.sendMessage(msg);
            }
        }
    };

    //private boolean firstErrorFlag = true;

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (mErrorDlg == null) {
                mErrorDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("网络连接出错啦...")
                        .setCancelText("设置网络")
                        .setConfirmText("退出应用")
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                Intent intent = null;
                                if (Build.VERSION.SDK_INT > 11) {
                                    //Android 3.0以上版本跳转至Wifi设置界面
                                    intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                } else {
                                    //Android 3.0以下版本跳转至Wifi设置界面
                                    intent = new Intent();
                                    ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                                    intent.setComponent(componentName);
                                    intent.setAction("android.intent.action.VIEW");
                                }
                                startActivityForResult(intent, Constants.REQUEST_WIFI_SETTING_CODE);
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                MainActivity.this.finish();
                            }
                        });
                mErrorDlg.show();
            } else {
                if (!mErrorDlg.isShowing()) {
                    mErrorDlg.show();
                }
            }
        }
    };

    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        localCookie = SharedPreferencesUtils.getString(context, Constants.COOKIE, "");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        tvCurTime = (TextView) findViewById(R.id.curTimeText);
        tvTotalTime = (TextView) findViewById(R.id.totalTimeText);
        ivNeedle = (ImageView) findViewById(R.id.iv_needle);
        ivDisk = (ImageView) findViewById(R.id.iv_disk);
        needleUpAnim = AnimationUtils.loadAnimation(this, R.anim.rotation_up);
        needleDownAnim = AnimationUtils.loadAnimation(this, R.anim.rotation_down);
        needleAnim = AnimationUtils.loadAnimation(this, R.anim.rotation_up_down);
        mDiskAnimator = new RotateAnimator(this, ivDisk);
        colorNum = Constants.BACKGROUND_COLORS.length;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        llLeftSlideMenu = (LinearLayout) findViewById(R.id.ll_left_slide_menu);
        rlUserLogin = (RelativeLayout) findViewById(R.id.rl_slide_menu_header);
        rlUserLogin.setOnClickListener(this);
        tvUserLoginTitle = (TextView) findViewById(R.id.tv_user_name);
        ivLoginArea = (ImageView) findViewById(R.id.iv_login_area);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        mToolbar.setTitle("DouFM");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);
        mDrawerList.setVerticalScrollBarEnabled(false);
        playMusicInfo = new MusicInfo();
        nextMusicInfo = new MusicInfo();

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        btnPlay = (Button) findViewById(R.id.btn_start_play);
        btnNextSong = (Button) findViewById(R.id.btn_play_next);
        btnPreSong = (Button) findViewById(R.id.btn_play_previous);
        btnPlayMode = (Button) findViewById(R.id.btn_play_mode);
        btnLove = (Button) findViewById(R.id.btn_love);
        seekBar = (MySeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setProgress(progress);
                mMusicCurrDuration = progress;
                tvCurTime.setText(TimeFormat.msToMinAndS(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekNow = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMusicDuration != 0 && mMusicCurrDuration <= (mMusicDuration * bufPercent / 100)) {
                    mMainMediaPlayer.seekTo(mMusicCurrDuration);
                } else {
                    mMusicCurrDuration = mMainMediaPlayer.getCurrentPosition();
                    tvCurTime.setText(TimeFormat.msToMinAndS(mMusicCurrDuration));
                    seekBar.setProgress(mMusicCurrDuration);
                }
                seekNow = false;
            }
        });

        btnPlay.setOnClickListener(this);
        btnNextSong.setOnClickListener(this);
        btnPreSong.setOnClickListener(this);
        //单曲循环/随便播放按钮点击响应
        btnPlayMode.setOnClickListener(this);
        btnLove.setOnClickListener(this);
        btnPreSong.setClickable(false); //一定要在绑定监听器之后

        try {
            cacheDir = CacheUtil.getDiskCacheDir(this, "music");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, CacheUtil.getAppVersion(this), 1, CacheUtil.DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mThemeNum = SharedPreferencesUtils.getInt(context, Constants.THEME, 13);
        SharedPreferencesUtils.putInt(this, Constants.THEME, mThemeNum);
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[mThemeNum]));
        mDrawerLayout.setBackgroundColor(Color.parseColor(Constants.BACKGROUND_COLORS[mThemeNum]));
        mPlayListNum = SharedPreferencesUtils.getInt(context, Constants.PLAYLIST, 0);
        ivNeedle.startAnimation(needleUpAnim);
        bcFilter = new IntentFilter(Constants.ACTION_CHOOSE_MUSIC);
        mReceiver = new MusicBroadcastReceiver();
        registerReceiver(mReceiver, bcFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PhoneIncomingListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        mListListener = new ListListener();
        if (isFirstLoad) {
            try {
                getMusicList();
            } catch (AuthFailureError authFailureError) {
                authFailureError.printStackTrace();
            }
            isFirstLoad = false;
        }
        updateLoginArea();
        //列表始终滚动到顶部
        mDrawerList.setSelection(0);

    }

    private void getMusicList() throws AuthFailureError {
        JsonArrayRequestWithCookie jsonArrayRequestWithCookie = new JsonArrayRequestWithCookie(Constants.PLAYLIST_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                JSONObject jo = new JSONObject();
                try {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jo = jsonArray.getJSONObject(i);
                        PlaylistInfo playlistInfo = new PlaylistInfo();
                        playlistInfo.setKey(jo.getString("key"));
                        playlistInfo.setName(jo.getString("name"));
                        mLeftResideMenuItemTitleList.add(jo.getString("name"));
                        playlistInfo.setMusic_list(jo.getString("music_list"));
                        mPlaylistInfoList.add(playlistInfo);
                        Log.w("LOG", "key  " + playlistInfo.getKey());
                    }
                    channelListAdapter = new ChannelListAdapter(MainActivity.this, mLeftResideMenuItemTitleList);
                    mDrawerList.setAdapter(channelListAdapter);
                    mDrawerList.setOnItemClickListener(mListListener);
                    isLoadingSuccess = true;
                    initPlayer();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, errorListener);
        jsonArrayRequestWithCookie.setCookie(localCookie);
        RequestManager.getRequestQueue().add(jsonArrayRequestWithCookie);
    }

    private void initPlayer() {
        btnNextSong.setEnabled(false);
        btnPlay.setEnabled(false);
        mMainMediaPlayer = new MediaPlayer(); //创建媒体播放器
        mMainMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); //设置媒体流类型
        mMainMediaPlayer.setOnCompletionListener(this);
        mMainMediaPlayer.setOnErrorListener(this);
        mMainMediaPlayer.setOnBufferingUpdateListener(this);
        mMainMediaPlayer.setOnPreparedListener(this);
        mTimer.schedule(timerTask, 0, 1000);
        try {
            playRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        } finally {
            Toast.makeText(this, "已加载频道" + "『" + mPlaylistInfoList.get(mPlayListNum).getName() + "』", Toast.LENGTH_SHORT).show();
        }
    }

    private void playRandomMusic(final String playlist_key) throws AuthFailureError {
        changeMusic(false);
        final String MUSIC_URL = Constants.MUSIC_IN_PLAYLIST_URL + playlist_key + "/?num=1";
        JsonArrayRequestWithCookie jsonArrayRequestWithCookie = new JsonArrayRequestWithCookie(MUSIC_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                //如果当前有error dialog显示，关闭它
                if (mErrorDlg != null && mErrorDlg.isShowing()) {
                    mErrorDlg.dismiss();
                }
                //请求随机播放音乐文件信息
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    playMusicInfo.setTitle(jo.getString("title"));
                    playMusicInfo.setArtist(jo.getString("artist"));
                    playMusicInfo.setAudio(jo.getString("audio"));
                    playMusicInfo.setCover(jo.getString("cover"));
                    playMusicInfo.setKey(jo.getString("key"));
                    if (mMainMediaPlayer == null) {
                        mMainMediaPlayer = new MediaPlayer();
                        mMainMediaPlayer.setOnCompletionListener(MainActivity.this);
                        mMainMediaPlayer.setOnErrorListener(MainActivity.this);
                        mMainMediaPlayer.setOnBufferingUpdateListener(MainActivity.this);
                        mMainMediaPlayer.setOnPreparedListener(MainActivity.this);
                    }
                    mMainMediaPlayer.setDataSource(Constants.BASE_URL + playMusicInfo.getAudio()); //这种url路径
                    mMainMediaPlayer.prepareAsync(); //prepare自动播放
                    getCoverImageRequest(playMusicInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, errorListener);
        jsonArrayRequestWithCookie.setCookie(localCookie);
        RequestManager.getRequestQueue().add(jsonArrayRequestWithCookie);
    }

    private void getNextMusicInfo(String playlist_key) {
        final String MUSIC_URL = Constants.MUSIC_IN_PLAYLIST_URL + playlist_key + "/?num=1";
        RequestManager.getRequestQueue().add(
                new JsonArrayRequest(MUSIC_URL, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        try {
                            JSONObject jo = jsonArray.getJSONObject(0);
                            nextMusicInfo.setKey(jo.getString("key"));
                            nextMusicInfo.setTitle(jo.getString("title"));
                            nextMusicInfo.setArtist(jo.getString("artist"));
                            nextMusicInfo.setAudio(jo.getString("audio"));
                            nextMusicInfo.setCover(jo.getString("cover"));
                            mDownThread = new DownloadMusicThread(nextMusicInfo.getAudio());
                            mDownThread.start();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, errorListener)
        );
    }

    //任何情况下切换歌曲进行的操作首先考虑放入该方法中
    private void changeMusic(boolean type) {
        //true for cache,false for random
        if (type) {
            if (isPlay) {
                //正在播放状态下切歌，播放needleAnim动画
                needleDownFlag = false;
                ivNeedle.startAnimation(needleAnim);
            } else {
                needleDownFlag = true;
            }
        } else {
            needleDownFlag = true;
            if (isPlay) {
                ivNeedle.startAnimation(needleUpAnim);
            }
        }
        isPlay = false;
        mMusicDuration = 0;
        btnNextSong.setEnabled(false);
        btnPlay.setEnabled(false);
        mDiskAnimator.pause();
        seekBar.setProgress(0);
        seekBar.setSecondaryProgress(0);
        tvCurTime.setText("00:00");
        tvTotalTime.setText("00:00");
        //单曲循环模式下切换歌曲回到随机播放状态
        if (playLoopFlag) {
            playLoopFlag = false;
            btnPlayMode.setBackgroundResource(R.drawable.bg_btn_shuffle);//随机播放
        }
        mMainMediaPlayer.reset();
    }

    private void playCacheMusic() {
        changeMusic(true);
        String key = CacheUtil.hashKeyForDisk(playMusicInfo.getAudio());
        try {
            mMainMediaPlayer.setDataSource(cacheDir.toString() + "/" + key + ".0");
            mMainMediaPlayer.prepare();
            seekBar.setSecondaryProgress(seekBar.getMax());
        } catch (IOException e) {
            e.printStackTrace();
        }
        getCoverImageRequest(playMusicInfo);
        saveUserListenHistory();
    }

    private void getCoverImageRequest(final MusicInfo musicInfo) {
        RequestManager.getRequestQueue().add(new ImageRequest(Constants.BASE_URL + musicInfo.getCover(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        //对齐新歌曲信息显示时间
                        ivDisk.setImageBitmap(mDiskAnimator.getCroppedBitmap(bitmap));
                        mToolbar.setTitle(musicInfo.getTitle());
                        mToolbar.setSubtitle(musicInfo.getArtist());
                        updateLoveBtn();
                    }
                }, 0, 0, null, errorListener)
        );
    }

    private void updateLoveBtn() {
        Realm realm = Realm.getInstance(this);
        if (playMusicInfo.getAudio() != null) {
            RealmResults<UserLoveMusicInfo> realmResults = realm.where(UserLoveMusicInfo.class).equalTo("musicURL", playMusicInfo.getAudio()).findAll();
            if (realmResults.size() > 0) {
                loveFlag = true;
                btnLove.setBackgroundResource(R.drawable.bg_btn_loved);
            } else {
                loveFlag = false;
                btnLove.setBackgroundResource(R.drawable.bg_btn_love);
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        preMusicInfo = playMusicInfo;
        btnPreSong.setClickable(true);
        if (hasNextCache) {
            playMusicInfo = nextMusicInfo;
            nextMusicInfo = new MusicInfo();
            playCacheMusic();
            hasNextCache = false;
        } else {
            if (mDownThread != null) {
                mDownThread.runFlag = false;
                mDownThread = null;
            }
            playMusicInfo = new MusicInfo();
            try {
                playRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
            } catch (AuthFailureError authFailureError) {
                authFailureError.printStackTrace();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mMainMediaPlayer != null) {
            mMainMediaPlayer.stop();
            mMainMediaPlayer.release();
            mMainMediaPlayer = null;
        }
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("网络连接出错啦...")
                .setCancelText("等待")
                .setConfirmText("退出")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        return;
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        finish();
                    }
                })
                .show();
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //保存当前下载的进度
        if (percent != bufPercent) {
            bufPercent = percent;
            if (mMusicDuration != 0) {
                seekBar.setSecondaryProgress(mMusicDuration * bufPercent / 100);
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (needleDownFlag) {
            ivNeedle.startAnimation(needleDownAnim);
        }
        mDiskAnimator.play();
        mMusicDuration = mMainMediaPlayer.getDuration();
        tvTotalTime.setText(TimeFormat.msToMinAndS(mMusicDuration));
        seekBar.setMax(mMusicDuration);
        isPlay = true;
        mMainMediaPlayer.start();
        btnPlay.setBackgroundResource(R.drawable.btn_stop_play);
        btnNextSong.setEnabled(true);
        btnPlay.setEnabled(true);
        getNextMusicInfo(mPlaylistInfoList.get(mPlayListNum).getKey());
        saveUserListenHistory();

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        unregisterReceiver(mReceiver);
        if (mMainMediaPlayer != null) {
            mMainMediaPlayer.stop();
            mMainMediaPlayer.release();
            mMainMediaPlayer = null;
        }
        RequestManager.getRequestQueue().cancelAll(this);
    }

    private void PhoneIncomingListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onBackPressed() {
        //如果左边栏打开时，返回键关闭左边栏
        if (mDrawerLayout.isDrawerOpen(llLeftSlideMenu)) {
            mDrawerLayout.closeDrawer(llLeftSlideMenu);
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(llLeftSlideMenu)) {
                mDrawerLayout.closeDrawer(llLeftSlideMenu);
                mDrawerLayout.setFocusableInTouchMode(true);
            } else {
                mDrawerLayout.openDrawer(llLeftSlideMenu);
                mDrawerLayout.setFocusableInTouchMode(false);
            }
        } else if (item.getItemId() == R.id.app_about_team) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("DouFM - Android客户端")
                    .setContentText(getResources().getString(R.string.title_activity_about))
                    .show();
        } else if (item.getItemId() == R.id.switch_theme) {
            int colorIndex = (int) (Math.random() * colorNum);
            if (colorIndex == colorNum) {
                colorIndex--;
            }
            if (colorIndex < 0) {
                colorIndex = 0;
            }
            mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[colorIndex]));
            mDrawerLayout.setBackgroundColor(Color.parseColor(Constants.BACKGROUND_COLORS[colorIndex]));
            mThemeNum = colorIndex;
            SharedPreferencesUtils.putInt(context, Constants.THEME, mThemeNum);
            SharedPreferencesUtils.putInt(context, Constants.PLAYLIST, mPlayListNum);
            updateRLUserLoginBg();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_play:
                if (isPlay) {
                    isPlay = false;
                    ivNeedle.startAnimation(needleUpAnim);
                    btnPlay.setBackgroundResource(R.drawable.btn_start_play);
                    mMainMediaPlayer.pause();
                    mDiskAnimator.pause();
                } else {
                    isPlay = true;
                    ivNeedle.startAnimation(needleDownAnim);
                    btnPlay.setBackgroundResource(R.drawable.btn_stop_play);
                    mMainMediaPlayer.start();
                    mDiskAnimator.play();
                }
                break;
            case R.id.btn_play_next:
                preMusicInfo = playMusicInfo;
                btnPreSong.setClickable(true);
                if (hasNextCache) {
                    playMusicInfo = nextMusicInfo;
                    nextMusicInfo = new MusicInfo();
                    playCacheMusic();
                    hasNextCache = false;
                } else {
                    if (mDownThread != null) {
                        mDownThread.runFlag = false;
                        mDownThread = null;
                    }
                    playMusicInfo = new MusicInfo();
                    try {
                        playRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
                    } catch (AuthFailureError authFailureError) {
                        authFailureError.printStackTrace();
                    }
                }
                break;
            case R.id.btn_play_previous:
                if (mDownThread != null) {
                    mDownThread.runFlag = false;
                    mDownThread = null;
                }
                playMusicInfo = preMusicInfo;
                btnPreSong.setClickable(false);
                String key = CacheUtil.hashKeyForDisk(playMusicInfo.getAudio());
                try {
                    if (mDiskLruCache.get(key) != null) {
                        changeMusic(true);
                        mMainMediaPlayer.setDataSource(cacheDir.toString() + "/" + key + ".0");
                        mMainMediaPlayer.prepare();
                        seekBar.setSecondaryProgress(seekBar.getMax());
                    } else {
                        changeMusic(false);
                        mMainMediaPlayer.setDataSource(Constants.BASE_URL + playMusicInfo.getAudio());
                        mMainMediaPlayer.prepareAsync();
                        saveUserListenHistory();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getCoverImageRequest(playMusicInfo);
                break;
            case R.id.btn_play_mode:
                if (playLoopFlag) {
                    btnPlayMode.setBackgroundResource(R.drawable.bg_btn_shuffle);//随机播放
                    Toast.makeText(getApplicationContext(), "随机播放", Toast.LENGTH_SHORT).show();
                    mMainMediaPlayer.setLooping(false);
                } else {
                    btnPlayMode.setBackgroundResource(R.drawable.bg_btn_one);//单曲循环
                    Toast.makeText(getApplicationContext(), "单曲循环", Toast.LENGTH_SHORT).show();
                    mMainMediaPlayer.setLooping(true);
                }
                playLoopFlag = !playLoopFlag;
                break;
            case R.id.btn_love:
                if (User.getInstance().getLogin()) {
                    if (loveFlag) {
                        btnLove.setBackgroundResource(R.drawable.bg_btn_love);
                        deleteLoveMusic();
                        Toast.makeText(getApplicationContext(), "为您取消收藏", Toast.LENGTH_SHORT).show();
                    } else {
                        btnLove.setBackgroundResource(R.drawable.bg_btn_loved);
                        saveLoveMusic();
                        Toast.makeText(getApplicationContext(), "为您收藏本歌", Toast.LENGTH_SHORT).show();
                    }
                    loveFlag = !loveFlag;
                } else {
                    showTips();
                }

                break;
            case R.id.rl_slide_menu_header:
                if (mDrawerLayout.isDrawerOpen(llLeftSlideMenu)) {
                    mDrawerLayout.closeDrawer(llLeftSlideMenu);
                }
                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_THEME, mThemeNum);
                if (User.getInstance().getLogin()) {
                    intent.setClass(MainActivity.this, UserActivity.class);
                    startActivityForResult(intent, Constants.REQUEST_USER_CODE);
                } else {
                    intent.setClass(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, Constants.REQUEST_LOGIN_CODE);
                }
                break;
        }
    }

    private void showTips() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("尚未登录，是否马上登录?")
                .setCancelText("不了，下次")
                .setConfirmText("马上登录")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), Constants.REQUEST_LOGIN_CODE);
                        sDialog.dismiss();
                    }
                }).show();
    }

    private class ListListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isLoadingSuccess) {
                mDrawerLayout.closeDrawers();
                mPlayListNum = position;
                mDiskAnimator.pause();
                preMusicInfo = playMusicInfo;
                btnPreSong.setClickable(true);
                if (mDownThread != null) {
                    mDownThread.runFlag = false;
                    mDownThread = null;
                }
                playMusicInfo = new MusicInfo();
                try {
                    playRandomMusic(mPlaylistInfoList.get(position).getKey());
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                }
            }
        }
    }

    private class DownloadMusicThread extends Thread {

        public boolean runFlag;
        private String url;

        public DownloadMusicThread(String url) {
            super();
            this.url = url;
            runFlag = true;
        }

        @Override
        public void run() {
            super.run();
            try {
                String key = CacheUtil.hashKeyForDisk(url);
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (downloadUrlToStream(Constants.BASE_URL + url, outputStream)) {
                        editor.commit();
                        hasNextCache = true;
                    } else {
                        editor.abort();
                    }
                }
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
            HttpURLConnection urlConnection = null;
            BufferedOutputStream out = null;
            BufferedInputStream in = null;
            try {
                final URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
                out = new BufferedOutputStream(outputStream, 8 * 1024);
                int b;
                while ((b = in.read()) != -1) {
                    if (runFlag) {
                        out.write(b);
                    } else {
                        return false;
                    }
                }
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    private class MyPhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //来电
                    if (isPlay) {
                        mMainMediaPlayer.pause();
                        isPlay = false;
                        phoneCome = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //通话结束
                    if (phoneCome && mMainMediaPlayer != null) {
                        mMainMediaPlayer.start();
                        isPlay = true;
                        phoneCome = false;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_LOGIN_CODE:
                if (resultCode == 100) {
                    updateLoginArea();
                    saveUserListenHistory();
                }
                break;
            case Constants.REQUEST_USER_CODE:
                if (resultCode == 100) {
                    updateLoginArea();
                    Toast.makeText(MainActivity.this, "您已退出登录", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 200) {
                    //此代码需要保留，应该返回主界面有两种情况，这种情况不需要更新登录状态
                }
                break;
            //设置wifi后返回(忽略是否设置成功)随机播放下一首，关闭Dialog
            case Constants.REQUEST_WIFI_SETTING_CODE:
                try {
                    playRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                }
                break;
        }
    }

    private void saveUserListenHistory() {
        if (User.getInstance().getLogin()) {
/*            //本地保存
            Realm realm = Realm.getInstance(this);
            //如果历史记录已存在，这不在保存
            if (playMusicInfo.getAudio() != null) {
                RealmResults<UserHistoryInfo> realmResults = realm.where(UserHistoryInfo.class).equalTo("musicURL", playMusicInfo.getAudio()).findAll();
                if (realmResults.size() == 0) {
                    RealmResults<UserHistoryInfo> records = realm.where(UserHistoryInfo.class).findAll();
                    realm.beginTransaction();
                    //保存
                    UserHistoryInfo userHistoryInfo = realm.createObject(UserHistoryInfo.class);
                    userHistoryInfo.setHistory_id(records.size() + 1);
                    userHistoryInfo.setUserID(User.getInstance().getUserID());
                    userHistoryInfo.setTitle(playMusicInfo.getTitle());
                    userHistoryInfo.setSinger(playMusicInfo.getArtist());
                    userHistoryInfo.setMusicURL(playMusicInfo.getAudio());
                    userHistoryInfo.setCover(playMusicInfo.getCover());
                    realm.commitTransaction();
                }*/
            //上传服务器
            if (playMusicInfo.getKey() != null) {
                try {
                    uploadUserOp("listened", playMusicInfo.getKey());
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                }
            }
        }
    }

    private void saveLoveMusic() {
        if (User.getInstance().getLogin()) {
            Realm realm = Realm.getInstance(this);
            if (playMusicInfo.getAudio() != null) {
                RealmResults<UserLoveMusicInfo> realmResults = realm.where(UserLoveMusicInfo.class).equalTo("musicURL", playMusicInfo.getAudio()).findAll();
                if (realmResults.size() == 0) {
                    RealmResults<UserLoveMusicInfo> records = realm.where(UserLoveMusicInfo.class).findAll();
                    realm.beginTransaction();
                    UserLoveMusicInfo userLoveMusicInfo = realm.createObject(UserLoveMusicInfo.class);
                    userLoveMusicInfo.setLove_id(records.size() + 1);
                    userLoveMusicInfo.setUserID(User.getInstance().getUserID());
                    userLoveMusicInfo.setTitle(playMusicInfo.getTitle());
                    userLoveMusicInfo.setSinger(playMusicInfo.getArtist());
                    userLoveMusicInfo.setMusicURL(playMusicInfo.getAudio());
                    userLoveMusicInfo.setCover(playMusicInfo.getCover());
                    for (int i = 1; i < records.size(); i++) {
                        records.get(i - 1).setLove_id(i);
                    }
                    realm.commitTransaction();
                    realm.close();
                }
                //上传服务器
                if (playMusicInfo.getKey() != null) {
                    try {
                        uploadUserOp("favor", playMusicInfo.getKey());
                    } catch (AuthFailureError authFailureError) {
                        authFailureError.printStackTrace();
                    }
                }

            }
        }
    }

    private void deleteLoveMusic() {
        if (User.getInstance().getLogin()) {
            if (playMusicInfo.getAudio() != null) {
                Realm realm = Realm.getInstance(this);
                RealmResults<UserLoveMusicInfo> realmResults = realm.where(UserLoveMusicInfo.class).equalTo("musicURL", playMusicInfo.getAudio()).findAll();
                if (realmResults.size() > 0) {
                    RealmResults<UserLoveMusicInfo> records = realm.where(UserLoveMusicInfo.class).findAll();
                    realm.beginTransaction();
                    //保存
                    realmResults.get(0).removeFromRealm();
                    for (int i = 1; i < records.size(); i++) {
                        records.get(i - 1).setLove_id(i);
                    }
                    realm.commitTransaction();
                    realm.close();
                }
                //上传服务器，重复操作表示取消原先的操作
                if (playMusicInfo.getKey() != null) {
                    try {
                        uploadUserOp("favor", playMusicInfo.getKey());
                    } catch (AuthFailureError authFailureError) {
                        authFailureError.printStackTrace();
                    }
                }
            }
        }
    }

    private void uploadUserOp(String opType, String musicKey) throws AuthFailureError {
        HashMap<String, String> opMap = new HashMap<String, String>();
        opMap.put("op", opType);
        opMap.put("key", musicKey);
        JSONObject opJsonObject = new JSONObject(opMap);
        JsonObjectRequestWithCookie jsonObjectRequestWithCookie = new JsonObjectRequestWithCookie(Constants.USER_HISTORY_URL, opJsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.w("LOG", "get response jsonObject from post user history" + jsonObject.toString());
                try {
                    if (jsonObject.getString("status").equals("listened_success")) {
                        Log.w("LOG", "post listened success");
                    } else if (jsonObject.getString("status").equals("favor_success")) {
                        Log.w("LOG", "favor success");
                    } else if (jsonObject.getString("status").equals("dislike_success")) {
                        Log.w("LOG", "dislike success");
                    } else if (jsonObject.getString("status").equals("shared_success")) {
                        Log.w("LOG", "shared success");
                    } else {
                        Log.w("LOG", "post /api/use/history/ failure");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("LOG", "操作失败");
            }
        }, opMap);
        String localCookie = SharedPreferencesUtils.getString(context, Constants.COOKIE, "");
        jsonObjectRequestWithCookie.setCookie(localCookie);
        RequestManager.getRequestQueue().add(jsonObjectRequestWithCookie);
    }

    private MusicInfo findMusic(Intent intent) {
        MusicInfo tempMusicInfo = new MusicInfo();
        byte listType = intent.getByteExtra(Constants.EXTRA_LIST_TYPE, (byte) -1);
        if (listType == Constants.HISTORY_TYPE) {
            UserHistoryInfo userHistoryInfo = (UserHistoryInfo) intent.getSerializableExtra(Constants.EXTRA_MUSIC_ID);
            tempMusicInfo.setKey(userHistoryInfo.getKey());
            tempMusicInfo.setTitle(userHistoryInfo.getTitle());
            tempMusicInfo.setArtist(userHistoryInfo.getSinger());
            tempMusicInfo.setAudio(userHistoryInfo.getMusicURL());
            tempMusicInfo.setCover(userHistoryInfo.getCover());
        } else if (listType == Constants.LOVE_TYPE) {
            UserLoveMusicInfo userLoveMusicInfo = (UserLoveMusicInfo) intent.getSerializableExtra(Constants.EXTRA_MUSIC_ID);
            tempMusicInfo.setKey(userLoveMusicInfo.getKey());
            tempMusicInfo.setTitle(userLoveMusicInfo.getTitle());
            tempMusicInfo.setArtist(userLoveMusicInfo.getSinger());
            tempMusicInfo.setAudio(userLoveMusicInfo.getMusicURL());
            tempMusicInfo.setCover(userLoveMusicInfo.getCover());
        }
        return tempMusicInfo;
    }

    private class MusicBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mDownThread != null) {
                mDownThread.runFlag = false;
                mDownThread = null;
            }
            preMusicInfo = playMusicInfo;
            btnPreSong.setClickable(true);
            playMusicInfo = findMusic(intent);
            String key = CacheUtil.hashKeyForDisk(playMusicInfo.getAudio());
            try {
                if (mDiskLruCache.get(key) != null) {
                    changeMusic(true);
                    mMainMediaPlayer.setDataSource(cacheDir.toString() + "/" + key + ".0");
                    mMainMediaPlayer.prepare();
                    seekBar.setSecondaryProgress(seekBar.getMax());
                    saveUserListenHistory();
                } else {
                    changeMusic(false);
                    mMainMediaPlayer.setDataSource(Constants.BASE_URL + playMusicInfo.getAudio());
                    mMainMediaPlayer.prepareAsync();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            getCoverImageRequest(playMusicInfo);
        }
    }

    private void updateLoginArea() {
        updateRLUserLoginBg();
        if (User.getInstance().getLogin()) {
            tvUserLoginTitle.setText("你好, " + SharedPreferencesUtils.getString(this, Constants.LOGIN_USR_NAME, ""));
        } else {
            tvUserLoginTitle.setText("使用睿思账号登陆");
        }
    }

    private void updateRLUserLoginBg() {
        rlUserLogin.setBackgroundResource(Constants.SLIDE_MENU_HEADERS[SharedPreferencesUtils.getInt(this, Constants.THEME, 13)]);
    }
}
