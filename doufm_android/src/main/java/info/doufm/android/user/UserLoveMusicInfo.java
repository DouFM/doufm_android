package info.doufm.android.user;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created with Android Studio.
 * Time: 04:18
 * Info:
 */
public class UserLoveMusicInfo extends RealmObject implements Serializable {

    private int love_id;         //记录编号
    private String userID;          //用户id
    private String key;             //音乐ID
    private String title;           //音乐标题
    private String musicURL;        //音乐路径
    private String cover;           //封面地址
    private String singer;          //歌手

    public int getLove_id() {
        return love_id;
    }

    public void setLove_id(int love_id) {
        this.love_id = love_id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMusicURL() {
        return musicURL;
    }

    public void setMusicURL(String musicURL) {
        this.musicURL = musicURL;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
