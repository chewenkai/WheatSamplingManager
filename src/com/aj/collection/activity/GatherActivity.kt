package com.aj.collection.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import android.widget.*

import com.aj.Constant
import com.aj.SystemBarTintManager
import com.aj.collection.*
import com.aj.collection.adapters.SampleExelGridAdapter
import com.aj.collection.bean.ImageInfo
import com.aj.collection.bean.SheetCell
import com.aj.collection.bean.Superior
import com.aj.database.DaoSession
import com.aj.database.SAMPLINGTABLE
import com.aj.database.SAMPLINGTABLEDao
import com.aj.database.TASKINFO
import com.aj.database.TASKINFODao
import com.aj.database.TEMPLETTABLEDao
import com.aj.collection.activity.http.API
import com.aj.collection.activity.http.ReturnCode
import com.aj.collection.activity.http.URLs
import com.aj.collection.activity.tools.*
import com.aj.collection.activity.ui.viewimage.PictureViewActivity
import com.aj.collection.activity.ui.GridViewEx
import com.aj.collection.activity.ui.HeadControlPanel
import com.aj.collection.activity.ui.HeadControlPanel.LeftImageOnClick
import com.aj.collection.activity.ui.HeadControlPanel.RightSecondOnClick
import com.aj.collection.activity.ui.HeadControlPanel.rightFirstImageOnClick
import com.aj.collection.activity.ui.MyLayout
import com.aj.collection.activity.ui.SheetCellUI
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.baidu.location.LocationClientOption
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.toast

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.util.HashMap
import kotlin.collections.ArrayList

class GatherActivity : AppCompatActivity() {

    internal var intent: Intent? = null                 //传递时间的意图
    internal var sheetJsonStr: String = ""                 //抽样单json
    internal var templetID: Long = 0
    internal var templetName: String = ""             //抽样单模板名称
    internal var samplingID: Long? = -1L          //抽样单ID
    // 初始化单元格内容的容器
    val sheetCellUIList = ArrayList<SheetCellUI>()

    internal var root_path: String = ""              //跟目录
    internal var child_path: String = ""             //储存照片录像等信息的子目录目录
    internal var isFixPoint: Boolean = false             //是否是定点采样
    internal var isMakeUp: Boolean = false
    internal var sid_of_server: Long? = null             //原抽样单的sid
    internal var fs: FileStream? = null  //写文件用到的流
    internal var lu: MyLayout? = null                   //布局生成类
    internal var tv_gps: TextView? = null                //gps显示控件
    internal var tv_num: TextView? = null                //抽样单编号的TextView
    internal var bt_gps: TextView? = null                //gps暂停按钮
    var addSamplingButton: TextView? = null
    internal var num: String = ""                    //抽样单编号
    internal var whichTask: Long = 0                 //被点击的任务
    internal var taskName: String =""
    internal var parentView: LinearLayout? = null
    internal var smsManager = SmsManager.getDefault()
    private var phone_number = "15004600323"
    private var kaiguan = false
    private var isHavePicture = false
    private var isHaveVideo = false

    internal var queue: RequestQueue? = null //init Volley;

    private val mContext = this

    //database part
    var daoSession: DaoSession? = null
    var taskinfoDao: TASKINFODao? = null
    var templettableDao:TEMPLETTABLEDao? = null
    var samplingtableDao:SAMPLINGTABLEDao? = null

    private var samplingtables: List<SAMPLINGTABLE>? = null //当前这张抽样单，应该是一张

    private var taskinfo: TASKINFO? = null

    internal var headPanel: HeadControlPanel? = null

    fun sendMes() {
        if (!kaiguan) {
            SPUtils.put(this@GatherActivity, taskName, true, SPUtils.WHICHTASK)
            T.showShort(this@GatherActivity, "保存成功")
            return
        }
        val sms_content = "任务名：" + templetName + "\n" +
                "编号：" + num +
                tv_gps!!.text.toString()
        if (sms_content.length > 70) {
            val contents = smsManager.divideMessage(sms_content)
            for (sms in contents) {
                smsManager.sendTextMessage(phone_number, null, sms, null, null)
            }
        } else {
            smsManager.sendTextMessage(phone_number, null, sms_content, null, null)
        }
        SPUtils.put(this@GatherActivity, taskName, true, SPUtils.WHICHTASK)
        T.showShort(this@GatherActivity, "保存成功，并已发送定位短信")
    }

    private var etCompanyName: EditText? = null
    private var etItem: EditText? = null
    private var etItemID: EditText? = null
    private var companyName: String? = null
    var itemName: String = ""
    var itemID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        dialog = Dialog(this@GatherActivity)
        ExitApplication.getInstance().addActivity(this)
        //dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.form_layout)

        queue = Volley.newRequestQueue(this)
        daoSession= ((mContext as Activity).application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession!!.taskinfoDao
        templettableDao = daoSession!!.templettableDao
        samplingtableDao = daoSession!!.samplingtableDao
        //沉浸状态栏
        SystemBarTintManager.setStatusBarTint(this@GatherActivity, Color.argb(0, 59, 59, 59))//透明状态栏

        val EMPTY_STRING = ""
        phone_number = SPUtils.get(this, SPUtils.JIANKONG, EMPTY_STRING, SPUtils.get(this, SPUtils.LOGIN_USER, EMPTY_STRING, SPUtils.USER_DATA) as String) as String

        //发送短信的开关是否打开
        val `object` = SPUtils.get(this, SPUtils.KAIGUAN, false, SPUtils.get(this, SPUtils.LOGIN_USER, EMPTY_STRING, SPUtils.USER_DATA) as String)
        if (`object` != null)
            kaiguan = `object` as Boolean
        else
            kaiguan = false

        parentView = findViewById(R.id.form_parent) as LinearLayout

        // 没有下列7行代码会产生焦点问题。EditText获取焦点后，下滑ScrollView会跳回EditText
        val scroll = findViewById(R.id.sheet_scroll_view) as ScrollView
        scroll.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS)
        scroll.setFocusable(true)
        scroll.setFocusableInTouchMode(true)
        scroll.setOnTouchListener { v, event ->
            v.requestFocusFromTouch()
            return@setOnTouchListener false }

        //Intent中包含了关于任务的信息
        intent = getIntent()

        //获取各种任务信息
        whichTask = intent!!.getLongExtra("whichTask", -1)               //获取任务ID
        sheetJsonStr = intent!!.getStringExtra("res")                         //获得抽样单JSON
        templetID = intent!!.getLongExtra("templetID", -1)               //获取模板ID
        isMakeUp = intent!!.getBooleanExtra("isMakeUp", false)           //获取是否是补采的标识
        if (isMakeUp) { //make up have sid
            sid_of_server = intent!!.getLongExtra("sid_of_server", -1)   //获取SID
            samplingID = intent!!.getLongExtra("samplingID", -1)         //获取抽样单的ID
            if (!samplingID!!.equals(-1L))
                samplingtables = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.Id.eq(samplingID)).list()
        } else {
            sid_of_server = -1L
        }

        val taskinfos = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(whichTask)).list()//查询任务
        if (taskinfos.size != 1) {
            printLineInLog()
            finish()
        } else
            taskinfo = taskinfos[0]

        taskName = taskinfo!!.task_name                              //获取任务名称
        templetName = templettableDao!!.queryBuilder().where(TEMPLETTABLEDao.Properties.TempletID.eq(templetID)).list()[0].templet_name       //抽样单模板名称

        //获取并检查创建媒体文件夹
        root_path = Util.getMediaFolder(mContext)
        if (root_path.isEmpty()) {
            Toast.makeText(mContext, "没有媒体文件夹", Toast.LENGTH_LONG).show()
            finish()
        }
        val taskfolder = File(root_path)
        if (!taskfolder.exists() && !taskfolder.mkdirs()) {
            Toast.makeText(mContext, "没有媒体文件夹", Toast.LENGTH_LONG).show()
            finish()
        }

        // New Code

        // 抽样单Json文本转为Json对象
        val resultJson = JSONObject(sheetJsonStr)
        if (!resultJson.has(SheetProtocol().SHEET_JSON_KEY)){
            toast(getString(R.string.sheet_parse_error))
            finish()
        }

        // 提取抽样单Json文本
        val sheetCells = resultJson.getString(SheetProtocol().SHEET_JSON_KEY)
        // 利用Gson将Json文本转为SheetCell列表
        val turnsType = object : TypeToken<List<SheetCell>>() {}.type
        var sheetCellList:List<SheetCell> = Gson().fromJson(sheetCells, turnsType)
        // 加载界面前生成一个抽样单唯一ID
        val autoGeneratedSheetID = getAutoGeneratedSheetID()
        // 生成并加载界面
        for (sheetCell:SheetCell in sheetCellList){
            if (sheetCell.cell_type==SheetProtocol().TYPE_TEXT
                    || sheetCell.cell_type==SheetProtocol().TYPE_EDIT_TEXT
                    || sheetCell.cell_type==SheetProtocol().TYPE_RADIO
                    || sheetCell.cell_type==SheetProtocol().TYPE_GEOGRAPHIC_COORDINATES
                    || sheetCell.cell_type==SheetProtocol().TYPE_ADDRESS
                    || sheetCell.cell_type==SheetProtocol().TYPE_PHOTOS
                    || sheetCell.cell_type==SheetProtocol().TYPE_VEDIOS
                    || sheetCell.cell_type==SheetProtocol().TYPE_SIGN){
                val cellUI = SheetCellUI(this, sheetCell, autoGeneratedSheetID)
                sheetCellUIList.add(cellUI)
                parentView!!.addView(cellUI.cell.getView())
            }
        }


        //设置顶部面板按钮
        headPanel = findViewById(R.id.head_layout) as HeadControlPanel
        headPanel!!.setRightFirstVisible(View.VISIBLE)
        headPanel!!.setRightSecondVisible(View.VISIBLE)
        if (headPanel != null) {
            headPanel!!.initHeadPanel()
            headPanel!!.setMiddleTitle(resources.getString(R.string.templetTitle))
            headPanel!!.setLeftImage(R.drawable.ic_menu_back)
            val l = LeftImageOnClick {
                val alertDialog: AlertDialog
                val builder = AlertDialog.Builder(this@GatherActivity, AlertDialog.THEME_HOLO_LIGHT)
                builder.setTitle("温馨提示")
                builder.setMessage("确定要返回吗?")
                builder.setPositiveButton("确定") { dialog, which ->
                    //TODO 判断新拍的照片并删除
                    setResult(Constant.WEIXINTASKREFRESHITEM_FROMDO)
                    Util.decendLocalSeq(applicationContext, 1)
                    finish()    //退出
                }
                builder.setNegativeButton("取消", null)
                alertDialog = builder.create()
                alertDialog.show()    //显示对话框
            }
            headPanel!!.setLeftImageOnClick(l)
            headPanel!!.setRightFirstImage(R.drawable.save_file)
            headPanel!!.setRightFirstText("保存")
            val r = rightFirstImageOnClick {
                //抽样单模板的保存按钮！！
                val jsonArray = JSONArray()
                for (sheetCellUI in sheetCellUIList){
                    jsonArray.put(sheetCellUI.cell.getJsonContent())
                }
                Log.e("SHEET_JSON",jsonArray.toString())
                return@rightFirstImageOnClick
                companyName = etCompanyName!!.text.toString()
                itemName = etItem!!.text.toString().replace(" ", "")
                itemID = etItemID!!.text.toString()

                if (tv_gps!!.text.toString() == "" || (application as CollectionApplication).latitude == -1.0 || (application as CollectionApplication).longitude == -1.0 ||
                        (application as CollectionApplication).location_mode == Constant.CAN_NOT_GET_LOCATION) {
                    T.showShort(this@GatherActivity, "正在定位中...请稍后保存~")
                    return@rightFirstImageOnClick
                }
                if (itemName == "" || itemID == "") {
                    T.showShort(this@GatherActivity, R.string.should_not_keep_item_info_empty)
                    return@rightFirstImageOnClick
                }

                val samplingCount = lu!!.samplingBlockSubitems.size
                val saveProgressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
                saveProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                saveProgressDialog.max = samplingCount
                saveProgressDialog.progress = 1
                saveProgressDialog.setMessage("保存中...")
                saveProgressDialog.setCancelable(false)
                saveProgressDialog.setOnCancelListener { }
                saveProgressDialog.show()

                for (i in 0..samplingCount - 1) {

                    saveProgressDialog.progress = i + 1
                    saveProgressDialog.setMessage("样品" + (i + 1).toString() + "保存中...")

                    parentView!!.removeAllViews()
                    parentView!!.addView(lu!!.samplingBlockSubitems[i])

                    for (j in lu!!.otherBlockSubitems.indices) {
                        parentView!!.addView(lu!!.otherBlockSubitems[j])
                    }

                    try {
                        val mediaPath = lu!!.samplingBlockSubitems[i].tag as String

                        val samplingInfoJson = createJsonStr(false, parentView!!, mediaPath) //获取样品所有信息

                        //解析出样品的信息
                        val isEditDone = samplingInfoJson.getBoolean(GatherActivity.NECCESSARY)
                        val mes = samplingInfoJson.getString(GatherActivity.SAMPLING_JSON_CONT)
                        val item = samplingInfoJson.getString(GatherActivity.SAMPLING_JSON_ITEM)
                        val itemID = samplingInfoJson.getString(GatherActivity.SAMPLING_JSON_ITEMID)
                        val num = samplingInfoJson.getString(GatherActivity.SAMPLING_JSON_NUM)
                        val GPSInfo = samplingInfoJson.getJSONObject(GatherActivity.SAMPLING_JSON_GPS)


                        if (!isMakeUp) {        //正常保存抽样单

                            val samplingtable = SAMPLINGTABLE(null, whichTask, templetID, item + "-" + itemID, companyName, mes, num,
                                    isEditDone, false, false, false, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(), null, null,
                                    GPSInfo.getDouble(Constant.LATITUDE), GPSInfo.getDouble(Constant.LONGITUDE),
                                    GPSInfo.getInt(Constant.LOCATION_MODE), num)
                            samplingtableDao!!.insertOrReplace(samplingtable)

                            val taskinfo = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(whichTask)).list()
                            if (taskinfo.size != 1) {
                                Log.e("XXXXXXXXXXXX", "MyLayout.createTextView 1034 line 根据任务iD查询的任务数不等于1 ")
                            }
                            sendMes()
                            finish()
                        } else {                 //补采抽样单时的保存

                            if (sid_of_server!!.equals(-1L)) {//原抽样单的sid用于上传给服务器，服务器将原抽样单状态置位为“已补采”，不应为不存在
                                Toast.makeText(mContext, "没有接受到sid", Toast.LENGTH_LONG).show()
                                Log.e("XXXXXXXXXXX", "没有接受到sid")
                                return@rightFirstImageOnClick
                            }

                            val listener = Response.Listener<String> { s ->
                                Log.e("setSMadeUpSuc", s)
                                try {
                                    val resultJson = JSONObject(s)
                                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                                    //                                            String result = resultJson.getString(URLs.KEY_MESSAGE);

                                    if (errorCode == ReturnCode.Code0) {//connected

                                        //设置当前抽样单状态为“已补采”
                                        for (i in samplingtables!!.indices) {
                                            samplingtables!![i].check_status = Constant.S_STATUS_NOT_USED
                                            samplingtableDao!!.insertOrReplace(samplingtables!![i])
                                        }

                                        //将当前抽样单以后的抽样单ID+1（即将当前抽样单之后的抽样单往后挪一个，留出一个位置插入补采的抽样单）
                                        if (!samplingID!!.equals(-1L)) {
                                            val allSamplingtables = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.Id.between(samplingID!! + 1, samplingtableDao!!.queryBuilder().list().size)).list()

                                            for (i in allSamplingtables.indices) {
                                                allSamplingtables[i].id = allSamplingtables[i].id!! + 1
                                                samplingtableDao!!.insertOrReplace(allSamplingtables[i])
                                            }

                                        }

                                        //将补采抽样单插入
                                        val samplingtable = SAMPLINGTABLE(samplingID!! + 1, whichTask, templetID, "$item-$itemID-补采", companyName, mes, num,
                                                isEditDone, false, false, true, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(), null, null,
                                                GPSInfo.getDouble(Constant.LATITUDE), GPSInfo.getDouble(Constant.LONGITUDE), GPSInfo.getInt(Constant.LOCATION_MODE), num)
                                        samplingtableDao!!.insertOrReplace(samplingtable)

                                        sendMes()

                                        finish()

                                    } else {//other return code
                                        ReturnCode(applicationContext, errorCode, true)
                                    }

                                    if (saveProgressDialog.isShowing)
                                        saveProgressDialog.dismiss()

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    printLineInLog()

                                    if (saveProgressDialog.isShowing)
                                        saveProgressDialog.dismiss()
                                }
                            }

                            val errorListener = Response.ErrorListener {
                                if (saveProgressDialog.isShowing)
                                    saveProgressDialog.dismiss()

                                T.showShort(applicationContext, getString(R.string.make_up_sampling_need_network))
                            }

                            //设置服务器的当前抽样单状态为已补采
                            if (samplingtables!!.size == 1)
                                setSamplingStatusMadeUp(listener, errorListener, samplingtables!![0].sid_of_server.toString())
                            else {
                                Toast.makeText(mContext, "查询到多张原抽样单", Toast.LENGTH_LONG).show()
                                printLineInLog()
                                return@rightFirstImageOnClick
                            }

                        }

                        if (saveProgressDialog.progress == samplingCount)
                            saveProgressDialog.dismiss()


                    } catch (e: JSONException) {
                        e.printStackTrace()
                        if (saveProgressDialog.isShowing)
                            saveProgressDialog.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        T.showLong(this@GatherActivity, "生成jason错误" +
                                "" + e.toString())
                        if (saveProgressDialog.isShowing)
                            saveProgressDialog.dismiss()
                    }

                }
            }
            headPanel!!.setRightFirstImageOnClick(r)
            headPanel!!.setRightSecondImage(R.drawable.print)
            headPanel!!.seRightSecondText("打印")
            val t = RightSecondOnClick {
                var toPrint = ""

                val LL_All = lu!!.getparentView()

                for (i in 0..LL_All.childCount - 1) {
                    for (k in 0..((LL_All.getChildAt(i) as LinearLayout).getChildAt(1) as LinearLayout).childCount - 1) {
                        val myWidget = ((LL_All.getChildAt(i) as LinearLayout).getChildAt(1) as LinearLayout).getChildAt(k) as LinearLayout

                        if ((myWidget.getChildAt(0) as LinearLayout).childCount <= 2)
                        //排除没有checkbox的控件
                            continue

                        val cb = ((myWidget.getChildAt(0) as LinearLayout).getChildAt(2) as LinearLayout).getChildAt(0) as CheckBox
                        if (cb.isChecked) {

                            //判断是否是地理信息 如果是 读取并跳过 如果不是 继续判断是否是checkbox
                            if (((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString() == lu!!.TYPE_GPS_STRING && myWidget.childCount > 2) {
                                //不是多选控件的直接读取控件数据
                                if (toPrint == "")
                                    toPrint = toPrint + ">>" + ((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString() +
                                            (myWidget.getChildAt(1) as TextView).text.toString()
                                else
                                    toPrint = toPrint + "\n>>" + ((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString() +
                                            (myWidget.getChildAt(1) as TextView).text.toString()
                                continue
                            }

                            //判断是否是拍照 如果是 直接跳过

                            if (((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString() == lu!!.TYPE_CAMERA_STRING && myWidget.childCount > 2) {
                                continue
                            }


                            //判断是否是多选框的控件,是多选的要遍历每个多选框是否选择
                            if (myWidget.childCount > 2) {

                                for (l in 0..(myWidget.getChildAt(1) as LinearLayout).childCount - 1) {

                                    if (l == 0) {

                                        if (toPrint == "") {
                                            toPrint = toPrint + ">>" + ((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString()
                                            if (((myWidget.getChildAt(1) as LinearLayout).getChildAt(l) as CheckBox).isChecked)
                                                toPrint = toPrint + " " + ((myWidget.getChildAt(1) as LinearLayout).getChildAt(l) as CheckBox).text.toString()
                                        } else {
                                            toPrint = toPrint + "\n>>" + ((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString()
                                            if (((myWidget.getChildAt(1) as LinearLayout).getChildAt(l) as CheckBox).isChecked)
                                                toPrint = toPrint + " " + ((myWidget.getChildAt(1) as LinearLayout).getChildAt(l) as CheckBox).text.toString()
                                        }

                                    } else if (((myWidget.getChildAt(1) as LinearLayout).getChildAt(l) as CheckBox).isChecked) {
                                        toPrint = toPrint + " " + ((myWidget.getChildAt(1) as LinearLayout).getChildAt(l) as CheckBox).text.toString()
                                    }
                                }

                                continue
                            }

                            //不是多选控件的直接读取控件数据
                            if (toPrint == "")
                                toPrint = toPrint + ">>" + ((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString() +
                                        (myWidget.getChildAt(1) as TextView).text.toString()
                            else
                                toPrint = toPrint + "\n>>" + ((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString() +
                                        (myWidget.getChildAt(1) as TextView).text.toString()
                        }
                    }

                }

                val i = Intent(this@GatherActivity, PrintActivity::class.java)
                i.putExtra("toPrint", toPrint)
                i.putExtra("num", tv_num!!.text)
                startActivity(i)
            }
            headPanel!!.setRightSecondOnClick(t)

            //非补采时，可以点击添加一个样品信息
            if (!isMakeUp) {
                headPanel!!.setRightThirdImage(R.drawable.add_sampling)
                headPanel!!.setRightThirdText("加样")
                val thirdOnClick = HeadControlPanel.RightThirdOnClick {
                    //加样前要检查样品信息是否合法

                    companyName = etCompanyName!!.text.toString()

                    itemName = etItem!!.text.toString().replace(" ", "")
                    itemID = etItemID!!.text.toString()

                    if (tv_gps!!.text.toString() == "" || (application as CollectionApplication).latitude == -1.0 || (application as CollectionApplication).longitude == -1.0 ||
                            (application as CollectionApplication).location_mode == Constant.CAN_NOT_GET_LOCATION) {
                        T.showShort(this@GatherActivity, "正在定位中...请稍后再试")
                        return@RightThirdOnClick
                    }
                    if (itemName == "" || itemID == "") {
                        T.showShort(this@GatherActivity, getString(R.string.should_not_keep_item_info_empty))
                        return@RightThirdOnClick
                    }

                    lu!!.addOneSampling(true)

                    //添加样品需要重新签字，对当前签字清除
                    val ourSign = lu!!.samplingOurSignImaView
                    val othersSign = lu!!.samplingOtherSignImaView

                    if (ourSign == null || othersSign == null) {
                        T.showShort(parentView!!.context, "添加样品失败，获取不到对象！")

                        printLineInLog()
                        return@RightThirdOnClick
                    }

                    val cont = ""
                    ourSign.tag = cont
                    ourSign.setImageBitmap(null)
                    ourSign.setImageResource(R.drawable.edit_query)
                    othersSign.tag = cont
                    othersSign.setImageBitmap(null)
                    othersSign.setImageResource(R.drawable.edit_query)

                    reLoadVariable()
                }
                headPanel!!.setRightThirdOnClick(thirdOnClick)
                headPanel!!.setmRightThirdVisible(View.VISIBLE)
            }
        }
//        fs = FileStream(this)
//        lu = MyLayout(parentView, this, fs!!, root_path, MyLayout.DO_FORM, whichTask, null)    //生成布局的类
//        lu!!.initLayout(sheetJsonStr, true)           //初始化
//        isFixPoint = lu!!.isFixPoint        //是否是定点采样
//
//        //初始化控件
//        etCompanyName = lu!!.companyNameET                                       //赋值被抽样单位名称控件
//
//        etItem = lu!!.ItemsET                                                    //赋值样品名称控件
//        itemName = etItem!!.text.toString().replace(" ", "")                //获取样品名称
//
//        etItemID = lu!!.ItemsIDET                                                //赋值样品ID控件
//        itemID = etItemID!!.text.toString()                                 //获取样品ID
//
//        tv_num = lu!!.numberIdTV                                                 //赋值抽样单编码控件
//
//        tv_gps = lu!!.gpsIdTV                                                    //赋值GPS控件
//        (application as CollectionApplication).mLocationResult = tv_gps    //将tv_gps赋值给application中的textview
//        (application as CollectionApplication).gps_pause = false           //查看和修改模式默认gps开启
//        tv_gps!!.text = getString(R.string.gpsFailed)
//
//        bt_gps = lu!!.gpsBtnTV                                                   //赋值GPS按钮控件
//
//        tv_gps!!.setTextColor(resources.getColor(R.color.text_color_gray))
//        tv_num!!.setTextColor(resources.getColor(R.color.text_color_gray))
//
//        //根据是否是补采获取抽样单编码
//        if (isMakeUp)
//            num = tv_num!!.text.toString() + getString(R.string.resample_flag)
//        else
//            num = Util.getSamplingNum(mContext, taskinfo)
//
//        tv_num!!.text = num                                                    //设置抽样单编码
//
//        addSamplingButton = lu!!.addSamplingButton                               //暂时没用
//
//        child_path = root_path + File.separator + tv_num!!.text.toString()
//
//        lu!!.samplingBlockSubitems[lu!!.samplingBlockSubitems.size - 1].tag = child_path//将媒体文件夹的路径保存在样品情况的LinearLayout中
//
//        val cameraSearch = File(child_path)
//        if (!cameraSearch.exists())
//            cameraSearch.mkdirs()
//
//        updateGridView()
//        updateVideoGridView()
    }

    /**
     *生成一个抽样单的唯一编号（按照老师的规定）
     */
    fun getAutoGeneratedSheetID():String{
        val taskinfo = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(whichTask)).list()
        if (taskinfo.size != 1) {
            toast( "生成抽样单编号出错")
            finish()
            return ""
        }
        val taskLetter = taskinfo[0].task_letter
        return Util.getSamplingNum(this, taskLetter)//taskname+imei+date+liushui
    }

    /**
     * 添加或删除完样品情况后重新为样品名称、样品编号、抽样单编号和childpath等赋值
     */
    fun reLoadVariable() {
        etItem = lu!!.ItemsET

        etItemID = lu!!.ItemsIDET

        tv_num = lu!!.numberIdTV

        child_path = root_path + File.separator + tv_num!!.text.toString()

        lu!!.samplingBlockSubitems[lu!!.samplingBlockSubitems.size - 1].tag = child_path//将媒体文件夹的路径保存在样品情况的LinearLayout中

        updateGridView()
        updateVideoGridView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUESTCODEFORDATE)
        //日期选择器返回的日期
        {
            if (data != null) {
                val ymd = data.getStringExtra("ymd")
                val id = data.getIntExtra("btn_id", 0)
                val btn = findViewById(id) as Button
                if (ymd != null) {
                    btn.text = ymd
                } else {
                    btn.text = "重新获取日期"
                }
            }
        } else if (requestCode == REQUESTCODEFORVIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                isHaveVideo = true
                val uriVideo = data!!.data
                val video_path = uriVideo.toString()
                val video_name = File(video_path).name
                val intent = Intent().setAction(Constant.NEW_VEDIO_BROADCAST_ACT)
                sendBroadcast(intent)


            }
        } else if (requestCode == REQUESTCODEFORPICTURE) {
            if (data != null) {
                // 没有指定特定存储路径的时候
                val id = data.getIntExtra("view_id", 0)        // imageView id
                val pName = data.getStringExtra("pName")    //图片名称
                val picPath = data.getStringExtra("picture_path")
                val intent = Intent().setAction(Constant.NEW_PHOTO_BROADCAST_ACT)
                intent.putExtra(Constant.CELL_ID_EXTRA_KEY, id)
                sendBroadcast(intent)

            } else {
                L.d("data IS null, file saved on target position.")
            }
        }
    }

    /**
     * 由布局生成json字符串

     * @param isSave 是否保存
     * *
     * @return
     * *
     * @throws JSONException
     */
    @Throws(JSONException::class)
    private fun createJsonStr(isSave: Boolean, parentView: LinearLayout, media_path: String): JSONObject {

        val jsonObject = JSONObject()

        var neccessaryIsEdit = true

        val count = parentView.childCount    //获得条目数
        //parentView 下面是itemlayout,itemlayout下面是textview和linearlayout，linearLayout里面是具体的linearLayout
        val taskObject = JSONObject()    //第一层task
        val taskValue = JSONArray()    //task的值
        for (i in 0..count - 1)
        //对应七个模块
        {
            val module_layout = parentView.getChildAt(i) as LinearLayout    //模块布局
            val module_key = ((module_layout.getChildAt(0) as LinearLayout).getChildAt(0) as TextView).text.toString()
            val module_value = module_layout.getChildAt(1) as LinearLayout
            val module_object = JSONObject()
            val module_array = JSONArray()
            for (j in 0..module_value.childCount - 1) {
                var itemObject = JSONObject()
                val item_layout = module_value.getChildAt(j) as LinearLayout//条目布局
                val itemTag = item_layout.tag as HashMap<String, Any>    //获得条目信息
                val type = itemTag[MyLayout.TYPE] as String
                val necessary = itemTag[MyLayout.NECESSARY] as String
                //类型为title/sign/camera时,直接将tag转成jsonObject即可
                if (type == MyLayout.TYPE_TABLE) {
                    if (isFixPoint) {
                        itemObject = lu!!.mapToJson(itemTag, null)
                    } else {
                        val cont = itemName//将抽样单名字保存在TABLE中
                        itemObject = lu!!.mapToJson(itemTag, cont)
                    }
                } else if (type == MyLayout.TYPE_CAMERA) {

                    //获取本抽样单下的图片名称集合

                    val savedPictureNames = ArrayList<String>()

                    val cameraSearch = File(media_path)
                    if (!cameraSearch.exists())
                        cameraSearch.mkdirs()

                    //遍历文件夹获取文件名
                    if (cameraSearch.listFiles() != null) {
                        for (k in 0..cameraSearch.listFiles().size - 1) {
                            if (cameraSearch.listFiles()[k].name.startsWith("CAMERA_")) {
                                savedPictureNames.add(cameraSearch.listFiles()[k].name)
                            }
                        }
                    }

                    //将名称集mapToJson
                    itemObject = lu!!.mapToJson(itemTag, savedPictureNames)

                    //判断是否已填
                    if (necessary == "T" && savedPictureNames.isEmpty()) {
                        neccessaryIsEdit = false
                    }

                } else if (type == MyLayout.TYPE_VIDEO) {

                    val savedVedioNames = ArrayList<String>()

                    val cameraSearch = File(media_path)
                    if (!cameraSearch.exists())
                        cameraSearch.mkdirs()

                    //遍历文件夹
                    if (cameraSearch.listFiles() != null) {
                        for (k in 0..cameraSearch.listFiles().size - 1) {
                            if (cameraSearch.listFiles()[k].name.startsWith("VIDEO_")) {
                                savedVedioNames.add(cameraSearch.listFiles()[k].name)
                            }
                        }
                    }

                    //将名称集mapToJson
                    itemObject = lu!!.mapToJson(itemTag, savedVedioNames)

                    //判断是否已填
                    if (necessary == "T" && savedVedioNames.isEmpty()) {
                        neccessaryIsEdit = false
                    }
                } else if (type == MyLayout.TYPE_AUTO || type == MyLayout.TYPE_MY_COMPANY
                        || type == MyLayout.TYPE_MY_COMPANY_ADDRESS || type == MyLayout.TYPE_SAMPLING_CONTACT
                        || type == MyLayout.TYPE_SAMPLING_PHONE) {
                    val cont = (item_layout.getChildAt(1) as TextView).text.toString()    //输入的内容
                    itemObject = lu!!.mapToJson(itemTag, cont)
                } else if (type == MyLayout.TYPE_INPUT || type == MyLayout.TYPE_ADDR
                        || type == MyLayout.TYPE_COMPANY) {
                    val editText = item_layout.getChildAt(1) as EditText
                    val cont = editText.text.toString()    //输入的内容
                    itemObject = lu!!.mapToJson(itemTag, cont)
                    if (necessary == "T" && cont.isEmpty()) {
                        neccessaryIsEdit = false
                    }
                } else if (type == MyLayout.TYPE_ITEMS) {
                    val editText = item_layout.getChildAt(1) as EditText
                    val cont = editText.text.toString()    //输入的内容
                    itemObject = lu!!.mapToJson(itemTag, cont)
                    if (necessary == "T" && cont.isEmpty()) {
                        neccessaryIsEdit = false
                    }

                    jsonObject.put(GatherActivity.SAMPLING_JSON_ITEM, cont)//将样品名称保存并返回

                } else if (type == MyLayout.TYPE_ITEM_ID) {
                    val editText = item_layout.getChildAt(1) as EditText
                    val cont = editText.text.toString()    //输入的内容
                    itemObject = lu!!.mapToJson(itemTag, cont)
                    if (necessary == "T" && cont.isEmpty()) {
                        neccessaryIsEdit = false
                    }

                    jsonObject.put(GatherActivity.SAMPLING_JSON_ITEMID, cont)//将样品编号保存并返回

                } else if (type == MyLayout.TYPE_NUMBER || type == MyLayout.TYPE_COLLDATE) {
                    val cont = (item_layout.getChildAt(1) as TextView).text.toString()    //自动生成的编号
                    if (type == MyLayout.TYPE_NUMBER) {
                        num = cont
                    }
                    itemObject = lu!!.mapToJson(itemTag, cont)

                    jsonObject.put(GatherActivity.SAMPLING_JSON_NUM, cont)//将抽样单编号保存并返回

                } else if (type == MyLayout.TYPE_DATE) {
                    var cont = (item_layout.getChildAt(1) as Button).text.toString()    //日期内容
                    if (necessary == "T" && cont == MyLayout.CHOOSE_DATE) {
                        neccessaryIsEdit = false
                    }
                    if (cont == MyLayout.CHOOSE_DATE) {
                        cont = ""
                    }
                    itemObject = lu!!.mapToJson(itemTag, cont)
                } else if (type == MyLayout.TYPE_SIGN) {
                    var cont = (item_layout.getChildAt(1) as ImageView).tag as String    //签名图片名称
                    itemObject = lu!!.mapToJson(itemTag, cont)
                    if (necessary == "T" && cont == "none") {
                        neccessaryIsEdit = false
                    }
                    if (isSave) {
                        cont = ""
                        itemTag.put(MyLayout.CONT, cont as Any)    //更换Tag为空
                        (item_layout.getChildAt(1) as ImageView).tag = cont
                        (item_layout.getChildAt(1) as ImageView).setImageBitmap(null)
                        (item_layout.getChildAt(1) as ImageView).setImageResource(R.drawable.edit_query)
                    }

                } else if (type == MyLayout.TYPE_GPS) {
                    val cont = (item_layout.getChildAt(1) as TextView).text.toString()
                    itemObject = lu!!.mapToJson(itemTag, cont)
                    if (necessary == "T" && (cont.isEmpty() || cont == getString(R.string.gpsFailed))) {
                        neccessaryIsEdit = false
                    }

                    jsonObject.put(GatherActivity.SAMPLING_JSON_GPS, (item_layout.getChildAt(1) as TextView).tag as JSONObject)

                } else if (type == MyLayout.TYPE_SELECT) {
                    val multi_value = JSONArray()
                    var selectedCounter = 0
                    for (s in 0..(item_layout.getChildAt(1) as LinearLayout).childCount - 1) {

                        //获得多选框内的内容
                        val checkBox = (item_layout.getChildAt(1) as LinearLayout).getChildAt(s) as CheckBox
                        val multi_array_value = checkBox.text.toString()
                        val checkboxValue = JSONObject()
                        checkboxValue.put("value", multi_array_value)
                        checkboxValue.put("isChecked", checkBox.isChecked)
                        multi_value.put(checkboxValue)
                        if (checkBox.isChecked)
                            selectedCounter++
                    }
                    itemObject = lu!!.mapToJson(itemTag, multi_value)    //这里传的是jsonArray
                    if (necessary == "T" && selectedCounter == 0) {
                        neccessaryIsEdit = false
                    }

                }
                module_array.put(itemObject)
            }
            module_object.put(lu!!.getNameKey(module_key), module_array)
            taskValue.put(module_object)    //将itemObject 放入task里面
        }
        taskObject.put(MyLayout.TASK, taskValue)

        jsonObject.put(GatherActivity.NECCESSARY, neccessaryIsEdit)
        jsonObject.put(GatherActivity.SAMPLING_JSON_CONT, taskObject.toString())

        return jsonObject
    }

    internal var tempMode: LocationClientOption.LocationMode = LocationClientOption.LocationMode.Hight_Accuracy//定位模式
    private val tempcoor = Constant.COORDINATION_STADARD//定位坐标标准   bd09ll 百度经纬度标准    "gcj02"国家测绘局标准    "bd09"百度墨卡托标准
    //    public MyLocationListenner myListener;//定位监听器，会每隔span时间返回一次数据，后面还需要LocationClient注册定位监听器
    var mVibrator: Vibrator? = null

    override fun onDestroy() {
        //((CollectionApplication)getApplication()).mLocationResult=null;
        //        File todelete = new File(root_path + File.separator + tv_num.getText().toString());
        //        if (todelete.exists())
        //            FileUtil.delete(todelete);

//        lu!!.recycleParentView()
        unregisterAllBroadcast()
        System.gc()
        super.onDestroy()
    }

    private fun unregisterAllBroadcast() {
        for (cellUI in sheetCellUIList){
            when (cellUI.getCellType()){
                SheetProtocol().TYPE_ADDRESS -> (cellUI.cell as TypeAddress).unregisterLocationBroadcast()
                SheetProtocol().TYPE_GEOGRAPHIC_COORDINATES -> (cellUI.cell as TypeGeographicCoordinates).unregisterLocationBroadcast()
                SheetProtocol().TYPE_PHOTOS -> {
                    (cellUI.cell as TypePhotos).unregisterLocationBroadcast()
                    (cellUI.cell as TypePhotos).unregisterNewPhotoBroadcast()
                }
                SheetProtocol().TYPE_VEDIOS -> {
                    (cellUI.cell as TypeVedios).unregisterLocationBroadcast()
                    (cellUI.cell as TypeVedios).unregisterNewVedioBroadcast()
                }

            }
        }
    }

    /**
     * 更新最后一个样品grid中的图片
     */
    fun updateGridView() {

        val item_layout = lu!!.samplingBlockSubitems[lu!!.samplingBlockSubitems.size - 1]//最后一个样品的块条目
        val module_value = item_layout.getChildAt(1) as LinearLayout

        for (i in 0..module_value.childCount - 1) {
            val myWidget = module_value.getChildAt(i) as LinearLayout

            if ((myWidget.getChildAt(0) as LinearLayout).childCount == 2) {//选出照相和录像控件
                if (((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString() == lu!!.TYPE_CAMERA_STRING) {//判断这个是照相控件
                    val mGridView = myWidget.getChildAt(2) as GridViewEx

                    //定义照片集 并添加照片
                    val mImages = ArrayList<ImageInfo>()

                    mImages.add(ImageInfo(ImageInfo.PICTURE))//第一个添加的是照相图片

                    val cameraSearch = File(child_path)
                    if (!cameraSearch.exists())
                        cameraSearch.mkdirs()

                    //其他的照片遍历child文件夹得道
                    if (cameraSearch.listFiles() != null) {
                        for (j in 0..cameraSearch.listFiles().size - 1) {
                            if (cameraSearch.listFiles()[j].name.startsWith("CAMERA_")) {
                                mImages.add(ImageInfo(cameraSearch.listFiles()[j].path))

                            }
                        }
                    }

                    mGridView.tag = mImages
                    mGridView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val mImages = mGridView.tag as ArrayList<ImageInfo>

                        if (i == 0) {//点击第一个是拍照控件

                            val intent = Intent(this@GatherActivity, CameraView::class.java)
                            intent.putExtra("root_path", child_path)    //将存储图片根目录传递过去
                            intent.putExtra("view_id", GatherActivity.REQUESTCODEFORPICTURE + 0)        //控件ID
                            intent.putExtra("location", tv_gps!!.text.toString())
                            intent.putExtra("number", tv_num!!.text.toString())
                            startActivityForResult(intent, GatherActivity.REQUESTCODEFORPICTURE)
                            return@OnItemClickListener
                        }

                        //其他的是查看照片
                        val intent = Intent(this@GatherActivity, PictureViewActivity::class.java)
                        intent.putExtra("picPath", mImages[i].localPath)
                        startActivity(intent)
                    }

                    mGridView.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
                        val mImages = mGridView.tag as ArrayList<ImageInfo>
                        if (i != 0) {
                            val file = File(mImages[i].localPath)
                            if (dialog!!.isShowing) {
                                return@OnItemLongClickListener false
                            }

                            val inflater = this@GatherActivity
                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val layout = inflater.inflate(
                                    R.layout.dialogview_two_button, null) as RelativeLayout
                            val Title = layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu) as TextView
                            val Message = layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu) as TextView

                            val positivebutton = layout.findViewById(R.id.textview_positive_button) as TextView
                            val negativebutton = layout.findViewById(R.id.textview_negative_button) as TextView

                            Title.text = "删除该图片"
                            Message.text = "确认删除？"

                            positivebutton.setOnClickListener {
                                dialog!!.dismiss()
                                if (file.exists()) {
                                    file.delete()
                                    updateGridView()
                                } else
                                    Log.e("XXXXXXXX", "长按删除文件，文件不存在，不应该")
                            }
                            negativebutton.setOnClickListener { dialog!!.dismiss() }


                            dialog!!.setContentView(layout)
                            dialog!!.setCancelable(false)
                            dialog!!.show()

                        }
                        true
                    }

                    (mGridView.adapter as SampleExelGridAdapter).setmImages(mImages)//自动notify数据
                }
            }
        }

    }


    /**
     * 更新最后一个样品Videogrid中的图片
     */
    fun updateVideoGridView() {

        val item_layout = lu!!.samplingBlockSubitems[lu!!.samplingBlockSubitems.size - 1]//最后一个样品的块条目
        val module_value = item_layout.getChildAt(1) as LinearLayout

        for (i in 0..module_value.childCount - 1) {
            val myWidget = module_value.getChildAt(i) as LinearLayout

            if ((myWidget.getChildAt(0) as LinearLayout).childCount == 2) {//选出照相和录像控件
                if (((myWidget.getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString() == lu!!.TYPE_VIDEO_STRING) {//判断这个是录像控件
                    val mGridView = myWidget.getChildAt(2) as GridViewEx

                    //定义照片集 并添加照片
                    val mImages = ArrayList<ImageInfo>()

                    mImages.add(ImageInfo(ImageInfo.VIDEO))//第一个添加的是

                    val cameraSearch = File(child_path)
                    if (!cameraSearch.exists())
                        cameraSearch.mkdirs()


                    //其他的照片遍历child文件夹得道
                    if (cameraSearch.listFiles() != null) {
                        for (j in 0..cameraSearch.listFiles().size - 1) {
                            if (cameraSearch.listFiles()[j].name.startsWith("VIDEO_")) {
                                val video_image = ImageInfo(createVideoThumbnail(cameraSearch.listFiles()[j].path, 0))
                                video_image.mediaType = ImageInfo.VIDEOBMP
                                video_image.localPath = cameraSearch.listFiles()[j].path
                                mImages.add(video_image)
                            }
                        }
                    }

                    mGridView.tag = mImages
                    mGridView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val mImages = mGridView.tag as ArrayList<ImageInfo>
                        if (i == 0) {//点击第一个是录像控件

                            val video_name = File(child_path + File.separator + "VIDEO_" + (mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId + Util.getCurrentTime("yyMMddHHmmss") + ".mp4")
                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0)//更改录像质量 min=0 max =1
                            //intent.putExtra("view_id", v.getId());
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(video_name))
                            startActivityForResult(intent, GatherActivity.REQUESTCODEFORVIDEO)
                            return@OnItemClickListener
                        }
                        //其他的是查看照片
                        val url = Uri.parse("file://" + mImages[i].localPath)
                        val type = "video/mp4"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(url, type)
                        startActivity(intent)
                    }

                    mGridView.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
                        val mImages = mGridView.tag as ArrayList<ImageInfo>
                        if (i != 0) {
                            val file = File(mImages[i].localPath)
                            if (dialog!!.isShowing) {
                                return@OnItemLongClickListener false
                            }

                            val inflater = this@GatherActivity
                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val layout = inflater.inflate(
                                    R.layout.dialogview_two_button, null) as RelativeLayout
                            val Title = layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu) as TextView
                            val Message = layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu) as TextView

                            val positivebutton = layout.findViewById(R.id.textview_positive_button) as TextView
                            val negativebutton = layout.findViewById(R.id.textview_negative_button) as TextView

                            Title.text = "删除该视频"
                            Message.text = "确认删除？"

                            positivebutton.setOnClickListener {
                                dialog!!.dismiss()
                                if (file.exists()) {
                                    file.delete()
                                    updateVideoGridView()
                                } else
                                    Log.e("XXXXXXXX", "长按删除文件，文件不存在，不应该")
                            }
                            negativebutton.setOnClickListener { dialog!!.dismiss() }


                            dialog!!.setContentView(layout)
                            dialog!!.setCancelable(false)
                            dialog!!.show()
                        }
                        true
                    }

                    (mGridView.adapter as SampleExelGridAdapter).setmImages(mImages)//自动notify数据
                }
            }
        }

    }

    /**
     * 截取视频制定时间的帧画面

     * @param filePath
     * *
     * @return
     */
    fun createVideoThumbnail(filePath: String, time: Long): Bitmap {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        bitmap = retriever.getFrameAtTime(time)
        return bitmap
    }


    override fun onResume() {
        super.onResume()
//        if (isHaveVideo && lu!!.vidIMGObject != null) {
//            lu!!.vidIMGObject.visibility = View.GONE
//        } else if (!isHaveVideo && lu!!.vidIMGObject != null) {
//            lu!!.vidIMGObject.visibility = View.GONE
//        }
//
//        if (isHavePicture && lu!!.picIMGObjet != null) {
//            lu!!.picIMGObjet.visibility = View.GONE
//        } else if (!isHavePicture && lu!!.picIMGObjet != null) {
//            lu!!.picIMGObjet.visibility = View.GONE
//        }

        val gps = Util.isOpen(this)
        val isNetConnected = (application as CollectionApplication).isNetworkConnected
        if (!gps) {
            openGPSDialog(gps, isNetConnected)

        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {    //监控back键
            val alertDialog: AlertDialog
            val builder = AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
            builder.setTitle("温馨提示")
            builder.setMessage("确定要返回吗?")
            builder.setPositiveButton("确定") { dialog, which ->
                //TODO 判断新拍的照片并删除
                setResult(Constant.WEIXINTASKREFRESHITEM_FROMDO)
                Util.decendLocalSeq(applicationContext, 1)
                finish()    //退出
            }
            builder.setNegativeButton("取消", null)
            alertDialog = builder.create()
            alertDialog.show()    //显示对话框
        }
        return super.onKeyDown(keyCode, event)
    }

    private var dialog: Dialog? = null

    /**
     * 打开GPS提示对话框
     */
    internal fun openGPSDialog(isGpsOpened: Boolean, isNetWorkOpened: Boolean) {
        if (isGpsOpened)
            return

        if (dialog!!.isShowing) {
            return
        }

        val inflater = this@GatherActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(
                R.layout.dialogview_three_button, null) as RelativeLayout
        val Title = layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu) as TextView
        val Message = layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu) as TextView

        val positivebutton = layout.findViewById(R.id.textview_positive_button) as TextView
        val openGPRSButton = layout.findViewById(R.id.textview_openGPRS_button) as TextView
        val negativebutton = layout.findViewById(R.id.textview_negative_button) as TextView

        if (isGpsOpened && !isNetWorkOpened) {
            positivebutton.visibility = View.GONE
            openGPRSButton.visibility = View.VISIBLE
            Title.text = "获取位置的描述信息需要开启网络"
            Message.text = "建议开启"
        } else if (!isGpsOpened && isNetWorkOpened) {
            positivebutton.visibility = View.VISIBLE
            openGPRSButton.visibility = View.GONE
            Title.text = "本软件需开启GPS定位开关"
            Message.text = "必须开启，否则无法打开抽样单"
        } else if (!isGpsOpened && !isNetWorkOpened) {
            positivebutton.visibility = View.VISIBLE
            openGPRSButton.visibility = View.VISIBLE
            Title.text = "本软件需开启GPS定位开关\n获取位置的描述信息需要开启网络"
            Message.text = "是否开启？"
        }

        positivebutton.setOnClickListener {
            dialog!!.dismiss()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        openGPRSButton.setOnClickListener {
            dialog!!.dismiss()
            if (android.os.Build.VERSION.SDK_INT > 10) {
                //3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
            } else {
                startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
            }
        }

        negativebutton.setOnClickListener {
            dialog!!.dismiss()
            if (!isGpsOpened)
                this@GatherActivity.finish()
        }


        dialog!!.setContentView(layout)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }


    /**
     * 保存的时候 设置抽样单状态为已补采

     * @param listener
     * *
     * @param errorListener
     * *
     * @param sid
     */
    fun setSamplingStatusMadeUp(listener: Response.Listener<String>, errorListener: Response.ErrorListener, sid: String) {
        val stringRequest = API.setSamplingStatusMadeUp(listener, errorListener, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String, sid)
        queue!!.add(stringRequest)
    }

    /**
     * 打印行数到日志
     */
    fun printLineInLog() {
        Log.e(GatherActivity::class.java.name, Util.getLineInfo())
    }

    companion object {
        val REQUESTCODEFORDATE = 100001            //接收选择的日期
        //    public static final int REQUESTCODEFORSIGN = 10001;        //接收签名后的图片
        val REQUESTCODEFORPICTURE = 1001           //接收拍照后的图片
        val REQUESTCODEFORVIDEO = 2001             //接收录像的名称

        val NECCESSARY = "neccessary"         //从json中解析必填是否全部填好
        val SAMPLING_JSON_CONT = "cont"       //从json中解析出抽样单界面转成的json
        val SAMPLING_JSON_ITEM = "item"       //从json中解析样品名称
        val SAMPLING_JSON_ITEMID = "itemID"   //从json中解析样品编号
        val SAMPLING_JSON_NUM = "num"         //从json中解析出抽样单编号
        val SAMPLING_JSON_GPS = "gps"         //从json中解析出GPS信息
    }
}
