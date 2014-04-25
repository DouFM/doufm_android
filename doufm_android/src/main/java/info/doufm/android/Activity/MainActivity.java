package info.doufm.android.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import info.doufm.android.R;
import info.doufm.android.ResideMenu.ResideMenu;
import info.doufm.android.ResideMenu.ResideMenuItem;

/**
 * Created with Android Studio.
 * Date 2014-04-26
 *
 * Reside Menu的初始化和设置
 *
 * @author Qichao Chen
 * @version 1.0
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private MainActivity mContext;
    private ResideMenu mResideMenu;
    private ResideMenuListener mReisdeMenulistener;
    //左侧菜单项
    private List<ResideMenuItem> mLeftResideMenuItemList;
    private List<String> mLeftResideMenuItemTitleList;
    private List<Integer> mLeftResideMenuItemIconList;

    //右侧菜单项
    private List<ResideMenuItem> mRightResideMenuItemList;
    private List<String> mRightResideMenuItemTitleList;
    private List<Integer> mRightResideMenuItemIconList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        InitResideMenu();
    }

    private void InitResideMenu() {
        //初始化Reside Menu风格
        mResideMenu = new ResideMenu(this);
        mResideMenu.setBackground(R.drawable.reside_menu_background);
        mResideMenu.attachToActivity(this);
        mReisdeMenulistener = new ResideMenuListener();
        mResideMenu.setMenuListener(mReisdeMenulistener);

        //初始化左侧ResideMenu Item
        mLeftResideMenuItemList = new ArrayList<ResideMenuItem>();
        mLeftResideMenuItemTitleList = new ArrayList<String>();
        mLeftResideMenuItemIconList = new ArrayList<Integer>();

        mLeftResideMenuItemTitleList.add("测试");
        mLeftResideMenuItemIconList.add(new Integer(R.drawable.rm_icon_home));
        for (int i = 0; i < mLeftResideMenuItemTitleList.size(); i++) {
            mLeftResideMenuItemList.add(new ResideMenuItem(this,mLeftResideMenuItemIconList.get(i), mLeftResideMenuItemTitleList.get(i)));
        }

        //禁用右侧ResideMenu
        mResideMenu.setDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        mResideMenu.setMenuItems(mLeftResideMenuItemList,ResideMenu.DIRECTION_LEFT);
        findViewById(R.id.btn_open_left_reside_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });

    }

    @Override
    public void onClick(View view) {

    }

    private class ResideMenuListener implements ResideMenu.OnMenuListener{

        @Override
        public void openMenu() {
            //打开Reside Menu
        }

        @Override
        public void closeMenu() {
            //关闭Reside Menu
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev) || mResideMenu.onInterceptTouchEvent(ev);
    }

    public ResideMenu getResideMenu(){
        return mResideMenu;
    }
}
