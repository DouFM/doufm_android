package info.doufm.android.Info;

/**
 * Created with Android Studio.
 * Date 2014-04-26
 * Playlist Information
 * @author Qichao Chen
 * @version 1.0
 */
public class PlaylistInfo {

    private String key;            //歌曲key
    private String name;           //播放列表名字
    private String music_list;     //分播放列表中歌曲数

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMusic_list() {
        return music_list;
    }

    public void setMusic_list(String music_list) {
        this.music_list = music_list;
    }
}
