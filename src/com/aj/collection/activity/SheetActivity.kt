package com.aj.collection.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.util.Log
import android.view.*
import android.widget.*
import com.aj.Constant
import com.aj.collection.*
import com.aj.collection.bean.SheetCell
import com.aj.collection.http.API
import com.aj.collection.http.ReturnCode
import com.aj.collection.http.URLs
import com.aj.collection.tools.*
import com.aj.collection.ui.SheetCellUI
import com.aj.collection.database.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.baidu.location.BDLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class SheetActivity : AppCompatActivity() {

    internal var intent: Intent? = null                 //传递时间的意图
    internal var sheetJsonStr: String = ""                 //抽样单json
    internal var templetID: Long = 0
    internal var templetName: String = ""             //抽样单模板名称
    internal var samplingID: Long? = -1L          // 数据库中的抽样单ID（被点击的抽样单ID）
    // 抽样单的内容（界面层面的）
    val sheetCellUIList = ArrayList<SheetCellUI>()
    // 多张抽样单列表
    val sheetList = ArrayList<ArrayList<SheetCellUI>>()
    // 抽样单模板类(Json 转为的相应对象)
    var sheetCellList = ArrayList<SheetCell>()

    // 该类可以实现的几种模式
    internal var currentMode = MODE_TEMPLATE // 模板模式，可以进行加样
    internal var mediaRootPath: String = ""  // 媒体文件的根目录
    internal var sid_of_server: Long? = null  // 原抽样单的sid
    internal var autoGeneratedSheetID = ""  // 手机生成的特定格式抽样单编号
    internal var taskID: Long = 0  // 被点击的任务的ID
    internal var taskName: String = ""  // 任务名称
    internal var sheetScrollView: ScrollView? = null // 抽样单界面的滚动界面
    internal var parentView: LinearLayout? = null  // 抽样单界面的根View，所有单元格都由此添加
    internal var smsManager = SmsManager.getDefault()
    private var phone_number = "13100958919"
    private var kaiguan = false

    internal var queue: RequestQueue? = null //init Volley;
    private val mContext = this

    //database part
    var daoSession: DaoSession? = null
    var taskinfoDao: TASKINFODao? = null
    var templettableDao: TEMPLETTABLEDao? = null
    var samplingtableDao: SAMPLINGTABLEDao? = null

    private var sampleTable: SAMPLINGTABLE? = null
    private var taskinfo: TASKINFO? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        dialog = Dialog(this@SheetActivity)
        ExitApplication.getInstance().addActivity(this)

        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.form_layout)

        when (currentMode) {
            MODE_TEMPLATE -> supportActionBar?.title = "填写抽样单"
            MODE_EDIT -> supportActionBar?.title = "编辑抽样单"
            MODE_LOOK_THROUGH -> supportActionBar?.title = "查看抽样单"
            MODE_MAKE_UP -> supportActionBar?.title = "补采抽样单"
        }

        // 初始化网络框架和数据库框架
        queue = (application as CollectionApplication).requestQueue
        daoSession = ((mContext as Activity).application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession!!.taskinfoDao
        templettableDao = daoSession!!.templettableDao
        samplingtableDao = daoSession!!.samplingtableDao

        val EMPTY_STRING = ""
        phone_number = SPUtils.get(this, SPUtils.JIANKONG, EMPTY_STRING, SPUtils.get(this, SPUtils.LOGIN_USER, EMPTY_STRING, SPUtils.USER_DATA) as String) as String

        //发送短信的开关是否打开
        val `object` = SPUtils.get(this, SPUtils.KAIGUAN, false, SPUtils.get(this, SPUtils.LOGIN_USER, EMPTY_STRING, SPUtils.USER_DATA) as String)
        if (`object` != null)
            kaiguan = `object` as Boolean
        else
            kaiguan = false

        // 初始化界面容器
        parentView = findViewById(R.id.form_parent) as LinearLayout
        sheetScrollView = findViewById(R.id.sheet_scroll_view) as ScrollView

        // 没有下列7行代码会产生焦点问题。EditText获取焦点后，下滑ScrollView会跳回EditText
        val scroll = findViewById(R.id.sheet_scroll_view) as ScrollView
        scroll.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS)
        scroll.setFocusable(true)
        scroll.setFocusableInTouchMode(true)
        scroll.setOnTouchListener { v, event ->
            v.requestFocusFromTouch()
            return@setOnTouchListener false
        }

        //Intent中包含了关于抽样单的信息
        intent = getIntent()
        currentMode = intent!!.getIntExtra("Mode", MODE_TEMPLATE)  // 获取加载模式
        //获取各种传递来信息
        if (currentMode == MODE_MAKE_UP || currentMode == MODE_LOOK_THROUGH || currentMode == MODE_EDIT) {
            //补采模式，需要获取服务器的ID
            sid_of_server = intent!!.getLongExtra("sid_of_server", -1L)   //获取SID
            samplingID = intent!!.getLongExtra("samplingID", -1L)         //获取抽样单的ID
            sampleTable = querySampleTable(samplingID!!)  // 查询抽样单
            sheetJsonStr = sampleTable!!.sampling_content  //获得抽样单JSON
            taskID = sampleTable!!.taskID  // 数据库中的任务ID
            templetID = sampleTable!!.templetID  // 数据库中的模板ID
            // 获取存取媒体的文件名
            autoGeneratedSheetID = sampleTable?.media_folder ?: ""
            if (autoGeneratedSheetID.isEmpty()) {
                toast("查询媒体文件夹失败")
                finish()
            }
            // 查询模板
            val template = templettableDao?.queryBuilder()?.where(TEMPLETTABLEDao.Properties.TempletID.
                    eq(templetID))?.list()?.get(0)
            val templateContent = template?.templet_content ?: ""
            if (templateContent.isEmpty()) {
                toast("查询模板数据失败")
                finish()
            } else {
                parseJson2UI(templateContent)
                parseJsonIntoUI(sheetJsonStr)
            }
        } else if (currentMode == MODE_TEMPLATE) {
            // 模板模式
            sid_of_server = -1L  //不是补采，服务器的ID为-1
            taskID = intent!!.getLongExtra("taskID", -1)  //获取任务ID
            sheetJsonStr = intent!!.getStringExtra("res")  //获得抽样单JSON
            templetID = intent!!.getLongExtra("templetID", -1)  //获取模板ID
            // 导入分割界面
            parentView?.addView(importSheetDividerUI())
            // 加载界面前生成一个抽样单唯一ID
            autoGeneratedSheetID = getAutoGeneratedSheetID()
            parseJson2UI(sheetJsonStr)
        } else {
            toast("未知模式")
            finish()
        }

        // 根据任务ID在数据库中查找任务信息
        val taskinfos = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list()//查询任务
        if (taskinfos.size != 1) { // 只能有一个任务被查到
            printLineInLog()
            finish()
        } else
            taskinfo = taskinfos[0]

        taskName = taskinfo!!.task_name                              //获取任务名称
        templetName = templettableDao!!.queryBuilder().where(TEMPLETTABLEDao.Properties.TempletID.eq(templetID)).list()[0].templet_name       //抽样单模板名称

        //获取并检查创建媒体文件夹
        mediaRootPath = Util.getMediaFolder(mContext)
        if (mediaRootPath.isEmpty()) {
            Toast.makeText(mContext, "没有媒体文件夹", Toast.LENGTH_LONG).show()
            finish()
        }
        val taskfolder = File(mediaRootPath)
        if (!taskfolder.exists() && !taskfolder.mkdirs()) {
            Toast.makeText(mContext, "没有媒体文件夹", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (currentMode) {
            MODE_TEMPLATE -> {
                val inflater = menuInflater
                inflater.inflate(R.menu.sheet_menu_add_print_save, menu)
            }
            MODE_MAKE_UP, MODE_EDIT -> {
                val inflater = menuInflater
                inflater.inflate(R.menu.sheet_menu_print_save, menu)
            }
            MODE_LOOK_THROUGH -> {
                val inflater = menuInflater
                inflater.inflate(R.menu.sheet_menu_print, menu)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_bar_add_sample -> {
                //加样前要检查样品信息是否合法
                // 判断GPS信息
                for (sheetCellUI in sheetCellUIList) {
                    if (!sheetCellUI.cell.isFilled()) {
                        when (sheetCellUI.getCellType()) {
                            SheetProtocol().TYPE_ADDRESS -> {
                                toast("正在定位中，请稍后保存")
                                return true
                            }
                            SheetProtocol().TYPE_GEOGRAPHIC_COORDINATES -> {
                                toast("正在定位中，请稍后保存")
                                return true
                            }
                        }
                    }
                }
                // 设置当前所有单元格为不可编辑+设置当前所有单元格为不打印
                for (sheetCellUI in sheetCellUIList) {
                    sheetCellUI.cell.setCellDisable()
                    sheetCellUI.cell.setCellNotPrint()
                }
                // 取消注册当前抽样单的广播接收器
                unregisterAllBroadcast()
                // 滚动到最底下
                sheetScrollView?.fullScroll(ScrollView.FOCUS_DOWN)
                // 导入分割界面
                parentView?.addView(importSheetDividerUI())
                // 设置新的抽样单ID
                autoGeneratedSheetID = getAutoGeneratedSheetID()
                // 添加新的抽样单，可复制的单元格生成界面，不可复制的不生成界面，复制原内容到单元格列表
                addSample()
            }
            R.id.action_bar_print -> {
                var toPrint = ""
                // 获取打印内容
                for (sheetCellUI in sheetCellUIList) {
                    toPrint += sheetCellUI.cell.getPrintContent() + "\n"
                }
                val i = Intent(this@SheetActivity, PrintActivity::class.java)
                i.putExtra("toPrint", toPrint)
                i.putExtra("num", sheetList[0][0].autoGeneratedSheetID)
                startActivity(i)
            }
            R.id.action_bar_save -> {
                //抽样单模板的保存按钮
                // 判断必填项是否全部填写
                var isAllRequiredCellFill = true
                for (sheetCellUI in sheetCellUIList) {
                    if (!sheetCellUI.cell.isFilled()) {
                        isAllRequiredCellFill = false
                        when (sheetCellUI.getCellType()) {
                            SheetProtocol().TYPE_ADDRESS -> {
                                toast("正在定位中，请稍后")
                                return true
                            }
                            SheetProtocol().TYPE_GEOGRAPHIC_COORDINATES -> {
                                toast("正在定位中，请稍后")
                                return true
                            }
                        }
                    }
                }


                val sheetCount = sheetList.size
                val saveProgressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
                saveProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                saveProgressDialog.max = sheetCount
                saveProgressDialog.progress = 1
                saveProgressDialog.setMessage("保存中...")
                saveProgressDialog.setCancelable(false)
                saveProgressDialog.setOnCancelListener { }
                saveProgressDialog.show()

                for (i in 0..sheetCount - 1) {

                    saveProgressDialog.progress = i + 1
                    saveProgressDialog.setMessage("样品" + (i + 1).toString() + "保存中...")


                    try {
                        // 当前表的单元格列表
                        val cellUIList = sheetList[i]
                        val samplingInfoJson = getJsonFromSheet(cellUIList)

                        // 寻找GPS信息单元格
                        var latitudeStr = 0.0
                        var longitudeStr = 0.0
                        var locationType = BDLocation.TypeOffLineLocation
                        val cellGeographicCoordinates = getGPSInfoBySheet(cellUIList)
                        if (cellGeographicCoordinates != null &&
                                TypeGeographicCoordinates::class.java.isInstance(cellGeographicCoordinates.cell)) {
                            latitudeStr = (cellGeographicCoordinates.cell as TypeGeographicCoordinates).latitude.toDouble()
                            longitudeStr = (cellGeographicCoordinates.cell as TypeGeographicCoordinates).longitude.toDouble()
                            locationType = (cellGeographicCoordinates.cell as TypeGeographicCoordinates).location_info_type
                        }

                        when (currentMode) {
                            MODE_TEMPLATE -> {
                                //正常保存抽样单
                                val samplingtable = SAMPLINGTABLE(null, taskID, templetID, sheetList[i][0].cell.get_cell_value(),
                                        sheetList[i][1].cell.get_cell_value(), samplingInfoJson.toString(), autoGeneratedSheetID,
                                        isAllRequiredCellFill, false, false, false, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(), null, null,
                                        latitudeStr, longitudeStr, locationType, autoGeneratedSheetID, false)
                                samplingtableDao!!.insertOrReplace(samplingtable)

                                val taskinfo = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list()
                                if (taskinfo.size != 1) {
                                    Log.e("XXXXXXXXXXXX", "MyLayout.createTextView 1034 line 根据任务iD查询的任务数不等于1 ")
                                }
                                sendMes()
                                finish()
                            }
                            MODE_EDIT -> {
                                //正常保存抽样单
                                sampleTable?.show_name = sheetList[i][0].cell.get_cell_value()
                                sampleTable?.sampling_address = sheetList[i][1].cell.get_cell_value()
                                sampleTable?.sampling_content = samplingInfoJson.toString()
                                sampleTable?.is_saved = isAllRequiredCellFill
                                sampleTable?.latitude = latitudeStr
                                sampleTable?.longitude = longitudeStr
                                sampleTable?.location_mode = locationType
                                samplingtableDao!!.insertOrReplace(sampleTable)

                                val taskinfo = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list()
                                if (taskinfo.size != 1) {
                                    Log.e("XXXXXXXXXXXX", "MyLayout.createTextView 1034 line 根据任务iD查询的任务数不等于1 ")
                                }
                                sendMes()
                                finish()
                            }
                            MODE_MAKE_UP -> {
                                //非补采的抽样单，获取服务器ID后保存
                                if (sid_of_server!!.equals(-1L)) {//原抽样单的sid用于上传给服务器，服务器将原抽样单状态置位为“已补采”，不应为不存在
                                    Toast.makeText(mContext, "没有接受到sid", Toast.LENGTH_LONG).show()
                                    Log.e("XXXXXXXXXXX", "没有接受到sid")
                                    return true
                                }

                                val listener = Response.Listener<String> { s ->
                                    Log.e("setSMadeUpSuc", s)
                                    try {
                                        val resultJson = JSONObject(s)
                                        val errorCode = resultJson.getString(URLs.KEY_ERROR)

                                        if (errorCode == ReturnCode.Code0) {//connected

                                            //设置当前抽样单状态为“已补采”
                                            sampleTable!!.check_status = Constant.S_STATUS_NOT_USED
                                            samplingtableDao!!.insertOrReplace(sampleTable)

                                            //将当前抽样单以后的抽样单ID+1（即将当前抽样单之后的抽样单往后挪一个，留出一个位置插入补采的抽样单）
                                            if (!samplingID!!.equals(-1L)) {
                                                val allSamplingtables = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.Id.between(samplingID!! + 1, samplingtableDao!!.queryBuilder().list().size)).list()

                                                for (i in allSamplingtables.indices) {
                                                    allSamplingtables[i].id = allSamplingtables[i].id!! + 1
                                                    samplingtableDao!!.insertOrReplace(allSamplingtables[i])
                                                }
                                            }

                                            //将补采抽样单插入
                                            val samplingtable = SAMPLINGTABLE(samplingID!! + 1, taskID, templetID, sheetList[i][0].cell.get_cell_value() + "-补采",
                                                    sheetList[i][1].cell.get_cell_value(), samplingInfoJson.toString(), autoGeneratedSheetID,
                                                    isAllRequiredCellFill, false, false, true, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(), null, null,
                                                    latitudeStr, longitudeStr, locationType, autoGeneratedSheetID, false)
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
                                setSamplingStatusMadeUp(listener, errorListener, sampleTable!!.sid_of_server.toString())
                            }
                        }

                        if (saveProgressDialog.progress == sheetCount)
                            saveProgressDialog.dismiss()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        if (saveProgressDialog.isShowing)
                            saveProgressDialog.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        T.showLong(this@SheetActivity, "生成jason错误" +
                                "" + e.toString())
                        if (saveProgressDialog.isShowing)
                            saveProgressDialog.dismiss()
                    }

                }
            }
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

    /**
     * 导入抽样单的分割界面
     */
    fun importSheetDividerUI(): TextView {
        // 导入界面
        val mInflater: LayoutInflater = LayoutInflater.from(mContext)
        val contentView = mInflater.inflate(R.layout.sheet_divider, null)
        val textView = contentView.findViewById(R.id.text_view_sheet_divider) as TextView
        val ithSheet: String = (sheetList.size + 1).toString()
        textView.text = "第 $ithSheet 个样品:"
        return textView
    }

    /**
     * 将Json解析成界面
     */
    fun parseJson2UI(sheetJsonStr: String) {
        // 抽样单Json文本转为Json对象
        val resultJson = JSONObject(sheetJsonStr)
        if (!resultJson.has(SheetProtocol().SHEET_JSON_KEY)) {
            toast(getString(R.string.sheet_parse_error))
            finish()
        }

        // 提取抽样单Json文本
        var sheetCells = resultJson.getString(SheetProtocol().SHEET_JSON_KEY)
        // 利用Gson将Json文本转为SheetCell列表
        val turnsType = object : TypeToken<List<SheetCell>>() {}.type
        sheetCellList = Gson().fromJson(sheetCells, turnsType)
        // 生成并加载界面
        for (sheetCell: SheetCell in sheetCellList) {
            val cellUI = SheetCellUI(this, sheetCell, autoGeneratedSheetID)
            sheetCellUIList.add(cellUI)
            parentView!!.addView(cellUI.cell.getView())
        }
        // 将当前抽样单添加到列表
        sheetList.add(sheetCellUIList)
    }

    /**
     * 将Json数据填入UI
     */
    fun parseJsonIntoUI(sheetJsonStr: String) {
        // 抽样单Json文本转为Json对象
        val resultJson = JSONObject(sheetJsonStr)
        if (!resultJson.has(SheetProtocol().SHEET_JSON_KEY)) {
            toast(getString(R.string.sheet_parse_error))
            finish()
        }

        // 提取抽样单Json文本
        val sheetCells = resultJson.getString(SheetProtocol().SHEET_JSON_KEY)
        // 利用Gson将Json文本转为SheetCell列表
        val turnsType = object : TypeToken<List<SheetCell>>() {}.type
        val filledSheetCellList: List<SheetCell> = Gson().fromJson(sheetCells, turnsType)
        // 将抽样单值填入界面
        for (i in sheetCellUIList.indices) {
            val currentCellUI = sheetCellUIList[i]  // 当前加载的模板
            val currentFilledCell = filledSheetCellList[i]  // 已填写的抽样单

            when (currentMode) {
                MODE_EDIT, MODE_MAKE_UP -> {  // 编辑模式和补采模式下需要将填写的内容填入界面
                    currentCellUI.cell.setFilledContent(currentFilledCell.cell_value)
                }
                MODE_LOOK_THROUGH -> {  // 浏览模式下除了需要将填写的内容填入界面，还要禁止编辑操作
                    currentCellUI.cell.setFilledContent(currentFilledCell.cell_value)
                    currentCellUI.cell.setCellDisable()
                }
                else -> {
                }
            }

        }
    }

    /**
     * 根据抽样单ID在数据库中查找抽样单
     */
    fun querySampleTable(samplingID: Long): SAMPLINGTABLE {
        var samplingtables: List<SAMPLINGTABLE>? = null
        if (samplingID != -1L)
            samplingtables = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.Id.eq(samplingID)).list()
        if (samplingtables!!.size != 1) {  // 只能有一个抽样单被查到
            toast("查找抽样单遇到错误")
            finish()
        }
        return samplingtables[0]  // 在数据库中查询到抽样单
    }

    /**
     *生成一个抽样单的唯一编号（按照老师的规定）
     */
    fun getAutoGeneratedSheetID(): String {
        val taskinfo = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list()
        if (taskinfo.size != 1) {
            toast("生成抽样单编号出错")
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


    }

    /**
     * 将界面解析成Json
     */
    fun getJsonFromSheet(sheetCellUIList: ArrayList<SheetCellUI>): JSONObject {
        var jsonObject = JSONObject()
        val jsonArray = JSONArray()  //
        for (sheetCellUI in sheetCellUIList) {
            jsonArray.put(sheetCellUI.cell.getJsonContent())
        }
        jsonObject.put(SheetProtocol().SHEET_JSON_KEY, jsonArray)
        return jsonObject
    }

    /**
     * 寻找抽样单中的地理位置单元格，如果没有返回空
     */
    fun getGPSInfoBySheet(sheetCellUIList: ArrayList<SheetCellUI>): SheetCellUI? {
        for (sheetCellUI in sheetCellUIList) {
            if (sheetCellUI.getCellType() == SheetProtocol().TYPE_GEOGRAPHIC_COORDINATES)
                return sheetCellUI
        }
        return null
    }

    /**
     * 添加新的抽样单，可复制的单元格生成界面，不可复制的不生成界面，复制原内容到单元格列表
     */
    fun addSample() {
        // 初始化新的单元格列表
        val newSheetCellUIList = ArrayList<SheetCellUI>()

        try {
            // 生成并加载界面
            for (i in sheetCellList.indices) {
                if (sheetCellList[i].cell_copyable == SheetProtocol().True) {
                    // 可复制的单元格，生成新的单元格
                    val cellUI = SheetCellUI(this, sheetCellList[i], autoGeneratedSheetID)
                    newSheetCellUIList.add(cellUI)
                    parentView!!.addView(cellUI.cell.getView())
                } else {
                    // 不可复制的单元格，界面上不添加， 单元格列表中添加已经填写的单元格
                    newSheetCellUIList.add(sheetCellUIList.get(i))
                }
            }
            // 清除全局单元格列表
            sheetCellUIList.removeAll(sheetCellUIList)
            // 将新的单元格列表赋值到全局单元格列表
            sheetCellUIList.addAll(newSheetCellUIList)
            // 添加新的单元格列表到抽样单列表中
            sheetList.add(sheetCellUIList)
        } catch (e: OutOfMemoryError) {
            sheetList.remove(sheetCellUIList)
            toast("内存不足以继续添加样品，请保存当前样品后再添加")
        } catch (e: Exception){
            sheetList.remove(sheetCellUIList)
            toast("添加样品过多，请保存当前样品后再添加")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUESTCODEFORVIDEO) {
            if (resultCode == Activity.RESULT_OK) {
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

    override fun onDestroy() {
        //((CollectionApplication)getApplication()).mLocationResult=null;
        //        File todelete = new File(mediaRootPath + File.separator + tv_num.getText().toString());
        //        if (todelete.exists())
        //            FileUtil.delete(todelete);

//        lu!!.recycleParentView()
        unregisterAllBroadcast()
        super.onDestroy()
    }

    /**
     * 在Destroy前取消抽样单中的注册广播
     */
    private fun unregisterAllBroadcast() {
        for (cellUI in sheetCellUIList) {
            when (cellUI.getCellType()) {
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

    override fun onResume() {
        super.onResume()
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
//                Util.decendLocalSeq(applicationContext, 1)
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

        val inflater = this@SheetActivity
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
                this@SheetActivity.finish()
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
        Log.e(SheetActivity::class.java.name, Util.getLineInfo())
    }

    companion object {
        val REQUESTCODEFORDATE = 100001            //接收选择的日期
        //    public static final int REQUESTCODEFORSIGN = 10001;        //接收签名后的图片
        val REQUESTCODEFORPICTURE = 1001           //接收拍照后的图片
        val REQUESTCODEFORVIDEO = 2001             //接收录像的名称

        // 界面加载模式
        val MODE_TEMPLATE = 0 // 模板模式，可以进行加样
        val MODE_EDIT = 1 // 编辑模式，不可以进行加样
        val MODE_LOOK_THROUGH = 2 // 查看模式，不可以进行加样，并且不可编辑
        val MODE_MAKE_UP = 3 // 补采模式，不可以进行加样
    }

    /**
     * 发送短信(废弃)
     */
    fun sendMes() {
        if (!kaiguan) {
            SPUtils.put(this@SheetActivity, taskName, true, SPUtils.WHICHTASK)
            T.showShort(this@SheetActivity, "保存成功")
            return
        }
        val sms_content = ""
        if (sms_content.length > 70) {
            val contents = smsManager.divideMessage(sms_content)
            for (sms in contents) {
                smsManager.sendTextMessage(phone_number, null, sms, null, null)
            }
        } else {
            smsManager.sendTextMessage(phone_number, null, sms_content, null, null)
        }
        SPUtils.put(this@SheetActivity, taskName, true, SPUtils.WHICHTASK)
        T.showShort(this@SheetActivity, "保存成功，并已发送定位短信")
    }
}
