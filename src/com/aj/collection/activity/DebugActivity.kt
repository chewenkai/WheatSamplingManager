package com.aj.collection.activity

import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.app.Activity
import android.util.TimeUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.aj.Constant
import com.aj.collection.R
import com.aj.collection.database.DaoMaster
import com.aj.collection.database.DaoSession
import com.aj.collection.database.SAMPLINGTABLE
import com.aj.collection.database.SAMPLINGTABLEDao
import com.aj.collection.database.TASKINFO
import com.aj.collection.database.TASKINFODao
import com.aj.collection.database.TEMPLETTABLEDao
import com.aj.collection.http.API
import com.aj.collection.http.URLs
import com.aj.collection.tools.SPUtils
import com.aj.collection.tools.Util
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_debug.*
import org.jetbrains.anko.onClick

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus

class DebugActivity : Activity() {
    internal var login: TextView? = null
    internal var haveNewTask: TextView? = null
    internal var getNewTask: TextView? = null
    internal var setReceived: TextView? = null
    internal var uploadFile: TextView? = null
    internal var uploadSampling: TextView? = null
    internal var uploadLocation: TextView? = null
    internal var getSampleTask: TextView? = null
    internal var getTaskStatus: TextView? = null
    internal var output: TextView? = null

    //database part
    private val db: SQLiteDatabase? = null
    private val daoMaster: DaoMaster? = null
    private var daoSession: DaoSession? = null
    private var taskinfoDao: TASKINFODao? = null
    private var templettableDao: TEMPLETTABLEDao? = null
    private var samplingtableDao: SAMPLINGTABLEDao? = null

    internal var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        queue = Volley.newRequestQueue(this) //init Volley
        //database init
        daoSession = ((mContext as Activity).application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession!!.taskinfoDao
        templettableDao = daoSession!!.templettableDao
        samplingtableDao = daoSession!!.samplingtableDao

        progressDialog = ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT)

        initUI()

        setListener()
    }

    private fun setListener() {
        val param = ArrayList<String>()
        param.add(System.currentTimeMillis().toString())
        param.add("123")
        param.add("山东省")
        param.add("烟台市")
        param.add("莱阳市")
        param.add("飞龙花园小区")
        param.add("车文凯")
        param.add("13100958919")
        param.add("265200")
        param.add("370682199302156417")
        param.add("车文凯")
        param.add("622848026247356800")
        param.add("中国农业银行")
        register.onClick {
            progressDialog!!.setMessage("注册中...")
            progressDialog!!.show()

            val listener = Response.Listener<String> { s ->
                progressDialog!!.dismiss()
                output?.text = s
                try {
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val message = resultJson.getString(URLs.KEY_MESSAGE)
                    output?.text = "发送：\n${param.toString()}\n$s\n接收:\n错误码：$errorCode\n消息：$message"
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val errorListener = Response.ErrorListener { volleyError ->
                progressDialog!!.dismiss()
                output?.text = volleyError.toString()
            }

            val stringRequest = API.registUser(listener, errorListener, param[0],param[1],
                    param[2],param[3],param[4],param[5],param[6],param[7],param[8],param[9]
                    ,param[10],param[11],param[12])
            queue?.add(stringRequest)
        }

        login?.setOnClickListener {
            progressDialog!!.setMessage("登陆中...")
            progressDialog!!.show()

            val listener = Response.Listener<String> { s ->
                progressDialog!!.dismiss()
                output?.text = s
                try {
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val message = resultJson.getString(URLs.KEY_MESSAGE)
                    output?.text = "$s\n错误码：$errorCode\n消息：$message"
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val errorListener = Response.ErrorListener { volleyError ->
                progressDialog!!.dismiss()
                output?.text = volleyError.toString()
            }
            val stringRequest = API.login(listener, errorListener, "kevin", "123456")
            queue?.add(stringRequest)
        }

        haveNewTask?.setOnClickListener {
            progressDialog!!.setMessage("检查有无新任务...")
            progressDialog!!.show()
            val listener = Response.Listener<String> { s ->
                progressDialog!!.dismiss()
                output?.text = s
                try {
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val message = resultJson.getString(URLs.KEY_MESSAGE)
                    output?.text = "$s\n错误码：$errorCode\n消息：$message"
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val errorListener = Response.ErrorListener { volleyError ->
                progressDialog!!.dismiss()
                output?.text = volleyError.toString()
            }
            val stringRequest = API.haveNewTask(listener, errorListener, "kevin")
            queue?.add(stringRequest)
        }

        getNewTask?.setOnClickListener {
            progressDialog!!.setMessage("获取新任务...")
            progressDialog!!.show()
            val listener = Response.Listener<String> { s ->
                progressDialog!!.dismiss()
                output?.text = s
                try {
                    val resultJson = JSONObject(s)
                    val errorCode = resultJson.getString(URLs.KEY_ERROR)
                    val message = resultJson.getString(URLs.KEY_MESSAGE)
                    output?.text = "$s\n错误码：$errorCode\n消息：$message"
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            val errorListener = Response.ErrorListener { volleyError ->
                progressDialog!!.dismiss()
                output?.text = volleyError.toString()
            }
            val stringRequest = API.getNewTask(listener, errorListener, "kevin", "123456")
            queue?.add(stringRequest)
        }

        setReceived?.setOnClickListener { testReceived() }

        uploadFile?.setOnClickListener { testUploadIMG() }

        uploadSampling?.setOnClickListener { testUploadSampling() }

        uploadLocation?.setOnClickListener { testUploadLocation() }

        getSampleTask?.setOnClickListener { testGetSamplingStatus() }

        getTaskStatus?.setOnClickListener { testGetTaskStatus() }
    }

    private fun initUI() {
        login = findViewById(R.id.debug_login) as TextView
        haveNewTask = findViewById(R.id.debug_have_new_task) as TextView
        getNewTask = findViewById(R.id.debug_get_new_task) as TextView
        setReceived = findViewById(R.id.debug_set_received) as TextView
        uploadFile = findViewById(R.id.debug_upload_file) as TextView
        uploadSampling = findViewById(R.id.debug_upload_sampling) as TextView
        uploadLocation = findViewById(R.id.debug_upload_location) as TextView
        getSampleTask = findViewById(R.id.debug_get_sampling_status) as TextView
        getTaskStatus = findViewById(R.id.debug_get_task_status) as TextView

        output = findViewById(R.id.debug_output_info) as TextView
    }

    /***************************************************************************
     * *****************************接口测试**************************************
     */
    internal var queue: RequestQueue?=null
    internal var mContext: Context = this

    fun testReceived() {
        progressDialog!!.setMessage("设置接受...")
        progressDialog!!.show()
        val listener = Response.Listener<String> { s ->
            progressDialog!!.dismiss()
            output?.text = s
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val message = resultJson.getString(URLs.KEY_MESSAGE)
                output?.text = "$s\n错误码：$errorCode\n消息：$message"
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog!!.dismiss()
            output?.text = volleyError.toString()
        }
        val stringRequest = API.setTaskReceived(listener, errorListener, "kevin")
        queue?.add(stringRequest)
    }

    fun testUploadIMG() {
        //        progressDialog.setMessage("上传文件...");
        //        progressDialog.show();
        //        RequestQueue mSingleQueue = Volley.newRequestQueue(mContext, new MultiPartStack());
        //        Response.Listener<String> listener = new Response.Listener<String>() {
        //            @Override
        //            public void onResponse(String s) {
        //                progressDialog.dismiss();
        //                output?.setText(s);
        //                try {
        //                    JSONObject resultJson = new JSONObject(s);
        //                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
        //                    String message = resultJson.getString(URLs.KEY_MESSAGE);
        //                    output?.setText(s+"\n错误码："+errorCode+"\n消息："+message);
        //                } catch (JSONException e) {
        //                    e.printStackTrace();
        //                }
        //            }
        //        };
        //        Response.ErrorListener errorListener = new Response.ErrorListener() {
        //            @Override
        //            public void onErrorResponse(VolleyError volleyError) {
        //                progressDialog.dismiss();
        //                output?.setText(volleyError.toString());
        //            }
        //        };
        //
        //        File file = null;
        //        File sdcard = Environment.getExternalStorageDirectory();
        //        for (int i = 0; i < sdcard.listFiles().length; i++) {
        //            if (sdcard.listFiles()[i].isFile()) {
        //                file = sdcard.listFiles()[i];
        //                break;
        //            }
        //        }
        //
        //
        //        if (file == null || !file.exists()) {
        //            output?.setText("sdcard下没有文件！");
        //            return;
        //        }
        //        ArrayList<File> files = new ArrayList<>();
        //        files.add(file);
        //
        //        Map<String, String> map = new HashMap<String, String>();
        //        map.put("act", URLs.UPLOADSAMPLINGACT);
        //
        //        MultipartRequest multiPartRequest = API.uploadFiles(listener, errorListener, files, map);
        //        mSingleQueue.add(multiPartRequest);

    }


    fun testUploadSampling() {
        val samplingtables = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.Is_uploaded.eq(false),
                SAMPLINGTABLEDao.Properties.Check_status.notEq(Constant.S_STATUS_DELETE))
                .orderAsc(SAMPLINGTABLEDao.Properties.Id).list()
        if (samplingtables.size == 0) {
            Toast.makeText(mContext, "无抽样单", Toast.LENGTH_LONG).show()
            return
        }
        val samplingtable = samplingtables[0]
        val taskName: String
        if (taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(samplingtable.taskID)).list().size != 1)
            return
        else
            taskName = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(samplingtable.taskID)).list()[0].task_name


        val listener = Response.Listener<String> { s ->
            progressDialog!!.dismiss()
            output?.text = s
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val message = resultJson.getString(URLs.KEY_MESSAGE)
                output?.text = samplingtable.toString()+ "\n$s\n错误码：$errorCode\n消息：$message"
            } catch (e: JSONException) {
                output?.text = s
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog!!.dismiss()
            output?.text = volleyError.toString()
        }

        //send to api
        // TODO 抽样单名字为空的判断
        val stringRequest = API.uploadSampling(listener, errorListener, SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String, SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String,
                samplingtable.taskID.toString(), taskName, samplingtable.sampling_content, "抽样单名称",
                samplingtable.sid_of_server?.toString(), samplingtable.latitude, samplingtable.longitude,
                samplingtable.location_mode, samplingtable.sampling_unique_num, samplingtable.is_make_up)

        queue?.add(stringRequest)
        progressDialog!!.setMessage("上传中...")
        progressDialog!!.show()
    }

    fun testUploadLocation() {
        progressDialog!!.setMessage("上传位置...")
        progressDialog!!.show()

        val listener = Response.Listener<String> { s ->
            progressDialog!!.dismiss()
            output?.text = s
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val message = resultJson.getString(URLs.KEY_MESSAGE)
                output?.text = "$s\n错误码：$errorCode\n消息：$message"
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog!!.dismiss()
            output?.text = volleyError.toString()
        }
        val stringRequest = API.uploadLocation(listener, errorListener, "kevin", "123456",
                "12.22154", "15.12321", "1")

        queue?.add(stringRequest)
    }

    fun testGetSamplingStatus() {
        progressDialog!!.setMessage("获取抽样单状态...")
        progressDialog!!.show()
        val queue: RequestQueue
        queue = Volley.newRequestQueue(this) //init Volley

        val jsonArray = JSONArray()
        val allSamplingtables = samplingtableDao!!.queryBuilder().where(SAMPLINGTABLEDao.Properties.Sid_of_server.isNotNull).orderAsc().list()
        try {
            for (i in allSamplingtables.indices) {
                val jsonObject = JSONObject()
                jsonObject.put("sid", allSamplingtables[i].sid_of_server)
                jsonArray.put(jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val listener = Response.Listener<String> { s ->
            progressDialog!!.dismiss()
            output?.text = s
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val message = resultJson.getString(URLs.KEY_MESSAGE)
                output?.text = "$s\n错误码：$errorCode\n消息：$message"
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog!!.dismiss()
            output?.text = volleyError.toString()
        }
        val stringRequest = API.getSamplingStatus(listener, errorListener, "kevin", "123456", jsonArray.toString())
        queue.add(stringRequest)
    }

    fun testGetTaskStatus() {
        progressDialog!!.setMessage("获取任务状态...")
        progressDialog!!.show()
        val jsonArray = JSONArray()
        val alltaskinfos = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.isNotNull).orderAsc().list()
        try {
            for (i in alltaskinfos.indices) {
                val jsonObject = JSONObject()
                jsonObject.put("tid", alltaskinfos[i].taskID)
                jsonArray.put(jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val listener = Response.Listener<String> { s ->
            progressDialog!!.dismiss()
            output?.text = s
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val message = resultJson.getString(URLs.KEY_MESSAGE)
                output?.text = "$s\n错误码：$errorCode\n消息：$message"
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog!!.dismiss()
            output?.text = volleyError.toString()
        }
        val stringRequest = API.getTaskStatus(listener, errorListener,
                SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String,
                SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String, jsonArray.toString())

        queue?.add(stringRequest)
    }
}
