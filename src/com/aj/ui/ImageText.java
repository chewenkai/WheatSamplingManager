package com.aj.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aj.Constant;
import com.aj.collection.R;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;

public class ImageText extends LinearLayout
{
	public ImageView mImageView;
	public TextView mTextView;
	private final static int DEFAULT_IMAGE_WIDTH = 25;
    private final static int DEFAULT_IMAGE_HEIGHT = 25;
    private final static int DEFAULT_TEXT_SIZE = 10;
    private int CHECKED_COLOR = Color.rgb(29, 118, 199); //选中蓝色  
    private int UNCHECKED_COLOR = Color.GRAY;   //自然灰色

    private Context mContext;
	public ImageText(Context context, AttributeSet attrs)
	{
		super(context,attrs);
//		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		inflater.inflate(R.layout.image_text_layout, this,true);
		LayoutInflater.from(context).inflate(R.layout.image_text_layout, this,true);
		mImageView = (ImageView) findViewById(R.id.image_image_text);
		mTextView = (TextView) findViewById(R.id.text_image_text);
        mTextView.setTextColor(Color.WHITE);
        mContext=context;
	}
	public void setImage(int id)
	{
		if(mImageView != null)
		{
            try {
                mImageView.setImageResource(id);
            } catch (Exception e) {

                e.printStackTrace();
            }
            setImageSize(ScreenUtils.dip2px(DEFAULT_IMAGE_WIDTH,mContext), ScreenUtils.dip2px(DEFAULT_IMAGE_HEIGHT,mContext));
		}
	}
	public void setText(String s){  
        if(mTextView != null){  
            mTextView.setText(s);  
            mTextView.setTextColor(Color.WHITE);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,DEFAULT_TEXT_SIZE);
        }  
    }

    public TextView getTextView(){
        return  mTextView;
    }

	@Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {  
        // TODO Auto-generated method stub  
        return true;  
    }
	private void setImageSize(int w, int h){  
        if(mImageView != null){  
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();  
            params.width = w;  
            params.height = h;  
            mImageView.setLayoutParams(params);  
        }  
    }
	public void setChecked(int itemID){  
        if(mTextView != null){  
            mTextView.setTextColor(CHECKED_COLOR);  
        }  
        int checkDrawableId = -1;  
        switch (itemID){  
        case Constant.BTN_FLAG_DO:  
            checkDrawableId = R.drawable.do_select;  
            break;  
        case Constant.BTN_FLAG_SEE:  
            checkDrawableId = R.drawable.see_select;  
            break;  
        case Constant.BTN_FLAG_SETTING:  
            checkDrawableId = R.drawable.set_select;  
            break;  
        default:break;  
        }  
        if(mImageView != null){  
            mImageView.setImageResource(checkDrawableId);  
        }  
    }

}
