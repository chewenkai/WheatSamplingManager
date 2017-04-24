package com.aj.collection.ThirdModify_WeiXin;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aj.collection.R;
import com.aj.collection.service.LongTermService;
import com.aj.collection.service.MsgService;
import com.aj.collection.tools.ExitApplication;
import com.aj.collection.tools.SPUtils;

public class Exit extends Activity {
	//private MyDialog dialog;
	private Context mContext=this;
	private LinearLayout layout;
	private LinearLayout llNeedService;
	private CheckBox cbNeedService;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exit_dialog);
		//dialog=new MyDialog(this);
		llNeedService=(LinearLayout)findViewById(R.id.LL_need_service);
		cbNeedService=(CheckBox)findViewById(R.id.cb_need_service);
		layout=(LinearLayout)findViewById(R.id.exit_layout);

		if((boolean)SPUtils.get(mContext,SPUtils.KEEPSERVICE,true,SPUtils.KEEPSERVICE))
			cbNeedService.setChecked(true);
		else
			cbNeedService.setChecked(false);

		cbNeedService.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(cbNeedService.isChecked()){
					SPUtils.put(mContext,SPUtils.KEEPSERVICE,true,SPUtils.KEEPSERVICE);
				}else{
					SPUtils.put(mContext, SPUtils.KEEPSERVICE, false, SPUtils.KEEPSERVICE);
				}
			}
		});
		layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "提示：点击确定关闭应用程序！",
						Toast.LENGTH_SHORT).show();	
			}
		});

	}

	@Override
	public boolean onTouchEvent(MotionEvent event){

//		finish();
		return true;
	}
	
	public void exitbutton1(View v) {  
    	this.finish();    	
      }  
	public void exitbutton0(View v) {
		if(!cbNeedService.isChecked())
			stopService(new Intent(mContext, MsgService.class));
		stopService(new Intent(this, LongTermService.class));
		this.finish();
		ExitApplication.getInstance().exit();
    	//MainWeixin.instance.finish();//关闭Main 这个Activity
      }  
	
}
