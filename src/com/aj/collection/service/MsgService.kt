/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aj.collection.service

import android.app.*
import android.app.ActivityManager.RunningServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.support.v7.app.NotificationCompat
import android.util.Log
import android.widget.Toast

import com.aj.Constant
import com.aj.WeixinActivityMain
import com.aj.collection.activity.CollectionApplication
import com.aj.collection.R
import com.aj.collection.database.DaoMaster
import com.aj.collection.database.DaoSession
import com.aj.collection.database.SAMPLINGTABLE
import com.aj.collection.database.SAMPLINGTABLEDao
import com.aj.collection.database.TASKINFO
import com.aj.collection.database.TASKINFODao
import com.aj.collection.database.TEMPLETTABLE
import com.aj.collection.database.TEMPLETTABLEDao
import com.aj.collection.http.API
import com.aj.collection.http.ReturnCode
import com.aj.collection.http.URLs
import com.aj.collection.tools.KotlinUtil
import com.aj.collection.tools.SPUtils
import com.aj.collection.tools.Util
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application. The { StepServiceController} and
 * {StepServiceBinding} classes show how to interact with the service.
 *
 *
 *
 *
 * Notice the use of the [NotificationManager] when interesting things
 * happen in the service. This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
class MsgService : Service() {
    private val mContext = this
    private var mNM: NotificationManager? = null
    private var getNewTaskRequest: StringRequest? = null
    internal var queue: RequestQueue? = null

    //database part
    private val db: SQLiteDatabase? = null
    private val daoMaster: DaoMaster? = null
    private var daoSession: DaoSession? = null
    private var taskinfoDao: TASKINFODao? = null
    private var templettableDao: TEMPLETTABLEDao? = null
    private var samplingtableDao: SAMPLINGTABLEDao? = null


    override fun onCreate() {
        super.onCreate()
        (this.application as CollectionApplication).initDaoSession()
        daoSession = (this@MsgService.application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession!!.taskinfoDao
        templettableDao = daoSession!!.templettableDao
        samplingtableDao = daoSession!!.samplingtableDao

        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        queue = (application as CollectionApplication).requestQueue //init Volley

        registerTimeTickReceiver()
        getNewTask()
    }

    override fun onDestroy() {

        unregisterTimeTickReceiver()

        mNM!!.cancel(R.string.app_name)

        super.onDestroy()

    }

    fun registerTimeTickReceiver() {
        val filter1 = IntentFilter(Intent.ACTION_TIME_TICK)
        registerReceiver(mReceiver, filter1)
    }

    fun unregisterTimeTickReceiver() {
        unregisterReceiver(mReceiver)
    }

    private fun returnToLoginActivity() {
        if (mCallback != null)
            mCallback?.returnToLoginActivity()
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    inner class MsgBinder : Binder() {
        val service: MsgService
            get() = this@MsgService
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    /**
     * Receives messages from mContext.
     */
    private val mBinder = MsgBinder()

    interface ICallback {
        fun haveNewTask()

        fun getWeixinActitityContext():Context

        fun returnToLoginActivity()

        fun refreshBadgeView_callback()

        fun refreshDoingChildListView()

        fun refreshDoneChildListView()
    }

    private var mCallback: ICallback? = null

    fun registerCallback(cb: ICallback) {
        mCallback = cb
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

    /**
     * 广播接收器
     */
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Check action just to be on the safe side.
            if (intent.action == Intent.ACTION_TIME_TICK) {//每分钟的广播 1.if have new task
                getNewTask()
            }
        }
    }

    /**
     * get New Task
     */
    fun getNewTask() {
        val listener = Response.Listener<String> { s ->
            try {

                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val result = resultJson.getString(URLs.KEY_MESSAGE)

                if (errorCode == ReturnCode.Code0) {//connected
                    val taskJsonArray = JSONArray(result)
                    for (i in 0..taskJsonArray.length() - 1) {
                        val taskJsonObject = taskJsonArray.getJSONObject(i)
                        var hasNewTask = KotlinUtil.parseTaskDataIntoDatabase(mContext, taskJsonObject, taskinfoDao, templettableDao, samplingtableDao)
                        if (hasNewTask)
                            showNotification()
                    }
                    KotlinUtil.deleteTaskDosentExsistInList(taskJsonArray, taskinfoDao,
                            templettableDao, samplingtableDao)

                    mCallback?.refreshDoingChildListView()
//                    mCallback?.refreshDoneChildListView()

                } else {
                    ReturnCode(applicationContext, errorCode, true)
                    if (errorCode == ReturnCode.NO_SUCH_ACCOUNT || errorCode == ReturnCode.PASSWORD_INVALIDE) {
                        returnToLoginActivity()
                    }
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
        }
        getNewTaskRequest = API.getNewTask(listener, errorListener,
                SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String?,
                SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String?)
        queue?.add<String>(getNewTaskRequest)
    }
}
