package info.doufm.android.info;

/**
 * Created by lsc on 2014/12/27.
 */

public class UserHistoryInfoFromAPI {
    private String data;//日期
    private String op;//操作
    private String key;//歌曲ID
    private String title;//歌曲名
    private String cover;//封面key
    private String audio;//歌曲key

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
