package com.aj.collection.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aj.Constant;
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
    private Context mContext = this;
    RequestQueue queue;

    private final int updatePeriod = 5;// update period time,the units is minute;

    private double latitude = 0, longitude = 0;
    private int locationMod = Constant.CAN_NOT_GET_LOCATION;



    @Override
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(this); //init Volley
        initLocation();
    }


    @Override
    public void onDestroy() {
        mLocationClient.unRegisterLocationListener(myListener);//取消注册定位监听器
        super.onDestroy();

    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
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

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

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

                    } else{

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        };
        StringRequest stringRequest = API.uploadLocation(listener, errorListener,
                (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE),
                (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE), longitude_str, latitude_str, locationMod_str);

        queue.add(stringRequest);
    }


}
