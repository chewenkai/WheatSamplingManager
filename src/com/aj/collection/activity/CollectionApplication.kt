package com.aj.collection.activity

/**
 * Created by kevin on 15-9-26.
 */

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView

import com.aj.Constant
import com.aj.collection.R
import com.aj.collection.database.*
import com.aj.collection.http.API
import com.aj.collection.http.ReturnCode
import com.aj.collection.http.URLs
import com.aj.collection.tools.SPUtils
import com.aj.collection.tools.Util
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.github.yuweiguocn.library.greendao.MigrationHelper
import com.jrs.utils.SprtPrinter
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.QueueProcessingType

import org.json.JSONException
import org.json.JSONObject

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Collection Application
 */
class CollectionApplication : AppContext() {

    var cellIdentity = 0  //抽样单单元格的身份标示，在应用中自加,用于减少拍照单元格拍完照片后的数据处理量，去除后除了影响稍许性能没有其他影响

    internal var queue: RequestQueue? = null
    var sprtPrinter: SprtPrinter? = null
    var globalLatitude = -1.0
    var globalLongitude = -1.0
    var globalLocationMode = Constant.CAN_NOT_GET_LOCATION
    var globalLocationTime = Constant.CAN_NOT_GET_LOCATION_TIME.toLong()

    var global_location: BDLocation? = null

    var global_device_sn = Constant.CAN_NOT_GET_SERIES_NUMBER

    internal val FIVE_MINUTE = 300//300second=5min
    internal var location_timer = 0

    var mLocationResult: TextView? = null
    var gps_pause = false

    var daoSession: DaoSession? = null  // database session

    override fun onCreate() {
        super.onCreate()

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        try {
            SDKInitializer.initialize(this)
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }

        CollectionApplication.applicationContext = this
        instance = this



        queue = Volley.newRequestQueue(this) //init Volley

        initLocation()
    }

    fun initDaoSession(){
        val userName = SPUtils.get(this, SPUtils.LOGIN_NAME, Constant.DB_DEFAULT_NAME, SPUtils.LOGIN_VALIDATE) as String
        val helper = MySQLiteOpenHelper(this, userName, null)
        val db = helper.writableDb
        daoSession =  DaoMaster(db).newSession()
    }

    inner class MySQLiteOpenHelper(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?) : DaoMaster.OpenHelper(context, name, factory) {

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            MigrationHelper.migrate(db, SAMPLINGTABLEDao::class.java, TASKINFODao::class.java, TEMPLETTABLEDao::class.java)
        }
    }

    // lazy initialize the request queue, the queue instance will be
    // created when it is accessed for the first time
    val requestQueue: RequestQueue
        get() {
            if (queue == null) {
                queue = Volley.newRequestQueue(applicationContext)
            }
            return queue!!
        }

    /**
     * 取得DaoSession

     * @param context
     * *
     * @return
     */
    fun getDaoSession(context: Context): DaoSession {
        return daoSession!!
    }


    /**************************************
     * 后台定位
     */


    var latitude = 0.0
    var longitude = 0.0
    var locationMod = Constant.CAN_NOT_GET_LOCATION

    internal var sb = StringBuffer(256) //通过stringbuffer添加地理信息
    internal var GPSCityInfo = ""//GPS城市描述信息
    internal var GPSCordInfo = ""//GPS坐标信息
    var location_mode: Int = 0

    /**
     * 定位SDK监听函数
     */
    inner class MyLocationListenner : BDLocationListener {

        override fun onReceiveLocation(location: BDLocation) {

            global_location = location

            val intent = Intent().setAction(Constant.LOCATION_BROADCAST_ACT)
            intent.putExtra(Constant.LOCATION_BROADCAST_VALUE, location)
            sendBroadcast(intent)

            //upload location information between five minute
            if (location_timer > FIVE_MINUTE) {
                if (location.locType == BDLocation.TypeGpsLocation) {//搜索到卫星 GPS定位成功 按钮显示开始
                    if (location.latitude != 0.0 && location.longitude != 0.0) {
                        longitude = location.longitude
                        latitude = location.latitude
                        locationMod = Constant.LOCATION_BY_GPS
                    }
                } else if (location.locType == BDLocation.TypeNetWorkLocation) {//网络定位 基站定位
                    if (location.latitude != 0.0 && location.longitude != 0.0) {
                        longitude = location.longitude
                        latitude = location.latitude
                        locationMod = Constant.LOCATION_BY_NETWORK
                    }
                } else if (location.locType == BDLocation.TypeOffLineLocation) {
                    if (location.latitude != 0.0 && location.longitude != 0.0) {
                        longitude = location.longitude
                        latitude = location.latitude
                        locationMod = Constant.LOCATION_BY_OFFLINE
                    }
                } else {
                    longitude = 0.0
                    latitude = 0.0
                    locationMod = Constant.CAN_NOT_GET_LOCATION
                }

                if (!(SPUtils.get(applicationContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String).isEmpty() && !(SPUtils.get(applicationContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String).isEmpty())
                    uploadLocation(longitude, latitude, locationMod)

                location_timer = 0                                                       //重新开始计时
            }

            if (gps_pause || mLocationResult == null)
            //GPS如果暂停 或者 显示地理信息的文本框没有被赋值则不进行下面操作
                return

            if (location.locType == BDLocation.TypeGpsLocation) {                  //搜索到卫星 GPS定位成功 按钮显示开始

                if (location.latitude != 0.0 && location.longitude != 0.0) {
                    sb.delete(0, sb.length)
                    sb.append(location.addrStr)
                    if (location.addrStr == null || location.addrStr == "null")
                        GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + "位置描述需要联网获取"
                    else
                        GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + location.addrStr
                    GPSCordInfo = "\n经度：" + location.longitude + "\n纬度：" + location.latitude + "\n来源GPS定位"
                    mLocationResult!!.text = GPSCityInfo + GPSCordInfo

                    //将位置信息以JSON格式嵌入到TextView中，在保存解析的时候再解析出来

                    val locationJson = JSONObject()
                    try {
                        locationJson.put(Constant.LATITUDE, location.latitude)
                        locationJson.put(Constant.LONGITUDE, location.longitude)
                        locationJson.put(Constant.LOCATION_MODE, Constant.LOCATION_BY_GPS)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    mLocationResult!!.tag = locationJson

                    //save the location information to private variable
                    latitude = location.latitude
                    longitude = location.longitude
                    location_mode = Constant.LOCATION_BY_GPS

                    //save the location information to global variable
                    globalLatitude = latitude
                    globalLongitude = longitude
                    globalLocationMode = location_mode
                    globalLocationTime = System.currentTimeMillis()
                }

            } else if (location.locType == BDLocation.TypeNetWorkLocation) {//网络定位 基站定位
                sb.delete(0, sb.length)
                sb.append(location.addrStr)
                if (location.addrStr == null || location.addrStr == "null")
                    GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + "位置描述需要联网获取"
                else
                    GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + location.addrStr
                GPSCordInfo = "\n经度：" + location.longitude + "\n纬度：" + location.latitude + "\n来源网络定位"
                mLocationResult!!.text = GPSCityInfo + GPSCordInfo

                //将位置信息以JSON格式嵌入到TextView中，在保存解析的时候再解析出来

                val locationJson = JSONObject()
                try {
                    locationJson.put(Constant.LATITUDE, location.latitude)
                    locationJson.put(Constant.LONGITUDE, location.longitude)
                    locationJson.put(Constant.LOCATION_MODE, Constant.LOCATION_BY_NETWORK)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                mLocationResult!!.tag = locationJson

                //save the location information to private variable
                latitude = location.latitude
                longitude = location.longitude
                location_mode = Constant.LOCATION_BY_NETWORK

                //save the location information to global variable
                globalLatitude = latitude
                globalLongitude = longitude
                globalLocationMode = location_mode
                globalLocationTime = System.currentTimeMillis()

            } else if (location.locType == BDLocation.TypeOffLineLocation) {
                if (location.addrStr == null || location.addrStr == "null")
                    GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + "位置描述需要联网获取"
                else
                    GPSCityInfo = "【" + Util.getCurrentTime("HH:mm") + "】" + location.addrStr
                GPSCordInfo = "\n经度：" + location.longitude + "\n纬度：" + location.latitude + "\n来源离线定位"
                mLocationResult!!.text = GPSCityInfo + GPSCordInfo

                val locationJson = JSONObject()
                try {
                    locationJson.put(Constant.LATITUDE, location.latitude)
                    locationJson.put(Constant.LONGITUDE, location.longitude)
                    locationJson.put(Constant.LOCATION_MODE, Constant.LOCATION_BY_OFFLINE)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                mLocationResult!!.tag = locationJson

                //save the location information to private variable
                latitude = location.latitude
                longitude = location.longitude
                location_mode = Constant.LOCATION_BY_OFFLINE

                //save the location information to global variable
                globalLatitude = latitude
                globalLongitude = longitude
                globalLocationMode = location_mode
                globalLocationTime = System.currentTimeMillis()
            } else {
                //判断与上次定位成功的时间间隔是否超过一定的小时数
                if (System.currentTimeMillis() - globalLocationTime > Constant.LOCATION_SUSTAIN_HOUR * 60 * 60 * 1000) {
                    mLocationResult!!.text = getString(R.string.gpsFailed)

                    latitude = -1.0
                    longitude = -1.0
                    location_mode = Constant.CAN_NOT_GET_LOCATION
                } else {//未超过使用上次定位成功的信息
                    val last_locate_time = globalLocationTime
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(last_locate_time))
                    if (location.addrStr == null || location.addrStr == "null")
                        GPSCityInfo = "【$time】位置描述需要联网获取"
                    else
                        GPSCityInfo = "【" + time + "】" + location.addrStr

                    //save the location information to private variable
                    latitude = globalLatitude
                    longitude = globalLongitude
                    location_mode = globalLocationMode

                    GPSCordInfo = "\n经度：" + longitude.toString() + "\n纬度：" + latitude.toString() +
                            "\n来自" + (System.currentTimeMillis() - globalLocationTime).toInt() / (1000 * 60) + "分钟前的定位信息"
                    mLocationResult!!.text = GPSCityInfo + GPSCordInfo
                }

            }

        }

    }

    internal var tempMode: LocationClientOption.LocationMode = LocationClientOption.LocationMode.Hight_Accuracy//定位模式
    private val tempcoor = Constant.COORDINATION_STADARD//定位坐标标准   bd09ll 百度经纬度标准    "gcj02"国家测绘局标准    "bd09"百度墨卡托标准
    var myListener: MyLocationListenner?=null //定位监听器，会每隔span时间返回一次数据，后面还需要LocationClient注册定位监听器
    var mVibrator: Vibrator? = null
    var mLocationClient: LocationClient? = null

    private fun initLocation() {
        mLocationClient = LocationClient(this.applicationContext)
        myListener = MyLocationListenner()
        mVibrator = applicationContext.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        mLocationClient!!.registerLocationListener(myListener)//注册定位监听器

        val option = LocationClientOption()
        option.locationMode = tempMode//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(tempcoor)//可选，默认gcj02，设置返回的定位结果坐标系，
        val span = 1000//设置刷新间隔

        //        option.setAddrType("all");
        option.setScanSpan(span)//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true)//可选，设置是否需要地址信息，默认不需要
        option.isOpenGps = true//可选，默认false,设置是否使用gps
        option.isLocationNotify = true//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true)//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false)//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(true)//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true)//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient!!.locOption = option
        mLocationClient!!.start()//定位SDK start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request


    }

    /**
     * sent the location to server

     * @param longitude
     * *
     * @param latitude
     * *
     * @param locationMod
     */
    fun uploadLocation(longitude: Double, latitude: Double, locationMod: Int) {

        val longitude_str = longitude.toString()
        val latitude_str = latitude.toString()
        val locationMod_str = locationMod.toString()


        val listener = Response.Listener<String> { s ->
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val content = resultJson.getString(URLs.KEY_MESSAGE)
                Log.e("位置上传失败，错误信息:", content)

                if (errorCode == ReturnCode.Code0) {
                    //                        Toast.makeText(mContext, "上传位置成功:" + longitude + "," + latitude + "," + locationMod, Toast.LENGTH_LONG).show();
                    //                        testGetSamplingStatus();
                } else if (errorCode == ReturnCode.USERNAME_OR_PASSWORD_INVALIDE) {
                    Log.e("XXXXXX", "位置上传时发现用户名和密码错误")
                    //                        returnToLoginActivity();
                } else
                    Log.e("后台上传位置失败", errorCode)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            //				Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
            Log.e("uploadLocaFail", volleyError.toString())
        }
        val stringRequest = API.uploadLocation(listener, errorListener,
                SPUtils.get(applicationContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String,
                SPUtils.get(applicationContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String, longitude_str, latitude_str, locationMod_str)

        queue!!.add(stringRequest)
    }

    private fun returnToLoginActivity() {
        val intent = Intent(applicationContext, LoginActivity::class.java)

        startActivity(intent)
    }

    companion object {

        var applicationContext: Context? = null
        var instance: CollectionApplication? = null
    }
}
