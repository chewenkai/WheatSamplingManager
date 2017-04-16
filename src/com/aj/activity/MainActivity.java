package com.aj.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aj.Constant;
import com.aj.SystemBarTintManager;
import com.aj.collection.R;
import com.aj.fragment.BaseFragment;
import com.aj.tools.FileStream;
import com.aj.tools.Util;
import com.aj.ui.BottomControlPanel;
import com.aj.ui.BottomControlPanel.BottomPanelCallback;
import com.aj.ui.HeadControlPanel;

public class MainActivity extends Activity implements BottomPanelCallback
{
	BottomControlPanel bottomPanel = null;  
    HeadControlPanel headPanel = null;
    
    private FragmentManager fragmentManager = null;  
    private FragmentTransaction fragmentTransaction = null;  
      
/*  private MessageFragment messageFragment; 
    private ContactsFragment contactsFragment; 
    private NewsFragment newsFragment; 
    private SettingFragment settingFragment;*/  
      
    public static String currFragTag = "";
    
    private FileStream mFileStream;
    private File taskFile, templetFile;
    private Dialog dialog;
    private AlertDialog.Builder builder;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog = new Dialog(MainActivity.this);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		
		initUI();  
        fragmentManager = getFragmentManager();  
        setDefaultFirstFragment(Constant.FRAGMENT_FLAG_DO);
        
        mFileStream = new FileStream(this);

	}



    @Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		boolean gps = Util.isOpen(this);
        if(!gps)
		{
            openGPSDialog();

		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private void initUI(){
        //沉浸状态栏
        SystemBarTintManager.setStatusBarTint(MainActivity.this, Color.argb(0, 38, 133, 206));//透明状态栏


        bottomPanel = (BottomControlPanel)findViewById(R.id.bottom_layout);  
        if(bottomPanel != null){  
            bottomPanel.initBottomPanel();  
            bottomPanel.setBottomCallback(this);  
        }  
        headPanel = (HeadControlPanel)findViewById(R.id.head_layout);  
        if(headPanel != null){  
            headPanel.initHeadPanel();  
        }  
    }  
  
    /* 处理BottomControlPanel的回调 
     * @see org.yanzi.ui.BottomControlPanel.BottomPanelCallback#onBottomPanelClick(int) 
     */  
    @Override  
    public void onBottomPanelClick(int itemId) {
        // TODO Auto-generated method stub   
        String tag = "";  
        if((itemId & Constant.BTN_FLAG_DO) != 0){  
            tag = Constant.FRAGMENT_FLAG_DO;  
        }else if((itemId & Constant.BTN_FLAG_SEE) != 0){  
            tag = Constant.FRAGMENT_FLAG_SEE;  
        }else if((itemId & Constant.BTN_FLAG_SETTING) != 0){  
            tag = Constant.FRAGMENT_FLAG_SETTING;  
        }  
        setTabSelection(tag); //切换Fragment   
        headPanel.setMiddleTitle(tag);//切换标题    
    }  
      
    private void setDefaultFirstFragment(String tag){  
//        Log.i("aj", "setDefaultFirstFragment enter... currFragTag = " + currFragTag);  
        setTabSelection(tag);  
        bottomPanel.defaultBtnChecked();  
//        Log.i("aj", "setDefaultFirstFragment exit...");  
    }  
      
    private void commitTransactions(String tag){  
        if (fragmentTransaction != null && !fragmentTransaction.isEmpty()) {  
            fragmentTransaction.commit();  
            currFragTag = tag;  
            fragmentTransaction = null;  
        }  
    }  
      
    private FragmentTransaction ensureTransaction( ){  
        if(fragmentTransaction == null){  
            fragmentTransaction = fragmentManager.beginTransaction();  
            fragmentTransaction  
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);  
              
        }  
        return fragmentTransaction;  
          
    }  
      
    private void attachFragment(int layout, Fragment f, String tag){  
        if(f != null){  
            if(f.isDetached()){  
                ensureTransaction();  
                fragmentTransaction.attach(f);  
                  
            }else if(!f.isAdded()){  
                ensureTransaction();  
                fragmentTransaction.add(layout, f, tag);  
            }  
        }  
    }  
      
    private Fragment getFragment(String tag){  
          
        BaseFragment f = (BaseFragment)fragmentManager.findFragmentByTag(tag);  
          
        if(f == null){  
//            Toast.makeText(getApplicationContext(), "fragment = null tag = " + tag, Toast.LENGTH_SHORT).show();  
            f = BaseFragment.newInstance(tag);  
        }  
        return f;  
          
    }  
    private void detachFragment(Fragment f){  
          
        if(f != null && !f.isDetached()){  
            ensureTransaction();  
            fragmentTransaction.detach(f);  
        }  
    }  
    /**切换fragment  
     * @param tag 
     */  
    private  void switchFragment(String tag){  
        if(TextUtils.equals(tag, currFragTag)){  
            return;  
        }  
        //把上一个fragment detach掉    
        if(currFragTag != null && !currFragTag.equals("")){  
            detachFragment(getFragment(currFragTag));  
        }  
        attachFragment(R.id.fragment_content, getFragment(tag), tag);  
        commitTransactions( tag);  
    }   
      
    /**设置选中的Tag 
     * @param tag 
     */  
    public  void setTabSelection(String tag) {  
        // 开启一个Fragment事务   
        fragmentTransaction = fragmentManager.beginTransaction();  
/*       if(TextUtils.equals(tag, Constant.FRAGMENT_FLAG_MESSAGE)){ 
           if (messageFragment == null) { 
                messageFragment = new MessageFragment(); 
            }  
             
        }else if(TextUtils.equals(tag, Constant.FRAGMENT_FLAG_CONTACTS)){ 
            if (contactsFragment == null) { 
                contactsFragment = new ContactsFragment(); 
            }  
             
        }else if(TextUtils.equals(tag, Constant.FRAGMENT_FLAG_NEWS)){ 
            if (newsFragment == null) { 
                newsFragment = new NewsFragment(); 
            } 
             
        }else if(TextUtils.equals(tag,Constant.FRAGMENT_FLAG_SETTING)){ 
            if (settingFragment == null) { 
                settingFragment = new SettingFragment(); 
            } 
        }else if(TextUtils.equals(tag, Constant.FRAGMENT_FLAG_SIMPLE)){ 
            if (simpleFragment == null) { 
                simpleFragment = new SimpleFragment(); 
            }  
             
        }*/  
         switchFragment(tag);  
           
    }  
  
    @Override  
    protected void onStop() {  
        // TODO Auto-generated method stub   
        super.onStop();  
        currFragTag = "";  
    }  
  
    @Override  
    protected void onSaveInstanceState(Bundle outState) {  
        // TODO Auto-generated method stub   
    }

    /**
     * 打开GPS提示对话框
     */
    void openGPSDialog() {
        if (dialog.isShowing()) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.dialogview_two_button, null);
        TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
        TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

        TextView positivebutton = (TextView) layout.findViewById(R.id.textview_positive_button);
        TextView negativebutton = (TextView) layout.findViewById(R.id.textview_negative_button);

        Title.setText("本软件需开启GPS定位开关");
        Message.setText("是否开启？");

        positivebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        });
        negativebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                MainActivity.this.finish();
            }
        });


        dialog.setContentView(layout);
        dialog.setCancelable(false);
        dialog.show();
    }
}
