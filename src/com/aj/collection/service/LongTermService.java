/*
 *kevin
 */

package com.aj.collection.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.aj.Constant;
import com.aj.WeixinActivityMain;
import com.aj.collection.activity.CollectionApplication;
import com.aj.collection.R;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLEDao;
import com.aj.collection.http.API;
import com.aj.collection.http.ReturnCode;
import com.aj.collection.http.URLs;
import com.aj.collection.tools.SPUtils;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This service contain some function witch need in a long term running.
 */
public class LongTermService extends Service {
    private static final String TAG = "com.aj.collection.service.LongTermService:\n";
    private Context mContext = this;
    private NotificationManager mNM;

    boolean isServiceRunning = false;

    RequestQueue queue;

    private final int updatePeriod = 5;// update period time,the units is minute;
    private int updateCounter = 0;// count the time

    private double latitude = 0, longitude = 0;
    private int locationMod = Constant.CAN_NOT_GET_LOCATION;
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
            if ("com."
                    .equals(service.service.getClassName())) {
                isServiceRunning = true;
                return isServiceRunning;
            }
        }
        return isServiceRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        daoSession = ((CollectionApplication) (LongTermService.this.getApplication())).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();


        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        queue = Volley.newRequestQueue(this); //init Volley

//        IntentFilter filter1 = new IntentFilter(Intent.ACTION_TIME_TICK);
//        registerReceiver(mReceiver, filter1);

//        initLocation();

//        final Handler handler=new Handler();
//        Runnable runnable=new Runnable() {
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//
//                handler.postDelayed(this, 6000);
//            }
//        };
//        handler.postDelayed(runnable, 6000);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {

//        unregisterReceiver(mReceiver);
        mNM.cancel(R.string.app_name);

        super.onDestroy();

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
        public LongTermService getService() {
            return LongTermService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Receives messages from mContext.
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

    }

    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    /**
     * 广播接收器
     *//*
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {//每分钟的广播 1.if have new task
                if (updatePeriod == updateCounter) {
                    loginValidate();//check if the username and password which saved in storage is right
                    updateCounter = 0;
                } else
                    updateCounter++;
            }
        }
    };*/



    /**
     * 获取wakelock锁
     *
     * @param c Context
     * @return
     */

    static PowerManager.WakeLock acquireWakeLock(Context c, int level) {
        PowerManager.WakeLock mWakeLock;
        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        int wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP;
        if (level == 3) {
            wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP;
        } else if (level == 2) {
            wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK;
        } else if (level == 1) {
            wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
        }
        mWakeLock = pm.newWakeLock(wakeFlags, TAG);
        mWakeLock.acquire();
        return mWakeLock;
    }


    private boolean isExitting = false;

    public void setExitFlag() {
        isExitting = true;

    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {

            if (location.getLocType() == BDLocation.TypeGpsLocation) {//搜索到卫星 GPS定位成功 按钮显示开始

                if (location.getLatitude() != 0 && location.getLongitude() != 0) {

                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    locationMod = Constant.LOCATION_BY_GPS;
                }

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {//网络定位 基站定位
                if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    locationMod = Constant.LOCATION_BY_NETWORK;
                }
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    locationMod = Constant.LOCATION_BY_OFFLINE;
                }
            } else {
                longitude = 0;
                latitude = 0;
                locationMod = Constant.CAN_NOT_GET_LOCATION;
            }

            uploadLocation(longitude, latitude, locationMod);

        }

    }

    LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;//定位模式
    private String tempcoor = Constant.COORDINATION_STADARD;//定位坐标标准   bd09ll 百度经纬度标准    "gcj02"国家测绘局标准    "bd09"百度墨卡托标准
    public MyLocationListenner myListener;//定位监听器，会每隔span时间返回一次数据，后面还需要LocationClient注册定位监听器
    public Vibrator mVibrator;
    public LocationClient mLocationClient = null;

    private void initLocation() {
        // mLocationClient = ((LocationApplication) getApplication()).mLocationClient;
        mLocationClient = new LocationClient(this.getApplicationContext());
        myListener = new MyLocationListenner();
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        mLocationClient.registerLocationListener(myListener);//注册定位监听器

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(tempcoor);//可选，默认gcj02，设置返回的定位结果坐标系，
        int span = updatePeriod*60 * 1000;//设置刷新间隔
//        int span = 10* 1000;
        option.setAddrType("all");
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(false);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(false);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);

        //((LocationApplication) getApplication()).mLocationResult = mapscreen;

        mLocationClient.start();//定位SDK start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request


    }

    /*******************************NET WORK******************************/

    /**
     * sent the location to server
     *
     * @param longitude
     * @param latitude
     * @param locationMod
     */
    public void uploadLocation(final double longitude,final double latitude,final int locationMod) {

        String longitude_str = String.valueOf(longitude);
        String latitude_str = String.valueOf(latitude);
        String locationMod_str = String.valueOf(locationMod);


        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String content = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {
//                        Toast.makeText(mContext, "上传位置成功:" + longitude + "," + latitude + "," + locationMod, Toast.LENGTH_LONG).show();
//                        testGetSamplingStatus();
                    } else{
                        new ReturnCode(getApplicationContext(), errorCode, false);
//                        if(errorCode.equals(ReturnCode.NO_SUCH_ACCOUNT)||errorCode.equals(ReturnCode.PASSWORD_INVALIDE))
//                            returnToLoginActivity();
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
                Log.e("uploadLocaFail", volleyError.toString());
            }
        };
        StringRequest stringRequest = API.uploadLocation(listener, errorListener,
                (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE),
                (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE), longitude_str, latitude_str, locationMod_str);

        queue.add(stringRequest);
    }


}
