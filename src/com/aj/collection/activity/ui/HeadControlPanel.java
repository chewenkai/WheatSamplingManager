package com.aj.collection.activity.ui;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aj.Constant;
import com.aj.collection.R;

public class HeadControlPanel extends RelativeLayout
{
	private Context mContext;  
    private TextView mMidleTitle;  
//    private ImageView mRightFirstImage;
    private ImageView mLeftImage;
    public ImageText mRightThirdImage;
    public ImageText mRightSecondImage;
    public ImageText mRightFirstImage;

    private LeftImageOnClick mLeftImageOnClick = null;
    private RightThirdOnClick mRightThirdImageOnClick=null;
    private RightSecondOnClick mRightSecondImageOnClick = null;
    private rightFirstImageOnClick mRightFirstImageOnClick = null;

    private static final float middle_title_size = 22
            ;
    private static final float right_title_size = 30f;   
    private static final int default_background_color = Color.rgb(30, 30, 30);
      
    public HeadControlPanel(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub  
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // TODO Auto-generated method stub
        mLeftImage = (ImageView) findViewById(R.id.left_img);
        mMidleTitle = (TextView)findViewById(R.id.midle_title);
        mRightThirdImage = (ImageText)findViewById(R.id.right_third);
        mRightSecondImage = (ImageText)findViewById(R.id.right_second);
        mRightFirstImage = (ImageText)findViewById(R.id.right_first);

        mRightThirdImage.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                mRightThirdImageOnClick.onImageClickListener();
            }
        });
        mRightSecondImage.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                mRightSecondImageOnClick.onImageClickListener();
            }
        });
        mLeftImage.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                mLeftImageOnClick.onImageClickListener();
            }
        });
        mRightFirstImage.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                mRightFirstImageOnClick.onImageClickListener();
            }
        });
        setBackgroundColor(default_background_color);
    }
    public void initHeadPanel(){

        if(mMidleTitle != null){
            setMiddleTitle(Constant.FRAGMENT_FLAG_DO);
        }
    }

    public void setMiddleTitle(String s){
        mMidleTitle.setText(s);
        mMidleTitle.setSingleLine();
        mMidleTitle.setEllipsize(TextUtils.TruncateAt.END);
        mMidleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,middle_title_size);
    }

    public void setMiddleTitleTextSize(int size){
        mMidleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
    }

    //设置左边按钮（返回箭头）

    public interface LeftImageOnClick {
        public void onImageClickListener();
    }

    public void setLeftImageOnClick(LeftImageOnClick l) {
        this.mLeftImageOnClick = l;
    }

    public void setLeftImage(int resId) {
        mLeftImage.setImageResource(resId);
    }

    //设置从右边数第三个按钮

    public void setmRightThirdVisible(int visibility){
        mRightThirdImage.setVisibility(visibility);
    }

    public interface RightThirdOnClick {
        public void onImageClickListener();

    }
    public void setRightThirdOnClick(RightThirdOnClick t) {
        this.mRightThirdImageOnClick = t;
    }

    public void setRightThirdImage(int resId) {
        mRightThirdImage.setImage(resId);
    }

    public void setRightThirdText(String str) {
        mRightThirdImage.setText(str);
    }

    //设置从右边数第二个按钮

    public void setRightSecondVisible(int visibility) {
    	mRightSecondImage.setVisibility(visibility);
    }

    public interface RightSecondOnClick {
    	public void onImageClickListener();

    }
    public void setRightSecondOnClick(RightSecondOnClick t) {
    	this.mRightSecondImageOnClick = t;
    }

    public void setRightSecondImage(int resId) {
        mRightSecondImage.setImage(resId);
    }

    public void seRightSecondText(String str) {
    	mRightSecondImage.setText(str);
    }

    //设置从右边数第一个按钮

    public void setRightFirstVisible(int visibility) {
        mRightFirstImage.setVisibility(visibility);

//        if(visibility==View.GONE) {
//            RelativeLayout.LayoutParams rightBtnParam = (RelativeLayout.LayoutParams) mRightSecondImage.getLayoutParams();
//            rightBtnParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            mRightSecondImage.setLayoutParams(rightBtnParam);
//        }
    }

    public interface rightFirstImageOnClick {
    	public void onImageClickListener();
    }

    public void setRightFirstImageOnClick(rightFirstImageOnClick r) {
    	this.mRightFirstImageOnClick = r;
    }

    public void setRightFirstImage(int resId) {
    	mRightFirstImage.setImage(resId);
    }

    public void setRightFirstText(String str) {
    	mRightFirstImage.setText(str);
    }

}
