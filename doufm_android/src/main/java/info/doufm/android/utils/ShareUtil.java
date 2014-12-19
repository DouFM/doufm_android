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
        mPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public int getTheme() {
        return mPreferences.getInt(Constants.THEME, 13);
    }

    public void setTheme(int theme) {
        mEditor.putInt(Constants.THEME, theme);
    }

    public int getPlayList() {
        return mPreferences.getInt(Constants.PLAYLIST, 0);
    }

    public void setPlayList(int listNo) {
        mEditor.putInt(Constants.PLAYLIST, listNo);
    }

    public void apply() {
        mEditor.apply();
    }
}
