package com.aj.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.aj.Constant;
import com.aj.collection.R;
/**
 * The main activity bottom layout.
 * @author Administrator
 *
 */
public class BottomControlPanel extends RelativeLayout implements View.OnClickListener
{
//	private Context mContext;  
    private ImageText mDoBtn = null;  
    private ImageText mSeeBtn = null;  
    private ImageText mSettingBtn = null;  
    private int DEFALUT_BACKGROUND_COLOR = Color.rgb(243, 243, 243); //Color.rgb(192, 192, 192)  
    private BottomPanelCallback mBottomCallback = null;  
    private List<ImageText> viewList = new ArrayList<ImageText>(); 
    /**
     * Tell activity which panel is selected.
     * @author Administrator
     *
     */
    public interface BottomPanelCallback{  
        public void onBottomPanelClick(int itemId);  
    }
	public BottomControlPanel(Context context, AttributeSet attrs) 
    {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub   
    }
	 @Override  
    protected void onFinishInflate() {  
        // TODO Auto-generated method stub   
        mDoBtn = (ImageText)findViewById(R.id.btn_do);  
        mSeeBtn = (ImageText)findViewById(R.id.btn_see);  
        mSettingBtn = (ImageText)findViewById(R.id.btn_setting);  
        setBackgroundColor(DEFALUT_BACKGROUND_COLOR);  
        viewList.add(mDoBtn);  
        viewList.add(mSeeBtn);  
        viewList.add(mSettingBtn);  
  
    }  
    public void initBottomPanel(){  
        if(mDoBtn != null){  
            mDoBtn.setImage(R.drawable.do_un_select);  
            mDoBtn.setText("任务");  
        }  
        if(mSeeBtn != null){  
            mSeeBtn.setImage(R.drawable.see_un_select);  
            mSeeBtn.setText("浏览");  
        }  
        if(mSettingBtn != null){  
            mSettingBtn.setImage(R.drawable.set_un_select);  
            mSettingBtn.setText("设置");  
        }  
        setBtnListener();  
    }  
    private void setBtnListener(){  
        int num = this.getChildCount();  
        for(int i = 0; i < num; i++){  
            View v = getChildAt(i);  
            if(v != null){  
                v.setOnClickListener(this);  
            }  
        }  
    }  
    public void setBottomCallback(BottomPanelCallback bottomCallback){  
        mBottomCallback = bottomCallback;  
    }  
    @Override  
    public void onClick(View v) {  
        // TODO Auto-generated method stub   
        initBottomPanel();  
        int index = -1;  
        switch(v.getId()){  
        case R.id.btn_do:  
            index = Constant.BTN_FLAG_DO;  
            mDoBtn.setChecked(Constant.BTN_FLAG_DO);  
            break;  
        case R.id.btn_see:  
            index = Constant.BTN_FLAG_SEE;  
            mSeeBtn.setChecked(Constant.BTN_FLAG_SEE);  
            break;  
        case R.id.btn_setting:  
            index = Constant.BTN_FLAG_SETTING;  
            mSettingBtn.setChecked(Constant.BTN_FLAG_SETTING);  
            break;  
        default:break;  
        }  
        if(mBottomCallback != null){  
            mBottomCallback.onBottomPanelClick(index);  
        }  
    }  
    public void defaultBtnChecked(){  
        if(mDoBtn != null){  
            mDoBtn.setChecked(Constant.BTN_FLAG_DO);  
        }  
    }  
    @Override  
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {  
        // TODO Auto-generated method stub   
        super.onLayout(changed, left, top, right, bottom);  
        layoutItems(left, top, right, bottom);  
    }  
    /**最左边和最右边的view由母布局的padding进行控制位置。这里需对第2、3个view的位置重新设置 
     * @param left 
     * @param top 
     * @param right 
     * @param bottom 
     */  
    private void layoutItems(int left, int top, int right, int bottom){  
        int n = getChildCount();  
        if(n == 0){  
            return;  
        }  
//        int paddingLeft = getPaddingLeft();  
//        int paddingRight = getPaddingRight();  
//        Log.i("aj", "paddingLeft = " + paddingLeft + " paddingRight = " + paddingRight);  
        int width = right - left;  
        int height = bottom - top;  
//        Log.i("aj", "width = " + width + " height = " + height);  
        int allViewWidth = 0;  
        for(int i = 0; i< n; i++){  
            View v = getChildAt(i);  
//            Log.i("aj", "v.getWidth() = " + v.getWidth());  
            allViewWidth += v.getWidth();  
        }  
//        int blankWidth = (width - allViewWidth - paddingLeft - paddingRight) / (n + 1);  
        int blankWidth = (width - allViewWidth  ) / ((n - 1)*2+2);  
//        Log.i("aj", "blankV = " + blankWidth );  
  
        LayoutParams params0 = (LayoutParams) viewList.get(0).getLayoutParams();  
        params0.leftMargin = blankWidth;  
        viewList.get(0).setLayoutParams(params0);  
        LayoutParams params1 = (LayoutParams) viewList.get(1).getLayoutParams();  
        params1.leftMargin = blankWidth*2;  
        viewList.get(1).setLayoutParams(params1);  
  
        LayoutParams params2 = (LayoutParams) viewList.get(2).getLayoutParams();  
        params2.leftMargin = blankWidth*2;  
        viewList.get(2).setLayoutParams(params2);  
    } 
	
}
