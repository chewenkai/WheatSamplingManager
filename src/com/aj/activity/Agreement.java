package com.aj.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.aj.collection.R;
import com.aj.ui.HeadControlPanel;

public class Agreement extends Activity{

	HeadControlPanel headPanel = null;

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.agreement);

			headPanel = (HeadControlPanel) findViewById(R.id.head_layout);
			headPanel.setRightFirstVisible(View.GONE);
			headPanel.setRightSecondVisible(View.GONE);
			if (headPanel != null) {
				headPanel.initHeadPanel();
				headPanel.setMiddleTitle("服务许可");
				headPanel.setLeftImage(R.drawable.ic_menu_back);
				HeadControlPanel.LeftImageOnClick l = new HeadControlPanel.LeftImageOnClick() {

					@Override
					public void onImageClickListener() {
						finish();    //退出
					}
				};
				headPanel.setLeftImageOnClick(l);
			}

	        WebView wView = (WebView)findViewById(R.id.mi_agreement);   
	        WebSettings wSet = wView.getSettings();   
	        wSet.setJavaScriptEnabled(true);   
	                   
	        wView.loadUrl("file:///android_asset/agreement.html");
	        //wView.loadUrl("content://com.android.htmlfileprovider/sdcard/index.html");
//	        wView.loadUrl("http://wap.baidu.com");
			wView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {

					return false;

				}
			});

	    }
		public void back(View view) {
			Agreement.this.finish();
		}

}
