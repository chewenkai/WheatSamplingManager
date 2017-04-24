package com.aj.collection.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.aj.collection.R;
import com.aj.collection.http.URLs;
import com.aj.collection.tools.SPUtils;
import com.aj.collection.tools.T;
import com.aj.collection.ui.HeadControlPanel;
import com.aj.collection.ui.HeadControlPanel.LeftImageOnClick;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegistActivity extends Activity
{
	private EditText userET,pwdET,repwdET,unitNameET,unitPhoneET,unitAddrET,unitCZET,unitPostET;
	private EditText jiankongET;
	private String jiankong;
	private String user,pwd,repwd,unitName,unitPhone,unitAddr,unitCZ,unitPost;
	HeadControlPanel headPanel = null;
	private DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener()
	{
		
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			// TODO Auto-generated method stub
			save();
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.regist_layout);
		init();
		
	}
	private void init()
	{
		headPanel = (HeadControlPanel)findViewById(R.id.head_layout);  
		headPanel.setRightFirstVisible(View.VISIBLE);
        if(headPanel != null){  
            headPanel.initHeadPanel();  
            headPanel.setMiddleTitle("注册");
            headPanel.setLeftImage(R.drawable.ic_menu_back);
            LeftImageOnClick l = new LeftImageOnClick()
			{
				
				@Override
				public void onImageClickListener()
				{
					// TODO Auto-generated method stub
					finish();
				}
			};
			headPanel.setLeftImageOnClick(l);
			headPanel.setRightFirstImage(R.drawable.save_file);
			headPanel.setRightFirstText("保存");
			//headPanel.getRightImageText().getTextView().setVisibility(View.GONE);
			HeadControlPanel.rightFirstImageOnClick r = new HeadControlPanel.rightFirstImageOnClick()
			{
				
				@Override
				public void onImageClickListener()
				{
					// TODO Auto-generated method stub
					AlertDialog alertDialog;
					AlertDialog.Builder builder = new AlertDialog.Builder(RegistActivity.this);
					builder.setTitle("温馨提示");
					builder.setMessage("是否保存？");
					builder.setPositiveButton("是", onClickListener);
					builder.setNegativeButton("否", null);
					alertDialog = builder.create();
					alertDialog.show();
				}
			};
			headPanel.setRightFirstImageOnClick(r);
        }
        
        userET = (EditText) findViewById(R.id.user_reg);
        pwdET = (EditText) findViewById(R.id.pwd_reg);
        repwdET = (EditText) findViewById(R.id.repwd_reg);
        unitNameET = (EditText) findViewById(R.id.unit_name_reg);
        unitPhoneET = (EditText) findViewById(R.id.unit_phone);
        unitCZET = (EditText) findViewById(R.id.unit_cz_reg);
        unitAddrET = (EditText) findViewById(R.id.unit_addr_reg);
        unitPostET = (EditText) findViewById(R.id.unit_post_reg);
        
        jiankongET= (EditText) findViewById(R.id.jiankong_dianhua);
	}
	private void save()
	{
		// TODO Auto-generated method stub
		user = userET.getText().toString().replace(" ", "");
		pwd = pwdET.getText().toString().replace(" ", "");
		repwd = repwdET.getText().toString().replace(" ", "");
		unitName = unitNameET.getText().toString().replace(" ", "");
		unitPhone = unitPhoneET.getText().toString().replace(" ", "");
		unitCZ = unitCZET.getText().toString().replace(" ", "");
		unitAddr = unitAddrET.getText().toString().replace(" ", "");
		unitPost = unitPostET.getText().toString().replace(" ", "");
		
		jiankong = jiankongET.getText().toString().replace(" ", "");
		if(user.equals("") || pwd.equals("")||repwd.equals("") || unitName.equals("")
				||unitPhone.equals("")||unitCZ.equals("")||unitAddr.equals("")
				||unitPost.equals("") || jiankong.equals(""))
		{
			T.showShort(this, "所有栏目不能为空");
			return;
		}
		if(SPUtils.contains(this, user, SPUtils.USER_DATA))
		{
			T.showShort(this, "帐户已存在");
			return;
		}
		if(!pwd.equals(repwd))
		{
			T.showShort(this, "两次密码不一致");
			return;
		}
		SPUtils.put(this, user, pwd, SPUtils.USER_DATA);
		SPUtils.put(this, SPUtils.UNIT_USER, user, user);
		SPUtils.put(this, SPUtils.UNIT_NAME, unitName, user);
		SPUtils.put(this, SPUtils.UNIT_PHONE, unitPhone, user);
		SPUtils.put(this, SPUtils.UNIT_ADDR, unitAddr, user);
		SPUtils.put(this, SPUtils.UNIT_CZ, unitCZ, user);
		SPUtils.put(this, SPUtils.UNIT_POST, unitPost, user);
		SPUtils.put(this, SPUtils.JIANKONG, jiankong, user);
		SPUtils.put(this, SPUtils.KAIGUAN, false, user);
		
		T.showShort(RegistActivity.this,"保存成功");
		finish();
	}

	public void registTask(){
		final Context mContext=this;
		RequestQueue queue = Volley.newRequestQueue(this);
		StringRequest request = new StringRequest(Request.Method.POST, URLs.REGISTER,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String s) {
						Log.e(">>>>>>>>>", s.toString());

					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError volleyError) {

						Log.e(">>>>>>>>>", volleyError.toString());
						Toast.makeText(mContext, "网络没有连接", Toast.LENGTH_LONG).show();
					}
				}
		) {
			@Override
			public Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("username", "kevinFormChina");
				map.put("password", "123456789");
				map.put("email", "769776082@qq.com");
				return map;
			}

		};
		queue.add(request);
	}

}
