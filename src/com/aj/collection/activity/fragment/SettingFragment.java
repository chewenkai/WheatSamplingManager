package com.aj.collection.activity.fragment;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.Constant;
import com.aj.collection.activity.DetecedUnit;
import com.aj.collection.activity.MainActivity;
import com.aj.collection.activity.Navigation.MapActivity;
import com.aj.collection.R;
import com.aj.collection.activity.tools.FileStream;
import com.jrs.utils.FileUtils;

public class SettingFragment extends BaseFragment
{
	
	private LinearLayout unitBtn;
	private LinearLayout dataBtn;
	private LinearLayout clearCacheBtn;
	private LinearLayout mapButton;
	private LinearLayout versionBtn;

	private TextView tv_clearcache;

	private AlertDialog alertDialog;
	private AlertDialog.Builder builder;

	FileStream mFileStream = new FileStream(getActivity().getApplicationContext());

	private FtpServer mFtpServer;	
	private int port =12345;// 端口号	
	private String ftpConfigDir = Environment.getExternalStorageDirectory().getAbsolutePath() 
			+ "/ftpConfig/";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.set_content,container,false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MainActivity.currFragTag=Constant.FRAGMENT_FLAG_SETTING;

		//基本信息
		unitBtn = (LinearLayout) getActivity().findViewById(R.id.unit_btn);
		unitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getActivity(), DetecedUnit.class);
				getActivity().startActivity(i);
			}
		});

		//清除缓存
			//显示缓存大小
		tv_clearcache=(TextView)getActivity().findViewById(R.id.clear_cache_tv);

		getCacheSize();

		//清理缓存
		clearCacheBtn= (LinearLayout)getActivity().findViewById(R.id.clear_cache_btn);
		clearCacheBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(getActivity());

				dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				LayoutInflater inflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				RelativeLayout layout = (RelativeLayout) inflater.inflate(
						R.layout.dialogview_twocheckbox_onebutton, null);
				TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
				TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

				final CheckBox task = (CheckBox) layout.findViewById(R.id.checkbox_task);
				final CheckBox templet = (CheckBox) layout.findViewById(R.id.checkbox_templet);

				LinearLayout LL_task = (LinearLayout) layout.findViewById(R.id.LL_task);
				LinearLayout LL_template = (LinearLayout) layout.findViewById(R.id.LL_templet);

				LL_task.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (task.isChecked()) {
							task.setChecked(false);
						} else {
							task.setChecked(true);
						}
					}
				});

				LL_template.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (templet.isChecked()) {
							templet.setChecked(false);
						} else {
							templet.setChecked(true);
						}
					}
				});

				TextView positivebutton = (TextView) layout.findViewById(R.id.textview_save_button);


				Title.setText("清除本地抽样缓存");
				Message.setText("请选择要清除的内容");

				positivebutton.setText(" 清理 ");


				positivebutton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!task.isChecked() && !templet.isChecked()) {
							Toast.makeText(getActivity(), "没有清理任何内容", Toast.LENGTH_LONG).show();
							dialog.dismiss();
							return;
						}

						FileUtils futil = new FileUtils();

						if (task.isChecked())
							futil.deleteFile(mFileStream.getTaskFile());

						if (templet.isChecked())
							futil.deleteFile(mFileStream.getTempletFile());

						Toast.makeText(getActivity(), "清理成功！", Toast.LENGTH_LONG).show();

						getCacheSize();

						dialog.dismiss();
					}
				});


				dialog.setContentView(layout);
				dialog.setCancelable(true);
				dialog.show();
			}
		});

		//地图导航
		mapButton=(LinearLayout)getActivity().findViewById(R.id.map_navi_btn);
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent=new Intent(getActivity().getApplicationContext(), MapActivity.class);
				getActivity().startActivity(intent);
			}
		});

		//版本信息
		versionBtn = (LinearLayout)getActivity().findViewById(R.id.vertion_btn);
		versionBtn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("版本信息");
				builder.setMessage("抽样监督管理系统V1.0");
				alertDialog = builder.create();
				alertDialog.show();
			}
		});

		//数据传输
		dataBtn = (LinearLayout) getActivity().findViewById(R.id.data_btn);
		dataBtn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				copyResourceFile(R.raw.users, ftpConfigDir + "users.properties");
				String info = "本机IP：\n" + getLocalIpAddress()
						+ "\n";
				File f = new File(ftpConfigDir);				
				if (!f.exists())					
					f.mkdir();				
				copyResourceFile(R.raw.users, ftpConfigDir + "users.properties");
				Config1();
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("传输数据");
				builder.setMessage(info + "已启动连接，在断开之前请勿操作。");
				builder.setCancelable(false);
				builder.setPositiveButton("断开连接", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// TODO Auto-generated method stub
						AlertDialog ad ;
						AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
						ab.setTitle("温馨提示：");
						ab.setMessage("确认断开连接?");
						ab.setCancelable(false);
						ab.setPositiveButton("是", new DialogInterface.OnClickListener()
						{
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								// TODO Auto-generated method stub
								mFtpServer.stop();
							}
						});
						ab.setNegativeButton("否", new DialogInterface.OnClickListener()
						{
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								// TODO Auto-generated method stub
								alertDialog.show();
							}
						});
						ad = ab.create();
						ad.show();
					}
				});
				alertDialog = builder.create();
				alertDialog.show();
			}
		});
	}
	public String getLocalIpAddress() 
	{		
		WifiManager wifiManager = (WifiManager)getActivity(). getSystemService(getActivity().WIFI_SERVICE);
		int ip = wifiManager.getConnectionInfo().getIpAddress();
		String realIp = intToIp(ip);
		return realIp;
	}	
	public String intToIp(int ip)
	{
		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." +((ip >> 16) & 0xFF) +
				"." + ((ip >> 24) & 0xFF);
	}
	private void copyResourceFile(int rid, String targetFile) 
	{	
		InputStream fin = (getActivity().getApplicationContext()).getResources().openRawResource(rid);	
		FileOutputStream fos = null;	
		int length;		
		try 
		{		
			fos = new FileOutputStream(targetFile);		
			byte[] buffer = new byte[1024];		
			while ((length = fin.read(buffer)) != -1)
			{				
				fos.write(buffer, 0, length);			
			}		
		} 
		catch (FileNotFoundException e) 
		{		
			e.printStackTrace();	
		} 
		catch (IOException e) 
		{		
			e.printStackTrace();	
		} 
		finally {		
			if (fin != null) 
			{			
				try 
				{			
					fin.close();	
				}
				catch (IOException e)
				{				
					e.printStackTrace();
				}			
			}			
			if (fos != null)
			{			
				try {		
					fos.close();	
				}catch (IOException e) {				
					e.printStackTrace();		
				}		
			}		
		}	
	}	
	void Config1() {	
		FtpServerFactory serverFactory = new FtpServerFactory();	
		ListenerFactory factory = new ListenerFactory();
		factory.setPort(port);
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		String[] str = { "mkdir", ftpConfigDir };	
		try
		{		
			Process ps = Runtime.getRuntime().exec(str);	
			try
			{			
				ps.waitFor();		
			} catch (InterruptedException e) {		
				e.printStackTrace();		
			}		
		}
		catch (IOException e) 
		{			
			e.printStackTrace();	
		}		
		String filename = ftpConfigDir + "users.properties";	
		System.out.println("ftpConfig is "+filename);
		File files = new File(filename);	
		userManagerFactory.setFile(files);	
		serverFactory.setUserManager(userManagerFactory.createUserManager());
			
		try
		{		
			serverFactory.addListener("default",factory.createListener());	
			FtpServer server = serverFactory.createServer();	
			this.mFtpServer = server;			
			server.start();	
		}
		catch (FtpException e) 
		{		
			e.printStackTrace();	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public void onDetach()
	{
		// TODO Auto-generated method stub
		super.onDetach();
		if (null != mFtpServer)
		{		
			mFtpServer.stop();		
			mFtpServer = null;	
		}
	}

	public void getCacheSize(){
		long size= 0;
		try {
			size = FileUtils.getFileSizes(mFileStream.getTaskFile()) +
					FileUtils.getFileSizes(mFileStream.getTempletFile());
		} catch (Exception e) {
			e.printStackTrace();
			size=-1;
		}

		if(size==-1){
			tv_clearcache.setText("清理缓存" + " (未知)");
		}else{
			tv_clearcache.setText("清理缓存" + " (" + FileUtils.FormetFileSize(size)+")");
		}
	}
}
