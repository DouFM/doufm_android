package info.doufm.android.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.umeng.analytics.MobclickAgent;

import info.doufm.android.R;
import info.doufm.android.playview.PlayView;

/**
 * 网易云音乐播放器播放界面UI及简单媒体播放
 *
 * @author Lqh
 */
public class TryListenActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private FrameLayout mContainer;
    private PlayView mPlayView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_listen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContainer = (FrameLayout) findViewById(R.id.try_media_player);
        mPlayView = new PlayView(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mContainer.addView(mPlayView, lp);
        mediaPlayer = MediaPlayer.create(this, R.raw.home);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        mPlayView.play();
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