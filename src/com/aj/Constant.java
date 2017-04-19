package com.aj;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.aj.activity.CollectionApplication;

public class Constant {
    final static public String DeviceID = ((TelephonyManager) CollectionApplication.applicationContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

    //Btn的标识
    public static final int BTN_FLAG_DO = 0x01;
    public static final int BTN_FLAG_SEE = 0x01 << 1;
    public static final int BTN_FLAG_SETTING = 0x01 << 2;

    //Fragment的标识   
    public static final String FRAGMENT_FLAG_DO = "在抽任务";
    public static final String FRAGMENT_FLAG_SEE = " 已抽任务";
    public static final String FRAGMENT_FLAG_SETTING = "设置";

    public static final int WEIXINTASKREFRESHITEM_FROMDO = 7690001;

    //Page Flag ,judged is doing page or done/history page
    public static final int DOING_PAGE = 7690002;
    public static final int HISTORY_PAGE = 7690003;

    //DataBase
    public static final String DB_DEFAULT_NAME = "collection";
    public static final int S_STATUS_HAVE_NOT_UPLOAD = -1;      //抽样单未上传
    public static final int S_STATUS_CHECKING = 0;              //抽样单审核中
    public static final int S_STATUS_PASSED = 1;                //抽样单已通过
    public static final int S_STATUS_NOT_PASS = 2;              //抽样单未通过
    public static final int S_STATUS_DELETE = 3;                //抽样单被删除
    public static final int S_STATUS_TASKFINISHED = 4;
    public static final int S_STATUS_NOT_USED = 5;              //抽样单已补采

    //Task
    public static final int TASK_DONE = 1;
    public static final int TASK_DOING = 0;

    //Location
    public static final int CAN_NOT_GET_LOCATION_TIME = -1;
    public static final int CAN_NOT_GET_LOCATION = -1;
    public static final int LOCATION_BY_GPS = 0;
    public static final int LOCATION_BY_NETWORK = 1;
    public static final int LOCATION_BY_OFFLINE = 2;

    public static final int LOCATION_SUSTAIN_HOUR = 3;

    public static final String COORDINATION_STADARD = "bd09ll";//定位坐标标准   bd09ll 百度经纬度标准    "gcj02"国家测绘局标准    "bd09"百度墨卡托标准

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String LOCATION_MODE = "location_mod";


    //upload sampling
    public static final int UNKOWN_SAMPLING_TYPE = -1;
    public static final int NORMAL_SAMPLING_TYPE = 2;
    public static final int RESAMPLE_SAMPLING_TYPE = 1;

    //sampling
    public static final String NUMBER_UNNIQUE_NONE = "";
    public static final String DO_NOT_HAVE_SID = "-1";

    //when login
    public static final String CAN_NOT_GET_SERIES_NUMBER = "";

    //printer
    public static int SHOW_MSG_AND_OPEN_SETTING = 101;
    public static int SHOW_MSG = 100;

    public static final String LOCATION_BROADCAST_ACT = "com.kevin.location.broadcast";
    public static final String LOCATION_BROADCAST_VALUE = "broadcast_location_value";

}
