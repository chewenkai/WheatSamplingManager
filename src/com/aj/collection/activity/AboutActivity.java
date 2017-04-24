package com.aj.collection.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.aj.collection.R;
import com.aj.collection.ui.HeadControlPanel;


public class AboutActivity extends Activity{

	HeadControlPanel headPanel = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);

		headPanel = (HeadControlPanel) findViewById(R.id.head_layout);
		headPanel.setRightFirstVisible(View.GONE);
		headPanel.setRightSecondVisible(View.GONE);
		if (headPanel != null) {
			headPanel.initHeadPanel();
			headPanel.setMiddleTitle("关于");
			headPanel.setLeftImage(R.drawable.ic_menu_back);
			HeadControlPanel.LeftImageOnClick l = new HeadControlPanel.LeftImageOnClick() {

				@Override
				public void onImageClickListener() {
					finish();    //退出
				}
			};
			headPanel.setLeftImageOnClick(l);
		}

		TextView tv_about = (TextView) findViewById(R.id.tv_about);
		TextView website = (TextView) findViewById(R.id.website);
		try {
			String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			tv_about.setText("版本号: "+version);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
//		SpannableString spanText = new SpannableString("最专业的幼儿教育交流平台");
//		spanText.setSpan(new StyleSpan(Typeface.ITALIC),0,spanText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//		center_text.setText(spanText);
//		center_text.setTextColor(Color.GRAY);
		
		SpannableString sp = new SpannableString("使用条款和隐私政策"); 
		           
		sp.setSpan(new ForegroundColorSpan(Color.argb(0xcc,0x03,0x6e,0xb8)), 0 ,sp.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
		website.setText(sp); 
		
		
		website.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//转向用户协议
				toAgreement();
				
			}
		});
	}
	
	public void back(View view){
		finish();
	}
	/*
	 * 转向用户协议
	 */
	private void toAgreement() {
		Intent intent = new Intent();
		intent.setClass(this, Agreement.class);
		startActivity(intent);
	}
}
