package info.doufm.android.Play;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with Android Studio.
 * Date 2014-04-26r
 * 封装播放音乐的功能
 *
 * @author Qichao Chen
 * @version 1.0
 */
public class PlayMusic implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private static final String LOG_TAG = "PlayMusic";

    private MediaPlayer mediaPlay;          //媒体播放器
    private Timer mTimer = new Timer();     //计时器
    private boolean isEnd = false; //播放是否结束

    public PlayMusic() {

        try {
            mediaPlay = new MediaPlayer(); //创建媒体播放器
            mediaPlay.setAudioStreamType(AudioManager.STREAM_MUSIC); //设置媒体流类型
            mediaPlay.setOnBufferingUpdateListener(this);
            mediaPlay.setOnCompletionListener(this);
            mediaPlay.setOnPreparedListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTimer.schedule(timerTask,0,1000);
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (mediaPlay == null){
                return;
            }
            if (mediaPlay.isPlaying()){
                //处理播放
            }
        }
    };

    /**
     * 播放在线音乐
     * @param url
     */
    public void PlayOnline(String url){
        try{
            mediaPlay.reset();
            mediaPlay.setDataSource(url); //这种url路径
            mediaPlay.prepare(); //prepare自动播放
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放
     */
    public void play(){
        mediaPlay.start();
    }

    /**
     * 暂停
     */
    public void pause(){
        mediaPlay.pause();
    }

    /**
     * 停止
     */
    public void stop(){
        if (mediaPlay != null){
            mediaPlay.stop();
            mediaPlay.release();
            mediaPlay = null;
        }
    }

    /**
     * 缓冲更新
     * @param mediaPlayer
     * @param i
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        isEnd = true;
        mediaPlay.start();
        Log.i(LOG_TAG,"onCompletion");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Log.i(LOG_TAG,"onPrepared");
    }
}
