package info.doufm.android.utils;

/**
 * 常量帮助类
 * Created on 2014-12-07
 */
public class Constants {
    public static final int DISMISS = 1000;
    public static final int UPDATE_TIME = 2000;
    public static final int REQUEST_LOGIN_CODE = 300;
    public static final int REQUEST_USER_CODE = 400;

    //extra
    public static final String EXTRA_THEME = "info.doufm.android.Theme";
    //API
    public static final String BASE_URL = "http://doufm.info";
    public static final String PLAYLIST_URL = BASE_URL + "/api/playlist/?start=0";
    public static final String MUSIC_IN_PLAYLIST_URL = BASE_URL + "/api/playlist/";
    public static final String TEST_URL = "http://115.29.140.122:5001";
    public static final String LOGIN_URL = TEST_URL + "/api/app_auth/";
    public static final String USER_URL = TEST_URL + "/api/user/";
    public static final String CURRENT_USER_URL = TEST_URL + "/api/user/current/";
    public static final String USER_HISTORY_URL = TEST_URL + "/api/user/current/history/";

    public static final String USER_FAVOR_URL = TEST_URL + "/api/user/current/favor";
    //Meterial Design主题(500 300 100)
    public static final String[] ACTIONBAR_COLORS = {"#607d8b", "#ff5722", "#795548",
            "#ffc107", "#ff9800", "#259b24",
            "#8bc34a", "#cddc39", "#03a9f4",
            "#00bcd4", "#009688", "#673ab7",
            "#673ab7", "#3f51b5", "#5677fc",
            "#e51c23", "#e91e63", "#9c27b0",
            "#607d8b"};

    public static final String[] BACKGROUND_COLORS = {"#90a4ae", "#ff8a65", "#a1887f",
            "#ffd54f", "#ffb74d", "#42bd41",
            "#aed581", "#dce775", "#4fc3f7",
            "#4dd0e1", "#4db6ac", "#9575cd",
            "#9575cd", "#7986cb", "#91a7ff",
            "#f36c60", "#f06292", "#ba68c8",
            "#90a4ae"};
    //SharedPreferences
    public static final String USER_INFO = "userinfo";
    public static final String THEME = "theme";

    public static final String PLAYLIST = "playlist";
}
