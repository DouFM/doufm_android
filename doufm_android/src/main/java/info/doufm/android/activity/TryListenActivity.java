package info.doufm.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.Timer;
import java.util.TimerTask;

import info.doufm.android.R;
import info.doufm.android.playview.MySeekBar;
import info.doufm.android.playview.RotateAnimator;
import info.doufm.android.utils.Constants;
import info.doufm.android.utils.TimeFormat;

/**
 * 网易云音乐播放器播放界面UI及简单媒体播放
 *
 * @author Lqh
 */
public class TryListenActivity extends ActionBarActivity {
    protected static final String TAG = "seekBar";
    private MediaPlayer mediaPlayer;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private Animation needleUpAnim;
    private Animation needleDownAnim;
    private Button btnPlay;
    private Button btnPlayMode;
    private boolean isPlay = false;
    private RotateAnimator mDiskAnimator;
    private ImageView ivDisk;
    private ImageView ivNeedle;
    //来电标志:只当正在播放的情况下有来电时置为true
    private boolean phoneCome = false;
    //更新播放进度及时间
    private TextView tvCurTime, tvTotalTime;
    private boolean seekNow = false; //互斥变量，防止定时器与SeekBar拖动时进度冲突
    private MySeekBar seekBar;
    private int mMusicDuration;            //音乐总时间
    private int mMusicCurrDuration;        //当前播放时间
    private Timer mTimer = new Timer();  //计时器
    private boolean isFirstLoad = true;
    //定义Handler对象
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.UPDATE_TIME) {
                Log.i(TAG, "handler get the message");
                //更新音乐播放状态
                if (isPlay) {
                    Log.i(TAG, "handler get the message, and fet currentPosition" + mediaPlayer.getCurrentPosition());
                    mMusicCurrDuration = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(mMusicCurrDuration);
                }
            }
        }
    };
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            }
            if (mediaPlayer != null && !seekNow) {
                Message msg = new Message();
                msg.what = Constants.UPDATE_TIME;
                handler.sendMessage(msg);
                Log.i(TAG, "i have schedule the timerTask");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_listen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ivDisk = (ImageView) findViewById(R.id.iv_disk);
        ivNeedle = (ImageView) findViewById(R.id.iv_needle);
        needleUpAnim = AnimationUtils.loadAnimation(this, R.anim.rotation_up);
        needleDownAnim = AnimationUtils.loadAnimation(this, R.anim.rotation_down);
        mDiskAnimator = new RotateAnimator(this, ivDisk);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        btnPlay = (Button) findViewById(R.id.btn_start_play);
        btnPlay.setBackgroundResource(R.drawable.btn_stop_play);
        btnPlayMode = (Button) findViewById(R.id.btn_play_mode);
        btnPlayMode.setBackgroundResource(R.drawable.bg_btn_one);//单曲循环
        btnPlayMode.setClickable(false);
        mToolbar.setTitle("DouFM");
        mToolbar.setSubtitle("试听中...");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //新建、初始化MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.home);
        mediaPlayer.setLooping(true);

        //更新音乐播放进度
        tvCurTime = (TextView) findViewById(R.id.curTimeText);
        tvTotalTime = (TextView) findViewById(R.id.totalTimeText);
        seekBar = (MySeekBar) findViewById(R.id.seekbar);
        mMusicDuration = mediaPlayer.getDuration();
        seekBar.setMax(mMusicDuration);
        seekBar.setSecondaryProgress(mMusicDuration);
        tvTotalTime.setText(TimeFormat.msToMinAndS(mMusicDuration));
        Log.i(TAG, "on create");
        mMusicCurrDuration = 0;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setProgress(progress);
                mMusicCurrDuration = progress;
                tvCurTime.setText(TimeFormat.msToMinAndS(progress));
                Log.i(TAG, "current progress is " + mMusicCurrDuration);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekNow = true;
                Log.i(TAG, "start seek");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMusicDuration != 0 && mMusicCurrDuration <= mMusicDuration) {
                    mediaPlayer.seekTo(mMusicCurrDuration);
                    Log.i(TAG, "after seek, seek to current time" + mMusicCurrDuration);
                }
                //如果进度条拉超了，则还是留在原位
                else {
                    mMusicCurrDuration = mediaPlayer.getCurrentPosition();
                    tvCurTime.setText(TimeFormat.msToMinAndS(mMusicCurrDuration));
                    seekBar.setProgress(mMusicCurrDuration);
                    Log.i(TAG, "after seek,still in current position " + mMusicCurrDuration);
                }
                seekNow = false;
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlay) {
                    ivNeedle.startAnimation(needleUpAnim);
                    btnPlay.setBackgroundResource(R.drawable.btn_start_play);
                    mDiskAnimator.pause();
                    mediaPlayer.pause();
                    isPlay = false;
                } else {
                    ivNeedle.startAnimation(needleDownAnim);
                    btnPlay.setBackgroundResource(R.drawable.btn_stop_play);
                    mDiskAnimator.play();
                    mediaPlayer.start();
                    isPlay = true;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        PhoneIncomingListener();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (isFirstLoad) {
            //一旦视图可见，则播放碟片转动动画，拨杆落下，播放音乐，设置暂停按钮
            isFirstLoad = false;
            isPlay = true;
            mediaPlayer.start();
            ivNeedle.startAnimation(needleDownAnim);
            mDiskAnimator.play();
            mTimer.schedule(timerTask, 0, 1000);
        }
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.i(TAG, "onDestroy");
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
                        mediaPlayer.pause();
                        isPlay = false;
                        phoneCome = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //通话结束
                    if (phoneCome && mediaPlayer != null) {
                        mediaPlayer.start();
                        isPlay = true;
                        phoneCome = false;
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TryListenActivity.this, SplashActivity.class);
        startActivity(intent);
        TryListenActivity.this.finish();
    }
}