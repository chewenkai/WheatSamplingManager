//package com.aj;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.net.wifi.WifiManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.Settings;
//import android.support.design.widget.TabLayout;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.view.ViewPager;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.Window;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.aj.activity.DetecedUnit;
//import com.aj.activity.Navigation.MapActivity;
//import com.aj.collection.R;
//import com.aj.tools.ExitApplication;
//import com.aj.tools.FileStream;
//import com.aj.tools.SPUtils;
//import com.aj.tools.Util;
//import com.aj.ui.widget.FileUtil;
//import com.gordonwong.materialsheetfab.MaterialSheetFab;
//import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
//import com.aj.adapters.MainPagerAdapter;
//import com.jrs.utils.FileUtils;
//
//import net.micode.compass.CompassActivity;
//import net.micode.notes.ui.NotesListActivity;
//
//import org.apache.ftpserver.FtpServer;
//import org.apache.ftpserver.FtpServerFactory;
//import org.apache.ftpserver.ftplet.FtpException;
//import org.apache.ftpserver.listener.ListenerFactory;
//import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
///**
// * Created by Gordon Wong on 7/17/2015.
// *
// * Main activity for material sheet fab sample.
// */
//public class NiceActivity extends AppCompatActivity implements View.OnClickListener {
//
//	private ActionBarDrawerToggle drawerToggle;
//	private DrawerLayout drawerLayout;
//	private MaterialSheetFab materialSheetFab;
//	private int statusBarColor;
//
//	private LinearLayout unitBtn;
//	private LinearLayout dataBtn;
//	private LinearLayout clearCacheBtn;
//
//	private LinearLayout versionBtn;
//	private LinearLayout toolbox;
//	private LinearLayout exitBtn;
//
//	private TextView tv_clearcache;
//	private TextView user_name,company_name;//抽屉顶部的用户名和公司名
//
//	private AlertDialog alertDialog;
//	private AlertDialog.Builder builder;
//
//	FileStream mFileStream = new FileStream(this);
//
//	private FtpServer mFtpServer;
//	private int port =12345;// 端口号
//	private String ftpConfigDir = Environment.getExternalStorageDirectory().getAbsolutePath()
//			+ "/ftpConfig/";
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		//LeakCanary.install(NiceActivity.this.getApplication());
//		setTitle(R.string.app_name);
//		dialog = new Dialog(NiceActivity.this);
//		ExitApplication.getInstance().addActivity(this);
//		//dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_nice);
//		setupActionBar();
//		setupDrawer();
//		//setupFab();
//		setupTabs();
//
//		SystemBarTintManager.setStatusBarTint(NiceActivity.this, Color.argb(255, 0, 188, 212));
//
//
//	}
//
//
//
//	private Dialog dialog;
//	/**
//	 * 打开GPS提示对话框
//	 */
//	void openGPSDialog() {
//		if (dialog.isShowing()) {
//			return;
//		}
//
//		LayoutInflater inflater = (LayoutInflater) NiceActivity.this
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		RelativeLayout layout = (RelativeLayout) inflater.inflate(
//				R.layout.dialogview_two_button, null);
//		TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
//		TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);
//
//		TextView positivebutton = (TextView) layout.findViewById(R.id.textview_positive_button);
//		TextView negativebutton = (TextView) layout.findViewById(R.id.textview_negative_button);
//
//		Title.setText("本软件需开启GPS定位开关");
//		Message.setText("是否开启？");
//
//		positivebutton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				dialog.dismiss();
//				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//				startActivity(intent);
//
//			}
//		});
//		negativebutton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				dialog.dismiss();
//				NiceActivity.this.finish();
//			}
//		});
//
//
//		dialog.setContentView(layout);
//		dialog.setCancelable(false);
//		dialog.show();
//	}
//
//	@Override
//	protected void onResume() {
//		boolean gps = Util.isOpen(this);
//		if(!gps)
//		{
//			openGPSDialog();
//
//		}
//		super.onResume();
//		//基本信息
//		unitBtn = (LinearLayout) findViewById(R.id.unit_btn);
//		unitBtn.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent i = new Intent(NiceActivity.this, DetecedUnit.class);
//				startActivity(i);
//			}
//		});
//
//		user_name=(TextView)findViewById(R.id.user_name);
//		company_name=(TextView)findViewById(R.id.company_name);
//
//		String login_user=(String) SPUtils.get(this, SPUtils.LOGIN_USER, "", SPUtils.USER_DATA);
//		user_name.setText(login_user);
//		company_name.setText("所属单位:"+(String)SPUtils.get(this, SPUtils.UNIT_NAME, "",login_user ));
//
//		//清除缓存
//		//显示缓存大小
//		tv_clearcache=(TextView)findViewById(R.id.clear_cache_tv);
//
//		getCacheSize();
//
//		//清理缓存
//		clearCacheBtn= (LinearLayout)findViewById(R.id.clear_cache_btn);
//		clearCacheBtn.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				final Dialog dialog = new Dialog(NiceActivity.this);
//
//				dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//
//				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//				LayoutInflater inflater = (LayoutInflater) NiceActivity.this
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				RelativeLayout layout = (RelativeLayout) inflater.inflate(
//						R.layout.dialogview_twocheckbox_onebutton, null);
//				TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
//				TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);
//
//				final CheckBox task = (CheckBox) layout.findViewById(R.id.checkbox_task);
//				final CheckBox templet = (CheckBox) layout.findViewById(R.id.checkbox_templet);
//
//				LinearLayout LL_task = (LinearLayout) layout.findViewById(R.id.LL_task);
//				LinearLayout LL_template = (LinearLayout) layout.findViewById(R.id.LL_templet);
//
//				LL_task.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						if (task.isChecked()) {
//							task.setChecked(false);
//						} else {
//							task.setChecked(true);
//						}
//					}
//				});
//
//				LL_template.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						if (templet.isChecked()) {
//							templet.setChecked(false);
//						} else {
//							templet.setChecked(true);
//						}
//					}
//				});
//
//				TextView positivebutton = (TextView) layout.findViewById(R.id.textview_save_button);
//
//
//				Title.setText("清除本地抽样缓存");
//				Message.setText("请选择要清除的内容");
//
//				positivebutton.setText(" 清理 ");
//
//
//				positivebutton.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						if (!task.isChecked() && !templet.isChecked()) {
//							Toast.makeText(NiceActivity.this, "没有清理任何内容", Toast.LENGTH_LONG).show();
//							dialog.dismiss();
//							return;
//						}
//
//						FileUtils futil = new FileUtils();
//
//						if (task.isChecked())
//							futil.deleteFile(mFileStream.getTaskFile());
//
//						if (templet.isChecked())
//							futil.deleteFile(mFileStream.getTempletFile());
//
//						Toast.makeText(NiceActivity.this, "清理成功！", Toast.LENGTH_LONG).show();
//
//						getCacheSize();
//
//						dialog.dismiss();
//					}
//				});
//
//
//				dialog.setContentView(layout);
//				dialog.setCancelable(true);
//				dialog.show();
//			}
//		});
//
//		//工具箱
//		toolbox=(LinearLayout)NiceActivity.this.findViewById(R.id.toolbox_btn);
//		toolbox.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//
//				final Dialog toolbox_dialog = new Dialog(
//						NiceActivity.this);
//
//				LayoutInflater inflater = (LayoutInflater) NiceActivity.this
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				RelativeLayout layout = (RelativeLayout) inflater.inflate(
//						R.layout.dialogview_toolbox, null);
//
//				//地图导航
//				LinearLayout mapButton=(LinearLayout)layout.findViewById(R.id.map_navi_btn);
//				mapButton.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						Intent intent=new Intent(NiceActivity.this.getApplicationContext(), MapActivity.class);
//						NiceActivity.this.startActivity(intent);
//					}
//				});
//
//				//记事本
//				LinearLayout minote_btn=(LinearLayout)layout.findViewById(R.id.minote_btn);
//				minote_btn.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						startActivity(new Intent(NiceActivity.this, NotesListActivity.class));
//					}
//				});
//
//				//指南针
//				LinearLayout compass_btn=(LinearLayout)layout.findViewById(R.id.compass_btn);
//				compass_btn.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						startActivity(new Intent(NiceActivity.this, CompassActivity.class));
//					}
//				});
//
//
//				toolbox_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//				toolbox_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//				toolbox_dialog.setContentView(layout);
//				toolbox_dialog.setCancelable(true);
//				toolbox_dialog.show();
//
//
//			}
//		});
//
//
//		//版本信息
//		versionBtn = (LinearLayout)NiceActivity.this.findViewById(R.id.vertion_btn);
//		versionBtn.setOnClickListener(new View.OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				builder = new AlertDialog.Builder(NiceActivity.this);
//				builder.setTitle("版本信息");
//				builder.setMessage("抽样监督管理系统V1.0");
//				alertDialog = builder.create();
//				alertDialog.show();
//			}
//		});
//
//		exitBtn = (LinearLayout)NiceActivity.this.findViewById(R.id.exitapp);
//		exitBtn.setOnClickListener(new View.OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				ExitApplication.getInstance().exit();
//			}
//		});
//
//		//数据传输
//		dataBtn = (LinearLayout) NiceActivity.this.findViewById(R.id.data_btn);
//		dataBtn.setOnClickListener(new View.OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				copyResourceFile(R.raw.users, ftpConfigDir + "users.properties");
//				String info = "本机IP：\n" + getLocalIpAddress()
//						+ "\n";
//				File f = new File(ftpConfigDir);
//				if (!f.exists())
//					f.mkdir();
//				copyResourceFile(R.raw.users, ftpConfigDir + "users.properties");
//				Config1();
//				builder = new AlertDialog.Builder(NiceActivity.this);
//				builder.setTitle("传输数据");
//				builder.setMessage(info + "已启动连接，在断开之前请勿操作。");
//				builder.setCancelable(false);
//				builder.setPositiveButton("断开连接", new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick(DialogInterface dialog, int which)
//					{
//						AlertDialog ad ;
//						AlertDialog.Builder ab = new AlertDialog.Builder(NiceActivity.this);
//						ab.setTitle("温馨提示：");
//						ab.setMessage("确认断开连接?");
//						ab.setCancelable(false);
//						ab.setPositiveButton("是", new DialogInterface.OnClickListener()
//						{
//
//							@Override
//							public void onClick(DialogInterface dialog, int which)
//							{
//								mFtpServer.stop();
//								setupTabs();
//							}
//						});
//						ab.setNegativeButton("否", new DialogInterface.OnClickListener()
//						{
//
//							@Override
//							public void onClick(DialogInterface dialog, int which)
//							{
//								alertDialog.show();
//							}
//						});
//						ad = ab.create();
//						ad.show();
//					}
//				});
//				alertDialog = builder.create();
//				alertDialog.show();
//			}
//		});
//	}
//
//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//		drawerToggle.syncState();
//	}
//
//	@Override
//	public void onBackPressed() {
//		AlertDialog alertDialog;
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder = new AlertDialog.Builder(this);
//		builder.setTitle("退出提示");
//		builder.setMessage("确定退出应用?");
//		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				ExitApplication.getInstance().exit();
//				finish();    //退出
//			}
//		});
//		builder.setNegativeButton("否", null);
//		alertDialog = builder.create();
//		alertDialog.show();    //显示对话框
//	}
//
//	/**
//	 * Sets up the action bar.
//	 */
//	private void setupActionBar() {
//		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//	}
//
//	/**
//	 * Sets up the navigation drawer.
//	 */
//	private void setupDrawer() {
//		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.opendrawer,
//				R.string.closedrawer);
//		drawerLayout.setDrawerListener(drawerToggle);
//	}
//
//	/**
//	 * Sets up the tabs.
//	 */
//	private void setupTabs() {
//		// Setup view pager
//		ViewPager viewpager = (ViewPager) findViewById(R.id.viewpager);
//		viewpager.setAdapter(new MainPagerAdapter(this, getSupportFragmentManager()));
//		viewpager.setOffscreenPageLimit(MainPagerAdapter.NUM_ITEMS);
//		//updatePage(viewpager.getCurrentItem());
//
//		// Setup tab layout
//		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
//		tabLayout.setupWithViewPager(viewpager);
//		viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//			@Override
//			public void onPageScrolled(int i, float v, int i1) {
//			}
//
//			@Override
//			public void onPageSelected(int i) {
//				//updatePage(i);
//			}
//
//			@Override
//			public void onPageScrollStateChanged(int i) {
//			}
//		});
//	}
//
//	/**
//	 * Sets up the Floating action button.
//	 */
//	private void setupFab() {
//
////		Fab fab = (Fab) findViewById(R.id.fab);
////		View sheetView = findViewById(R.id.fab_sheet);
////		View overlay = findViewById(R.id.overlay);
////		int sheetColor = getResources().getColor(R.color.background_card);
////		int fabColor = getResources().getColor(R.color.theme_accent);
////
////		// Create material sheet FAB
////		materialSheetFab = new MaterialSheetFab(fab, sheetView, overlay, sheetColor, fabColor);
////
////		// Set material sheet event listener
////		materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
////			@Override
////			public void onShowSheet() {
////				// Save current status bar color
////				statusBarColor = getStatusBarColor();
////				// Set darker status bar color to match the dim overlay
////				setStatusBarColor(getResources().getColor(R.color.theme_primary_dark2));
////			}
////
////			@Override
////			public void onHideSheet() {
////				// Restore status bar color
////				setStatusBarColor(statusBarColor);
////			}
////		});
////
////		// Set material sheet item click listeners
////		findViewById(R.id.fab_sheet_item_recording).setOnClickListener(this);
////		findViewById(R.id.fab_sheet_item_reminder).setOnClickListener(this);
////		findViewById(R.id.fab_sheet_item_photo).setOnClickListener(this);
////		findViewById(R.id.fab_sheet_item_note).setOnClickListener(this);
//	}
//
//	/**
//	 * Called when the selected page changes.
//	 *
//	 * @param selectedPage selected page
//	 */
//	private void updatePage(int selectedPage) {
//		updateFab(selectedPage);
//		updateSnackbar(selectedPage);
//	}
//
//	/**
//	 * Updates the FAB based on the selected page
//	 *
//	 * @param selectedPage selected page
//	 */
//	private void updateFab(int selectedPage) {
//		switch (selectedPage) {
//		case MainPagerAdapter.ALL_POS:
//			materialSheetFab.showFab();
//			break;
//		case MainPagerAdapter.SHARED_POS:
//			materialSheetFab.showFab(0,
//					-getResources().getDimensionPixelSize(R.dimen.snackbar_height));
//			break;
////		case MainPagerAdapter.FAVORITES_POS:
//		default:
//			materialSheetFab.hideSheetThenFab();
//			break;
//		}
//	}
//
//	/**
//	 * Updates the snackbar based on the selected page
//	 *
//	 * @param selectedPage selected page
//	 */
//	private void updateSnackbar(int selectedPage) {
////		View snackbar = findViewById(R.id.snackbar);
////		switch (selectedPage) {
////		case MainPagerAdapter.SHARED_POS:
////			snackbar.setVisibility(View.VISIBLE);
////			break;
////		case MainPagerAdapter.ALL_POS:
//////		case MainPagerAdapter.FAVORITES_POS:
////		default:
////			snackbar.setVisibility(View.GONE);
////			break;
////		}
//	}
//
//	/**
//	 * Toggles opening/closing the drawer.
//	 */
//	private void toggleDrawer() {
//		if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
//			drawerLayout.closeDrawer(GravityCompat.START);
//		} else {
//			drawerLayout.openDrawer(GravityCompat.START);
//		}
//	}
//
//	@Override
//	public void onClick(View v) {
////		Toast.makeText(this, R.string.sheet_item_pressed, Toast.LENGTH_SHORT).show();
////		materialSheetFab.hideSheet();
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.menu_main, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case android.R.id.home:
//			toggleDrawer();
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}
//
//
//
//
//
//
//
//
//
//
//	private int getStatusBarColor() {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			//return getWindow().getStatusBarColor();
//		}
//		return 0;
//	}
//
//	private void setStatusBarColor(int color) {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			//getWindow().setStatusBarColor(color);
//		}
//	}
//
//	public String getLocalIpAddress()
//	{
//		WifiManager wifiManager = (WifiManager)NiceActivity.this. getSystemService(NiceActivity.this.WIFI_SERVICE);
//		int ip = wifiManager.getConnectionInfo().getIpAddress();
//		String realIp = intToIp(ip);
//		return realIp;
//	}
//	public String intToIp(int ip)
//	{
//		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." +((ip >> 16) & 0xFF) +
//				"." + ((ip >> 24) & 0xFF);
//	}
//	private void copyResourceFile(int rid, String targetFile)
//	{
//		InputStream fin = (NiceActivity.this.getApplicationContext()).getResources().openRawResource(rid);
//		FileOutputStream fos = null;
//		int length;
//		try
//		{
//			fos = new FileOutputStream(targetFile);
//			byte[] buffer = new byte[1024];
//			while ((length = fin.read(buffer)) != -1)
//			{
//				fos.write(buffer, 0, length);
//			}
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		finally {
//			if (fin != null)
//			{
//				try
//				{
//					fin.close();
//				}
//				catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			}
//			if (fos != null)
//			{
//				try {
//					fos.close();
//				}catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//	void Config1() {
//		FtpServerFactory serverFactory = new FtpServerFactory();
//		ListenerFactory factory = new ListenerFactory();
//		factory.setPort(port);
//		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
//		String[] str = { "mkdir", ftpConfigDir };
//		try
//		{
//			Process ps = Runtime.getRuntime().exec(str);
//			try
//			{
//				ps.waitFor();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		String filename = ftpConfigDir + "users.properties";
//		System.out.println("ftpConfig is "+filename);
//		File files = new File(filename);
//		userManagerFactory.setFile(files);
//		serverFactory.setUserManager(userManagerFactory.createUserManager());
//
//		try
//		{
//			serverFactory.addListener("default",factory.createListener());
//			FtpServer server = serverFactory.createServer();
//			this.mFtpServer = server;
//			server.start();
//		}
//		catch (FtpException e)
//		{
//			e.printStackTrace();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
//	@Override
//	public void onDestroy()
//	{
//		// TODO Auto-generated method stub
//		super.onDestroy();
//		dialog=null;
//		if (null != mFtpServer)
//		{
//			mFtpServer.stop();
//			mFtpServer = null;
//		}
//	}
//
//	public void getCacheSize(){
//		long size= 0;
//		try {
//			size = FileUtils.getFileSizes(mFileStream.getTaskFile()) +
//					FileUtils.getFileSizes(mFileStream.getTempletFile());
//		} catch (Exception e) {
//			e.printStackTrace();
//			size=-1;
//		}
//
//		if(size==-1){
//			tv_clearcache.setText("清理缓存" + " (未知)");
//		}else{
//			tv_clearcache.setText("清理缓存" + " (" + FileUtils.FormetFileSize(size) + ")");
//		}
//	}
//}
