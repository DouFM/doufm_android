package info.doufm.android.utils;

import java.text.SimpleDateFormat;

/**
 * Created by Acker on 2014/12/10.
 */
public class TimeFormat {

    public static String msToMinAndS(int ms) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(ms);
    }
}
