package info.doufm.android.utils;

import info.doufm.android.R;

/**
 * 常量帮助类
 * Created on 2014-12-07
 */
public class Constants {
    public static final int DISMISS = 1000;
    public static final int UPDATE_TIME = 2000;
    public static final int REQUEST_LOGIN_CODE = 300;
    public static final int REQUEST_USER_CODE = 400;
    public static final int REQUEST_WIFI_SETTING_CODE = 500;
    //action
    public static final String ACTION_CHOOSE_MUSIC = "info.doufm.android.action.CHOOSE_MUSIC";
    //extra
    public static final String EXTRA_THEME = "info.doufm.android.theme";
    public static final String EXTRA_MUSIC_ID = "info.doufm.android.music_id";
    public static final String EXTRA_LIST_TYPE = "info.doufm.android.list_type";
    public static final String BASE_URL = "http://doufm.info";
    public static final String PLAYLIST_URL = BASE_URL + "/api/playlist/";
    public static final String MUSIC_IN_PLAYLIST_URL = BASE_URL + "/api/playlist/";
    public static final String LOGIN_URL = BASE_URL + "/api/app_auth/";
    public static final String MUSIC_URL = BASE_URL + "/api/music/";
    public static final String CHANNEL_URL = BASE_URL + "/api/channel/";
    public static final String LOGOUT_URL = BASE_URL + "/api/user/logout/";
    public static final String USER_PROFILE_URL = BASE_URL + "/api/user/profile/";
    public static final String USER_MUSIC_URL = BASE_URL + "/api/user/music/";
    public static final String USER_HISTORY_URL = BASE_URL + "/api/user/history/";

    /*public static final String USER_URL = TEST_URL + "/api/user/";
    public static final String CURRENT_USER_URL = TEST_URL + "/api/user/current/";
    public static final String USER_FAVOR_URL = TEST_URL + "/api/user/current/favor";*/


    //Meterial Design主题(500 300 100)
    public static final String[] ACTIONBAR_COLORS = {
            "#ff5722", "#795548", "#ffc107",
            "#ff9800", "#259b24", "#8bc34a",
            "#cddc39", "#03a9f4", "#00bcd4",
            "#009688", "#673ab7", "#3f51b5",
            "#5677fc", "#e51c23", "#e91e63",
            "#9c27b0", "#607d8b"};

    public static final String[] BACKGROUND_COLORS = {
            "#ff8a65", "#a1887f", "#ffd54f",
            "#ffb74d", "#42bd41", "#aed581",
            "#dce775", "#4fc3f7", "#4dd0e1",
            "#4db6ac", "#9575cd", "#7986cb",
            "#91a7ff", "#f36c60", "#f06292",
            "#ba68c8", "#90a4ae"};

    public static final int[] SLIDE_MENU_HEADERS = {
            R.drawable.theme_01, R.drawable.theme_02, R.drawable.theme_03,
            R.drawable.theme_04, R.drawable.theme_05, R.drawable.theme_06,
            R.drawable.theme_07, R.drawable.theme_08, R.drawable.theme_09,
            R.drawable.theme_10, R.drawable.theme_11, R.drawable.theme_12,
            R.drawable.theme_13, R.drawable.theme_14, R.drawable.theme_15,
            R.drawable.theme_16, R.drawable.theme_00};
    //SharedPreferences
    public static final String USER_INFO = "userinfo";
    public static final String THEME = "theme";
    public static final String SAVE_USER_LOGIN_INFO_FLAG = "save_login_info_flag";
    public static final String LOGIN_USR_NAME = "rm_user_name";
    public static final String LOGIN_USR_PASSWORD = "rm_user_password";
    public static final String PLAYLIST = "playlist";
    public static final String COOKIE = "cookie";
    public static final byte HISTORY_TYPE = 1;
    public static final byte LOVE_TYPE = 2;
}
