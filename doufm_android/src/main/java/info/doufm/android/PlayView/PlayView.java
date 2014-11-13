package info.doufm.android.PlayView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.os.ConditionVariable;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import info.doufm.android.R;

/**
 * Created by Tang on 11/13 0013.
 */
public class PlayView extends SurfaceView implements Callback, Runnable {

    private Bitmap needleBmp;
    private Bitmap discBmp;
    private Bitmap discBgBmp;
    private Bitmap bgBmp;
    private Bitmap mLcBmp;

    private int mWidth;
    private int mHeight;

    private int cycleTime = 8000;
    private Matrix mDiscMatrix;
    private Matrix mLcMatrix;

    private int interval = 10;
    private float everyRotate = (360) / ((float) cycleTime / interval);

    private int mRotates;
    private boolean mRun = false;
    private boolean mIsPlay = false;

    private Thread mFrameThread;
    private ConditionVariable mVariable = new ConditionVariable();

    public PlayView(Context context) {
        super(context);
        initBmp();
        //setBackgroundResource(R.drawable.ic_bg);
        getHolder().addCallback(this);

    }

    private void initBmp() {
        needleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.icn_play_needle);
        discBmp = BitmapFactory.decodeResource(getResources(),R.drawable.icn_play_disc);
        discBgBmp = BitmapFactory.decodeResource(getResources(),R.drawable.play_disc_bg);
        bgBmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bg);
    }

    private void releaseBmp() {
        if (needleBmp != null) {
            needleBmp.recycle();
        }
        if (discBmp != null) {
            discBmp.recycle();
        }
        if (discBgBmp != null) {
            discBgBmp.recycle();
        }
        if(mLcBmp != null){
            mLcBmp.recycle();
        }
    }

    private void drawBmp(Canvas c, Bitmap b, int cx, int cy, Paint p) {
        c.drawBitmap(b, cx - b.getWidth() / 2, cy - b.getHeight() / 2, null);
    }

    private void doDraw(Canvas c) {

        // 去锯齿
        c.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        int cx = mWidth / 2;
        int cy = mHeight / 2;
        drawBmp(c, discBgBmp, cx, cy, null);

        if(mDiscMatrix == null){
            mDiscMatrix = new Matrix();
            mDiscMatrix.setTranslate(mWidth / 2 - discBmp.getWidth() / 2f,
                    mHeight / 2 - discBmp.getHeight() / 2f);
        }

        if(mLcMatrix == null){
            mLcMatrix = new Matrix();
            mLcMatrix.setTranslate(mWidth / 2 - (discBmp.getWidth() - 60) / 2f,
                    mHeight / 2 - (discBmp.getHeight() - 60) / 2f);
        }

        if (mIsPlay) {
            if (mRotates >= 360) {
                mRotates = 0;
                mDiscMatrix.reset();
                mLcMatrix.reset();
                mDiscMatrix.setTranslate(mWidth / 2 - discBmp.getWidth() / 2f,
                        mHeight / 2 - discBmp.getHeight() / 2f);
                mLcMatrix.setTranslate(mWidth / 2 - (discBmp.getWidth() - 60) / 2f,
                        mHeight / 2 - (discBmp.getHeight() - 60) / 2f);
            }
            mDiscMatrix.postRotate(everyRotate, cx, cy);
            mLcMatrix.postRotate(everyRotate, cx, cy);
            mRotates += everyRotate;
        }

        if(mLcBmp == null){
            int w = discBmp.getWidth() - 60;
            int h = discBmp.getHeight() - 60;
            mLcBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
            Canvas c2 = new Canvas(mLcBmp);
            Paint p = new Paint();

            c2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            p.setColor(Color.LTGRAY);
            p.setStyle(Paint.Style.FILL);
            c2.drawCircle(w / 2, h / 2, Math.min(w, h) / 2, p);

            p.setColor(Color.DKGRAY);
            p.setStrokeWidth(10f);
            c2.drawLine(0, h / 2, w, h / 2, p);
            c2.drawLine(w / 2, 0, w / 2, h, p);

        }

        c.drawBitmap(mLcBmp, mLcMatrix, null);
        c.drawBitmap(discBmp, mDiscMatrix, null);

        int left = mWidth / 2 - needleBmp.getWidth();
        int top = 30;
        c.drawBitmap(needleBmp, left, top, null);

    }

    public void play() {
        mIsPlay = true;
        mVariable.open();
    }

    public void pause() {
        mIsPlay = false;
    }

    private void stop() {
        if (mFrameThread != null) {
            mRun = false;
            mIsPlay = false;
            mFrameThread = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;

        if(bgBmp.getWidth() != mWidth || bgBmp.getHeight() != mHeight){
            bgBmp = ThumbnailUtils.extractThumbnail(bgBmp, width, height);
        }

        if (mFrameThread == null) {
            mFrameThread = new Thread(this);
            mFrameThread.start();
        }
        mVariable.close();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    @Override
    public void run() {
        mRun = true;
        Canvas c;
        while (mRun) {
            if(!mIsPlay){
                pause();
            }
            c = getHolder().lockCanvas();
            if (c == null) {
                continue;
            }
            c.drawColor(Color.parseColor("#ff6e40"));
            doDraw(c);
            getHolder().unlockCanvasAndPost(c);
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
