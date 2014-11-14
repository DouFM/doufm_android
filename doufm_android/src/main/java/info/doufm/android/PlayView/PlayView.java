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
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
    private Bitmap mCDBitmap;
    private String mBgColor = "#ff6e40";

    private int mWidth;
    private int mHeight;

    private int cycleTime = 8000;
    private Matrix mDiscMatrix;
    private Matrix mCDCoverMatrix;

    private int interval = 10;
    private float everyRotate = (360) / ((float) cycleTime / interval);

    private int mRotates;
    private boolean mRun = false;
    private boolean mIsPlay = false;

    private Thread mFrameThread;
    private ConditionVariable mVariable = new ConditionVariable();
    private int mdiscBmpWidth;
    private int mdiscBmpHeight;
    private boolean flag = true;

    public PlayView(Context context) {
        super(context);
        initBmp();
        //setBackgroundResource(R.drawable.ic_bg);
        getHolder().addCallback(this);

    }

    private void initBmp() {
        needleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.icn_play_needle);
        discBmp = BitmapFactory.decodeResource(getResources(), R.drawable.icn_play_disc);
        discBgBmp = BitmapFactory.decodeResource(getResources(), R.drawable.play_disc_bg);
        bgBmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bg);
        mdiscBmpWidth = discBmp.getWidth();
        mdiscBmpHeight = discBmp.getHeight();
        mCDBitmap = getCroppedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fm), Math.min((mdiscBmpWidth - 200) / 2, (mdiscBmpHeight - 200) / 2));
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
        if (mLcBmp != null) {
            mLcBmp.recycle();
        }
    }

    private void drawBmp(Canvas c, Bitmap b, int cx, int cy, Paint p) {
        c.drawBitmap(b, cx - b.getWidth() / 2, cy - b.getHeight() / 2, null);
    }

    private void doDraw(Canvas c) {

        // 去锯齿
        c.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        int cx = mWidth / 2;
        int cy = mHeight / 2;
        drawBmp(c, discBgBmp, cx, cy, null);
        if(mDiscMatrix == null){
            mDiscMatrix = new Matrix();
            mDiscMatrix.setTranslate(cx - discBmp.getWidth() / 2,cy - discBmp.getHeight() / 2);
        }

        if(mCDCoverMatrix == null){
            mCDCoverMatrix = new Matrix();
            mCDCoverMatrix.setTranslate (cx - mCDBitmap.getWidth() / 2,cy - mCDBitmap.getHeight() / 2);
        }

        if (mIsPlay) {
            if (mRotates >= 360) {
                mRotates = 0;
                mDiscMatrix.reset();
                mCDCoverMatrix.reset();
                mDiscMatrix.setTranslate(cx - discBmp.getWidth() / 2,cy - discBmp.getHeight() / 2);
                mCDCoverMatrix.setTranslate (cx - mCDBitmap.getWidth() / 2,cy - mCDBitmap.getHeight() / 2);
            }

            mDiscMatrix.postRotate(everyRotate, cx, cy);
            mCDCoverMatrix.postRotate(everyRotate, cx, cy);
            mRotates += everyRotate;
        }

        c.drawBitmap(mCDBitmap, mCDCoverMatrix, null);
        c.drawBitmap(discBmp, mDiscMatrix, null);

        int left = mWidth / 2 - needleBmp.getWidth();
        int top = 40;
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
            flag = true;
            mFrameThread = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (flag){
            mWidth = width;
            mHeight = height;
            flag = false;
        }

        if (bgBmp.getWidth() != mWidth || bgBmp.getHeight() != mHeight) {
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

    public void SetCDImage(Bitmap bitmap) {
        mCDBitmap = getCroppedBitmap(bitmap, Math.min((mdiscBmpWidth - 200) / 2, (mdiscBmpHeight - 200) / 2));
    }

    public void SetBgColor(String color){
        mBgColor = color;
    }

    @Override
    public void run() {
        mRun = true;
        Canvas c;
        while (mRun) {
            if (!mIsPlay) {
                pause();
            }
            c = getHolder().lockCanvas();
            if (c == null) {
                continue;
            }
            c.drawColor(Color.parseColor(mBgColor));
            doDraw(c);
            getHolder().unlockCanvasAndPost(c);
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap scaledSrcBmp;
        int diameter = radius * 2;
        if (bmp.getWidth() != diameter || bmp.getHeight() != diameter)
            scaledSrcBmp = Bitmap.createScaledBitmap(bmp, diameter, diameter, false);
        else
            scaledSrcBmp = bmp;
        Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(), scaledSrcBmp.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(), scaledSrcBmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(scaledSrcBmp.getWidth() / 2,
                scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);
        return output;
    }
}
