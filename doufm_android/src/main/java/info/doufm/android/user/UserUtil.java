package info.doufm.android.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 用户帮助类
 * Created on 2014-12-07
 */
public class UserUtil {

    /**
     * 生成全小写的MD5值
     *
     * @param password
     * @return
     */
    public static String toLowerCaseMD5(String password) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(" UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString().toLowerCase();
    }

    /**
     * 使用AppCompat-V7生成圆形图片
     *
     * @param context
     * @param mImageID
     * @return
     */
    public static RoundedBitmapDrawable getCircleImage(Context context, int mImageID) {
        Bitmap src = BitmapFactory.decodeResource(context.getResources(), mImageID);
        Bitmap dst;
        if (src.getWidth() >= src.getHeight()) {
            dst = Bitmap.createBitmap(src, src.getWidth() / 2 - src.getHeight() / 2, 0, src.getHeight(), src.getHeight());
        } else {
            dst = Bitmap.createBitmap(src, src.getHeight() / 2 - src.getWidth() / 2, 0, src.getWidth(), src.getWidth());
        }
        RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(context.getResources(), dst);
        rbd.setCornerRadius(dst.getHeight() / 2);
        rbd.setAntiAlias(true);
        return rbd;
    }
}
