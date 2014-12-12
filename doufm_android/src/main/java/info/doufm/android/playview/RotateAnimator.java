package info.doufm.android.playview;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.view.animation.LinearInterpolator;

import info.doufm.android.R;

/**
 * Created by Acker on 2014/12/12.
 */
public class RotateAnimator {
    private ObjectAnimator anim;
    public boolean notFirstFlag;
    private Context context;

    public RotateAnimator(Context context, Object object) {
        this.context = context;
        anim = ObjectAnimator.ofFloat(object, "rotation", 0, 359);
        anim.setDuration(40000);
        anim.setRepeatCount(-1);
        anim.setInterpolator(new LinearInterpolator());
    }

    public RotateAnimator(Context context, Object object, long duration, int repeatCount, TimeInterpolator timeInterpolator, float fromDegree, float toDegree) {
        this.context = context;
        anim = ObjectAnimator.ofFloat(object, "rotation", fromDegree, toDegree);
        anim.setDuration(duration);
        anim.setRepeatCount(repeatCount);
        anim.setInterpolator(timeInterpolator);
    }

    public void pause() {
        if (Build.VERSION.SDK_INT >= 19) {
            anim.pause();
        } else {
            anim.cancel();
            float curDegree = Float.parseFloat(anim.getAnimatedValue("rotation").toString()) % 360f;
            anim.setFloatValues(curDegree, 359 + curDegree);
        }
    }

    public void play() {
        if (!notFirstFlag) {
            anim.start();
            notFirstFlag = true;
        } else {
            if (Build.VERSION.SDK_INT >= 19) {
                anim.resume();
            } else {
                anim.start();
            }
        }
    }

    public void stop() {
        anim.end();
    }

    public Bitmap getCroppedBitmap(Bitmap bmp) {
        Bitmap discBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icn_play_disc);
        int radius = Math.min((discBmp.getWidth() - 200) / 2, (discBmp.getHeight() - 200) / 2);
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
