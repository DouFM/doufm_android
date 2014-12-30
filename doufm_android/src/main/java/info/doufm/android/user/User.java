package info.doufm.android.user;

import android.content.Context;

import info.doufm.android.utils.CyptoUtils;
import info.doufm.android.utils.SharedPreferencesUtils;

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

    public static final String TAG_SP_USER_NAME = "username";
    public static final String TAG_SP_USER_PASSWORD = "password";
    public static final String TAG_SP_USER_ID = "user_id";
    public static final String TAG_SP_USER_LOGIN = "user_login";

    private User() {
        isLogin = SharedPreferencesUtils.getBoolean(context, TAG_SP_USER_LOGIN, false);
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
        return SharedPreferencesUtils.getString(context, TAG_SP_USER_NAME, "");
    }


    public void setUserName(String userName) {
        this.userName = userName;
        SharedPreferencesUtils.putString(context, TAG_SP_USER_NAME, userName);
    }

    public String getUserPassword() {
        if (!getLogin()) {
            return "";
        }
        return CyptoUtils.decode(CyptoUtils.KEY, SharedPreferencesUtils.getString(context, TAG_SP_USER_PASSWORD, ""));
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
        SharedPreferencesUtils.putString(context, TAG_SP_USER_PASSWORD, CyptoUtils.encode(CyptoUtils.KEY, userPassword));
    }

    public String getUserID() {
        if (!getLogin()) {
            return "";
        }
        return SharedPreferencesUtils.getString(context, TAG_SP_USER_ID, "");
    }

    public void setUserID(String userID) {
        this.userID = userID;
        SharedPreferencesUtils.putString(context, TAG_SP_USER_ID, userID);
    }

    public boolean getLogin() {
        return SharedPreferencesUtils.getBoolean(context, TAG_SP_USER_LOGIN, false);
    }

    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
        SharedPreferencesUtils.putBoolean(context, TAG_SP_USER_LOGIN, isLogin);
    }

    private void updateStatus(boolean isLogin) {
        setLogin(isLogin);
        if (isLogin) {
            userName = getUserName();
            userPassword = getUserPassword();
            userID = getUserID();
        }
    }

    public void Quit() {
        setUserName("");
        setLogin(false);
        setUserID("");
        setUserPassword("");
    }
}
