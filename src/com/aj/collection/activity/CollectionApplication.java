package com.aj.collection.activity;

/**
 * Created by kevin on 15-9-26.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import com.aj.Constant;
import com.aj.collection.R;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.collection.activity.http.API;
import com.aj.collection.activity.http.ReturnCode;
import com.aj.collection.activity.http.URLs;
import com.aj.collection.activity.tools.SPUtils;
import com.aj.collection.activity.tools.Util;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ClearCacheRequest;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.jrs.utils.SprtPrinter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Collection Application
 */
public class CollectionApplication extends AppContext {

    public int cellIdentity = 0;  //抽样单单元格的身份标示，在应用中自加,用于减少拍照单元格拍完照片后的数据处理量，去除后除了影响稍许性能没有其他影响

    public static Context applicationContext;
    private static CollectionApplication instance;

    private static DaoMaster daoMaster;

    RequestQueue queue;

    SprtPrinter sprtPrinter;

    public double globalLatitude = -1d, globalLongitude = -1d;
    public int globalLocationMode = Constant.CAN_NOT_GET_LOCATION;
    public long globalLocationTime = Constant.CAN_NOT_GET_LOCATION_TIME;

    public BDLocation global_location;

    public String global_device_sn = Constant.CAN_NOT_GET_SERIES_NUMBER;

    final int FIVE_MINUTE =300;//300second=5min
    int location_timer=0;

    public TextView mLocationResult;
    public boolean gps_pause=false;

    @Override
    public void onCreate() {
        super.onCreate();

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        try{
            SDKInitializer.initialize(this);
        }catch (UnsatisfiedLinkError e){
            e.printStackTrace();
        }

        applicationContext = this;
        instance = this;

        queue = Volley.newRequestQueue(this); //init Volley

        initLocation();
    }


    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (queue == null) {
            queue = Volley.newRequestQueue(getApplicationContext());
        }

        File cacheDir = new File(this.getCacheDir(), "/data/data/com.aj.collection/cache/volley/");
        DiskBasedCache cache = new DiskBasedCache(cacheDir);
        queue.start();

        // clear all volley caches.
        queue.add(new ClearCacheRequest(cache, null));

        return queue;
    }


    public static CollectionApplication getInstance() {
        return instance;
    }

    /**
     * 广播接收器
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {//每分钟的广播 1.if have new task

            }
        }
    };



    public static ImageLoaderConfiguration config;

    public static void initImageLoader(Context context) {
        config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                // .writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);
    }


    public static DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.imageloading)
            .showImageForEmptyUri(R.drawable.pictures_empty)
            .showImageOnFail(R.drawable.pictures_empty).cacheInMemory(true)
            .cacheOnDisk(true).considerExifParams(true).build();

//    public static DisplayImageOptions defaultUserOptions = new DisplayImageOptions.Builder()
//            .showImageOnLoading(R.drawable.default_user_avatar)
//            .showImageForEmptyUri(R.drawable.default_user_avatar)
//            .showImageOnFail(R.drawable.default_user_avatar)
//            .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
//            .build();


    /**
     * 取得DaoMaster
     *
     * @param context
     * @return
     */
    public DaoMaster getDaoMaster(Context context) {

        DaoMaster.OpenHelper helper;

        String userName = (String) SPUtils.get(context, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE);
        if (userName.isEmpty())
            helper = new DaoMaster.DevOpenHelper(context, Constant.DB_DEFAULT_NAME, null);
        else
            helper = new DaoMaster.DevOpenHelper(context, userName, null);

        daoMaster = new DaoMaster(helper.getWritableDatabase());

        return daoMaster;
    }

    /**
     * 取得DaoSession
     *
     * @param context
     * @return
     */
    public DaoSession getDaoSession(Context context) {
        return getDaoMaster(context).newSession();
    }


    /**
     * 获取本地mac地址
     *
     * @return
     */
    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }


    /**
     * 登录侦听器
     */
    public interface LoginCompletedListener {
        void onFailed(String uid, String code, String message);

        void onLoginSuccess(String uid);

        void onLoginAppServerFailed(String uid, String code, String message);

        void onFirstLogin();
    }

    /**************************************
     * 后台定位
     ***************************************/


    public double latitude = 0, longitude = 0;
    public int locationMod = Constant.CAN_NOT_GET_LOCATION;

    StringBuffer sb = new StringBuffer(256); //通过stringbuffer添加地理信息
    String GPSCityInfo = "";//GPS城市描述信息
    String GPSCordInfo = "";//GPS坐标信息
    public int location_mode;

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            global_location = location;

            Intent intent = new Intent().setAction(Constant.LOCATION_BROADCAST_ACT);
            intent.putExtra(Constant.LOCATION_BROADCAST_VALUE, location);
            sendBroadcast(intent);

            //upload location information between five minute
            if(location_timer> FIVE_MINUTE){
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

                if (!((String) SPUtils.get(getApplicationContext(), SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)).isEmpty()&&
                        !((String) SPUtils.get(getApplicationContext(), SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)).isEmpty())
                    uploadLocation(longitude, latitude, locationMod);

                location_timer=0;                                                       //重新开始计时
            }

            if (gps_pause||mLocationResult==null)                                       //GPS如果暂停 或者 显示地理信息的文本框没有被赋值则不进行下面操作
                return;

            if (location.getLocType() == BDLocation.TypeGpsLocation) {                  //搜索到卫星 GPS定位成功 按钮显示开始

                if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                    sb.delete(0, sb.length());
                    sb.append(location.getAddrStr());
                    if (location.getAddrStr() == null || location.getAddrStr().equals("null"))
                        GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + "位置描述需要联网获取";
                    else
                        GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + location.getAddrStr();
                    GPSCordInfo = "\n经度：" + location.getLongitude() + "\n纬度：" + location.getLatitude() + "\n来源GPS定位";
                    mLocationResult.setText(GPSCityInfo + GPSCordInfo);

                    //将位置信息以JSON格式嵌入到TextView中，在保存解析的时候再解析出来

                    JSONObject locationJson=new JSONObject();
                    try {
                        locationJson.put(Constant.LATITUDE,location.getLatitude());
                        locationJson.put(Constant.LONGITUDE,location.getLongitude());
                        locationJson.put(Constant.LOCATION_MODE,Constant.LOCATION_BY_GPS);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mLocationResult.setTag(locationJson);

                    //save the location information to private variable
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    location_mode = Constant.LOCATION_BY_GPS;

                    //save the location information to global variable
                    globalLatitude = latitude;
                    globalLongitude = longitude;
                    globalLocationMode = location_mode;
                    globalLocationTime = System.currentTimeMillis();
                }

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {//网络定位 基站定位
                sb.delete(0, sb.length());
                sb.append(location.getAddrStr());
                if (location.getAddrStr() == null || location.getAddrStr().equals("null"))
                    GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + "位置描述需要联网获取";
                else
                    GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + location.getAddrStr();
                GPSCordInfo = "\n经度：" + location.getLongitude() + "\n纬度：" + location.getLatitude() + "\n来源网络定位";
                mLocationResult.setText(GPSCityInfo + GPSCordInfo);

                //将位置信息以JSON格式嵌入到TextView中，在保存解析的时候再解析出来

                JSONObject locationJson=new JSONObject();
                try {
                    locationJson.put(Constant.LATITUDE,location.getLatitude());
                    locationJson.put(Constant.LONGITUDE,location.getLongitude());
                    locationJson.put(Constant.LOCATION_MODE,Constant.LOCATION_BY_NETWORK);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mLocationResult.setTag(locationJson);

                //save the location information to private variable
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                location_mode = Constant.LOCATION_BY_NETWORK;

                //save the location information to global variable
                globalLatitude = latitude;
                globalLongitude = longitude;
                globalLocationMode = location_mode;
                globalLocationTime = System.currentTimeMillis();

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                if (location.getAddrStr() == null || location.getAddrStr().equals("null"))
                    GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + "位置描述需要联网获取";
                else
                    GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + location.getAddrStr();
                GPSCordInfo = "\n经度：" + location.getLongitude() + "\n纬度：" + location.getLatitude() + "\n来源离线定位";
                mLocationResult.setText(GPSCityInfo + GPSCordInfo);

                JSONObject locationJson=new JSONObject();
                try {
                    locationJson.put(Constant.LATITUDE,location.getLatitude());
                    locationJson.put(Constant.LONGITUDE,location.getLongitude());
                    locationJson.put(Constant.LOCATION_MODE,Constant.LOCATION_BY_OFFLINE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mLocationResult.setTag(locationJson);

                //save the location information to private variable
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                location_mode = Constant.LOCATION_BY_OFFLINE;

                //save the location information to global variable
                globalLatitude = latitude;
                globalLongitude = longitude;
                globalLocationMode = location_mode;
                globalLocationTime = System.currentTimeMillis();
            } else {
                //判断与上次定位成功的时间间隔是否超过一定的小时数
                if (System.currentTimeMillis() - globalLocationTime >
                        Constant.LOCATION_SUSTAIN_HOUR * 60 * 60 * 1000) {
                    mLocationResult.setText(getString(R.string.gpsFailed));

                    latitude = -1d;
                    longitude = -1d;
                    location_mode = Constant.CAN_NOT_GET_LOCATION;
                } else {//未超过使用上次定位成功的信息
                    long last_locate_time = globalLocationTime;
                    String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(last_locate_time));
                    if (location.getAddrStr() == null || location.getAddrStr().equals("null"))
                        GPSCityInfo = "【" + time + "】" + "位置描述需要联网获取";
                    else
                        GPSCityInfo = "【" + time + "】" + location.getAddrStr();

                    //save the location information to private variable
                    latitude = globalLatitude;
                    longitude =globalLongitude;
                    location_mode = globalLocationMode;

                    GPSCordInfo = "\n经度：" + String.valueOf(longitude) + "\n纬度：" + String.valueOf(latitude) +
                            "\n来自" + ((int) (System.currentTimeMillis() - globalLocationTime) / (1000 * 60)) + "分钟前的定位信息";
                    mLocationResult.setText(GPSCityInfo + GPSCordInfo);
                }

            }

        }

    }

    LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;//定位模式
    private String tempcoor = Constant.COORDINATION_STADARD;//定位坐标标准   bd09ll 百度经纬度标准    "gcj02"国家测绘局标准    "bd09"百度墨卡托标准
    public MyLocationListenner myListener;//定位监听器，会每隔span时间返回一次数据，后面还需要LocationClient注册定位监听器
    public Vibrator mVibrator;
    public LocationClient mLocationClient = null;

    private void initLocation() {
        mLocationClient = new LocationClient(this.getApplicationContext());
        myListener = new MyLocationListenner();
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        mLocationClient.registerLocationListener(myListener);//注册定位监听器

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(tempcoor);//可选，默认gcj02，设置返回的定位结果坐标系，
        int span = 1000;//设置刷新间隔

//        option.setAddrType("all");
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);
        mLocationClient.start();//定位SDK start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request


    }

    /**
     * sent the location to server
     *
     * @param longitude
     * @param latitude
     * @param locationMod
     */
    public void uploadLocation(final double longitude, final double latitude, final int locationMod) {

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
                    } else if (errorCode.equals(ReturnCode.USERNAME_OR_PASSWORD_INVALIDE)) {
                        //TODO may have other return code
                        Log.e("XXXXXX", "位置上传时发现用户名和密码错误");
//                        returnToLoginActivity();
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
                (String) SPUtils.get(getApplicationContext(), SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE),
                (String) SPUtils.get(getApplicationContext(), SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE), longitude_str, latitude_str, locationMod_str);

        queue.add(stringRequest);
    }

    private void returnToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

        startActivity(intent);
    }
}
