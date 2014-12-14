package info.doufm.android.user;

import java.util.Date;
import java.util.List;

import info.doufm.android.info.MusicInfo;

/**
 * 用于存储用户信息
 * Create on 2014-12-10
 */

/*
    客户端调用服务端的API完成操作
    用户只要管理个人信息和喜欢的歌曲就可以，不喜欢的歌曲由服务端筛掉
    故只需包含以下信息即可
 */
public class User {

    private String level;       //用户权限 disable, normal,admin
    private String key;         //用户key
    private String name;        //用户名
    private String password;   //用户密码
    private String regist_date; //注册时间
    private int listened;     //听过的歌曲数
    private int favor;        //喜欢的歌曲数
    private int dislike;      //不喜欢的歌曲数
    private int skipped;      //跳过的歌曲数
    private List<HistoryInfo> historyList;  //操作信息列表
    private List<MusicInfo> favorList;   //喜欢歌曲列表


    public String getLevel() {
        return level;
    }

    //不需要此API
    public void setLevel(String level) {
        this.level = level;
    }

    public String getKey() {
        return key;
    }

    //不需要此API
    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    //对密码和用户名的合法判断应放在java bean外
    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegist_date() {
        return regist_date;
    }

    public void setRegist_date(String regist_date) {
        this.regist_date = regist_date;
    }

    public int getListened() {
        return listened;
    }

    public void setListened(int listened) {
        this.listened = listened;
    }

    public int getFavor() {
        return favor;
    }

    public void setFavor(int favor) {
        this.favor = favor;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public List<HistoryInfo> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<HistoryInfo> historyList) {
        this.historyList = historyList;
    }

    public List<MusicInfo> getFavorList() {
        return favorList;
    }

    public void setFavorList(List<MusicInfo> favorList) {
        this.favorList = favorList;
    }
}
