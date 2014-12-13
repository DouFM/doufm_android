package info.doufm.android.user;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.doufm.android.R;

/**
 * 自定义loginDialog 仿SweetAlertDialog
 * Create on 2014-12-13
 */

public class LoginDialog extends Dialog implements View.OnClickListener{

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

    private Drawable mCustomImgDrawable;
    private ImageView mCustomImage;
    private Button mConfirmButton;
    private Button mCancelButton;

    private String mTitleText;
    private String mContentText;
    private String mCancelText;
    private String mConfirmText;
    private int mDialogType;

    private OnLoginDialogClickListener mConfirmClickListener;
    private OnLoginDialogClickListener mCancelClickListener;

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
        mTitleTextView = (TextView)findViewById(R.id.login_title_text);
        mContentTextView = (TextView)findViewById(R.id.login_content_text);
        //获取登录界面组件
        mLoginLayout = (LinearLayout) findViewById(R.id.login_layout);
        mLoginNameView = (EditText) mLoginLayout.findViewById(R.id.login_name);
        mLoginPswView = (EditText) mLoginLayout.findViewById(R.id.login_psw);
        //获取注册界面组件
        mRegistLayout = (LinearLayout) findViewById(R.id.regist_layout);
        mRegistNameView = (EditText) mRegistLayout.findViewById(R.id.regist_name);
        mRegistPswView = (EditText) mRegistLayout.findViewById(R.id.regist_psw);
        mRegistPswConfirmView = (EditText) mRegistLayout.findViewById(R.id.regist_psw_confirm);
        mCustomImage = (ImageView) findViewById(R.id.custom_image);
        setTitleText(mTitleText);
        setContentText(mContentText);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);

        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        changeDialogType(mDialogType, true);
    }

    public void changeDialogType(int dialogType){
        changeDialogType(dialogType,false);
    }
    private void changeDialogType(int dialogType, boolean fromCreate) {
        mDialogType = dialogType;
        // call after created views
        if (mDialogView != null) {
            if (!fromCreate) {
                // restore all of views state before switching alert type
                restore();
            }
            switch (mDialogType) {
                //加入登录框类型
                case LOGIN_TYPE:
                    mLoginLayout.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.VISIBLE);
                    mCancelButton.setVisibility(View.VISIBLE);
                    //有一个图标
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
                    setCanceledOnTouchOutside(true);
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

    public String getCancelText(){
        return mCancelText;
    }

    public String getConfirmText() {
        return mConfirmText;
    }

    public EditText getLoginNameView() {
        return mLoginNameView;
    }

    public EditText getLoginPswView() {
        return mLoginPswView;
    }

    //获得注册相关的editText组件
    public EditText getRegistNameView(){
        return mRegistNameView;
    }

    public EditText getRegistPswView(){
        return mRegistPswView;
    }

    public EditText getRegistPswConfirmView(){
        return mRegistPswConfirmView;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public LoginDialog setCancelClickListener(OnLoginDialogClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    public LoginDialog setConfirmClickListener(OnLoginDialogClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {
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
