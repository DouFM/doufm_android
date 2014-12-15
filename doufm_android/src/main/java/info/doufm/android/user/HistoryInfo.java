package info.doufm.android.user;


/**
 * 历史操作信息类
 * Create on 2014-12-13
 */

public class HistoryInfo {

    private String date;   //操作时间
    private String op;   //操作类型
    private String key;  //音乐key
    private String title;//音乐标题
    private String cover;//封面地址

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
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
}
