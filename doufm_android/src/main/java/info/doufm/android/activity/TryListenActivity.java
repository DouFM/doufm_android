package info.doufm.android.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;

import info.doufm.android.R;
import info.doufm.android.playview.RotateAnimator;

/**
 * 网易云音乐播放器播放界面UI及简单媒体播放
 *
 * @author Lqh
 */
public class TryListenActivity extends ActionBarActivity implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private Animation needleUpAnim;
    private Animation needleDownAnim;
    private Animation needleAnim;
    private Button btnPlay;
    private boolean isPlay;
    private RotateAnimator mDiskAnimator;
    private ImageView ivDisk;
    private ImageView ivNeedle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_listen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ivDisk = (ImageView) findViewById(R.id.iv_disk);
        ivNeedle = (ImageView) findViewById(R.id.iv_needle);
        needleUpAnim = AnimationUtils.loadAnimation(this, R.anim.rotation_up);
        needleDownAnim = AnimationUtils.loadAnimation(this, R.anim.rotation_down);
        needleAnim = AnimationUtils.loadAnimation(this, R.anim.rotation_up_down);
        mDiskAnimator = new RotateAnimator(this, ivDisk);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        mToolbar.setTitle("DouFM");
        mToolbar.setSubtitle("试听中...");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mediaPlayer = MediaPlayer.create(this, R.raw.home);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        mDiskAnimator.play();
        btnPlay = (Button) findViewById(R.id.btn_start_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlay) {
                    ivNeedle.startAnimation(needleUpAnim);
                    mDiskAnimator.pause();
                    mediaPlayer.pause();
                    isPlay = false;
                } else {
                    ivNeedle.startAnimation(needleDownAnim);
                    mDiskAnimator.play();
                    mediaPlayer.start();
                    isPlay = true;
                }
            }
        });
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}