package com.aj


import android.app.*
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.NotificationCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import com.aj.collection.R
import com.aj.collection.activity.*
import com.aj.collection.activity.Navigation.MapActivity
import com.aj.collection.activity.ThirdModify_WeiXin.Exit
import com.aj.collection.activity.http.API
import com.aj.collection.activity.http.ReturnCode
import com.aj.collection.activity.http.URLs
import com.aj.collection.activity.service.MsgService
import com.aj.collection.activity.tools.*
import com.aj.collection.adapters.TaskAdapter
import com.aj.collection.adapters.TaskData
import com.aj.collection.bean.Sheet
import com.aj.database.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.baidu.mobstat.SendStrategyEnum
import com.baidu.mobstat.StatService
import com.jrs.utils.FileUtils
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import net.micode.compass.CompassActivity
import net.micode.notes.ui.NotesListActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onClick
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class WeixinActivityMain : Activity() {

    private var mTabPager: ViewPager? = null
    private var mTabImg: ImageView? = null// 动画图片
    private var mTabImage1: ImageView? = null
    private var mTabImage2: ImageView? = null
    private var mTabImage3: ImageView? = null
    private var mTabL1: LinearLayout? = null
    private var mTabL2: LinearLayout? = null
    private var mTabL3: LinearLayout? = null
    private var mTabText1: TextView? = null
    private var mTabText2: TextView? = null
    private var mTabText3: TextView? = null
    var badgeView1: TextView? = null
    var badgeView2: TextView? = null
    var badgeView3: TextView? = null
    internal var view1: View? = null
    internal var view2: View? = null
    internal var view3: View? = null
    private val zero = 0// 动画图片偏移量
    private var currIndex = 0// 当前页卡编号
    private var one: Int = 0//单个水平动画位移
    private var two: Int = 0
    private var mClose: LinearLayout? = null
    private var mCloseBtn: LinearLayout? = null
    private var layout: View? = null
    private var menu_display = false
    private var menuWindow: PopupWindow? = null
    private var inflater: LayoutInflater? = null
    //database part
    private var daoSession: DaoSession? = null
    private var taskinfoDao: TASKINFODao? = null
    private var templettableDao: TEMPLETTABLEDao? = null
    private var samplingtableDao: SAMPLINGTABLEDao? = null

    private var mNM: NotificationManager? = null

    internal var queue: RequestQueue? = null
    private var getNewTaskRequest: StringRequest? = null
    private var setTaskReceivedRequest: StringRequest? = null
    private var getSamplingStatusRequest: StringRequest? = null
    private var getTaskStatusRequest: StringRequest? = null
    internal var mContext: Context = this

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ExitApplication.getInstance().addActivity(this)
        setContentView(R.layout.main_weixin)
        //init database
        daoSession = (application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession!!.taskinfoDao
        templettableDao = daoSession!!.templettableDao
        samplingtableDao = daoSession!!.samplingtableDao
        //启动activity时不自动弹出软键盘
        window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        instance = this

        actionBar!!.setDisplayShowHomeEnabled(false)//actionBar不显示程序图标


        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mTabPager = findViewById(R.id.tabpager) as ViewPager
        mTabPager!!.setOnPageChangeListener(MyOnPageChangeListener())

        mTabL1 = findViewById(R.id.LL_task_doing) as LinearLayout
        mTabL2 = findViewById(R.id.LL_task_done) as LinearLayout
        mTabL3 = findViewById(R.id.LL_task_setting) as LinearLayout

        mTabImage1 = findViewById(R.id.img_weixin) as ImageView
        mTabImage2 = findViewById(R.id.img_address) as ImageView
        mTabImage3 = findViewById(R.id.img_settings) as ImageView

        mTabText1 = findViewById(R.id.text_task) as TextView
        mTabText2 = findViewById(R.id.text_task_done) as TextView
        mTabText3 = findViewById(R.id.text_setting) as TextView

        badgeView1 = findViewById(R.id.badge_view_task) as TextView
        badgeView2 = findViewById(R.id.badge_view_task_done) as TextView
        badgeView3 = findViewById(R.id.badge_view_task_setting) as TextView

        mTabImg = findViewById(R.id.img_tab_now) as ImageView
        mTabL1!!.setOnClickListener(MyOnClickListener(0))
        mTabL2!!.setOnClickListener(MyOnClickListener(1))
        mTabL3!!.setOnClickListener(MyOnClickListener(2))

        mTabImage1!!.setOnClickListener(MyOnClickListener(0))
        mTabImage2!!.setOnClickListener(MyOnClickListener(1))
        mTabImage3!!.setOnClickListener(MyOnClickListener(2))

        val currDisplay = windowManager.defaultDisplay//获取屏幕当前分辨率
        val displayWidth = currDisplay.width
        val displayHeight = currDisplay.height
        one = displayWidth / 3 //设置水平动画平移大小
        two = one * 2
        //three = one*3;


        //将要分页显示的View装入数组中
        val mLi = LayoutInflater.from(this)
        view1 = mLi.inflate(R.layout.weixin_task_content, null)
        view2 = mLi.inflate(R.layout.weixin_history_content, null)
        view3 = mLi.inflate(R.layout.weixin_setting_content, null)
        //View view4 = mLi.inflate(R.layout.setting_content, null);

        //每个页面的view数据
        val views = ArrayList<View>()
        views.add(view1!!)
        views.add(view2!!)
        views.add(view3!!)
        //views.add(view4);

        //填充ViewPager的数据适配器
        val mPagerAdapter = object : PagerAdapter() {

            override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
                return arg0 === arg1
            }

            override fun getCount(): Int {
                return views.size
            }

            override fun destroyItem(container: View?, position: Int, `object`: Any?) {
                (container as ViewPager).removeView(views.get(position))
            }

            override fun instantiateItem(container: View?, position: Int): Any {
                (container as ViewPager).addView(views.get(position))
                return views.get(position)
            }
        }

        mTabPager!!.adapter = mPagerAdapter

        initDoContent(view1!!)
        initDoneContent(view2!!)
        initSettingUI(view3!!)

        //start and bind the service
        startMsgService()
        bindMsgService()

        queue = (application as CollectionApplication).requestQueue //init Volley

        //refresh the task status and sampling status
        updateTaskStatus(false)
        updateSamplingStatus(false)

        mobstat() //百度移动统计
    }


    /**
     * 头标点击监听
     */
    inner class MyOnClickListener(i: Int) : View.OnClickListener {
        private var index = 0

        init {
            index = i
        }

        override fun onClick(v: View) {
            mTabPager!!.currentItem = index
        }
    }


    /* 页卡切换监听(原作者:D.Winter)
       */
    inner class MyOnPageChangeListener : OnPageChangeListener {
        override fun onPageSelected(arg0: Int) {
            var animation: Animation? = null
            when (arg0) {
                0 -> {
                    mTabImage1!!.setImageDrawable(resources.getDrawable(R.drawable.tab_weixin_pressed))
                    if (currIndex == 1) {
                        animation = TranslateAnimation(one.toFloat(), 0f, 0f, 0f)
                        mTabImage2!!.setImageDrawable(resources.getDrawable(R.drawable.tab_address_normal))
                    } else if (currIndex == 2) {
                        animation = TranslateAnimation(two.toFloat(), 0f, 0f, 0f)
                        mTabImage3!!.setImageDrawable(resources.getDrawable(R.drawable.tab_settings_normal))
                    }

                    mTabText1!!.setTextColor(Color.parseColor("#58b62d"))
                    mTabText2!!.setTextColor(Color.parseColor("#585858"))
                    mTabText3!!.setTextColor(Color.parseColor("#585858"))

                    //refresh doing page
                    refreshDoingTaskData(showProgDialog = false)
                }
                1 -> {
                    mTabImage2!!.setImageDrawable(resources.getDrawable(R.drawable.tab_address_pressed))
                    if (currIndex == 0) {
                        animation = TranslateAnimation(zero.toFloat(), one.toFloat(), 0f, 0f)
                        mTabImage1!!.setImageDrawable(resources.getDrawable(R.drawable.tab_weixin_normal))
                    } else if (currIndex == 2) {
                        animation = TranslateAnimation(two.toFloat(), one.toFloat(), 0f, 0f)
                        mTabImage3!!.setImageDrawable(resources.getDrawable(R.drawable.tab_settings_normal))
                    }
                    mTabText1!!.setTextColor(Color.parseColor("#585858"))
                    mTabText2!!.setTextColor(Color.parseColor("#58b62d"))
                    mTabText3!!.setTextColor(Color.parseColor("#585858"))

                    //refresh history page
                    refreshDoneTaskData(showProgDialog = true)
                }
                2 -> {
                    mTabImage3!!.setImageDrawable(resources.getDrawable(R.drawable.tab_settings_pressed))
                    if (currIndex == 0) {
                        animation = TranslateAnimation(zero.toFloat(), two.toFloat(), 0f, 0f)
                        mTabImage1!!.setImageDrawable(resources.getDrawable(R.drawable.tab_weixin_normal))
                    } else if (currIndex == 1) {
                        animation = TranslateAnimation(one.toFloat(), two.toFloat(), 0f, 0f)
                        mTabImage2!!.setImageDrawable(resources.getDrawable(R.drawable.tab_address_normal))
                    }
                    mTabText1!!.setTextColor(Color.parseColor("#585858"))
                    mTabText2!!.setTextColor(Color.parseColor("#585858"))
                    mTabText3!!.setTextColor(Color.parseColor("#58b62d"))
                }
            }
            currIndex = arg0
            animation!!.fillAfter = true// True:图片停在动画结束位置
            animation.duration = 150
            mTabImg!!.startAnimation(animation)
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

        override fun onPageScrollStateChanged(arg0: Int) {}
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {  //获取 back键

            if (menu_display) {         //如果 Menu已经打开 ，先关闭Menu
                menuWindow!!.dismiss()
                menu_display = false
            } else {
                val intent = Intent()
                intent.setClass(this@WeixinActivityMain, Exit::class.java)
                startActivity(intent)
            }
            return false
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {   //获取 Menu键
            if (!menu_display) {
                //获取LayoutInflater实例
                inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                //这里的main布局是在inflate中加入的哦，以前都是直接this.setContentView()的吧？呵呵
                //该方法返回的是一个View的对象，是布局中的根
                layout = inflater!!.inflate(R.layout.main_menu, null)

                //下面我们要考虑了，我怎样将我的layout加入到PopupWindow中呢很简单
                menuWindow = PopupWindow(layout, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT) //后两个参数是width和height
                //menuWindow.showAsDropDown(layout); //设置弹出效果
                //menuWindow.showAsDropDown(null, 0, layout.getHeight());
                menuWindow!!.showAtLocation(this.findViewById(R.id.mainweixin), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0) //设置layout在PopupWindow中显示的位置
                //如何获取我们main中的控件呢？也很简单
                mClose = layout!!.findViewById(R.id.menu_close) as LinearLayout
                mCloseBtn = layout!!.findViewById(R.id.menu_close_btn) as LinearLayout


                //下面对每一个Layout进行单击事件的注册吧。。。
                //比如单击某个MenuItem的时候，他的背景色改变
                //事先准备好一些背景图片或者颜色
                mCloseBtn!!.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(arg0: View) {
                        //Toast.makeText(Main.this, "退出", Toast.LENGTH_LONG).show();
                        val intent = Intent()
                        intent.setClass(this@WeixinActivityMain, Exit::class.java)
                        startActivity(intent)
                        menuWindow!!.dismiss() //响应点击事件之后关闭Menu
                    }
                })
                menu_display = true
            } else {
                //如果当前已经为显示状态，则隐藏起来
                menuWindow!!.dismiss()
                menu_display = false
            }

            return false

        } else
            return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.WEIXINTASKREFRESHITEM_FROMDO)
        //刷新列表
        {

        }
    }

    override fun onStop() {
        // Unregister our receiver.
        unregisterReceiver(timeTickReceiver)
        unbindMsgService()
        cancleRequestFromQueue()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        doingTaskAdapter?.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        doingTaskAdapter?.onRestoreInstanceState(savedInstanceState)
    }
    public override fun onResume() {
        bindMsgService()

        val filter1 = IntentFilter(Intent.ACTION_TIME_TICK)
        registerReceiver(timeTickReceiver, filter1)// just remember unregister it

        // refresh data
        refreshDoingTaskData(showProgDialog = false)
        refreshDoneTaskData(showProgDialog = false)

        //refresh history page
        //        notifyDoneChildListDataChanged();
        //        notifyDoneParentListDataChanged();
        super.onResume()
    }

    /**
     * 更新已完成的任务数据列表
     */
    fun refreshDoneTaskData(showProgDialog : Boolean) {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("数据加载中...")
        if (showProgDialog)
            progressDialog.show()
        doAsync {
            val taskData = queryDoneTaskData()
            uiThread {
                doneTaskAdapter?.taskList?.removeAll(doneTaskAdapter?.taskList!!)
                doneTaskAdapter?.taskList?.addAll(taskData)
                doneTaskAdapter?.notifyDataSetChanged()
                refreshBadgeView1()
                if (showProgDialog)
                    progressDialog.dismiss()
            }
        }
    }

    /**
     * 更新正在进行的任务数据列表
     */
    fun refreshDoingTaskData(showProgDialog : Boolean) {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("数据加载中...")
        if (showProgDialog)
            progressDialog.show()
        doAsync {
            val taskData = queryDoingTaskData()
            uiThread {
                doingTaskAdapter?.taskList?.removeAll(doingTaskAdapter?.taskList!!)
                doingTaskAdapter?.taskList?.addAll(taskData)
                doingTaskAdapter?.notifyDataSetChanged()
                refreshBadgeView1()
                if (showProgDialog)
                    progressDialog.dismiss()
            }
        }
    }

    override fun onDestroy() {
        mNM!!.cancel(R.string.app_name)

        super.onDestroy()
    }

    private var mFileStream: FileStream? = null
    private var doingParentListView: RecyclerView? = null
    var doingTaskAdapter:TaskAdapter? = null

    /**
     * 初始化任务界面

     * @param view
     */
    fun initDoContent(view: View) {
        val emptyview = view.findViewById(R.id.taskEmptyView) as RelativeLayout

        doingParentListView = view.findViewById(R.id.expandablelistview_doingtask) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        var taskData = ArrayList<TaskData>()
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("数据加载中...")
        progressDialog.show()
        doAsync {
            taskData = queryDoingTaskData()
            uiThread {
                // 判断是否显示空列表时的图片
                if (taskData.size == 0) emptyview.visibility=View.VISIBLE else emptyview.visibility = View.GONE
                // 加载任务列表
                doingTaskAdapter = TaskAdapter(mContext, taskData)
                doingParentListView!!.layoutManager = layoutManager
                doingParentListView!!.adapter = doingTaskAdapter
                doingTaskAdapter?.setOnGroupExpandCollapseListener(object : GroupExpandCollapseListener{
                    override fun onGroupCollapsed(group: ExpandableGroup<*>?) {

                    }
                    override fun onGroupExpanded(group: ExpandableGroup<*>?) {
                        val taskData = group as TaskData
                        val taskID =  taskData.taskID
                        val taskinfo = taskinfoDao?.queryBuilder()?.where(TASKINFODao.Properties.TaskID.eq(taskID))
                                ?.orderAsc(TASKINFODao.Properties.TaskID)?.list()?.get(0)
                        taskinfo?.is_new_task = false
                        taskinfoDao?.insertOrReplace(taskinfo)
                        (mContext as WeixinActivityMain).refreshBadgeView1()
                    }

                })
                progressDialog.dismiss()
            }
        }
    }


    private var doneTaskListView: RecyclerView? = null
    var doneTaskAdapter:TaskAdapter? = null

    /**
     * 初始化历史完成任务界面

     * @param view
     */
    fun initDoneContent(view: View) {
        if (mFileStream == null)
            mFileStream = FileStream(this@WeixinActivityMain)

        doneTaskListView = view.findViewById(R.id.expandablelistview_doingtask) as RecyclerView
        val emptyview = view.findViewById(R.id.taskEmptyView) as RelativeLayout
        val layoutManager = LinearLayoutManager(this)
        var taskData = ArrayList<TaskData>()
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("数据加载中...")
        progressDialog.show()
        doAsync {
            taskData = queryDoneTaskData()
            uiThread {
                // 判断是否显示空列表时的图片
                if (taskData.size == 0) emptyview.visibility=View.VISIBLE else emptyview.visibility = View.GONE
                // 加载任务列表
                doneTaskAdapter = TaskAdapter(mContext, taskData)
                doneTaskListView!!.layoutManager = layoutManager
                doneTaskListView!!.adapter = doneTaskAdapter
                progressDialog.dismiss()
            }
        }
    }


    private var unitBtn: LinearLayout? = null
    private var manualCheckTask: LinearLayout? = null
    private var clearCacheBtn: LinearLayout? = null

    private var versionBtn: LinearLayout? = null
    private var toolbox: LinearLayout? = null
    private var exitBtn: LinearLayout? = null
    private var contactMe: LinearLayout? = null
    private var debugInterface: LinearLayout? = null

    private var tv_clearcache: TextView? = null
    private var user_name: TextView? = null
    private var company_name: TextView? = null//抽屉顶部的用户名和公司名

    /**
     * 初始化设置界面
     */
    fun initSettingUI(v: View) {

        //基本信息
        unitBtn = v.findViewById(R.id.unit_btn) as LinearLayout
        unitBtn!!.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View) {
                val i = Intent(this@WeixinActivityMain, DetecedUnit::class.java)
                startActivity(i)
            }
        })

        user_name = v.findViewById(R.id.user_name) as TextView
        company_name = v.findViewById(R.id.company_name) as TextView

        val login_user = SPUtils.get(this, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String?
        user_name!!.text = login_user
        company_name!!.text = "所属单位:" + (SPUtils.get(this, SPUtils.SAMPLING_COMPANY, "没有填写", SPUtils.USER_INFO) as String?)!!

        // 手动检查新任务
        manualCheckTask = v.findViewById(R.id.check_new_task_btn) as LinearLayout
        manualCheckTask?.onClick {
            haveNewTask()
        }
        //清除缓存
        //显示缓存大小
        tv_clearcache = v.findViewById(R.id.clear_cache_tv) as TextView

        getCacheSize()

        //清理缓存
        clearCacheBtn = v.findViewById(R.id.clear_cache_btn) as LinearLayout
        clearCacheBtn!!.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View) {
                val dialog = Dialog(this@WeixinActivityMain)

                dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                val inflater = this@WeixinActivityMain
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout = inflater.inflate(
                        R.layout.dialogview_twocheckbox_onebutton, null) as RelativeLayout
                val Title = layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu) as TextView
                val Message = layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu) as TextView

                val task = layout.findViewById(R.id.checkbox_task) as CheckBox
                val templet = layout.findViewById(R.id.checkbox_templet) as CheckBox

                val LL_task = layout.findViewById(R.id.LL_task) as LinearLayout
                val LL_template = layout.findViewById(R.id.LL_templet) as LinearLayout

                LL_task.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        if (task.isChecked) {
                            task.isChecked = false
                        } else {
                            task.isChecked = true
                        }
                    }
                })

                LL_template.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        if (templet.isChecked) {
                            templet.isChecked = false
                        } else {
                            templet.isChecked = true
                        }
                    }
                })

                val positivebutton = layout.findViewById(R.id.textview_save_button) as TextView


                Title.text = "清除本地抽样缓存"
                Message.text = "请选择要清除的内容"

                positivebutton.text = " 清理 "


                positivebutton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        if (!task.isChecked && !templet.isChecked) {
                            Toast.makeText(this@WeixinActivityMain, "没有清理任何内容", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                            return
                        }

                        val futil = FileUtils()

                        if (task.isChecked)
                            futil.deleteFile(mFileStream!!.taskFile)

                        if (templet.isChecked)
                            futil.deleteFile(mFileStream!!.templetFile)

                        Toast.makeText(this@WeixinActivityMain, "清理成功！", Toast.LENGTH_LONG).show()

                        getCacheSize()

                        dialog.dismiss()
                    }
                })


                dialog.setContentView(layout)
                dialog.setCancelable(true)
                dialog.show()
            }
        })

        //工具箱
        toolbox = v.findViewById(R.id.toolbox_btn) as LinearLayout
        toolbox!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {

                val toolbox_dialog = Dialog(
                        this@WeixinActivityMain)

                val inflater = this@WeixinActivityMain
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout = inflater.inflate(
                        R.layout.dialogview_toolbox, null) as RelativeLayout

                //地图导航
                val mapButton = layout.findViewById(R.id.map_navi_btn) as LinearLayout
                mapButton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        val intent = Intent(this@WeixinActivityMain.applicationContext, MapActivity::class.java)
                        this@WeixinActivityMain.startActivity(intent)
                    }
                })

                //记事本
                val minote_btn = layout.findViewById(R.id.minote_btn) as LinearLayout
                minote_btn.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        startActivity(Intent(this@WeixinActivityMain, NotesListActivity::class.java))
                    }
                })

                //指南针
                val compass_btn = layout.findViewById(R.id.compass_btn) as LinearLayout
                compass_btn.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        startActivity(Intent(this@WeixinActivityMain, CompassActivity::class.java))
                    }
                })


                toolbox_dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                toolbox_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                toolbox_dialog.setContentView(layout)
                toolbox_dialog.setCancelable(true)
                toolbox_dialog.show()


            }
        })

        contactMe = v.findViewById(R.id.contactme_btn) as LinearLayout
        contactMe!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                sendTextToEmail()
            }
        })

        //版本信息
        versionBtn = v.findViewById(R.id.vertion_btn) as LinearLayout
        versionBtn!!.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View) {
                val intent = Intent(this@WeixinActivityMain, AboutActivity::class.java)
                startActivity(intent)
            }
        })

        //unregist
        exitBtn = v.findViewById(R.id.exitapp) as LinearLayout
        exitBtn!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                returnToLoginActivity()
                finish()
            }
        })

        debugInterface = v.findViewById(R.id.LL_debug) as LinearLayout
        debugInterface!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val intent = Intent(mContext, DebugActivity::class.java)
                startActivity(intent)
            }
        })

    }

    /**
     * 给我的邮件发消息
     */
    private fun sendTextToEmail() {
        val mEmailAddress = "769776082@qq.com"
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mEmailAddress))

        intent.putExtra("subject", "Result of " + getString(R.string.app_name))
        intent.putExtra("body", "你好开发者，我是...")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            //TODO 手机中没有邮箱软件，打开反馈界面
            Toast.makeText(applicationContext, "没有检测到邮箱程序，请先安装邮箱（如QQ邮箱）", Toast.LENGTH_LONG).show()
        }

    }

    fun getCacheSize() {
        var size: Long = 0
        try {
            size = FileUtils.getFileSizes(mFileStream!!.taskFile) + FileUtils.getFileSizes(mFileStream!!.templetFile)
        } catch (e: Exception) {
            e.printStackTrace()
            size = -1
        }

        if (size == -1L) {
            tv_clearcache!!.text = "清理缓存" + " (未知)"
        } else {
            tv_clearcache!!.text = "清理缓存" + " (" + FileUtils.FormetFileSize(size) + ")"
        }
    }

    /**********************************************************************************
     * ******************************* R E F R E S H *************************************
     * ***********************************************************************************
     */

    fun refreshBadgeView1() {

        val newTaskSize = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.Is_finished.eq(false), TASKINFODao.Properties.Is_new_task.eq(true)).orderAsc(TASKINFODao.Properties.TaskID).list().size

        if (newTaskSize == 0)
            badgeView1!!.visibility = View.GONE
        else {
            badgeView1!!.visibility = View.VISIBLE
            badgeView1!!.text = (newTaskSize).toString()

        }

    }

    /**
     * 查找正在进行的多级列表的任务数据
     */
    fun queryDoingTaskData(): ArrayList<TaskData> {
        //init database
        daoSession = (application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession!!.taskinfoDao
        templettableDao = daoSession!!.templettableDao
        samplingtableDao = daoSession!!.samplingtableDao
        //queryTaskName
        val taskinfos = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.Is_finished.eq(false)).orderAsc(TASKINFODao.Properties.TaskID).list()
        return taskInfo2TaskData(taskinfos)
    }

    /**
     * 查找已完成的多级列表的任务数据
     */
    fun queryDoneTaskData(): ArrayList<TaskData> {
        //init database
        daoSession = (application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession!!.taskinfoDao
        templettableDao = daoSession!!.templettableDao
        samplingtableDao = daoSession!!.samplingtableDao
        //queryTaskName
        val taskinfos = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.Is_finished.eq(true)).orderAsc(TASKINFODao.Properties.TaskID).list()
        return taskInfo2TaskData(taskinfos)
    }

    /**
     * taskInfo类转TaskData
     */
    fun taskInfo2TaskData(taskInfos: List<TASKINFO>): ArrayList<TaskData>{
        val taskSet = ArrayList<TaskData>()  // 任务列表
        for (taskInfo in taskInfos) {
            // 当前任务下查找抽样单
            val sheetSet = ArrayList<Sheet>()  // 盛放抽样单的列表
            val sheets = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskInfo.taskID)).
                    orderAsc(SAMPLINGTABLEDao.Properties.Id).list()  // 查找任务下的抽样单
            // 先添加一个模板
            var templets = templettableDao?.queryBuilder()?.where(TEMPLETTABLEDao.Properties.TaskID.eq(taskInfo.taskID))?.
                    orderAsc(TEMPLETTABLEDao.Properties.TempletID)?.list()
            // 当前定义的一个任务就一个模板
            var templet = templets?.get(0)
            sheetSet.add(Sheet(null, templet?.taskID, templet?.templetID, templet?.templet_name, null, templet?.templet_content, null, null, null, null, null,
                    null, null, null, null,null, null, null, null, true, true))
            for (sheet in sheets) {
                // 生成Sheet类型，给ExpandableRecycleView用
                val s = Sheet(sheet.id, sheet.taskID, sheet.templetID, sheet.show_name, sheet.sampling_address,
                        sheet.sampling_content, sheet.media_folder, sheet.is_saved, sheet.is_uploaded,
                        sheet.is_server_sampling, sheet.is_make_up, sheet.check_status, sheet.saved_time, sheet.uploaded_time,
                        sheet.sid_of_server, sheet.latitude, sheet.longitude, sheet.location_mode, sheet.sampling_unique_num, false, true)
                sheetSet.add(s)
            }
            val taskData = TaskData(sheetSet, taskInfo.taskID, taskInfo.task_name, taskInfo.task_letter,
                    taskInfo.is_finished, taskInfo.is_new_task, taskInfo.download_time, taskInfo.description)
            taskSet.add(taskData)
        }
        return taskSet
    }

    /**
     * 百度移动统计
     */
    internal fun mobstat() {
        // 设置AppKey
        StatService.setAppKey("c90777801b") // appkey必须在mtj网站上注册生成，该设置建议在AndroidManifest.xml中填写，代码设置容易丢失

        /*
                * 设置渠道的推荐方法。该方法同setAppChannel（String）， 如果第三个参数设置为true（防止渠道代码设置会丢失的情况），将会保存该渠道，每次设置都会更新保存的渠道，
                * 如果之前的版本使用了该函数设置渠道，而后来的版本需要AndroidManifest.xml设置渠道，那么需要将第二个参数设置为空字符串,并且第三个参数设置为false即可。
                * appChannel是应用的发布渠道，不需要在mtj网站上注册，直接填写就可以 该参数也可以设置在AndroidManifest.xml中
                */
        // StatService.setAppChannel(this, "RepleceWithYourChannel", true);
        // 测试时，可以使用1秒钟session过期，这样不断的间隔1S启动退出会产生大量日志。
        StatService.setSessionTimeOut(30)
        // setOn也可以在AndroidManifest.xml文件中填写，BaiduMobAd_EXCEPTION_LOG，打开崩溃错误收集，默认是关闭的
        StatService.setOn(this, StatService.EXCEPTION_LOG)
        /*
                * 设置启动时日志发送延时的秒数<br/> 单位为秒，大小为0s到30s之间<br/> 注：请在StatService.setSendLogStrategy之前调用，否则设置不起作用
                *
                * 如果设置的是发送策略是启动时发送，那么这个参数就会在发送前检查您设置的这个参数，表示延迟多少S发送。<br/> 这个参数的设置暂时只支持代码加入，
                * 在您的首个启动的Activity中的onCreate函数中使用就可以。<br/>
                */
        StatService.setLogSenderDelayed(0)
        /*
                * 用于设置日志发送策略<br /> 嵌入位置：Activity的onCreate()函数中 <br />
                *
                * 调用方式：StatService.setSendLogStrategy(this,SendStrategyEnum. SET_TIME_INTERVAL, 1, false); 第二个参数可选：
                * SendStrategyEnum.APP_START SendStrategyEnum.ONCE_A_DAY SendStrategyEnum.SET_TIME_INTERVAL 第三个参数：
                * 这个参数在第二个参数选择SendStrategyEnum.SET_TIME_INTERVAL时生效、 取值。为1-24之间的整数,即1<=rtime_interval<=24，以小时为单位 第四个参数：
                * 表示是否仅支持wifi下日志发送，若为true，表示仅在wifi环境下发送日志；若为false，表示可以在任何联网环境下发送日志
                */
        StatService.setSendLogStrategy(this, SendStrategyEnum.APP_START, 0, true)
        // 调试百度统计SDK的Log开关，可以在Eclipse中看到sdk打印的日志，发布时去除调用，或者设置为false
        StatService.setDebugOn(true)
    }

    /**
     * 广播接收器
     */
    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_TIME_TICK) {//每分钟的广播 1.if have new task
                updateTaskStatus(false)
                updateSamplingStatus(false)
//                haveNewTask()
            }
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private fun showNotification() {
        val mBuilder = NotificationCompat.Builder(this)
        mBuilder.setSmallIcon(R.drawable.ic_launcher1)
        mBuilder.setContentTitle(resources.getString(R.string.app_name))
        mBuilder.setContentText("您有一个新任务!")

        val intent = Intent(this, WeixinActivityMain::class.java)
        mBuilder.setContentIntent(PendingIntent.getBroadcast(this, 0, intent, 0))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, mBuilder.build())
    }


    /***************************
     * Service
     */

    private val mCallback = object : MsgService.ICallback {

        override fun haveNewTask() {
           refreshDoingTaskData(showProgDialog = false)
        }

        override fun getWeixinActitityContext(): Context {
            return this@WeixinActivityMain
        }

        override fun returnToLoginActivity() {
            (mContext as WeixinActivityMain).returnToLoginActivity()
        }

        override fun refreshBadgeView1_callback() {
            refreshBadgeView1()
        }

        override fun refreshDoingChildListView() {
            refreshDoingTaskData(showProgDialog = false)
        }
    }

    private var mService: MsgService? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = (service as MsgService.MsgBinder).service

            mService!!.registerCallback(mCallback)


        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
        }
    }


    private fun startMsgService() {

        startService(Intent(this@WeixinActivityMain, MsgService::class.java))

    }

    private fun bindMsgService() {
        bindService(Intent(this@WeixinActivityMain, MsgService::class.java), mConnection,
                Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND)
    }

    private fun unbindMsgService() {
        if (mService != null)
            unbindService(mConnection)
    }

    private fun stopMsgService() {
        stopService(Intent(this@WeixinActivityMain, MsgService::class.java))
        if (mService != null) {
            mService!!.stopSelf()
        }

    }

    private fun returnToLoginActivity() {
        stopMsgService()
        Util.stopLongTermService(applicationContext)
        SPUtils.put(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
        SPUtils.put(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.putExtra("needTurnToMain", false)
        startActivity(intent)
        finish()
    }


    /************************网络操作接口 */
    internal fun cancleRequestFromQueue() {
        if (getNewTaskRequest != null)
            getNewTaskRequest!!.cancel()
        if (setTaskReceivedRequest != null)
            setTaskReceivedRequest!!.cancel()
        if (getSamplingStatusRequest != null)
            getSamplingStatusRequest!!.cancel()
        if (getTaskStatusRequest != null)
            getTaskStatusRequest!!.cancel()
    }

    /**
     * 检查是否有新任务
     */
    fun haveNewTask() {
        stopMsgService()

        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("检查新任务中...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val listener = object : Response.Listener<String> {
            override fun onResponse(s: String) {
                progressDialog.dismiss()
                try {

                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val result = resultJson.getString(URLs.KEY_MESSAGE)

                    if (errorCode == ReturnCode.Code0) {//connected
                        if (result == URLs.RESULT_NEWTASK) {
                            // 1.send msg to main mContext 2.save to sharedPreference 3.show a notification
                            getNewTask()
                        } else if (result == URLs.RESULT_NOTHING) {
                            // don't have new task. just TOast
                            Toast.makeText(mContext, "没有新任务", Toast.LENGTH_LONG).show()
                        } else
                            Log.e("XXXXXXX", "接收到不该出现的结果")
                    } else {
                        ReturnCode(applicationContext, errorCode, true)
                        if (errorCode == ReturnCode.NO_SUCH_ACCOUNT || errorCode == ReturnCode.PASSWORD_INVALIDE) {
                            returnToLoginActivity()
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("XXXXXXXX", "havaNewTask 返回值出问题")
                    startMsgService()
                }

            }
        }
        val errorListener = object : Response.ErrorListener {
            override fun onErrorResponse(volleyError: VolleyError) {
                progressDialog.dismiss()
                startMsgService()
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
                Log.e("HaveNewTaskFail", volleyError.toString())
            }
        }
        val stringRequest = API.haveNewTask(listener, errorListener, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String?)
        queue!!.add<String>(stringRequest)
    }

    /**
     * get New Task
     */
    fun getNewTask() {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("接受新任务中...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val listener = object : Response.Listener<String> {
            override fun onResponse(s: String) {
                //      Log.e("GetNewTaskSuc", s);
                try {

                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val result = resultJson.getString(URLs.KEY_MESSAGE)

                    if (errorCode == ReturnCode.Code0) {//connected
                        SPUtils.put(mContext, SPUtils.RECEIVED_TASK, result, SPUtils.TEMPORARY_SAVE)
                        setReceived()
                    } else {
                        ReturnCode(applicationContext, errorCode, true)
                        if (errorCode == ReturnCode.NO_SUCH_ACCOUNT || errorCode == ReturnCode.PASSWORD_INVALIDE) {
                            returnToLoginActivity()
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    progressDialog.dismiss()
                    startMsgService()
                }

                progressDialog.dismiss()

            }
        }
        val errorListener = object : Response.ErrorListener {
            override fun onErrorResponse(volleyError: VolleyError) {
                progressDialog.dismiss()
                startMsgService()
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
                Log.e("GetNewTaskFail", volleyError.toString())
            }
        }
        getNewTaskRequest = API.getNewTask(listener, errorListener, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String?, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String?)
        queue!!.add<String>(getNewTaskRequest!!)
    }

    /**
     * tell server received the task
     */
    fun setReceived() {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("存入数据库中...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val listener = object : Response.Listener<String> {
            override fun onResponse(s: String) {
                startMsgService()
                try {
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val result = resultJson.getString(URLs.KEY_MESSAGE)

                    if (errorCode == ReturnCode.Code0) {//connected
                        if (result == URLs.RESULT_RECEIVEDOK) {
                            //TODO get new task  1.send msg to main mContext 2.save to sharedPreference 3.show a notification
                            val tasks = SPUtils.get(mContext, SPUtils.RECEIVED_TASK, "", SPUtils.TEMPORARY_SAVE) as String?

                            if (tasks!!.isEmpty()) {
                                Log.e("XXXXXXXX", "没有获取到任何东西，不应该为空，检查程序")
                                return
                            }

                            val taskJsonArray = JSONArray(tasks)
                            for (i in 0..taskJsonArray.length() - 1) {
                                val taskJsonObject = taskJsonArray.getJSONObject(i)
                                val taskID = taskJsonObject.getString(URLs.KEY_TASKID)
                                val taskName = taskJsonObject.getString(URLs.KEY_TASKNAME)
                                val taskLetter = taskJsonObject.getString(URLs.KEY_TASK_INI_LETTER)
                                val taskDes = taskJsonObject.getString(URLs.KEY_TASKDISCRIPTION)
                                val taskCont = taskJsonObject.getString(URLs.KEY_TASKCONT)
                                val sampling = taskJsonObject.getString(URLs.KEY_SAMPLING)//定点采样的抽样单

                                //任务id已存在，则不存入数据
                                if (taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list().size != 0) {
                                    T.showShort(applicationContext, "任务已存在！")
                                    if (progressDialog.isShowing)
                                        progressDialog.dismiss()
                                    return
                                }

                                //insert task
                                val taskinfo = TASKINFO(java.lang.Long.valueOf(taskID), taskName, taskLetter, false, true, System.currentTimeMillis(), taskDes)
                                taskinfoDao!!.insertOrReplace(taskinfo)

                                //insert Templet
                                val templettable = TEMPLETTABLE(null, taskinfo.taskID, taskinfo.task_name, taskCont, System.currentTimeMillis())
                                templettableDao!!.insertOrReplace(templettable)

                                //定点采样 insert sampling
                                val samplingsArray = JSONArray(sampling)


                                //                                progressDialog.setMax(samplingsArray.length());
                                //                                progressDialog.setProgress(0);
                                //                                progressDialog.show();


                                for (j in 0..samplingsArray.length() - 1) {
                                    //                                    progressDialog.setProgress(j + 1);
                                    //                                    if (j + 1 == samplingsArray.length())
                                    //                                        progressDialog.dismiss();

                                    // TODO 从抽样单Json中读取样品信息，如果为空，则应该显示默认“未填写”字样
                                    val samplingID = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLINGID)
                                    val samplingCont = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLINGCONT)
                                    val samplingName = samplingsArray.getJSONObject(j).getString(URLs.KEY_ITEMS)
                                    val samplingNum = samplingsArray.getJSONObject(j).getString(URLs.KEY_ITEMSID)
                                    val companyAddress = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLING_COMPANY_NAME)
                                    val mediaFolderChild = Util.getSamplingNum(mContext, taskinfo)

                                    val samplingtable = SAMPLINGTABLE(null, java.lang.Long.valueOf(taskID), templettable.templetID, samplingName + "-" + samplingNum, companyAddress,
                                            samplingCont, mediaFolderChild, false, false, true, false, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(), null, java.lang.Long.valueOf(samplingID), null, null, null, mediaFolderChild)


                                    samplingtableDao!!.insertOrReplace(samplingtable)
                                }


                            }

                            SPUtils.put(mContext, SPUtils.RECEIVED_TASK, "", SPUtils.TEMPORARY_SAVE)

                            if (progressDialog.isShowing)
                                progressDialog.dismiss()

                            //show a notification
                            showNotification()
                            refreshDoingTaskData(showProgDialog = false)
                        } else {
                            if (progressDialog.isShowing)
                                progressDialog.dismiss()

                            ReturnCode(applicationContext, errorCode, true)
                            if (errorCode == ReturnCode.NO_SUCH_ACCOUNT || errorCode == ReturnCode.PASSWORD_INVALIDE) {
                                returnToLoginActivity()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (progressDialog.isShowing)
                        progressDialog.dismiss()
                }

            }
        }
        val errorListener = object : Response.ErrorListener {
            override fun onErrorResponse(volleyError: VolleyError) {
                if (progressDialog.isShowing)
                    progressDialog.dismiss()
                startMsgService()
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
                Log.e("Received", volleyError.toString())
            }
        }
        setTaskReceivedRequest = API.setTaskReceived(listener, errorListener, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String?)
        queue!!.add<String>(setTaskReceivedRequest!!)
    }

    /**
     * 更新抽样单审核Status
     * server return like this:
     * {error:0,msg:[{sid:01,type:'0'},{sid:02,type:'1'}]}
     */
    fun updateSamplingStatus(showDialog: Boolean) {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        if (showDialog) {
            progressDialog.setMessage("正在刷新任务状态...")
            progressDialog.show()
        }
        val jsonArray = JSONArray()
        val allSamplingtables = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.Sid_of_server.isNotNull).orderAsc().list()
        try {
            for (i in allSamplingtables.indices) {
                val jsonObject = JSONObject()
                jsonObject.put("sid", allSamplingtables.get(i).sid_of_server)
                jsonArray.put(jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val listener = object : Response.Listener<String> {
            override fun onResponse(s: String) {
                if (showDialog)
                    progressDialog.dismiss()
                try {
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val content = resultJson.getString(URLs.KEY_MESSAGE)

                    var jsonObjectSamStatus: JSONObject
                    val sid: Long?
                    var status: Int
                    if (errorCode == ReturnCode.Code0) {
                        val jsonArraySamsStatus = JSONArray(content)

                        val samplingtables = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.Sid_of_server.isNotNull).orderAsc().list()

                        if (samplingtables.size != jsonArraySamsStatus.length()) {
                            //TODO 将返回的多余的抽样单删掉 并提示用户“服务器删除了**张抽样单，本地也将删除”
                            Log.e("XXXXXXX", "WeixinActivity.java 查询任务和返回任务对应数量不同")
                            return
                        }
                        for (i in samplingtables.indices) {

                            jsonObjectSamStatus = jsonArraySamsStatus.getJSONObject(i)
                            status = Integer.valueOf(jsonObjectSamStatus.getString("type"))!!
                            samplingtables.get(i).check_status = status

                        }
                        samplingtableDao!!.insertOrReplaceInTx(samplingtables)
                        refreshDoingTaskData(showProgDialog = false)

                        if (showDialog) {
                            Toast.makeText(applicationContext, "任务状态更新成功", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        ReturnCode(applicationContext, errorCode, true)
                        if (errorCode == ReturnCode.NO_SUCH_ACCOUNT || errorCode == ReturnCode.PASSWORD_INVALIDE)
                            returnToLoginActivity()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
        val errorListener = object : Response.ErrorListener {
            override fun onErrorResponse(volleyError: VolleyError) {
                if (showDialog)
                    progressDialog.dismiss()
                Log.e("getSamStatusFail", volleyError.toString())
                if (showDialog) {
                    Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
                }
            }
        }
        getSamplingStatusRequest = API.getSamplingStatus(listener, errorListener,
                SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String?,
                SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String?, jsonArray.toString())

        queue!!.add<String>(getSamplingStatusRequest!!)
    }

    /**
     * 更新任务状态
     * server return like this:
     * {error:0,msg:[{tid:01,type:'0'},{tid:02,type:'1'}]}
     */
    fun updateTaskStatus(showDialog: Boolean) {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        if (showDialog) {
            progressDialog.setMessage("正在刷新任务状态...")
            progressDialog.show()
        }
        val jsonArray = JSONArray()
        val alltaskinfos = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.isNotNull).orderAsc().list()
        try {
            for (i in alltaskinfos.indices) {
                val jsonObject = JSONObject()
                jsonObject.put("tid", alltaskinfos.get(i).taskID)
                jsonArray.put(jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val listener = object : Response.Listener<String> {
            override fun onResponse(s: String) {
                if (showDialog)
                    progressDialog.dismiss()
                try {
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val content = resultJson.getString(URLs.KEY_MESSAGE)

                    if (errorCode == ReturnCode.Code0) {
                        val jsonArrayTasksStatus = JSONArray(content)
                        var jsonObjectTaskStatus: JSONObject
                        val tid: Long?
                        var status: Int

                        if (alltaskinfos.size != jsonArrayTasksStatus.length())
                            Log.e("XXXXXXX", "WeixinActivity.java 查询任务和返回任务对应数量不同")

                        for (i in alltaskinfos.indices) {

                            jsonObjectTaskStatus = jsonArrayTasksStatus.getJSONObject(i)
                            status = Integer.valueOf(jsonObjectTaskStatus.getString("type"))!!
                            var isFinish = true
                            if (status == Constant.TASK_DOING)
                                isFinish = false
                            else if (status == Constant.TASK_DONE)
                                isFinish = true
                            alltaskinfos.get(i).is_finished = isFinish

                        }
                        taskinfoDao!!.insertOrReplaceInTx(alltaskinfos)//update database

                        refreshDoingTaskData(showProgDialog = false)
                        refreshDoneTaskData(showProgDialog = false)

                    } else {
                        ReturnCode(applicationContext, errorCode, true)
                        if (errorCode == ReturnCode.NO_SUCH_ACCOUNT || errorCode == ReturnCode.PASSWORD_INVALIDE)
                            returnToLoginActivity()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
        val errorListener = object : Response.ErrorListener {
            override fun onErrorResponse(volleyError: VolleyError) {
                if (showDialog)
                    progressDialog.dismiss()
                Log.e("getTaskStatusFail", volleyError.toString())
                if (showDialog) {
                    Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
                }
            }
        }
        getTaskStatusRequest = API.getTaskStatus(listener, errorListener,
                SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String?,
                SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String?, jsonArray.toString())

        queue!!.add<String>(getTaskStatusRequest!!)
    }

    companion object {

        var instance: WeixinActivityMain? = null
    }

}
    
    

