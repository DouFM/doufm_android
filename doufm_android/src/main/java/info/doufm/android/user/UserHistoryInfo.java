package info.doufm.android.user;


import java.io.Serializable;

import io.realm.RealmObject;

/**
 * 历史操作信息类
 * Create on 2014-12-13
 */

public class UserHistoryInfo extends RealmObject implements Serializable {

    private int history_id;         //记录编号
    private String userID;          //用户id
    private String key;             //音乐ID
    private String title;           //音乐标题
    private String musicURL;        //音乐路径
    private String cover;           //封面地址
    private String singer;          //歌手


    public int getHistory_id() {
        return history_id;
    }

    public void setHistory_id(int history_id) {
        this.history_id = history_id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getMusicURL() {
        return musicURL;
    }

    public void setMusicURL(String musicURL) {
        this.musicURL = musicURL;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


}
