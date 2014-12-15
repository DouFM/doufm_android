package info.doufm.android.user;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.doufm.android.R;

/**
 * 自定义登录界面类 以Dialog实现 仿SweetAlertDialog
 * Create on 2014-12-13
 */

public class LoginDialog extends Dialog implements View.OnClickListener {

    private View mDialogView;
    private TextView mTitleTextView;
    private TextView mContentTextView;
    //登录界面组件
    private LinearLayout mLoginLayout;
    private EditText mLoginNameView;
    private EditText mLoginPswView;
    //注册界面组件
    private LinearLayout mRegistLayout;
    private EditText mRegistNameView;
    private EditText mRegistPswView;
    private EditText mRegistPswConfirmView;
    //图标
    private ImageView mCustomImage;
    //确认和取消按钮
    private Button mConfirmButton;
    private Button mCancelButton;
    //用于保存各组件的内容
    private String mTitleText;
    private String mContentText;
    private String mCancelText;
    private String mConfirmText;
    private Drawable mCustomImgDrawable;
    private int mDialogType;//表示对话框的款式
    private OnLoginDialogClickListener mConfirmClickListener;
    private OnLoginDialogClickListener mCancelClickListener;
    //款式常量
    public final static int LOGIN_TYPE = 1;
    public final static int REGIST_TYPE = 2;

    public static interface OnLoginDialogClickListener {
        public void onClick(LoginDialog loginDialog);
    }

    public LoginDialog(Context context, int dialogType) {
        super(context, R.style.login_dialog);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mDialogType = dialogType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        //获取各组件
        mTitleTextView = (TextView) findViewById(R.id.login_title_text);
        mContentTextView = (TextView) findViewById(R.id.login_content_text);
        mLoginLayout = (LinearLayout) findViewById(R.id.login_layout);
        mLoginNameView = (EditText) mLoginLayout.findViewById(R.id.login_name);
        mLoginPswView = (EditText) mLoginLayout.findViewById(R.id.login_psw);
        mRegistLayout = (LinearLayout) findViewById(R.id.regist_layout);
        mRegistNameView = (EditText) mRegistLayout.findViewById(R.id.regist_name);
        mRegistPswView = (EditText) mRegistLayout.findViewById(R.id.regist_psw);
        mRegistPswConfirmView = (EditText) mRegistLayout.findViewById(R.id.regist_psw_confirm);
        mCustomImage = (ImageView) findViewById(R.id.custom_image);
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);

        setTitleText(mTitleText);
        setContentText(mContentText);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        changeDialogType(mDialogType, true);
    }

    public void changeDialogType(int dialogType) {
        changeDialogType(dialogType, false);
    }

    private void changeDialogType(int dialogType, boolean fromCreate) {
        mDialogType = dialogType;
        if (mDialogView != null) {
            if (!fromCreate) { //判断是否是初始创建，若不是将所有组件初始化
                restore();
            }
            switch (mDialogType) {
                case LOGIN_TYPE:
                    mLoginLayout.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.VISIBLE);
                    mCancelButton.setVisibility(View.VISIBLE);
                    setCustomImage(mCustomImgDrawable);
                    mCustomImage.setVisibility(View.VISIBLE);
                    mTitleTextView.setTextSize(14);
                    mTitleTextView.setTextColor(getContext().getResources().getColor(R.color.blue_btn_bg_color));

                    mTitleText = "用户登录";
                    mConfirmText = "登录";
                    mCancelText = "注册";

                    mTitleTextView.setText(mTitleText);
                    mConfirmButton.setText(mConfirmText);
                    mCancelButton.setText(mCancelText);
                    setCanceledOnTouchOutside(true);//设置点击对话框外的区域触发关闭对话框
                    break;
                case REGIST_TYPE:
                    mRegistLayout.setVisibility(View.VISIBLE);
                    //有一个图标
                    setCustomImage(mCustomImgDrawable);
                    mCustomImage.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.VISIBLE);
                    mCancelButton.setVisibility(View.VISIBLE);

                    mTitleTextView.setTextColor(getContext().getResources().getColor(R.color.blue_btn_bg_color));
                    mTitleTextView.setTextSize(14);

                    mTitleText = "用户注册";
                    mConfirmText = "注册";
                    mCancelText = "取消";

                    mTitleTextView.setText(mTitleText);
                    mConfirmButton.setText(mConfirmText);
                    mCancelButton.setText(mCancelText);

                    setCanceledOnTouchOutside(true);
                    break;
            }
        }
    }

    private void restore() {
        mCustomImage.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);
        mLoginLayout.setVisibility(View.GONE);
        mRegistLayout.setVisibility(View.GONE);
        mConfirmButton.setBackgroundResource(R.drawable.blue_button_background);
    }

    public LoginDialog setTitleText(String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            mTitleTextView.setText(mTitleText);
        }
        return this;
    }

    public LoginDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            mContentTextView.setVisibility(View.VISIBLE);
            mContentTextView.setText(mContentText);
        }
        return this;
    }


    public LoginDialog setCustomImage(int resourceId) {
        return setCustomImage(getContext().getResources().getDrawable(resourceId));
    }

    public LoginDialog setCustomImage(Drawable drawable) {
        mCustomImgDrawable = drawable;
        if (mCustomImage != null && mCustomImgDrawable != null) {
            mCustomImage.setVisibility(View.VISIBLE);
            mCustomImage.setImageDrawable(mCustomImgDrawable);
        }
        return this;
    }


    public LoginDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            mCancelButton.setVisibility(View.VISIBLE);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    public LoginDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    //获取按钮文本信息
    public String getCancelText() {
        return mCancelText;
    }

    public String getConfirmText() {
        return mConfirmText;
    }

    //获得登录相关的editText组件
    public EditText getLoginNameView() {
        return mLoginNameView;
    }

    public EditText getLoginPswView() {
        return mLoginPswView;
    }

    //获得注册相关的editText组件
    public EditText getRegistNameView() {
        return mRegistNameView;
    }

    public EditText getRegistPswView() {
        return mRegistPswView;
    }

    public EditText getRegistPswConfirmView() {
        return mRegistPswConfirmView;
    }

    public LoginDialog setCancelClickListener(OnLoginDialogClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    public LoginDialog setConfirmClickListener(OnLoginDialogClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    @Override
    public void onClick(View v) { //当确认按钮和取消按钮不设置监听器时 默认触发关闭对话框
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null) {
                mCancelClickListener.onClick(LoginDialog.this);
            } else {
                dismiss();
            }
        } else if (v.getId() == R.id.confirm_button) {
            if (mConfirmClickListener != null) {
                mConfirmClickListener.onClick(LoginDialog.this);
            } else {
                dismiss();
            }
        }
    }
}
