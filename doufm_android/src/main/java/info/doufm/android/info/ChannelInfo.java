package info.doufm.android.info;

/**
 * Created with Android Studio.
 * Date 2014-04-26
 * Channel Information
 *
 * @author Qichao Chen
 * @version 1.0
 */
public class ChannelInfo {
    private String key;                 //分类key
    private String name;                //分类名
    private String music_list;          //分类中歌曲数
    private String update_num;          //本分类每日更新数量
    private String playable;            //是否显示在播放列表中
    private String upload_date;         //分类创建时间时间

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

    public String getUpdate_num() {
        return update_num;
    }

    public void setUpdate_num(String update_num) {
        this.update_num = update_num;
    }

    public String getPlayable() {
        return playable;
    }

    public void setPlayable(String playable) {
        this.playable = playable;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }
}
