package info.doufm.android.Info;

/**
 * Created with Android Studio.
 * Date 2014-04-26r
 * Music Information
 * @author Qichao Chen
 * @version 1.0
 */
public class MusicInfo {
    private String key;             //歌曲key
    private String title;           //歌曲名
    private String artist;          //艺术家
    private String album;           //专辑
    private String company;         //唱片公司
    private String public_time;     //出版年份
    private String kbps;            //码率
    private String cover;           //专辑封面URL
    private String audio;           //音频URL
    private String upload_date;     //上传时间

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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPublic_time() {
        return public_time;
    }

    public void setPublic_time(String public_time) {
        this.public_time = public_time;
    }

    public String getKbps() {
        return kbps;
    }

    public void setKbps(String kbps) {
        this.kbps = kbps;
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

    public String getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }
}
