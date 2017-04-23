package com.aj.collection.adapters

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aj.Constant
import com.aj.WeixinActivityMain
import com.aj.collection.R
import com.aj.collection.activity.CollectionApplication
import com.aj.collection.activity.GatherActivity
import com.aj.collection.activity.http.API
import com.aj.collection.activity.http.ReturnCode
import com.aj.collection.activity.http.URLs
import com.aj.collection.activity.tools.SPUtils
import com.aj.collection.activity.tools.Util
import com.aj.collection.activity.ui.widget.FileUtil
import com.aj.collection.bean.Counter
import com.aj.collection.bean.TaskInfo
import com.aj.database.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils
import com.thoughtbot.expandablerecyclerview.models.ExpandableListPosition
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.MultiTypeExpandableRecyclerViewAdapter
import okhttp3.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onLongClick
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.ArrayList


/**
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */
class TaskAdapter(val mContext: Context, var taskList: ArrayList<TaskData>?) : MultiTypeExpandableRecyclerViewAdapter<TaskViewHolder, ChildViewHolder>(taskList) {

    private var daoSession: DaoSession? = null
    private var taskinfoDao: TASKINFODao? = null
    private var templettableDao: TEMPLETTABLEDao? = null
    private var samplingtableDao: SAMPLINGTABLEDao? = null
    internal var queue: RequestQueue? = null

    init {
        //database init
        daoSession = ((mContext as Activity).application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession?.getTASKINFODao()
        templettableDao = daoSession?.getTEMPLETTABLEDao()
        samplingtableDao = daoSession?.getSAMPLINGTABLEDao()
        queue = (mContext.application as CollectionApplication).requestQueue
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder? {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.parent_listview_header, parent, false)
        return TaskViewHolder(view)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        when (viewType) {
            TYPE_TEMPLATE -> {
                val templateView = LayoutInflater.from(parent.context).inflate(R.layout.child_listview_header_template, parent, false)
                return SheetTemplateViewHolder(templateView)
            }
            TYPE_SHEET -> {
                val sheet = LayoutInflater.from(parent.context).inflate(R.layout.child_listview_solitary_sheet, parent, false)
                return SheetViewHolder(sheet)
            }
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindChildViewHolder(holder: ChildViewHolder, flatPosition: Int, group: ExpandableGroup<*>,
                                       childIndex: Int) {
        val viewType = getItemViewType(flatPosition)
        val sheet = (group as TaskData).items[childIndex]
        when (viewType) {
            TYPE_SHEET -> {
                (holder as SheetViewHolder).setSheetTitle(sheet.show_name!!)
                val sheetViewHolder = holder as SheetViewHolder
                // 数据库中查询当前抽样单
                var samplingTable:SAMPLINGTABLE?
                if (samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.Id.eq(sheet.id))?.list()?.size == 1)
                    samplingTable = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.Id.eq(sheet.id))?.list()?.get(0)
                else {
                    return@onBindChildViewHolder
                }
                // 设置抽样单标题
                sheetViewHolder.sheetTitle?.text = sheet.show_name
                sheetViewHolder.sheetTitleDescription?.text = sheet.sampling_address
                // 设置指示器状态以及下方四个按钮的使能
                when (sheet.check_status) {
                    Constant.S_STATUS_HAVE_NOT_UPLOAD -> {
                        sheetViewHolder.uploadStatus?.text = "未上传"
                        sheetViewHolder.uploadStatus?.backgroundResource = R.drawable.shape_second_status_gray
                        // 可编辑
                        sheetViewHolder.edit?.isEnabled = true
                        sheetViewHolder.edit_img?.backgroundResource = R.drawable.edit_expand
                        if (sheet.is_server_sampling?:true || sheet.is_make_up?:true) {
                            // 补采抽样单和定点采样不能删除
                            sheetViewHolder.delete?.isEnabled = false
                            sheetViewHolder.delete_img?.backgroundResource = R.drawable.delete_disenable
                        }else{
                            // 随机采样中自己生成的抽样单可以删除
                            sheetViewHolder.delete?.isEnabled = true
                            sheetViewHolder.delete_img?.backgroundResource = R.drawable.delete_expand
                        }
                        // 已保存（填写完成的）抽样单才可以上传
                        if (sheet.is_saved?:true) {
                            sheetViewHolder.upload?.isEnabled = true
                            sheetViewHolder.upload_img?.backgroundResource = R.drawable.upload_expand
                        } else {
                            sheetViewHolder.upload?.isEnabled = false
                            sheetViewHolder.upload_img?.backgroundResource = R.drawable.upload_expand_disable
                        }

                        sheetViewHolder.makeUp?.isEnabled = false
                        sheetViewHolder.makeUp_img?.backgroundResource = (R.drawable.makeup_expand_disable)
                    }
                    Constant.S_STATUS_CHECKING -> {
                        sheetViewHolder.uploadStatus?.text = "审核中"
                        sheetViewHolder.uploadStatus?.backgroundResource = R.drawable.shape_second_status_blue
                        //enable make up button.disable others
                        sheetViewHolder.edit?.isEnabled = false
                        sheetViewHolder.edit_img?.backgroundResource = (R.drawable.edit_expand_disable)
                        sheetViewHolder.delete?.isEnabled =false
                        sheetViewHolder.delete_img?.backgroundResource = (R.drawable.delete_disenable)
                        sheetViewHolder.upload?.isEnabled =false
                        sheetViewHolder.upload_img?.backgroundResource = (R.drawable.upload_expand_disable)
                        sheetViewHolder.makeUp?.isEnabled = false
                        sheetViewHolder.makeUp_img?.backgroundResource = (R.drawable.makeup_expand_disable)
                    }
                    Constant.S_STATUS_PASSED -> {
                        sheetViewHolder.uploadStatus?.text = "已通过"
                        sheetViewHolder.uploadStatus?.backgroundResource = R.drawable.shape_second_status_green
                        //enable make up button.disable others
                        sheetViewHolder.edit?.isEnabled = false
                        sheetViewHolder.edit_img?.backgroundResource = (R.drawable.edit_expand_disable)
                        sheetViewHolder.delete?.isEnabled =false
                        sheetViewHolder.delete_img?.backgroundResource = (R.drawable.delete_disenable)
                        sheetViewHolder.upload?.isEnabled =false
                        sheetViewHolder.upload_img?.backgroundResource = (R.drawable.upload_expand_disable)
                        sheetViewHolder.makeUp?.isEnabled = false
                        sheetViewHolder.makeUp_img?.backgroundResource = (R.drawable.makeup_expand_disable)
                    }
                    Constant.S_STATUS_NOT_PASS -> {
                        sheetViewHolder.uploadStatus?.text = "未通过"
                        sheetViewHolder.uploadStatus?.backgroundResource = R.drawable.shape_second_status_red
                        //enable make up button.disable others
                        sheetViewHolder.edit?.isEnabled = false
                        sheetViewHolder.edit_img?.backgroundResource = (R.drawable.edit_expand_disable)
                        sheetViewHolder.delete?.isEnabled =false
                        sheetViewHolder.delete_img?.backgroundResource = (R.drawable.delete_disenable)
                        sheetViewHolder.upload?.isEnabled =false
                        sheetViewHolder.upload_img?.backgroundResource = (R.drawable.upload_expand_disable)
                        sheetViewHolder.makeUp?.isEnabled = true
                        sheetViewHolder.makeUp_img?.backgroundResource = (R.drawable.makeup_expand)
                    }
                    Constant.S_STATUS_NOT_USED -> {
                        sheetViewHolder.uploadStatus?.text = "已补采"
                        sheetViewHolder.uploadStatus?.backgroundResource = R.drawable.shape_second_status_yellow
                        //enable make up button.disable others
                        sheetViewHolder.edit?.isEnabled = false
                        sheetViewHolder.edit_img?.backgroundResource = (R.drawable.edit_expand_disable)
                        sheetViewHolder.delete?.isEnabled =false
                        sheetViewHolder.delete_img?.backgroundResource = (R.drawable.delete_disenable)
                        sheetViewHolder.upload?.isEnabled =false
                        sheetViewHolder.upload_img?.backgroundResource = (R.drawable.upload_expand_disable)
                        sheetViewHolder.makeUp?.isEnabled = false
                        sheetViewHolder.makeUp_img?.backgroundResource = (R.drawable.makeup_expand_disable)
                    }
                }
                // 根据表格必填项有没有填完显示未保存和已保存
                if (sheet.is_saved?:false) {
                    sheetViewHolder.saveStatus?.backgroundResource = (R.drawable.shape_second_status_green)
                    sheetViewHolder.saveStatus?.text = "已完成"
                } else {
                    sheetViewHolder.saveStatus?.backgroundResource = (R.drawable.shape_second_status_gray)
                    sheetViewHolder.saveStatus?.text = "未完成"
                }

                // 设置右边采样打开/隐藏按钮
                if (sheet.isHidden?:true)
                    sheetViewHolder.menuLayout?.visibility = View.GONE
                else
                    sheetViewHolder.menuLayout?.visibility = View.VISIBLE
                sheetViewHolder.moreLayout?.onClick {
                    if (sheet.isHidden == true) {
                        sheetViewHolder.menuLayout?.visibility = View.VISIBLE
                        (group as TaskData).items[childIndex].isHidden = false
                    }
                    else {
                        sheetViewHolder.menuLayout?.visibility = View.GONE
                        (group as TaskData).items[childIndex].isHidden = true
                    }
                }

                // 编辑
                sheetViewHolder.edit?.onClick {
                    val startForm = Intent(mContext, GatherActivity::class.java)
                    startForm.putExtra("samplingID", sheet.id) //文件名不包含后缀
                    startForm.putExtra("Mode", GatherActivity.MODE_EDIT) // 编辑模式，编辑模式下不可加样
                    mContext.startActivity(startForm)
                }

                // 删除
                sheetViewHolder.delete?.onClick {
                    samplingtableDao?.delete(samplingTable)
                    //also delete the media folder
                    val mediaFolder = TaskInfo(mContext, 0).getMediaFolderBySampleTable(samplingTable)
                    if (mediaFolder != null && mediaFolder.exists())
                        FileUtil.delete(mediaFolder)

                    Toast.makeText(mContext, "已删除" + sheet.show_name, Toast.LENGTH_SHORT).show()

                    //refresh data
                    notifyItemRemoved(childIndex)
                    (mContext as WeixinActivityMain).refreshTaskData()
                }

                // 上传
                sheetViewHolder.upload?.onClick {
                    val taskName: String
                    if (taskinfoDao?.queryBuilder()?.where(TASKINFODao.Properties.TaskID.eq(sheet.taskID))?.list()?.size != 1)
                        return@onClick
                    else
                        taskName = taskinfoDao?.queryBuilder()?.where(TASKINFODao.Properties.TaskID.eq(sheet.taskID))?.list()?.get(0)?.task_name?:""

                    val samplingtables = ArrayList<SAMPLINGTABLE>()
                    samplingtables.add(samplingTable!!)

                    uploadSamplingAndMedia(samplingtables, taskName)
                }

                // 补采
                sheetViewHolder.makeUp?.onClick {
                    val startForm = Intent(mContext, GatherActivity::class.java)
                    startForm.putExtra("samplingID",sheet.id)//get the id of sampling
                    startForm.putExtra("Mode", GatherActivity.MODE_MAKE_UP)
                    startForm.putExtra("sid_of_server", sheet.sid_of_server)//补采的时候通过存在服务器上的id来判断这是对 哪个抽样单进行的补采
                    (mContext as Activity).startActivity(startForm)
                }

                // 点击进入抽样单
                sheetViewHolder.upperView?.onClick {
                    val startForm = Intent(mContext, GatherActivity::class.java)
                    startForm.putExtra("res", sheet.sampling_content)    //字符串
                    startForm.putExtra("samplingID", sheet.id) //文件名不包含后缀
                    startForm.putExtra("Mode", GatherActivity.MODE_LOOK_THROUGH)//设置为查看模式
                    mContext.startActivity(startForm)
                }
                sheetViewHolder.upperView?.onLongClick {
                    mContext.toast(sheet.show_name?:"")
                    return@onLongClick true
                }
            }
            TYPE_TEMPLATE -> {
                val templetViewHolder = holder as SheetTemplateViewHolder
                val indicatorNum = TaskInfo(mContext, 0).getUploadIndicatorNum(sheet.templetID!!)//get indicator number
                // 设置标题
                templetViewHolder.templateTitle.text = sheet.show_name
                // 设置上传数量标识
                templetViewHolder.uploadIndicator.text = indicatorNum[0].toString() + "/" + indicatorNum[1].toString()
                // 上传当前模板下所有抽样单按钮
                templetViewHolder.uploadAllSheetsLayout.onClick {
                    //find the unuploaded sample and the saved samplings under the templet
                    val samplingtables = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TempletID.eq(sheet.templetID),
                            SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_HAVE_NOT_UPLOAD),
                            SAMPLINGTABLEDao.Properties.Check_status.notEq(Constant.S_STATUS_DELETE),
                            SAMPLINGTABLEDao.Properties.Is_saved.eq(true))
                            ?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
                    val taskName: String
                    if (taskinfoDao?.queryBuilder()?.where(TASKINFODao.Properties.TaskID.eq(sheet.taskID))?.list()?.size != 1)
                        return@onClick
                    else
                        taskName = taskinfoDao?.queryBuilder()?.where(TASKINFODao.Properties.TaskID.eq(sheet.taskID))?.list()?.get(0)?.task_name ?: ""
                    uploadSamplingAndMedia(samplingtables, taskName)
                }
                // 点击进去模板界面
                templetViewHolder.templateLayout.onClick {
                    val startForm = Intent(mContext as Activity, GatherActivity::class.java)
                    startForm.putExtra("res", sheet.sampling_content)    //字符串
                    startForm.putExtra("templetID", sheet.templetID ?: -1L) //文件名不包含后缀
                    startForm.putExtra("taskID", sheet.taskID)    //哪个任务被点击了
                    startForm.putExtra("Mode", GatherActivity.MODE_TEMPLATE)
                    mContext.startActivity(startForm)
                }
                templetViewHolder.templateLayout.onLongClick {
                    mContext.toast(sheet.show_name ?: "")
                    return@onLongClick true
                }
            }
        }
    }

    override fun onBindGroupViewHolder(holder: TaskViewHolder, flatPosition: Int,
                                       group: ExpandableGroup<*>) {
        val taskData = group as TaskData
        val taskID = taskData.taskID

        //New字符相关
        if (taskData.is_new_task)
            holder.newFlag?.visibility = (View.VISIBLE)
        else
            holder.newFlag?.visibility = (View.GONE)

        //status
        val notUplaodedSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Is_uploaded.eq(false))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
        val checkingSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_CHECKING))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
        val passedSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_PASSED))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
        val notPassedSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_NOT_PASS))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
        val notUsedSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_NOT_USED))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()

        holder.taskTitle?.text = taskData.task_name
        holder.counterNotUploaded?.text = notUplaodedSampling?.size.toString() + "个"
        holder.counterChecking?.text = checkingSampling?.size.toString() + "个"
        holder.counterPassed?.text = passedSampling?.size.toString() + "个"
        holder.counterNotPassed?.text = notPassedSampling?.size.toString() + "个"
        holder.counterNotUsed?.text = notUsedSampling?.size.toString() + "个"

        // 点击更新按钮更新数据
        holder.refreshTaskData?.onClick {
            (mContext as WeixinActivityMain).updateTaskStatus(true)
            mContext.updateSamplingStatus(true)
        }
    }

    override fun getChildViewType(position: Int, group: ExpandableGroup<*>?, childIndex: Int): Int {
        if ((group as TaskData).items[childIndex].isTemplate!!) {
            return TYPE_TEMPLATE
        } else {
            return TYPE_SHEET
        }
    }

    override fun isGroup(viewType: Int): Boolean {
        return viewType == ExpandableListPosition.GROUP
    }

    override fun isChild(viewType: Int): Boolean {
        return viewType == TYPE_TEMPLATE || viewType == TYPE_SHEET
    }

    companion object {
        val TYPE_TEMPLATE = 3
        val TYPE_SHEET = 4
    }

    /**
     * 一键上传抽样单及其产生的文件
     */
    fun uploadSamplingAndMedia(samplingtables: List<SAMPLINGTABLE>?, taskName: String) {

        if (samplingtables?.size == 0) {
            Toast.makeText(mContext, "没有可以上传的抽样单!", Toast.LENGTH_LONG).show()
            return
        }

        val files = ArrayList<File>()
        for (i in samplingtables!!.indices) {
            //search media files under this sampling,add them to files list
            val mediaFileName = samplingtables[i].media_folder
            val mediaFiles = File(Util.getMediaFolder(mContext) + File.separator + mediaFileName).listFiles()
            if (mediaFiles != null) {
                for (j in mediaFiles.indices) {
                    files.add(mediaFiles[j])
                }
            }
        }

        if (files.size == 0) {
            uploadSamplings(samplingtables, taskName)
        } else {
            uploadIMG(files, samplingtables, taskName)
        }


    }

    /**
     * 上传抽样单

     * @param samplingtables
     * *
     * @param taskName
     */
    fun uploadSamplings(samplingtables: List<SAMPLINGTABLE>, taskName: String) {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.max = samplingtables.size
        progressDialog.progress = 0
        progressDialog.setMessage(samplingtables[0].show_name + "上传中...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val listener = object : Response.Listener<String> {
            override fun onResponse(s: String) {
                try {
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val message = resultJson.getString(URLs.KEY_MESSAGE)
                    if (errorCode == ReturnCode.Code0) {//return code mean success
                        //save sid of server
                        try {
                            samplingtables[progressDialog.progress].sid_of_server = java.lang.Long.valueOf(message)
                            samplingtables[progressDialog.progress].is_uploaded = true
                            samplingtables[progressDialog.progress].check_status = Constant.S_STATUS_CHECKING

                            samplingtableDao?.insertOrReplace(samplingtables[progressDialog.progress])
                        } catch (e: NumberFormatException) {
                            progressDialog.dismiss()
                            Toast.makeText(mContext, "上传出错" + s, Toast.LENGTH_SHORT).show()
                            Toast.makeText(mContext, "返回错误的sid", Toast.LENGTH_LONG).show()
                            return
                        }

                        //judge if upload the last one
                        if (progressDialog.progress != progressDialog.max - 1) {// not the last sampling
                            progressDialog.progress = progressDialog.progress + 1
                            progressDialog.setMessage(samplingtables[progressDialog.progress].show_name + "上传中...")
                            val errorListener = Response.ErrorListener { volleyError ->
                                progressDialog.dismiss()
                                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
                                Log.e("uploadSamFail", volleyError.toString())
                            }
                            //send to api
                            val stringRequest = API.uploadSampling(this, errorListener, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String, samplingtables[progressDialog.progress].taskID.toString(),
                                    taskName, samplingtables[progressDialog.progress].sampling_content,
                                    samplingtables[progressDialog.progress].show_name,
                                    samplingtables[progressDialog.progress].sid_of_server.toString(),
                                    samplingtables[progressDialog.progress].latitude!!,
                                    samplingtables[progressDialog.progress].longitude!!,
                                    samplingtables[progressDialog.progress].location_mode!!,
                                    samplingtables[progressDialog.progress].sampling_unique_num,
                                    samplingtables[progressDialog.progress].is_make_up!!)
                            queue?.add(stringRequest)

                        } else { //uploaded the last one
                            progressDialog.dismiss()
                            //samplingtableDao.insertOrReplaceInTx(samplingtables);
                            //Toast
                            Toast.makeText(mContext, "抽样单上传成功", Toast.LENGTH_SHORT).show()
                            //update data set of child adapter
                            (mContext as WeixinActivityMain).notifyDoingChildListDataChanged()
                            //update data set of parent adapter
                            mContext.notifyDoingParentListDataChanged()
                        }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(mContext, "上传出错" + s, Toast.LENGTH_SHORT).show()
                        ReturnCode(mContext, errorCode, false)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    progressDialog.dismiss()
                    Toast.makeText(mContext, "上传出错" + s, Toast.LENGTH_SHORT).show()
                }

            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog.dismiss()
            Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
            Log.e("uploadSamFail", volleyError.toString())
        }

        //send to api
        val stringRequest = API.uploadSampling(listener, errorListener, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String, samplingtables[0].taskID.toString(), taskName, samplingtables[0].sampling_content, samplingtables[0].show_name,
                samplingtables[0].sid_of_server.toString(), samplingtables[0].latitude!!, samplingtables[0].longitude!!,
                samplingtables[0].location_mode!!, samplingtables[0].sampling_unique_num, samplingtables[0].is_make_up!!)
        queue?.add(stringRequest)

    }


    fun uploadIMG(files: ArrayList<File>, samplingtables: List<SAMPLINGTABLE>, taskName: String) {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.max = files.size
        progressDialog.progress = 0
        progressDialog.setMessage(files[0].name + "上传中...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val counter = Counter()
        var body: RequestBody? = null
        if (files[counter.counter].name.endsWith(".jpg")) {
            body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""), RequestBody.create(null, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String))
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""), RequestBody.create(null, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String))
                    .addFormDataPart("upfile", files[counter.counter].name, RequestBody.create(MediaType.parse("image/jpg"), files[counter.counter]))
                    .build()
        } else if (files[counter.counter].name.endsWith(".mp4")) {
            body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""), RequestBody.create(null, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String))
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""), RequestBody.create(null, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String))
                    .addFormDataPart("upfile", files[counter.counter].name, RequestBody.create(MediaType.parse("video/mp4"), files[counter.counter]))
                    .build()
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.unknownFileType), Toast.LENGTH_LONG).show()
            return
        }

        val request = Request.Builder()
                .url(URLs.UPLAODIMG)
                .post(body)
                .build()

        val client = OkHttpClient()

        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                WeixinActivityMain.instance!!.runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
                }

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                try {
                    val s = response.body().string()
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val message = resultJson.getString(URLs.KEY_MESSAGE)

                    if (errorCode != ReturnCode.Code0) {
                        WeixinActivityMain.instance!!.runOnUiThread {
                            progressDialog.dismiss()
                            Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (errorCode == ReturnCode.Code0 && progressDialog.progress == files.size - 1) {  // The final file
                        WeixinActivityMain.instance!!.runOnUiThread {
                            progressDialog.dismiss()
                            uploadSamplings(samplingtables, taskName)
                        }

                    } else {
                        counter.increase_one_step()
                        WeixinActivityMain.instance!!.runOnUiThread {
                            progressDialog.progress = progressDialog.progress + 1
                            progressDialog.setMessage(files[progressDialog.progress].name + "上传中...")
                        }

                        var body: RequestBody? = null
                        if (files[counter.counter].name.endsWith(".jpg")) {
                            body = MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""), RequestBody.create(null, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String))
                                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""), RequestBody.create(null, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String))
                                    .addFormDataPart("upfile", files[counter.counter].name, RequestBody.create(MediaType.parse("image/jpg"), files[counter.counter]))
                                    .build()
                        } else if (files[counter.counter].name.endsWith(".mp4")) {
                            body = MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""), RequestBody.create(null, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String))
                                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""), RequestBody.create(null, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String))
                                    .addFormDataPart("upfile", files[counter.counter].name, RequestBody.create(MediaType.parse("video/mp4"), files[counter.counter]))
                                    .build()
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.unknownFileType), Toast.LENGTH_LONG).show()
                            return
                        }

                        val request = Request.Builder()
                                .url(URLs.UPLAODIMG)
                                .post(body)
                                .build()

                        val client = OkHttpClient()
                        client.newCall(request).enqueue(this)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    WeixinActivityMain.instance!!.runOnUiThread {
                        progressDialog.dismiss()
                        Toast.makeText(mContext, mContext.getString(R.string.serverError), Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
        client.newCall(request).enqueue(callback)
    }
}