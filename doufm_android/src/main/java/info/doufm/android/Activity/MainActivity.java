package info.doufm.android.Activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import info.doufm.android.Play.PlayMusic;
import info.doufm.android.R;
import info.doufm.android.ResideMenu.ResideMenu;
import info.doufm.android.ResideMenu.ResideMenuItem;

/**
 * Created with Android Studio.
 * Date 2014-04-26
 * <p/>
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

    //播放和下一首按钮
    private Button btnPlayMusic, btnNextSong;

    //Test Music URL
    private String URL = "http://abv.cn/music/%E5%85%89%E8%BE%89%E5%B2%81%E6%9C%88.mp3";
    private MediaPlayer mediaPlayer;


    private PlayMusic player; //播放器
    private String musicURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        InitResideMenu();
        initPlayer();
    }


    private void initView() {
        btnPlayMusic = (Button) findViewById(R.id.btnPlayMusic);
        btnNextSong = (Button) findViewById(R.id.btnNextSong);
        btnPlayMusic.setOnClickListener(this);
        btnNextSong.setOnClickListener(this);
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
            mLeftResideMenuItemList.add(new ResideMenuItem(this, mLeftResideMenuItemIconList.get(i), mLeftResideMenuItemTitleList.get(i)));
        }

        //禁用右侧ResideMenu
        mResideMenu.setDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        mResideMenu.setMenuItems(mLeftResideMenuItemList, ResideMenu.DIRECTION_LEFT);
        findViewById(R.id.btn_open_left_reside_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });

    }

    private void initPlayer() {
        player = new PlayMusic();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnPlayMusic:
                try {
                    musicURL = URLEncoder.encode(URL, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                player.PlayOnline("http://abv.cn/music/%E5%85%89%E8%BE%89%E5%B2%81%E6%9C%88.mp3");
                break;
            case R.id.btnNextSong:
                player.pause();
                break;
        }

    }

    private class ResideMenuListener implements ResideMenu.OnMenuListener {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player = null;
        }
    }

    public ResideMenu getResideMenu() {
        return mResideMenu;
    }
}
