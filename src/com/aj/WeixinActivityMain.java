package com.aj;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.activity.AboutActivity;
import com.aj.activity.CollectionApplication;
import com.aj.activity.DebugActivity;
import com.aj.activity.DetecedUnit;
import com.aj.activity.LoginActivity;
import com.aj.activity.Navigation.MapActivity;
import com.aj.activity.ThirdModify_WeiXin.Exit;
import com.aj.adapters.DoingChildListAdapter;
import com.aj.adapters.DoingParentListAdapter;
import com.aj.adapters.DoneChildListAdapter;
import com.aj.adapters.DoneParentListAdapter;
import com.aj.adapters.ParentListItem;
import com.aj.bean.TaskInfo;
import com.aj.collection.R;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLE;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFO;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLE;
import com.aj.database.TEMPLETTABLEDao;
import com.aj.http.API;
import com.aj.http.ReturnCode;
import com.aj.http.URLs;
import com.aj.service.MsgService;
import com.aj.tools.ExitApplication;
import com.aj.tools.FileStream;
import com.aj.tools.SPUtils;
import com.aj.tools.T;
import com.aj.tools.Util;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.mobstat.SendStrategyEnum;
import com.baidu.mobstat.StatService;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.library.ExpandableLayoutListView;
import com.jrs.utils.FileUtils;
import com.library.ExpandableLayoutListViewItemListener;

import net.micode.compass.CompassActivity;
import net.micode.notes.ui.NotesListActivity;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeixinActivityMain extends Activity {

    public static WeixinActivityMain instance = null;

    private ViewPager mTabPager;
    private ImageView mTabImg;// 动画图片
    private ImageView mTabImage1, mTabImage2, mTabImage3;
    private LinearLayout mTabL1,mTabL2,mTabL3;
    private TextView mTabText1, mTabText2, mTabText3;
    public TextView badgeView1, badgeView2, badgeView3;
    View view1, view2, view3;
    private int zero = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int one;//单个水平动画位移
    private int two;
    private int three;
    final int duration = 750;
    private LinearLayout mClose;
    private LinearLayout mCloseBtn;
    private View layout;
    private boolean menu_display = false;
    private PopupWindow menuWindow;
    private LayoutInflater inflater;
    //private Button mRightBtn;

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    private NotificationManager mNM;

    RequestQueue queue;
    Context mContext = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new Dialog(WeixinActivityMain.this);
        ExitApplication.getInstance().addActivity(this);
        setContentView(R.layout.main_weixin);
        //init database
        daoSession = ((CollectionApplication) getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        instance = this;

        getActionBar().setDisplayShowHomeEnabled(false);//actionBar不显示程序图标


        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mTabPager = (ViewPager) findViewById(R.id.tabpager);
        mTabPager.setOnPageChangeListener(new MyOnPageChangeListener());

        mTabL1=(LinearLayout) findViewById(R.id.LL_task_doing);
        mTabL2=(LinearLayout) findViewById(R.id.LL_task_done);
        mTabL3=(LinearLayout) findViewById(R.id.LL_task_setting);

        mTabImage1 = (ImageView) findViewById(R.id.img_weixin);
        mTabImage2 = (ImageView) findViewById(R.id.img_address);
        mTabImage3 = (ImageView) findViewById(R.id.img_settings);

        mTabText1 = (TextView) findViewById(R.id.text_task);
        mTabText2 = (TextView) findViewById(R.id.text_task_done);
        mTabText3 = (TextView) findViewById(R.id.text_setting);

        badgeView1 = (TextView) findViewById(R.id.badge_view_task);
        badgeView2 = (TextView) findViewById(R.id.badge_view_task_done);
        badgeView3 = (TextView) findViewById(R.id.badge_view_task_setting);

        //mTab4 = (ImageView) findViewById(R.id.img_settings);
        mTabImg = (ImageView) findViewById(R.id.img_tab_now);
        mTabL1.setOnClickListener(new MyOnClickListener(0));
        mTabL2.setOnClickListener(new MyOnClickListener(1));
        mTabL3.setOnClickListener(new MyOnClickListener(2));

        mTabImage1.setOnClickListener(new MyOnClickListener(0));
        mTabImage2.setOnClickListener(new MyOnClickListener(1));
        mTabImage3.setOnClickListener(new MyOnClickListener(2));
        //mTab4.setOnClickListener(new MyOnClickListener(3));

        Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
        int displayWidth = currDisplay.getWidth();
        int displayHeight = currDisplay.getHeight();
        one = displayWidth / 3; //设置水平动画平移大小
        two = one * 2;
        //three = one*3;



        //将要分页显示的View装入数组中
        LayoutInflater mLi = LayoutInflater.from(this);
        view1 = mLi.inflate(R.layout.weixin_task_content, null);
        view2 = mLi.inflate(R.layout.weixin_history_content, null);
        view3 = mLi.inflate(R.layout.weixin_setting_content, null);
        //View view4 = mLi.inflate(R.layout.setting_content, null);

        //每个页面的view数据
        final ArrayList<View> views = new ArrayList<View>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        //views.add(view4);

        //填充ViewPager的数据适配器
        PagerAdapter mPagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return views.size();
            }

            @Override
            public void destroyItem(View container, int position, Object object) {
                ((ViewPager) container).removeView(views.get(position));
            }

            //@Override
            //public CharSequence getPageTitle(int position) {
            //return titles.get(position);
            //}

            @Override
            public Object instantiateItem(View container, int position) {
                ((ViewPager) container).addView(views.get(position));
                return views.get(position);
            }
        };

        mTabPager.setAdapter(mPagerAdapter);

        initDoContent(view1);
        initDoneContent(view2);
        initSettingUI(view3);

        //start and bind the service
        startMsgService();
        bindMsgService();

        queue = ((CollectionApplication) getApplication()).getRequestQueue(); //init Volley

        //refresh the task status and sampling status
        updateTaskStatus(false);
        updateSamplingStatus(false);

        mobstat(); //百度移动统计
    }


    /**
     * 头标点击监听
     */
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mTabPager.setCurrentItem(index);
        }
    }


    /* 页卡切换监听(原作者:D.Winter)
    */
    public class MyOnPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    mTabImage1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_pressed));
//                    mTabImage1.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(one, 0, 0, 0);
                        mTabImage2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
//                        mTabImage2.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, 0, 0, 0);
                        mTabImage3.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
//                        mTabImage3.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    }
//				else if (currIndex == 3) {
//					animation = new TranslateAnimation(three, 0, 0, 0);
//					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
//				}

                    mTabText1.setTextColor(Color.parseColor("#58b62d"));
                    mTabText2.setTextColor(Color.parseColor("#585858"));
                    mTabText3.setTextColor(Color.parseColor("#585858"));

                    //refresh doing page
                    notifyDoingChildListDataChanged();
                    notifyDoingParentListDataChanged();

                    break;
                case 1:
                    mTabImage2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_pressed));
//                    mTabImage2.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(zero, one, 0, 0);
                        mTabImage1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
//                        mTabImage1.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, one, 0, 0);
                        mTabImage3.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
//                        mTabImage3.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    }
//				else if (currIndex == 3) {
//					animation = new TranslateAnimation(three, one, 0, 0);
//					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
//				}
                    mTabText1.setTextColor(Color.parseColor("#585858"));
                    mTabText2.setTextColor(Color.parseColor("#58b62d"));
                    mTabText3.setTextColor(Color.parseColor("#585858"));

                    //refresh history page
                    notifyDoneChildListDataChanged();
                    notifyDoneParentListDataChanged();

                    break;
                case 2:
                    mTabImage3.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_pressed));
//                    mTabImage3.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(zero, two, 0, 0);
                        mTabImage1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
//                        mTabImage1.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    } else if (currIndex == 1) {
                        animation = new TranslateAnimation(one, two, 0, 0);
                        mTabImage2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
//                        mTabImage2.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(35, getApplicationContext()), ScreenUtils.dip2px(35, getApplicationContext())));
                    }
//				else if (currIndex == 3) {
//					animation = new TranslateAnimation(three, two, 0, 0);
//					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
//				}
                    mTabText1.setTextColor(Color.parseColor("#585858"));
                    mTabText2.setTextColor(Color.parseColor("#585858"));
                    mTabText3.setTextColor(Color.parseColor("#58b62d"));

                    break;
//			case 3:
//				mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_pressed));
//				if (currIndex == 0) {
//					animation = new TranslateAnimation(zero, three, 0, 0);
//					mTabImage1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));
//				} else if (currIndex == 1) {
//					animation = new TranslateAnimation(one, three, 0, 0);
//					mTabImage2.setImageDrawable(getResources().getDrawable(R.drawable.tab_address_normal));
//				}
//				else if (currIndex == 2) {
//					animation = new TranslateAnimation(two, three, 0, 0);
//					mTabImage3.setImageDrawable(getResources().getDrawable(R.drawable.tab_find_frd_normal));
//				}
//				break;
            }
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(150);
            mTabImg.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    //TODO生成对应的菜单,并添加到Menu中
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weixin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.isCheckable()) {
            menuItem.setChecked(true);
        }
        switch (menuItem.getItemId()) {
            case R.id.menu_refresh_task:
                haveNewTask();
                updateSamplingStatus(true);
                updateTaskStatus(true);
                break;
            case R.id.menu_scan_QR:

                break;

            default:
                break;
        }
        return true;
    }
*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {  //获取 back键

            if (menu_display) {         //如果 Menu已经打开 ，先关闭Menu
                menuWindow.dismiss();
                menu_display = false;
            } else {
                Intent intent = new Intent();
                intent.setClass(WeixinActivityMain.this, Exit.class);
                startActivity(intent);
            }
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {   //获取 Menu键
            if (!menu_display) {
                //获取LayoutInflater实例
                inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                //这里的main布局是在inflate中加入的哦，以前都是直接this.setContentView()的吧？呵呵
                //该方法返回的是一个View的对象，是布局中的根
                layout = inflater.inflate(R.layout.main_menu, null);

                //下面我们要考虑了，我怎样将我的layout加入到PopupWindow中呢？？？很简单
                menuWindow = new PopupWindow(layout, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); //后两个参数是width和height
                //menuWindow.showAsDropDown(layout); //设置弹出效果
                //menuWindow.showAsDropDown(null, 0, layout.getHeight());
                menuWindow.showAtLocation(this.findViewById(R.id.mainweixin), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                //如何获取我们main中的控件呢？也很简单
                mClose = (LinearLayout) layout.findViewById(R.id.menu_close);
                mCloseBtn = (LinearLayout) layout.findViewById(R.id.menu_close_btn);


                //下面对每一个Layout进行单击事件的注册吧。。。
                //比如单击某个MenuItem的时候，他的背景色改变
                //事先准备好一些背景图片或者颜色
                mCloseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        //Toast.makeText(Main.this, "退出", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setClass(WeixinActivityMain.this, Exit.class);
                        startActivity(intent);
                        menuWindow.dismiss(); //响应点击事件之后关闭Menu
                    }
                });
                menu_display = true;
            } else {
                //如果当前已经为显示状态，则隐藏起来
                menuWindow.dismiss();
                menu_display = false;
            }

            return false;

        } else
            return true;
    }

    //设置标题栏右侧按钮的作用
    public void btnmainright(View v) {
        //Intent intent = new Intent (WeixinActivityMain.this,MainTopRightDialog.class);
        //startActivity(intent);
        //Toast.makeText(getApplicationContext(), "点击了功能按钮", Toast.LENGTH_LONG).show();
    }

    public void startchat(View v) {      //小黑  对话界面
        //Intent intent = new Intent (WeixinActivityMain.this,ChatActivity.class);
        //startActivity(intent);
        //Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_LONG).show();
    }

    //	public void exit_settings(View v) {                           //退出  伪“对话框”，其实是一个activity
//		Intent intent = new Intent (WeixinActivityMain.this,ExitFromSettings.class);
//		startActivity(intent);
//	 }
    public void btn_shake(View v) {                                   //手机摇一摇
        //Intent intent = new Intent (WeixinActivityMain.this,ShakeActivity.class);
        //startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.WEIXINTASKREFRESHITEM_FROMDO)//刷新列表
        {
//            if (doingParentListView.currentExpandableLayout != null) {
//                ((DoingParentListAdapter) doingParentListView.getAdapter()).updateRightCommitButtonText(doingParentListView.currentExpandableLayout.getTaskID(), doingParentListView.currentExpandableLayout);
//            }
        }
    }

    @Override
    protected void onStop() {
        // Unregister our receiver.
        unregisterReceiver(mReceiver);
        unbindMsgService();
        super.onStop();
    }

    @Override
    public void onResume() {
        bindMsgService();

        IntentFilter filter1 = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, filter1);// just remember unregister it

        //refresh doing page
        notifyDoingChildListDataChanged();
        notifyDoingParentListDataChanged();

        //refresh history page
//        notifyDoneChildListDataChanged();
//        notifyDoneParentListDataChanged();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mNM.cancel(R.string.app_name);

        super.onDestroy();
    }

    private FileStream mFileStream;
    private ExpandableLayoutListView doingParentListView;
    //private ArrayAdapter<String> doingParentArrayAdapter;
    private DoingParentListAdapter doingParentArrayAdapter;
    //private ArrayList<String> doingParentAdapterDataSet=new ArrayList<String>();
    private List<ParentListItem> doingParentAdapterDataSet;

    /**
     * 初始化任务界面
     *
     * @param view
     */
    public void initDoContent(View view) {
        mFileStream = new FileStream(WeixinActivityMain.this);

        doingParentListView = (ExpandableLayoutListView) view.findViewById(R.id.expandablelistview_doingtask);
        doingParentAdapterDataSet = refreshDoingParentListDataSet();
        doingParentArrayAdapter = new DoingParentListAdapter(WeixinActivityMain.this, doingParentAdapterDataSet, doingParentListView, Constant.DOING_PAGE, (CollectionApplication) getApplication());

        doingParentListView.setAdapter(doingParentArrayAdapter);
        doingParentListView.setExpandableLayoutListViewItemListener(new ExpandableLayoutListViewItemListener() {
            @Override
            public void onCollapse() {
            }

            @Override
            public void onExpand() {
                doingParentArrayAdapter.refreshExpandableItemHeight();
            }

            @Override
            public void onCollapsed() {
                doingParentArrayAdapter.refreshExpandableItemHeight();
            }

            @Override
            public void onExpanded() {
                doingParentListView.setSelection(doingParentListView.currentPosition);
            }
        });

        doingParentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//				doingParentListView.currentExpandableLayout = (ExpandableLayoutItem) doingParentListView.getChildAt(i).findViewWithTag(ExpandableLayoutItem.class.getName());
//				doingParentListView.currentExpandableLayout.setSampleFileDir(doingParentListView.getTaskDir().listFiles()[i]);
//				doingParentListView.currentExpandableLayout.initChildListView(doingParentListView, doingParentAdapterDataSet.get(i).getTitle());


            }
        });

        RelativeLayout emptyview = (RelativeLayout) view.findViewById(R.id.taskEmptyView);
        doingParentListView.setEmptyView(emptyview);
        doingParentListView.setDivider(new ColorDrawable(Color.GRAY));
        doingParentListView.setDividerHeight(1);

        //调整listview的高度
//		int totalHeight = 0;
//		for (int i = 0; i < doingParentArrayAdapter.getCount(); i++) {
//			View listItem = doingParentArrayAdapter.getView(i, null, doingParentListView);
//			listItem.measure(0, 0);
//			totalHeight += listItem.getMeasuredHeight();
//		}
//
//		ViewGroup.LayoutParams params = doingParentListView.getLayoutParams();
//		params.height = totalHeight + (doingParentListView.getDividerHeight() * (doingParentArrayAdapter.getCount()-1));
//		//((ViewGroup.MarginLayoutParams)params).setMargins(Dp2Px(this,0), Dp2Px(this,0), Dp2Px(this,0), Dp2Px(this,0));
//		doingParentListView.setLayoutParams(params);


    }


    /**
     * get parent list title
     *
     * @return
     */
    public TextView getHeadText() {
        if (doingParentListView.currentExpandableLayout.headerLayout == null)
            return null;

        return ((TextView) doingParentListView.currentExpandableLayout.headerLayout.getChildAt(0).findViewById(R.id.header_text));
    }

    /**
     * get parent list img in the left ,now it's a narrow
     *
     * @return
     */
    public ImageView getHeadImg() {
        if (doingParentListView.currentExpandableLayout.headerLayout == null)
            return null;
        return ((ImageView) doingParentListView.currentExpandableLayout.headerLayout.getChildAt(0).findViewById(R.id.taskIfOpendImg));
    }

    /**
     * get parent list text in the right
     *
     * @return
     */
    public TextView getHeadRightText() {
        if (doingParentListView.currentExpandableLayout.headerLayout == null)
            return null;
        return ((TextView) doingParentListView.currentExpandableLayout.headerLayout.getChildAt(0).findViewById(R.id.newFlag));
    }

    /**
     * set The narrow closed
     */
    public void setTaskClosedImg() {
        getHeadImg().setImageResource(R.drawable.taskclosed);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(20, getApplicationContext()), ScreenUtils.dip2px(20, getApplicationContext()));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMargins(ScreenUtils.dip2px(5, getApplicationContext()), ScreenUtils.dip2px(5, getApplicationContext()),
                ScreenUtils.dip2px(5, getApplicationContext()), ScreenUtils.dip2px(5, getApplicationContext()));
        getHeadImg().setLayoutParams(params);
    }

    /**
     * set The narrow opened
     */
    public void setTaskOpenedImg() {
        getHeadImg().setImageResource(R.drawable.taskopened);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(20, getApplicationContext()), ScreenUtils.dip2px(20, getApplicationContext()));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMargins(ScreenUtils.dip2px(5, getApplicationContext()), ScreenUtils.dip2px(5, getApplicationContext()),
                ScreenUtils.dip2px(5, getApplicationContext()), ScreenUtils.dip2px(5, getApplicationContext()));
        getHeadImg().setLayoutParams(params);
    }


    private ExpandableLayoutListView doneParentListView;
    //private ArrayAdapter<String> doingParentArrayAdapter;
    private DoneParentListAdapter doneParentArrayAdapter;
    private List<ParentListItem> doneParentAdapterDataSet;

    /**
     * 初始化历史完成任务界面
     *
     * @param view
     */
    public void initDoneContent(View view) {
        if (mFileStream == null)
            mFileStream = new FileStream(WeixinActivityMain.this);

        doneParentListView = (ExpandableLayoutListView) view.findViewById(R.id.expandablelistview_doingtask);
        doneParentAdapterDataSet = refreshDoneParentListDataSet();
        doneParentArrayAdapter = new DoneParentListAdapter(this, doneParentAdapterDataSet, doneParentListView, Constant.HISTORY_PAGE);

        doneParentListView.setAdapter(doneParentArrayAdapter);
        doneParentListView.setExpandableLayoutListViewItemListener(new ExpandableLayoutListViewItemListener() {
            @Override
            public void onCollapse() {

            }

            @Override
            public void onExpand() {

//                doneParentListView.currentExpandableLayout.refreshExpandableItemHeight();//更新内层的高度
                doneParentArrayAdapter.refreshExpandableItemHeight();
//				doingParentListView.smoothScrollToPositionFromTop(doingParentListView.currentPosition, 0);
//				doingParentListView.setSelection(doingParentListView.currentPosition);
            }

            @Override
            public void onCollapsed() {
//                doneParentListView.currentExpandableLayout.refreshExpandableItemHeight();//更新内层的高度
                doneParentArrayAdapter.refreshExpandableItemHeight();

            }

            @Override
            public void onExpanded() {
                doneParentListView.setSelection(doneParentListView.currentPosition);
//				doingParentListView.smoothScrollToPosition(doingParentListView.currentPosition * 150);

            }
        });

        doneParentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//				doingParentListView.currentExpandableLayout = (ExpandableLayoutItem) doingParentListView.getChildAt(i).findViewWithTag(ExpandableLayoutItem.class.getName());
//				doingParentListView.currentExpandableLayout.setSampleFileDir(doingParentListView.getTaskDir().listFiles()[i]);
//				doingParentListView.currentExpandableLayout.initChildListView(doingParentListView, doingParentAdapterDataSet.get(i).getTitle());


            }
        });

        RelativeLayout emptyview = (RelativeLayout) view.findViewById(R.id.taskEmptyView);
        doneParentListView.setEmptyView(emptyview);
        doneParentListView.setDivider(new ColorDrawable(Color.GRAY));
        doneParentListView.setDividerHeight(1);
    }

    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    private LinearLayout unitBtn;
    private LinearLayout dataBtn;
    private LinearLayout clearCacheBtn;

    private LinearLayout versionBtn;
    private LinearLayout toolbox;
    private LinearLayout exitBtn;
    private LinearLayout contactMe;
    private LinearLayout debugInterface;

    private TextView tv_clearcache;
    private TextView user_name, company_name;//抽屉顶部的用户名和公司名

    private FtpServer mFtpServer;
    private int port = 12345;// 端口号
    private String ftpConfigDir = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/ftpConfig/";

    /**
     * 初始化设置界面
     */
    public void initSettingUI(View v) {

        //基本信息
        unitBtn = (LinearLayout) v.findViewById(R.id.unit_btn);
        unitBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(WeixinActivityMain.this, DetecedUnit.class);
                startActivity(i);
            }
        });

        user_name = (TextView) v.findViewById(R.id.user_name);
        company_name = (TextView) v.findViewById(R.id.company_name);

        String login_user = (String) SPUtils.get(this, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE);
        user_name.setText(login_user);
        company_name.setText("所属单位:" + (String) SPUtils.get(this, SPUtils.SAMPLING_COMPANY, "没有填写", SPUtils.USER_INFO));

        //清除缓存
        //显示缓存大小
        tv_clearcache = (TextView) v.findViewById(R.id.clear_cache_tv);

        getCacheSize();

        //清理缓存
        clearCacheBtn = (LinearLayout) v.findViewById(R.id.clear_cache_btn);
        clearCacheBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(WeixinActivityMain.this);

                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                LayoutInflater inflater = (LayoutInflater) WeixinActivityMain.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                RelativeLayout layout = (RelativeLayout) inflater.inflate(
                        R.layout.dialogview_twocheckbox_onebutton, null);
                TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
                TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

                final CheckBox task = (CheckBox) layout.findViewById(R.id.checkbox_task);
                final CheckBox templet = (CheckBox) layout.findViewById(R.id.checkbox_templet);

                LinearLayout LL_task = (LinearLayout) layout.findViewById(R.id.LL_task);
                LinearLayout LL_template = (LinearLayout) layout.findViewById(R.id.LL_templet);

                LL_task.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (task.isChecked()) {
                            task.setChecked(false);
                        } else {
                            task.setChecked(true);
                        }
                    }
                });

                LL_template.setOnClickListener(new View.OnClickListener() {
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
                            Toast.makeText(WeixinActivityMain.this, "没有清理任何内容", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            return;
                        }

                        FileUtils futil = new FileUtils();

                        if (task.isChecked())
                            futil.deleteFile(mFileStream.getTaskFile());

                        if (templet.isChecked())
                            futil.deleteFile(mFileStream.getTempletFile());

                        Toast.makeText(WeixinActivityMain.this, "清理成功！", Toast.LENGTH_LONG).show();

                        getCacheSize();

                        dialog.dismiss();
                    }
                });


                dialog.setContentView(layout);
                dialog.setCancelable(true);
                dialog.show();
            }
        });

        //工具箱
        toolbox = (LinearLayout) v.findViewById(R.id.toolbox_btn);
        toolbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog toolbox_dialog = new Dialog(
                        WeixinActivityMain.this);

                LayoutInflater inflater = (LayoutInflater) WeixinActivityMain.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                RelativeLayout layout = (RelativeLayout) inflater.inflate(
                        R.layout.dialogview_toolbox, null);

                //地图导航
                LinearLayout mapButton = (LinearLayout) layout.findViewById(R.id.map_navi_btn);
                mapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(WeixinActivityMain.this.getApplicationContext(), MapActivity.class);
                        WeixinActivityMain.this.startActivity(intent);
                    }
                });

                //记事本
                LinearLayout minote_btn = (LinearLayout) layout.findViewById(R.id.minote_btn);
                minote_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(WeixinActivityMain.this, NotesListActivity.class));
                    }
                });

                //指南针
                LinearLayout compass_btn = (LinearLayout) layout.findViewById(R.id.compass_btn);
                compass_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(WeixinActivityMain.this, CompassActivity.class));
                    }
                });


                toolbox_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                toolbox_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                toolbox_dialog.setContentView(layout);
                toolbox_dialog.setCancelable(true);
                toolbox_dialog.show();


            }
        });

        contactMe = (LinearLayout) v.findViewById(R.id.contactme_btn);
        contactMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTextToEmail();
            }
        });

        //版本信息
        versionBtn = (LinearLayout) v.findViewById(R.id.vertion_btn);
        versionBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeixinActivityMain.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        //unregist
        exitBtn = (LinearLayout) v.findViewById(R.id.exitapp);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToLoginActivity();
                finish();
            }
        });

        debugInterface = (LinearLayout) v.findViewById(R.id.LL_debug);
        debugInterface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, DebugActivity.class);
                startActivity(intent);
            }
        });
        //数据传输
        dataBtn = (LinearLayout) v.findViewById(R.id.data_btn);
        dataBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                copyResourceFile(R.raw.users, ftpConfigDir + "users.properties");
//                String info = "本机IP：\n" + getLocalIpAddress()
//                        + "\n";
//                File f = new File(ftpConfigDir);
//                if (!f.exists())
//                    f.mkdir();
//                copyResourceFile(R.raw.users, ftpConfigDir + "users.properties");
//                Config1();
//                builder = new AlertDialog.Builder(WeixinActivityMain.this);
//                builder.setTitle("传输数据");
//                builder.setMessage(info + "已启动连接，在断开之前请勿操作。");
//                builder.setCancelable(false);
//                builder.setPositiveButton("断开连接", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        AlertDialog ad;
//                        AlertDialog.Builder ab = new AlertDialog.Builder(WeixinActivityMain.this);
//                        ab.setTitle("温馨提示：");
//                        ab.setMessage("确认断开连接?");
//                        ab.setCancelable(false);
//                        ab.setPositiveButton("是", new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                mFtpServer.stop();
//                                //setupTabs();刷新界面
//                            }
//                        });
//                        ab.setNegativeButton("否", new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                alertDialog.show();
//                            }
//                        });
//                        ad = ab.create();
//                        ad.show();
//                    }
//                });
//                alertDialog = builder.create();
//                alertDialog.show();
                if (mService != null) {
                    haveNewTask();
                }

            }
        });
    }

    /**
     * 给我的邮件发消息
     */
    private void sendTextToEmail() {
        String mEmailAddress = "769776082@qq.com";
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                + mEmailAddress));

        intent.putExtra("subject", "Result of " + getString(R.string.app_name));
        intent.putExtra("body", "你好开发者，我是...");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //TODO 手机中没有邮箱软件，打开反馈界面
            Toast.makeText(getApplicationContext(), "没有检测到邮箱程序，请先安装邮箱（如QQ邮箱）", Toast.LENGTH_LONG).show();
        }
    }

    private Dialog dialog;

    /**
     * 打开GPS提示对话框
     */
    void openGPSDialog() {
        if (dialog.isShowing()) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) WeixinActivityMain.this
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
                WeixinActivityMain.this.finish();
            }
        });


        dialog.setContentView(layout);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void getCacheSize() {
        long size = 0;
        try {
            size = FileUtils.getFileSizes(mFileStream.getTaskFile()) +
                    FileUtils.getFileSizes(mFileStream.getTempletFile());
        } catch (Exception e) {
            e.printStackTrace();
            size = -1;
        }

        if (size == -1) {
            tv_clearcache.setText("清理缓存" + " (未知)");
        } else {
            tv_clearcache.setText("清理缓存" + " (" + FileUtils.FormetFileSize(size) + ")");
        }
    }



    public String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) +
                "." + ((ip >> 24) & 0xFF);
    }

    private void copyResourceFile(int rid, String targetFile) {
        InputStream fin = (WeixinActivityMain.this.getApplicationContext()).getResources().openRawResource(rid);
        FileOutputStream fos = null;
        int length;
        try {
            fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            while ((length = fin.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
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
        String[] str = {"mkdir", ftpConfigDir};
        try {
            Process ps = Runtime.getRuntime().exec(str);
            try {
                ps.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filename = ftpConfigDir + "users.properties";
        System.out.println("ftpConfig is " + filename);
        File files = new File(filename);
        userManagerFactory.setFile(files);
        serverFactory.setUserManager(userManagerFactory.createUserManager());

        try {
            serverFactory.addListener("default", factory.createListener());
            FtpServer server = serverFactory.createServer();
            this.mFtpServer = server;
            server.start();
        } catch (FtpException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(".spms");
        }
    }

    /**
     * dp>px
     *
     * @param context
     * @param dp
     * @return
     */
    public int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 更新外层listview的高度
     */
    public void refreshExpandableListViewHeight() {
//		ViewGroup.LayoutParams params = doingParentListView.getLayoutParams();
//
//		if(doingParentListView.currentExpandableLayout==null){
//			params.height = getTotalheightOfListView();
//		}else{
//			params.height = getTotalheightOfListView()+doingParentListView.currentExpandableLayout.getTotalHeightofItem();
//		}
//
//		doingParentListView.setLayoutParams(params);
    }

    /**
     * 计算外层Item的高度
     *
     * @return
     */
    public int getTotalheightOfListView() {
        //调整listview的高度
        int totalHeight = 0;
        for (int i = 0; i < doingParentArrayAdapter.getCount(); i++) {
            View listItem = doingParentArrayAdapter.getView(i, null, doingParentListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        totalHeight = totalHeight + (doingParentListView.getDividerHeight() * (doingParentArrayAdapter.getCount() - 1));
        return totalHeight;
    }


    /**********************************************************************************
     * ******************************* R E F R E S H *************************************
     * ***********************************************************************************
     */

    public void refreshBadgeView1() {

        final int newTaskSize = taskinfoDao.queryBuilder().
                where(TASKINFODao.Properties.Is_finished.eq(false), TASKINFODao.Properties.Is_new_task.eq(true)).
                orderAsc(TASKINFODao.Properties.TaskID).list().size();

        if (newTaskSize == 0)
            badgeView1.setVisibility(View.GONE);
        else {
            badgeView1.setVisibility(View.VISIBLE);
            badgeView1.setText(String.valueOf(newTaskSize));

        }

    }

    /**
     * 通知子listview的内容变化
     */
    public void notifyDoingChildListDataChanged() {
        if (doingParentListView.currentExpandableLayout != null) {
            refreshDoingChildListData(doingParentArrayAdapter.doingChildrenAdapter);
            doingParentArrayAdapter.refreshExpandableItemHeight();
        }
    }

    /**
     * 通知任务列表的内容变化
     */
    public void notifyDoingParentListDataChanged() {
        List<ParentListItem> newDataSet = refreshDoingParentListDataSet();
        doingParentAdapterDataSet.removeAll(doingParentAdapterDataSet);
        for (int i = 0; i < newDataSet.size(); i++) {
            doingParentAdapterDataSet.add(newDataSet.get(i));
        }
        doingParentArrayAdapter.adapterDataSet = doingParentAdapterDataSet;
        doingParentArrayAdapter.notifyDataSetChanged();
        doingParentArrayAdapter.refreshExpandableItemHeight();

        refreshBadgeView1();
    }

    /**
     * 刷新子listview 的内容Dataset
     */
    public void refreshDoingChildListData(DoingChildListAdapter adapter) {

        //init database
        daoSession = ((CollectionApplication) ((Activity) mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();

        //repare data ----> fileDataSet:all file need to show. templatePositionListSet:all templet postition in fileDataSet
        if (!adapter.dataSet.isEmpty())
            adapter.dataSet.removeAll(adapter.dataSet);

        List<TEMPLETTABLE> templettables = templettableDao.queryBuilder().
                where(TEMPLETTABLEDao.Properties.TaskID.eq(adapter.taskID)).
                orderAsc(TEMPLETTABLEDao.Properties.TempletID).list();

        for (int i = 0; i < templettables.size(); i++) {
            TEMPLETTABLE templettable = templettables.get(i);
            adapter.dataSet.add(templettable);
            List<SAMPLINGTABLE> samplingtables = samplingtableDao.queryBuilder().
                    where(SAMPLINGTABLEDao.Properties.TempletID.eq(templettable.getTempletID()),
                            SAMPLINGTABLEDao.Properties.Check_status.notEq(Constant.S_STATUS_DELETE)).
                    orderAsc(SAMPLINGTABLEDao.Properties.Id).list();
            for (int j = 0; j < samplingtables.size(); j++) {
                adapter.dataSet.add(samplingtables.get(j));
            }
        }

        //notify data changed
        adapter.notifyDataSetChanged();
    }


    /**
     * 更新ParentList head Data 会更新右边的指示器内容
     *
     * @return
     */
    public List<ParentListItem> refreshDoingParentListDataSet() {
        List<ParentListItem> listItems = new ArrayList<ParentListItem>();
        TaskInfo taskInfo = new TaskInfo(WeixinActivityMain.this, Constant.DOING_PAGE);
        //init database
        daoSession = ((CollectionApplication) getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();
        //queryTaskName
        List<TASKINFO> taskinfos = taskinfoDao.queryBuilder().
                where(TASKINFODao.Properties.Is_finished.notEq(true)).
                orderAsc(TASKINFODao.Properties.TaskID).list();

        int leftImg = R.drawable.taskclosed;
        String title = "";
        String rightText = "";

        for (int i = 0; i < taskinfos.size(); i++) {//遍历Templet文件夹下的每个任务
            title = taskinfos.get(i).getTask_name();//设置任务名
            //查询模板数量
            int[] rightCommitButtonNum = taskInfo.getRightCommitButtonNum(taskinfos.get(i).getTaskID());
            if (rightCommitButtonNum[0] == rightCommitButtonNum[1] && rightCommitButtonNum[0] != 0) {
                rightText = getResources().getString(R.string.commit_task);
            } else {
                rightText = "上传" + rightCommitButtonNum[0] + "/模板" + rightCommitButtonNum[1];
            }
            //添加到集合中
            ParentListItem item = new ParentListItem(title, rightText, leftImg);
            listItems.add(item);
        }

        return listItems;
    }


    /**
     * 通知子listview的内容变化
     */
    public void notifyDoneChildListDataChanged() {
        if (doneParentListView.currentExpandableLayout != null) {
            refreshDoneChildListData(doneParentArrayAdapter.doingChildrenAdapter, Constant.HISTORY_PAGE);
            doneParentArrayAdapter.refreshExpandableItemHeight();
        }


    }

    /**
     * 通知任务列表的内容变化
     */
    public void notifyDoneParentListDataChanged() {
        List<ParentListItem> newDataSet = refreshDoneParentListDataSet();
        doneParentAdapterDataSet.removeAll(doneParentAdapterDataSet);
        for (int i = 0; i < newDataSet.size(); i++) {
            doneParentAdapterDataSet.add(newDataSet.get(i));
        }

        doneParentArrayAdapter.notifyDataSetChanged();

    }

    /**
     * 刷新子listview 的内容Dataset
     */
    public void refreshDoneChildListData(DoneChildListAdapter adapter, int pageFlag) {

        //init database
        daoSession = ((CollectionApplication) ((Activity) mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();

        //repare data ----> fileDataSet:all file need to show. templatePositionListSet:all templet postition in fileDataSet
        if (!adapter.dataSet.isEmpty())
            adapter.dataSet.removeAll(adapter.dataSet);

        List<TEMPLETTABLE> templettables = templettableDao.queryBuilder().
                where(TEMPLETTABLEDao.Properties.TaskID.eq(adapter.taskID)).
                orderAsc(TEMPLETTABLEDao.Properties.TempletID).list();

        for (int i = 0; i < templettables.size(); i++) {
            TEMPLETTABLE templettable = templettables.get(i);
            adapter.dataSet.add(templettable);
            List<SAMPLINGTABLE> samplingtables = samplingtableDao.queryBuilder().
                    where(SAMPLINGTABLEDao.Properties.TempletID.eq(templettable.getTempletID())).
                    orderAsc(SAMPLINGTABLEDao.Properties.Id).list();
            for (int j = 0; j < samplingtables.size(); j++) {
                adapter.dataSet.add(samplingtables.get(j));
            }
        }

        //notify data changed
        adapter.notifyDataSetChanged();
    }

    /**
     * 更新ParentList head Data 会更新右边的指示器内容
     *
     * @return
     */
    public List<ParentListItem> refreshDoneParentListDataSet() {

        List<ParentListItem> listItems = new ArrayList<ParentListItem>();
        TaskInfo taskInfo = new TaskInfo(WeixinActivityMain.this, Constant.DOING_PAGE);

        //queryTaskName
        List<TASKINFO> taskinfos = taskinfoDao.queryBuilder().
                where(TASKINFODao.Properties.Is_finished.eq(true)).
                orderAsc(TASKINFODao.Properties.TaskID).list();

        int leftImg = R.drawable.taskclosed;
        String title = "";
        String rightText = "";

        for (int i = 0; i < taskinfos.size(); i++) {//遍历Templet文件夹下的每个任务
            title = taskinfos.get(i).getTask_name();//设置任务名
            //查询模板数量
            int[] rightCommitButtonNum = taskInfo.getRightCommitButtonNum(taskinfos.get(i).getTaskID());
            if (rightCommitButtonNum[0] == rightCommitButtonNum[1] && rightCommitButtonNum[0] != 0) {
                rightText = getResources().getString(R.string.commit_task);
            } else {
                rightText = "上传" + rightCommitButtonNum[0] + "/模板" + rightCommitButtonNum[1];
            }
            //添加到集合中
            ParentListItem item = new ParentListItem(title, rightText, leftImg);
            listItems.add(item);
        }

        return listItems;

    }

    /**
     * 百度移动统计
     */
    void mobstat() {
        // 设置AppKey
        StatService.setAppKey("c90777801b"); // appkey必须在mtj网站上注册生成，该设置建议在AndroidManifest.xml中填写，代码设置容易丢失

        /*
         * 设置渠道的推荐方法。该方法同setAppChannel（String）， 如果第三个参数设置为true（防止渠道代码设置会丢失的情况），将会保存该渠道，每次设置都会更新保存的渠道，
         * 如果之前的版本使用了该函数设置渠道，而后来的版本需要AndroidManifest.xml设置渠道，那么需要将第二个参数设置为空字符串,并且第三个参数设置为false即可。
         * appChannel是应用的发布渠道，不需要在mtj网站上注册，直接填写就可以 该参数也可以设置在AndroidManifest.xml中
         */
        // StatService.setAppChannel(this, "RepleceWithYourChannel", true);
        // 测试时，可以使用1秒钟session过期，这样不断的间隔1S启动退出会产生大量日志。
        StatService.setSessionTimeOut(30);
        // setOn也可以在AndroidManifest.xml文件中填写，BaiduMobAd_EXCEPTION_LOG，打开崩溃错误收集，默认是关闭的
        StatService.setOn(this, StatService.EXCEPTION_LOG);
        /*
         * 设置启动时日志发送延时的秒数<br/> 单位为秒，大小为0s到30s之间<br/> 注：请在StatService.setSendLogStrategy之前调用，否则设置不起作用
         *
         * 如果设置的是发送策略是启动时发送，那么这个参数就会在发送前检查您设置的这个参数，表示延迟多少S发送。<br/> 这个参数的设置暂时只支持代码加入，
         * 在您的首个启动的Activity中的onCreate函数中使用就可以。<br/>
         */
        StatService.setLogSenderDelayed(0);
        /*
         * 用于设置日志发送策略<br /> 嵌入位置：Activity的onCreate()函数中 <br />
         *
         * 调用方式：StatService.setSendLogStrategy(this,SendStrategyEnum. SET_TIME_INTERVAL, 1, false); 第二个参数可选：
         * SendStrategyEnum.APP_START SendStrategyEnum.ONCE_A_DAY SendStrategyEnum.SET_TIME_INTERVAL 第三个参数：
         * 这个参数在第二个参数选择SendStrategyEnum.SET_TIME_INTERVAL时生效、 取值。为1-24之间的整数,即1<=rtime_interval<=24，以小时为单位 第四个参数：
         * 表示是否仅支持wifi下日志发送，若为true，表示仅在wifi环境下发送日志；若为false，表示可以在任何联网环境下发送日志
         */
        StatService.setSendLogStrategy(this, SendStrategyEnum.APP_START, 0, true);
        // 调试百度统计SDK的Log开关，可以在Eclipse中看到sdk打印的日志，发布时去除调用，或者设置为false
        StatService.setDebugOn(true);
    }

    /**
     * 广播接收器
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {//每分钟的广播 1.if have new task
                updateTaskStatus(false);
                updateSamplingStatus(false);
            }
        }
    };

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher1);
        mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        mBuilder.setContentText("您有一个新任务!");

        Intent intent = new Intent(this, WeixinActivityMain.class);
        mBuilder.setContentIntent(PendingIntent.getBroadcast(this, 0, intent, 0));
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());

//        CharSequence text = getText(R.string.app_name);
//        Notification notification = new Notification(
//                R.drawable.ic_launcher1, null, System.currentTimeMillis());
//        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
//        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
//        Intent pedometerIntent = new Intent();
//        pedometerIntent.setComponent(new ComponentName(this, WeixinActivityMain.class));
//        pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                pedometerIntent, 0);
//        notification
//                .setLatestEventInfo(this, text, "您有一个新任务!", contentIntent);
//
//        mNM.notify(R.string.app_name, notification);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String s) {
        CharSequence text = getText(R.string.app_name);
        Notification notification = new Notification(
                R.drawable.ic_launcher1, null, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        Intent pedometerIntent = new Intent();
        pedometerIntent.setComponent(new ComponentName(this, WeixinActivityMain.class));
        pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                pedometerIntent, 0);
//        notification
//                .setLatestEventInfo(this, text, s, contentIntent);

        mNM.notify(R.string.app_name, notification);
    }


    /***************************
     * Service
     *******************************/

    private MsgService.ICallback mCallback = new MsgService.ICallback() {

        @Override
        public void haveNewTask() {
            notifyDoingChildListDataChanged();
            notifyDoingParentListDataChanged();
        }

        @Override
        public Context getWeixinActitityContext() {
            return WeixinActivityMain.this;
        }

        @Override
        public void returnToLoginActivity() {
            ((WeixinActivityMain) mContext).returnToLoginActivity();
        }

        @Override
        public void refreshBadgeView1_callback() {
            refreshBadgeView1();
        }

        @Override
        public void refreshDoingChildListView() {
            notifyDoingChildListDataChanged();
        }
    };

    private MsgService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((MsgService.MsgBinder) service).getService();

            mService.registerCallback(mCallback);


        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };


    private void startMsgService() {

        startService(new Intent(WeixinActivityMain.this, MsgService.class));

    }

    private void bindMsgService() {
        bindService(new Intent(WeixinActivityMain.this, MsgService.class), mConnection,
                Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindMsgService() {
        if (mService != null)
            unbindService(mConnection);
    }

    private void stopMsgService() {
        stopService(new Intent(WeixinActivityMain.this, MsgService.class));
        if (mService != null) {
            mService.stopSelf();
        }

    }

    private void returnToLoginActivity() {
        stopMsgService();
        Util.stopLongTermService(getApplicationContext());
        SPUtils.put(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE);
        SPUtils.put(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE);
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra("needTurnToMain", false);
        startActivity(intent);
        finish();
    }


    /************************接口*********************************/

    /**
     * 检查是否有新任务
     */
    public void haveNewTask() {
        stopMsgService();

        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage("检查新任务中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                try {

                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String result = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {//connected
                        if (result.equals(URLs.RESULT_NEWTASK)) {
                            // 1.send msg to main activity 2.save to sharedPreference 3.show a notification
                            getNewTask();
                        } else if (result.equals(URLs.RESULT_NOTHING)) {
                            // don't have new task. just TOast
                            Toast.makeText(mContext, "没有新任务", Toast.LENGTH_LONG).show();
                        } else
                            Log.e("XXXXXXX", "接收到不该出现的结果");
                    } else {
                        new ReturnCode(getApplicationContext(), errorCode, true);
                        if (errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT) || errorCode.equals(ReturnCode.PASSWORD_INVALIDE)) {
                            returnToLoginActivity();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("XXXXXXXX", "havaNewTask 返回值出问题");
                    startMsgService();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                startMsgService();
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                Log.e("HaveNewTaskFail", volleyError.toString());
            }
        };
        StringRequest stringRequest = API.haveNewTask(listener, errorListener
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE));
        queue.add(stringRequest);
    }

    /**
     * get New Task
     */
    public void getNewTask() {
        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage("接受新任务中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //      Log.e("GetNewTaskSuc", s);
                try {

                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String result = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {//connected
                        SPUtils.put(mContext, SPUtils.RECEIVED_TASK, result, SPUtils.TEMPORARY_SAVE);
                        setReceived();
                    } else {
                        new ReturnCode(getApplicationContext(), errorCode, true);
                        if (errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT) || errorCode.equals(ReturnCode.PASSWORD_INVALIDE)) {
                            returnToLoginActivity();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    startMsgService();
                }
                progressDialog.dismiss();

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                startMsgService();
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                Log.e("GetNewTaskFail", volleyError.toString());
            }
        };
        StringRequest stringRequest = API.getNewTask(listener, errorListener
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE));
        queue.add(stringRequest);
    }

    /**
     * tell server received the task
     */
    public void setReceived() {
        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage("存入数据库中...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                startMsgService();
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String result = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {//connected
                        if (result.equals(URLs.RESULT_RECEIVEDOK)) {
                            //TODO get new task  1.send msg to main activity 2.save to sharedPreference 3.show a notification
                            String tasks = (String) SPUtils.get(mContext, SPUtils.RECEIVED_TASK, "", SPUtils.TEMPORARY_SAVE);

                            if (tasks.isEmpty()) {
                                Log.e("XXXXXXXX", "没有获取到任何东西，不应该为空，检查程序");
                                return;
                            }

                            JSONArray taskJsonArray = new JSONArray(tasks);
                            for (int i = 0; i < taskJsonArray.length(); i++) {
                                JSONObject taskJsonObject = taskJsonArray.getJSONObject(i);
                                String taskID = taskJsonObject.getString(URLs.KEY_TASKID);
                                String taskName = taskJsonObject.getString(URLs.KEY_TASKNAME);
                                String taskLetter = taskJsonObject.getString(URLs.KEY_TASK_INI_LETTER);
                                String taskDes = taskJsonObject.getString(URLs.KEY_TASKDISCRIPTION);
                                String taskCont = taskJsonObject.getString(URLs.KEY_TASKCONT);
                                String sampling = taskJsonObject.getString(URLs.KEY_SAMPLING);//定点采样的抽样单

                                //任务id已存在，则不存入数据
                                if (taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list().size() != 0) {
                                    T.showShort(getApplicationContext(), "任务已存在！");
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    return;
                                }

                                //insert task
                                TASKINFO taskinfo = new TASKINFO(Long.valueOf(taskID), taskName, taskLetter, false, true, System.currentTimeMillis(), taskDes);
                                taskinfoDao.insertOrReplace(taskinfo);

                                //insert Templet
                                TEMPLETTABLE templettable = new TEMPLETTABLE(null, taskinfo.getTaskID(), taskinfo.getTask_name(), taskCont, System.currentTimeMillis());
                                templettableDao.insertOrReplace(templettable);

                                //定点采样 insert sampling
                                JSONArray samplingsArray = new JSONArray(sampling);


//                                progressDialog.setMax(samplingsArray.length());
//                                progressDialog.setProgress(0);
//                                progressDialog.show();


                                for (int j = 0; j < samplingsArray.length(); j++) {
//                                    progressDialog.setProgress(j + 1);
//                                    if (j + 1 == samplingsArray.length())
//                                        progressDialog.dismiss();

                                    // TODO 从抽样单Json中读取样品信息，如果为空，则应该显示默认“未填写”字样
                                    String samplingID = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLINGID);
                                    String samplingCont = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLINGCONT);
                                    String samplingName = samplingsArray.getJSONObject(j).getString(URLs.KEY_ITEMS);
                                    String samplingNum = samplingsArray.getJSONObject(j).getString(URLs.KEY_ITEMSID);
                                    String companyAddress = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLING_COMPANY_NAME);
                                    String mediaFolderChild = Util.getSamplingNum(mContext, taskinfo);

                                    SAMPLINGTABLE samplingtable = new SAMPLINGTABLE(null, Long.valueOf(taskID), templettable.getTempletID(), samplingName + "-" + samplingNum, companyAddress,
                                            samplingCont, mediaFolderChild, false, false, true, false, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(),
                                            null, Long.valueOf(samplingID), null, null, null,mediaFolderChild);


                                    samplingtableDao.insertOrReplace(samplingtable);
                                }


                            }

                            SPUtils.put(mContext, SPUtils.RECEIVED_TASK, "", SPUtils.TEMPORARY_SAVE);

                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                            //show a notification
                            showNotification();
                            refreshBadgeView1();
                            notifyDoingChildListDataChanged();
                            notifyDoingParentListDataChanged();
                        } else {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                            new ReturnCode(getApplicationContext(), errorCode, true);
                            if (errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT) || errorCode.equals(ReturnCode.PASSWORD_INVALIDE)) {
                                returnToLoginActivity();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                startMsgService();
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                Log.e("Received", volleyError.toString());
            }
        };
        StringRequest stringRequest = API.setTaskReceived(listener, errorListener
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE));
        queue.add(stringRequest);
    }

    /**
     * 更新抽样单审核Status
     * server return like this:
     * {error:0,msg:[{sid:01,type:'0'},{sid:02,type:'1'}]}
     */
    public void updateSamplingStatus(final boolean showDialog) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        if (showDialog) {
            progressDialog.setMessage("正在刷新任务状态...");
            progressDialog.show();
        }
        JSONArray jsonArray = new JSONArray();
        List<SAMPLINGTABLE> allSamplingtables = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.Sid_of_server.isNotNull()).orderAsc().list();
        try {
            for (int i = 0; i < allSamplingtables.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("sid", allSamplingtables.get(i).getSid_of_server());
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (showDialog)
                    progressDialog.dismiss();
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String content = resultJson.getString(URLs.KEY_MESSAGE);

                    JSONObject jsonObjectSamStatus;
                    Long sid;
                    int status;
                    if (errorCode.equals(ReturnCode.Code0)) {
                        JSONArray jsonArraySamsStatus = new JSONArray(content);

                        List<SAMPLINGTABLE> samplingtables = samplingtableDao.queryBuilder().
                                where(SAMPLINGTABLEDao.Properties.Sid_of_server.isNotNull()).orderAsc().list();

                        if (samplingtables.size() != jsonArraySamsStatus.length()) {
                            //TODO 将返回的多余的抽样单删掉 并提示用户“服务器删除了**张抽样单，本地也将删除”
                            Log.e("XXXXXXX", "WeixinActivity.java 查询任务和返回任务对应数量不同");
                            return;
                        }
                        for (int i = 0; i < samplingtables.size(); i++) {

                            jsonObjectSamStatus = jsonArraySamsStatus.getJSONObject(i);
                            status = Integer.valueOf(jsonObjectSamStatus.getString("type"));
                            samplingtables.get(i).setCheck_status(status);

                        }
                        samplingtableDao.insertOrReplaceInTx(samplingtables);

                        if (doingParentListView.currentExpandableLayout != null) {
                            doingParentArrayAdapter.doingChildrenAdapter.notifyDataSetChanged();
                        }
                        notifyDoingChildListDataChanged();

                        if (showDialog) {
                            Toast.makeText(getApplicationContext(), "任务状态更新成功", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        new ReturnCode(getApplicationContext(), errorCode, true);
                        if (errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT) || errorCode.equals(ReturnCode.PASSWORD_INVALIDE))
                            returnToLoginActivity();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (showDialog)
                    progressDialog.dismiss();
                Log.e("getSamStatusFail", volleyError.toString());
                if (showDialog) {
                    Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                }
            }
        };
        StringRequest stringRequest = API.getSamplingStatus(listener, errorListener,
                (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE),
                (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE), jsonArray.toString());

        queue.add(stringRequest);
    }

    /**
     * 更新任务状态
     * server return like this:
     * {error:0,msg:[{tid:01,type:'0'},{tid:02,type:'1'}]}
     */
    public void updateTaskStatus(final boolean showDialog) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        if (showDialog) {
            progressDialog.setMessage("正在刷新任务状态...");
            progressDialog.show();
        }
        JSONArray jsonArray = new JSONArray();
        final List<TASKINFO> alltaskinfos = taskinfoDao.queryBuilder().
                where(TASKINFODao.Properties.TaskID.isNotNull()).orderAsc().list();
        try {
            for (int i = 0; i < alltaskinfos.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tid", alltaskinfos.get(i).getTaskID());
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (showDialog)
                    progressDialog.dismiss();
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String content = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {
                        JSONArray jsonArrayTasksStatus = new JSONArray(content);
                        JSONObject jsonObjectTaskStatus;
                        Long tid;
                        int status;

                        if (alltaskinfos.size() != jsonArrayTasksStatus.length())
                            Log.e("XXXXXXX", "WeixinActivity.java 查询任务和返回任务对应数量不同");

                        for (int i = 0; i < alltaskinfos.size(); i++) {

                            jsonObjectTaskStatus = jsonArrayTasksStatus.getJSONObject(i);
                            status = Integer.valueOf(jsonObjectTaskStatus.getString("type"));
                            boolean isFinish = true;
                            if (status == Constant.TASK_DOING)
                                isFinish = false;
                            else if (status == Constant.TASK_DONE)
                                isFinish = true;
                            alltaskinfos.get(i).setIs_finished(isFinish);

                        }
                        taskinfoDao.insertOrReplaceInTx(alltaskinfos);//update database

                        /*for (int i = 0; i < jsonArrayTasksStatus.length(); i++) {
                            //get the tid and status
                            JSONObject jsonObjectTaskStatus = jsonArrayTasksStatus.getJSONObject(i);
                            Long tid = Long.valueOf(jsonObjectTaskStatus.getString("tid"));
                            int status = Integer.valueOf(jsonObjectTaskStatus.getString("type"));
                            boolean isFinish = true;
                            if (status == Constant.TASK_DOING)
                                isFinish = false;
                            else if (status == Constant.TASK_DONE)
                                isFinish = true;

                            //query task
                            List<TASKINFO> tasksinfo = taskinfoDao.queryBuilder().
                                    where(TASKINFODao.Properties.TaskID.eq(tid)).orderAsc().list();

                            String taskName="";
                            for (int j = 0; j < tasksinfo.size(); j++) {
                                tasksinfo.get(j).setIs_finished(isFinish);//set task status
                                if (taskName.isEmpty())
                                    taskName = taskName + tasksinfo.get(j).getTask_name();
                                else
                                    taskName = taskName + "," + tasksinfo.get(j).getTask_name();
                            }

//                            if(status==Constant.TASK_DONE)
//                                showNotification("任务"+taskName+"已完成！");

                            taskinfoDao.insertOrReplaceInTx(tasksinfo);//update database
                        }*/


                        if (doingParentListView.currentExpandableLayout != null) {
                            doingParentArrayAdapter.doingChildrenAdapter.notifyDataSetChanged();
                        }

                        List<TASKINFO> taskinfos = taskinfoDao.queryBuilder().
                                where(TASKINFODao.Properties.Is_finished.notEq(true)).
                                orderAsc(TASKINFODao.Properties.TaskID).list();
                        if (doingParentListView.currentPosition >= taskinfos.size()) {
                            doingParentListView.currentPosition = taskinfos.size() - 1;
                        }
                        if (doingParentListView.currentPosition < 0)
                            doingParentListView.currentPosition = 0;

                        notifyDoingParentListDataChanged();

                    } else {
                        new ReturnCode(getApplicationContext(), errorCode, true);
                        if (errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT) || errorCode.equals(ReturnCode.PASSWORD_INVALIDE))
                            returnToLoginActivity();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (showDialog)
                    progressDialog.dismiss();
                Log.e("getTaskStatusFail", volleyError.toString());
                if (showDialog) {
                    Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                }
            }
        };
        StringRequest stringRequest = API.getTaskStatus(listener, errorListener,
                (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE),
                (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE), jsonArray.toString());

        queue.add(stringRequest);
    }


}
    
    

