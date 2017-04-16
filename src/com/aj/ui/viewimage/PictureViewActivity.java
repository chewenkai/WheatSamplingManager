package com.aj.ui.viewimage;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import com.aj.collection.R;

import java.util.ArrayList;
import java.util.List;

public class PictureViewActivity extends FragmentActivity 
{
	
	// 屏幕宽度
	public static int screenWidth;
	// 屏幕高度
	public static int screenHeight;

	public static List<String> pictures=new  ArrayList<String>();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.picture_view_activity);
		  //初始化接口，应用启动的时候调用

		initViews();

		getData();
	}

	private void getData() {
		if(!pictures.isEmpty())
			pictures.removeAll(pictures);
		pictures.add(getIntent().getStringExtra("picPath"));
	}

	private void initViews() {

		screenWidth = getWindow().getWindowManager().getDefaultDisplay()
				.getWidth();
		screenHeight = getWindow().getWindowManager().getDefaultDisplay()
				.getHeight();

	}
	 
	
}