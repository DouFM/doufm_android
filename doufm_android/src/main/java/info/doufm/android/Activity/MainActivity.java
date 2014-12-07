package info.doufm.android.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;
import info.doufm.android.R;
import info.doufm.android.info.MusicInfo;
import info.doufm.android.info.PlaylistInfo;
import info.doufm.android.playview.MySeekBar;
import info.doufm.android.playview.PlayView;
import info.doufm.android.utils.CacheUtil;
import info.doufm.android.utils.Constants;
import libcore.io.DiskLruCache;


public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private boolean drawerArrowColor;
    private File cacheDir;
    private DiskLruCache mDiskLruCache = null;
    //添加了两个MusicInfo代替MusicURL、CoverURL等
    private MusicInfo playMusicInfo;
    private MusicInfo nextMusicInfo;
    private boolean hasNextCache = false;
    private DownloadMusicThread mDownThread;
    //Meterial Design主题(500 300 100)
    private String[] mActionBarColors = {"#607d8b", "#ff5722", "#795548",
            "#ffc107", "#ff9800", "#259b24",
            "#8bc34a", "#cddc39", "#03a9f4",
            "#00bcd4", "#009688", "#673ab7",
            "#673ab7", "#3f51b5", "#5677fc",
            "#e51c23", "#e91e63", "#9c27b0",
            "#607d8b"};
    private String[] mBackgroundColors = {"#90a4ae", "#ff8a65", "#a1887f",
            "#ffd54f", "#ffb74d", "#42bd41",
            "#aed581", "#dce775", "#4fc3f7",
            "#4dd0e1", "#4db6ac", "#9575cd",
            "#9575cd", "#7986cb", "#91a7ff",
            "#f36c60", "#f06292", "#ba68c8",
            "#90a4ae"};
    private String[] mCotrolBackgroundColors = {"#cfd8dc", "#ffccbc", "#d7ccc8",
            "#ffecb3", "#ffe0b2", "#a3e9a4",
            "#dcedc8", "#f0f4c3", "#b3e5fc",
            "#b2ebf2", "#b2dfdb", "#d1c4e9",
            "#dec4e9", "#c5cae9", "#d0d9ff",
            "#f9bdbb", "#f8bbd0", "#e1bee7",
            "#cfd8dc"};
    private int colorIndex = 0;
    private int colorNum;

    //菜单列表监听器
    private ListListener mListLisener;

    //播放界面相关
    private FrameLayout mContainer;
    private Button btnPlay;
    private Button btnNextSong;
    private MySeekBar seekBar;
    private PlayView mPlayView;

    private boolean isLoadingSuccess = false;
    private int mMusicDuration;            //音乐总时间
    private int mMusicCurrDuration;        //当前播放时间


    private Timer mTimer = new Timer();     //计时器
    private List<String> mLeftResideMenuItemTitleList = new ArrayList<String>();
    private List<PlaylistInfo> mPlaylistInfoList = new ArrayList<PlaylistInfo>();
    private int PLAYLIST_MENU_NUM = 0;
    private int mPlayListNum = 0;
    private boolean isFirstLoad = true;

    private ActionBar ab;

    private String mPreMusicURL;
    //音乐文件和封面路径
    private String MusicURL = "";
    private String CoverURL = "";
    private TextView tvMusicTitle;

    private boolean isPlay = false;
    //来电标志:只当正在播放的情况下有来电时置为true
    private boolean phoneCome = false;
    //播放器
    private MediaPlayer mMainMediaPlayer;

    private String PLAYLIST_URL = "http://doufm.info/api/playlist/?start=0";
    //Volley请求
    private RequestQueue mRequstQueue;

    private RelativeLayout rtBottom;

    private String mMusicTitle;
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
                    if (mMainMediaPlayer == null) {
                        return;
                    }
                    mMusicCurrDuration = mMainMediaPlayer.getCurrentPosition();
                    mMusicDuration = mMainMediaPlayer.getDuration();
                    seekBar.setMax(mMusicDuration);
                    if (mMusicDuration > 0) {
                        seekBar.setProgress(mMusicCurrDuration);
                    }
                }
            }
        }
    };

    private class ListListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isLoadingSuccess) {
                mDrawerLayout.closeDrawers();
/*                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                mPlayListNum = position;
                //mPlayView.pause();
                if (mDownThread != null) {
                    mDownThread.runFlag = false;
                    mDownThread = null;
                }
                playRandomMusic(mPlaylistInfoList.get(position).getKey());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        rtBottom = (RelativeLayout) findViewById(R.id.bottom);
        ab.setHomeButtonEnabled(true);
        colorNum = mBackgroundColors.length;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);
        mDrawerList.setVerticalScrollBarEnabled(false);
        tvMusicTitle = (TextView) findViewById(R.id.MusicTitle);
        playMusicInfo = new MusicInfo();
        nextMusicInfo = new MusicInfo();
        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, drawerArrow, R.string.drawer_open, R.string.drawer_close) {

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

        mContainer = (FrameLayout) findViewById(R.id.media_container);
        btnPlay = (Button) findViewById(R.id.btn_start_play);
        btnNextSong = (Button) findViewById(R.id.btn_stop_play);
        seekBar = (MySeekBar) findViewById(R.id.seekbar);

        mPlayView = new PlayView(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mContainer.addView(mPlayView, lp);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isPlay) {
                    isPlay = false;
                    btnPlay.setBackgroundResource(R.drawable.btn_start_play);
                    mMainMediaPlayer.pause();
                    mPlayView.pause();
                } else {
                    isPlay = true;
                    btnPlay.setBackgroundResource(R.drawable.btn_stop_play);
                    mMainMediaPlayer.start();
                    mPlayView.play();
                }
            }
        });

        btnNextSong.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.i(TAG,"after click:"+System.currentTimeMillis());
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
                    playRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
                }
            }
        });

        mRequstQueue = Volley.newRequestQueue(this);

        try {
            cacheDir = CacheUtil.getDiskCacheDir(this, "music");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, CacheUtil.getAppVersion(this), 1, CacheUtil.DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PhoneIncomingListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mListLisener = new ListListener();
        if (isFirstLoad) {
            getMusicList();
            isFirstLoad = false;
        }
    }

    private void getMusicList() {
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                            android.R.layout.simple_list_item_1, android.R.id.text1, mLeftResideMenuItemTitleList);
                    mDrawerList.setAdapter(adapter);
                    mDrawerList.setOnItemClickListener(mListLisener);
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
             * @throws com.android.volley.AuthFailureError
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
        btnNextSong.setEnabled(false);
        btnPlay.setEnabled(false);
        mMainMediaPlayer = new MediaPlayer(); //创建媒体播放器
        mMainMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); //设置媒体流类型
        mMainMediaPlayer.setOnCompletionListener(this);
        mMainMediaPlayer.setOnErrorListener(this);
        mMainMediaPlayer.setOnBufferingUpdateListener(this);
        mMainMediaPlayer.setOnPreparedListener(this);
        mTimer.schedule(timerTask, 0, 1000);
        playRandomMusic(mPlaylistInfoList.get(0).getKey());
    }

    private void playRandomMusic(String playlist_key) {
        btnNextSong.setEnabled(false);
        btnPlay.setEnabled(false);
        mPlayView.pause();
        //切换歌曲时立即停止正在播放的歌曲
        mMainMediaPlayer.reset();
        isPlay = false;
        final String MUSIC_URL = "http://doufm.info/api/playlist/" + playlist_key + "/?num=1";
        JsonArrayRequest jaq = new JsonArrayRequest(MUSIC_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                //请求随机播放音乐文件信息
                try {
                    //Log.i(TAG,"before setSource:"+System.currentTimeMillis());
                    JSONObject jo = jsonArray.getJSONObject(0);
                    playMusicInfo.setTitle(jo.getString("title"));
                    playMusicInfo.setAudio("http://doufm.info" + jo.getString("audio"));
                    playMusicInfo.setCover("http://doufm.info" + jo.getString("cover"));
                    mMainMediaPlayer.setDataSource(playMusicInfo.getAudio()); //这种url路径
                    mMainMediaPlayer.prepareAsync(); //prepare自动播放
                    getCoverImageRequest(playMusicInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, errorListener);
        mRequstQueue.add(jaq);
    }

    private void getNextMusicInfo(String playlist_key) {
        final String MUSIC_URL = "http://doufm.info/api/playlist/" + playlist_key + "/?num=1";
        JsonArrayRequest jar = new JsonArrayRequest(MUSIC_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    nextMusicInfo.setTitle(jo.getString("title"));
                    nextMusicInfo.setAudio("http://doufm.info" + jo.getString("audio"));
                    nextMusicInfo.setCover("http://doufm.info" + jo.getString("cover"));
                    mDownThread = new DownloadMusicThread(nextMusicInfo.getAudio());
                    mDownThread.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(MainActivity.this, "网络出错啦，请检查校园网设置", Toast.LENGTH_SHORT).show();
            }
        });
        mRequstQueue.add(jar);
    }

    private void playCacheMusic() {
        //切换歌曲时立即停止正在播放的歌曲
        mMainMediaPlayer.reset();
        isPlay = false;
        btnNextSong.setEnabled(false);
        btnPlay.setEnabled(false);
        mPlayView.pause();
        String key = CacheUtil.hashKeyForDisk(playMusicInfo.getAudio());
        try {
            mMainMediaPlayer.setDataSource(cacheDir.toString() + "/" + key + ".0");
            mMainMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getCoverImageRequest(playMusicInfo);
    }

    private class DownloadMusicThread extends Thread {

        private String url;
        public boolean runFlag;

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
                    if (downloadUrlToStream(url, outputStream)) {
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

    private void getCoverImageRequest(final MusicInfo musicInfo) {
        ImageRequest imageRequest = new ImageRequest(musicInfo.getCover(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                //对齐新歌曲信息显示时间
                mPlayView.SetCDImage(bitmap);
                tvMusicTitle.setText(musicInfo.getTitle());
                seekBar.setProgress(0);
            }
        }, 0, 0, null, errorListener);
        mRequstQueue.add(imageRequest);
    }

    private boolean fisrtErrorFlag = true;

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            timerTask.cancel();
            if (fisrtErrorFlag) {
                fisrtErrorFlag = false;
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("网络连接出错啦...")
                        .setConfirmText("退出")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                finish();
                                System.exit(0);
                            }
                        })
                        .show();
            }
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
        } else if (item.getItemId() == R.id.app_about_team) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("DouFM - Android客户端")
                    .setContentText(getResources().getString(R.string.title_activity_about))
                    .show();
        } else if (item.getItemId() == R.id.switch_theme) {
            colorIndex = (int) (Math.random() * colorNum);
            if (colorIndex == colorNum) {
                colorIndex--;
            }
            if (colorIndex < 0) {
                colorIndex = 0;
            }
            ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor(mActionBarColors[colorIndex])));
            mPlayView.SetBgColor(mBackgroundColors[colorIndex]);
            rtBottom.setBackgroundColor(Color.parseColor(mCotrolBackgroundColors[colorIndex]));
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

    @Override
    public void onCompletion(MediaPlayer mp) {
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
            playRandomMusic(mPlaylistInfoList.get(mPlayListNum).getKey());
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

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Log.i(TAG,"before start:"+System.currentTimeMillis());
        isPlay = true;
        mMainMediaPlayer.start();
        mPlayView.play();
        btnPlay.setBackgroundResource(R.drawable.btn_stop_play);
        btnNextSong.setEnabled(true);
        btnPlay.setEnabled(true);
        getNextMusicInfo(mPlaylistInfoList.get(mPlayListNum).getKey());

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
    }

    private void PhoneIncomingListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
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
                    //每次切回MainActivity执行
                    if (isPlay && mPlayView != null) {
                        mPlayView.play();
                    }
                    break;
            }
        }
    }

    private int mBackKeyPressedCount = 1;
    private long exitTime = 0;

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            if (mMainMediaPlayer == null) {
                return;
            }
            if (mMainMediaPlayer.isPlaying()) {
                //处理播放
                //Log.d(TAG , "TimerTask run");
                Message msg = new Message();
                msg.what = Constants.UPDATE_TIME;
                handler.sendMessage(msg);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sample, menu);
        return true;
    }
}
