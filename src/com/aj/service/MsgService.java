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

package com.aj.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.aj.Constant;
import com.aj.WeixinActivityMain;
import com.aj.activity.CollectionApplication;
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
import com.aj.tools.SPUtils;
import com.aj.tools.T;
import com.aj.tools.Util;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application. The { StepServiceController} and
 * {StepServiceBinding} classes show how to interact with the service.
 * <p/>
 * <p/>
 * Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service. This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
public class MsgService extends Service {
    private static final String TAG = "pedometer.StepService";
    private Context mContext = this;
    private PowerManager.WakeLock wakeLock;
    private NotificationManager mNM;

    boolean isServiceRunning = false;

    RequestQueue queue;

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    public boolean isServiceRunning() {
        isServiceRunning = false;
        ActivityManager manager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if ("name.bagi.levente.pedometer.StepService"
                    .equals(service.service.getClassName())) {
                isServiceRunning = true;
                return isServiceRunning;
            }
        }
        return isServiceRunning;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "[SERVICE] onCreate");
        super.onCreate();

        daoSession = ((CollectionApplication) (MsgService.this.getApplication())).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();


        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        queue = ((CollectionApplication) getApplication()).getRequestQueue(); //init Volley

        // Register our receiver for the ACTION_SCREEN_OFF action. This will
        // make our receiver
        // code be called whenever the phone enters standby mode.

        registerTimeTickReceiver();

        haveNewTask();// check if have a new task

        //loginValidate();//check if the username and password which saved in storage is right
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "[SERVICE] onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "[SERVICE] onDestroy");

        unregisterTimeTickReceiver();

        mNM.cancel(R.string.app_name);

        // wakeLock.release();

//        if (!isExitting) {
//            Intent i = new Intent(getApplicationContext(), MsgService.class);
//            getApplicationContext().startService(i);
//        }

        super.onDestroy();

    }

    public void registerTimeTickReceiver() {
        IntentFilter filter1 = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, filter1);
    }

    public void unregisterTimeTickReceiver() {
        unregisterReceiver(mReceiver);
    }

    private void returnToLoginActivity() {
        if (mCallback != null)
            mCallback.returnToLoginActivity();
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class MsgBinder extends Binder {
        public MsgService getService() {
            return MsgService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "[SERVICE] onBind");
        return mBinder;
    }

    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new MsgBinder();

    public interface ICallback {
        public void haveNewTask();

        public Context getWeixinActitityContext();

        public void returnToLoginActivity();

        public void refreshBadgeView1_callback();

        public void refreshDoingChildListView();
    }

    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

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

    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    /**
     * 广播接收器
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {//每分钟的广播 1.if have new task
                haveNewTask();
                //loginValidate();//check if the username and password which saved in storage is right
            }
        }
    };

    /**
     * 检查是否有新任务
     */
    public void haveNewTask() {
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {

                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String result = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {//connected
                        if (result.equals(URLs.RESULT_NEWTASK)) {
                            // 1.send msg to main activity 2.save to sharedPreference 3.show a notification
                            getNewTask();
                        } else if (result.equals(URLs.RESULT_NOTHING)) {
                            // don't have new task. Do nothing
                        } else
                            Log.e("XXXXXXX", "接收到不该出现的结果");
                    } else {
                        new ReturnCode(getApplicationContext(), errorCode, false);
                        if (errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT) || errorCode.equals(ReturnCode.PASSWORD_INVALIDE)) {
                            returnToLoginActivity();
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("XXXXXXXX", "havaNewTask 返回值出问题" + s);
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
//				Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
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
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("GetNewTaskSuc", "");
                try {

                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String result = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {//connected
                        SPUtils.put(mContext, SPUtils.RECEIVED_TASK, result, SPUtils.TEMPORARY_SAVE);
                        setReceived();
                    } else {
                        new ReturnCode(getApplicationContext(), errorCode, false);
                        if (errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT) || errorCode.equals(ReturnCode.PASSWORD_INVALIDE)) {
                            returnToLoginActivity();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("XXXXXXXX", "getNewTask 返回值出问题-->" + s);
                }

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
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
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String result = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {//connected
                        if (result.equals(URLs.RESULT_RECEIVEDOK)) {
                            //get new task  1.send msg to main activity 2.save to sharedPreference 3.show a notification
                            String tasks = (String) SPUtils.get(mContext, SPUtils.RECEIVED_TASK, "", SPUtils.TEMPORARY_SAVE);

                            if (tasks.isEmpty()) {
                                Log.e("XXXXXXXX", "不应该为空，检查程序");
                                return;
                            }

                            JSONArray taskJsonArray = new JSONArray(tasks);// contain sorts of tasks
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
                                    //T.showShort(getApplicationContext(), "任务已存在！");
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
                                for (int j = 0; j < samplingsArray.length(); j++) {
                                    String samplingID = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLINGID);
                                    String samplingCont = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLINGCONT);
                                    String samplingName = samplingsArray.getJSONObject(j).getString(URLs.KEY_ITEMS);
                                    String samplingNum = samplingsArray.getJSONObject(j).getString(URLs.KEY_ITEMSID);
                                    String companyAddress = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLING_COMPANY_NAME);
                                    String mediaFolderChild = Util.getSamplingNum(mContext, taskinfo);

                                    SAMPLINGTABLE samplingtable = new SAMPLINGTABLE(null, Long.valueOf(taskID), templettable.getTempletID(), samplingName + "-" + samplingNum, companyAddress,
                                            samplingCont, mediaFolderChild, false, false, true, false, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(),
                                            null, Long.valueOf(samplingID), null, null, null, mediaFolderChild);


                                    samplingtableDao.insertOrReplace(samplingtable);
                                }
                            }

                            SPUtils.put(mContext, SPUtils.RECEIVED_TASK, "", SPUtils.TEMPORARY_SAVE);
                            //show a notification
                            showNotification();
                            if (mCallback != null) {
                                mCallback.haveNewTask();
                                mCallback.refreshBadgeView1_callback();
                            }
                        } else {
                            new ReturnCode(getApplicationContext(), errorCode, false);
                            if (errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT) || errorCode.equals(ReturnCode.PASSWORD_INVALIDE)) {
                                returnToLoginActivity();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("XXXXXXXX", "setReceived 返回值出问题-->" + s);
                }

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
//				Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                Log.e("Received", volleyError.toString());
            }
        };
        StringRequest stringRequest = API.setTaskReceived(listener, errorListener
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE));
        queue.add(stringRequest);
    }


    /**
     * 登录验证
     */
    private void loginValidate() {
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String content = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {

                    } else {
                        new ReturnCode(getApplicationContext(), errorCode, false);
                        if (errorCode.equals(ReturnCode.USERNAME_OR_PASSWORD_INVALIDE))
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
//				Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                Log.e("LoginTestFail", volleyError.toString());
            }
        };
        StringRequest stringRequest = API.login(listener, errorListener,
                (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE),
                (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE));

        queue.add(stringRequest);
    }


//    /**
//     * 获取wakelock锁
//     *
//     * @param c Context
//     * @return
//     */
//
//    static PowerManager.WakeLock acquireWakeLock(Context c, int level) {
//        PowerManager.WakeLock mWakeLock;
//        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
//        int wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK
//                | PowerManager.ACQUIRE_CAUSES_WAKEUP;
//        if (level == 3) {
//            wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK
//                    | PowerManager.ACQUIRE_CAUSES_WAKEUP;
//        } else if (level == 2) {
//            wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK;
//        } else if (level == 1) {
//            wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
//        }
//        mWakeLock = pm.newWakeLock(wakeFlags, TAG);
//        mWakeLock.acquire();
//        return mWakeLock;
//    }


    private boolean isExitting = false;

    public void setExitFlag() {
        isExitting = true;

    }

    public static boolean tableExits(String tabName, SQLiteDatabase db) {
        boolean result = false;
        if (tabName == null) {
            return false;
        }
        Cursor cursor = null;
        String sql = "select count(*) from sqlite_master where type='table' and name ='"
                + tabName.trim() + "'";
        cursor = db.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            int count = cursor.getInt(0);
            if (count > 0)
                result = true;
        }

        return result;
    }


}
