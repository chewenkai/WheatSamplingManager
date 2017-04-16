package com.aj.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.aj.SystemBarTintManager;
import com.aj.collection.R;
import com.aj.tools.SPUtils;
import com.aj.ui.HeadControlPanel;
import com.aj.ui.HeadControlPanel.LeftImageOnClick;

public class DetecedUnit extends Activity
{
	private Switch kaiguan;
	private TextView userTV;
	private EditText unitNameET,unitAddrET,unitPostET,unitCZET,unitPhoneET;
	private String unitName,unitAddr,unitPost,unitCZ,unitPhone;
	private EditText jiankongET;
	private String jiankong;
	HeadControlPanel headPanel = null;
	String login_user;
	LinearLayout ll;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.set_base_info);
		//沉浸状态栏
		SystemBarTintManager.setStatusBarTint(DetecedUnit.this, Color.argb(0, 59, 59, 59));//透明状态栏
		init();
		ll = (LinearLayout) findViewById(R.id.ll_jiankong);
		unitNameET = (EditText)findViewById(R.id.unit_name_set);
		unitAddrET = (EditText)findViewById(R.id.unit_addr_set);
		unitPostET = (EditText)findViewById(R.id.unit_post_set);
		unitCZET = (EditText)findViewById(R.id.unit_cz_set);
		unitPhoneET = (EditText)findViewById(R.id.unit_phone_set);
		jiankongET = (EditText)findViewById(R.id.jiankong_set);
		userTV = (TextView)findViewById(R.id.user_set);
		kaiguan = (Switch)findViewById(R.id.switch_phone);
		String dv = "没有填写";
		login_user = (String) SPUtils.get(this, SPUtils.LOGIN_NAME, dv,SPUtils.LOGIN_VALIDATE );//登录的用户名
		userTV.setText(login_user);
		kaiguan.setChecked((Boolean)SPUtils.get(this, SPUtils.KAIGUAN, false, login_user));
		unitNameET.setText((String)SPUtils.get(this, SPUtils.SAMPLING_COMPANY, dv,SPUtils.USER_INFO ));
		unitAddrET.setText((String)SPUtils.get(this, SPUtils.SAMPLING_ADDR, dv,SPUtils.USER_INFO  ));
		unitPostET.setText((String)SPUtils.get(this, SPUtils.UNIT_POST, dv,login_user ));
		unitCZET.setText((String)SPUtils.get(this, SPUtils.UNIT_CZ, dv,login_user ));
		unitPhoneET.setText((String)SPUtils.get(this, SPUtils.SAMPLING_PHONE, dv,SPUtils.USER_INFO  ));

		jiankongET.setText((String)SPUtils.get(this, SPUtils.JIANKONG, dv, login_user));
		
		//根据开关状态改变栏目状态
//		if(!kaiguan.isChecked())
//		{
//			ll.setVisibility(View.INVISIBLE);
//		}
//		else
//		{
//			ll.setVisibility(View.VISIBLE);
//		}
//		kaiguan.setOnCheckedChangeListener(new OnCheckedChangeListener()
//		{
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//			{
//				if(isChecked)
//				{
//					ll.setVisibility(View.VISIBLE);
//				}
//				else
//				{
//					ll.setVisibility(View.INVISIBLE);
//				}
//			}
//		});
	}
	private void init()
	{
		headPanel = (HeadControlPanel)findViewById(R.id.head_layout);  
		headPanel.setRightFirstVisible(View.VISIBLE);
        if(headPanel != null){  
            headPanel.initHeadPanel();  
            headPanel.setMiddleTitle("个人信息");
            headPanel.setLeftImage(R.drawable.ic_menu_back);
            LeftImageOnClick l = new LeftImageOnClick()
			{
				@Override
				public void onImageClickListener()
				{
					finish();
				}
			};
			headPanel.setLeftImageOnClick(l);
//			headPanel.setRightFirstImage(R.drawable.save_file);
//			headPanel.setRightFirstText("1");
//			rightFirstImageOnClick r = new rightFirstImageOnClick()
//			{
//
//				@Override
//				public void onImageClickListener()
//				{
//					unitName = unitNameET.getText().toString().replace(" ", "");
//					unitAddr = unitAddrET.getText().toString().replace(" ", "");
//					unitPost = unitPostET.getText().toString().replace(" ", "");
//					unitCZ = unitCZET.getText().toString().replace(" ", "");
//					unitPhone = unitPhoneET.getText().toString().replace(" ", "");
//					jiankong = jiankongET.getText().toString().replace(" ", "");
//					if(unitName.equals("") || unitAddr.equals("") || unitPost.equals("")
//							|| unitCZ.equals("")||unitPhone.equals("")
//							||jiankong.equals(""))
//					{
//						T.showShort(DetecedUnit.this, "所有栏目不能为空");
//						return ;
//					}
//					SPUtils.put(DetecedUnit.this, SPUtils.SAMPLING_COMPANY, unitName, SPUtils.USER_INFO);
//					SPUtils.put(DetecedUnit.this, SPUtils.SAMPLING_ADDR, unitAddr, SPUtils.USER_INFO);
//					SPUtils.put(DetecedUnit.this, SPUtils.UNIT_POST, unitPost, login_user);
//					SPUtils.put(DetecedUnit.this, SPUtils.UNIT_CZ, unitCZ, login_user);
//					SPUtils.put(DetecedUnit.this, SPUtils.SAMPLING_PHONE, unitPhone, SPUtils.USER_INFO);
//					SPUtils.put(DetecedUnit.this, SPUtils.JIANKONG, jiankong, login_user);
//					SPUtils.put(DetecedUnit.this, SPUtils.KAIGUAN, kaiguan.isChecked(), login_user);
//					T.showShort(DetecedUnit.this, "保存成功");
//				}
//			};
//			headPanel.setRightFirstImageOnClick(r);
        }
	}
}
