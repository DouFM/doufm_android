package info.doufm.android.PlayView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Created by Tang on 11/13 0013.
 */
public class MySeekBar extends SeekBar {
    public MySeekBar(Context context) {
        super(context);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        //原来是要将TouchEvent传递下去的,我们不让它传递下去就行了
        //return super.onTouchEvent(event);

        return false ;
    }
}
