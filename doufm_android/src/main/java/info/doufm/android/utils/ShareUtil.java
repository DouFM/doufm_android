package info.doufm.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Acker on 2014/12/17.
 */
public class ShareUtil {

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public ShareUtil(Context context) {
        mPreferences = context.getSharedPreferences(Constants.userInfo, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public void setTheme(int theme) {
        mEditor.putInt(Constants.theme, theme);
    }

    public int getTheme() {
        return mPreferences.getInt(Constants.theme, 13);
    }

    public void setPlayList(int listNo) {
        mEditor.putInt(Constants.playList, listNo);
    }

    public int getPlayList() {
        return mPreferences.getInt(Constants.playList, 0);
    }

    public void commit() {
        mEditor.commit();
    }
}
