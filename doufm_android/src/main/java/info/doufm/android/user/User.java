package info.doufm.android.user;

import android.content.Context;

/**
 * 用户类
 */
public class User {

    private static Context context;
    private static User mUser;

    private String userName;
    private String userPassword;
    private String userID;
    private boolean isLogin;

    public static final String TAG_SP_USER = "user";
    public static final String TAG_SP_USER_NAME = "username";
    public static final String TAG_SP_USER_PASSWORD = "password";
    public static final String TAG_SP_USER_ID = "user_id";
    public static final String TAG_SP_USER_LOGIN = "user_login";

    private User() {
        isLogin = context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).getBoolean(TAG_SP_USER_LOGIN, false);
        updateStatus(isLogin);
    }

    public static void init(Context mContext) {
        context = mContext;
    }

    public static User getInstance() {
        if (mUser == null) {
            mUser = new User();
        }
        return mUser;
    }

    public String getUserName() {
        if (!getLogin()) {
            return "";
        }
        return context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).getString(TAG_SP_USER_NAME, "");
    }


    public void setUserName(String userName) {
        this.userName = userName;
        context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).edit().putString(TAG_SP_USER_NAME, userName).commit();
    }

    public String getUserPassword() {
        if (!getLogin()) {
            return "";
        }
        return context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).getString(TAG_SP_USER_PASSWORD, "");
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
        context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).edit().putString(TAG_SP_USER_PASSWORD, userPassword).commit();
    }

    public String getUserID() {
        if (!getLogin()) {
            return "";
        }
        return context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).getString(TAG_SP_USER_ID, "");
    }

    public void setUserID(String userID) {
        this.userID = userID;
        context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).edit().putString(TAG_SP_USER_ID, userID).commit();
    }

    public boolean getLogin() {
        return context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).getBoolean(TAG_SP_USER_LOGIN, false);
    }

    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
        context.getSharedPreferences(TAG_SP_USER, Context.MODE_PRIVATE).edit().putBoolean(TAG_SP_USER_LOGIN, isLogin).commit();
    }

    private void updateStatus(boolean isLogin) {
        setLogin(isLogin);
        if (isLogin) {
            userName = getUserName();
            userPassword = getUserPassword();
            userID = getUserID();
        }
    }
}
