package com.aj;

import android.content.Context;
import android.provider.CalendarContract;
import android.telephony.TelephonyManager;

import com.aj.collection.activity.CollectionApplication;

public class Constant {
//    final static public String DeviceID = ((TelephonyManager) CollectionApplication.Companion.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

    // 2017年小麦抽样单
    public static final String SHEETSTR = "{ \"specimen_sheet\": [ { \"cell_name\": \"样品机器编码\", \"cell_type\": \"type_serial_number\", \"cell_value\": \" \", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"F\"}, { \"cell_name\": \"抽样地点经纬度\", \"cell_type\": \"type_geographic_coordinates\", \"cell_value\": \"\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"T\"},{ \"cell_name\": \"品种名称\", \"cell_type \": \"type_edit_text\", \"cell_value\": \" \", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"F\"}, { \"cell_name\": \"抽样地点\", \"cell_type\": \"type_address\", \"cell_value\": \"\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"样品照片\", \"cell_type\": \"type_photos\", \"cell_value\": \"\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"F\", \"cell_default_print\": \"F\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"采样视频\", \"cell_type\": \"type_vedios\", \"cell_value\": \"\", \"cell_editable\": \"T\", \"cell_fill_required\": \"F\", \"cell_printable\": \"F\", \"cell_default_print\": \"F\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"该品种种植面积(亩)\", \"cell_type\": \"type_edit_text\", \"cell_value\": \" \", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"地力级别\", \"cell_type\": \"type_radio\", \"cell_value\": \"1级地,2级地,3级地\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"预估亩产水平(每亩)\", \"cell_type\": \"type_radio\", \"cell_value\": \"200-250,250-300,300-350,350-400,400-450,450-500,500以上\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"播种日期\", \"cell_type\": \"type_date_select\", \"cell_value\": \"\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"F\", \"cell_default_print\": \"F\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"取样时间\", \"cell_type\": \"type_auto_record_date\", \"cell_value\": \"\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"F\", \"cell_default_print\": \"F\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"施用农药情况\", \"cell_type\": \"type_edit_text\", \"cell_value\": \" \", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"F\", \"cell_default_print\": \"F\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"主要气象灾害\", \"cell_type\": \"type_multi_then_single_choice\", \"cell_value\": \"干旱,洪涝,高温,低温;严重,一般,轻微\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"主要病虫害发生情况\", \"cell_type\": \"type_multi_then_single_choice\", \"cell_value\": \"条锈病,赤霉病,白粉病,纹枯病,蚜虫,麦蜘蛛,吸浆虫;严重,一般,轻微\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"T\", \"cell_default_print\": \"T\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"病虫害情况照片\", \"cell_type\": \"type_photos\", \"cell_value\": \"\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"F\", \"cell_default_print\": \"F\", \"cell_copyable\": \"T\"}, { \"cell_name\": \"采样人签名\", \"cell_type\": \"type_sign\", \"cell_value\": \"\", \"cell_editable\": \"T\", \"cell_fill_required\": \"T\", \"cell_printable\": \"F\", \"cell_default_print\": \"F\", \"cell_copyable\": \"F\"} ] }";
    public static final String DEFAULT_SAMPLE_SN = "-1";

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

    public static final int DEFAULT_IMAGE_HEIGHT_DP = 180;

    // type photo
    public static final String GALLERY_CLICK_POSITION = "click_position";
    public static final String PHOTO_MEDIA_FOLDER = "photo_media_folder";
    public static final String CELL_ID_EXTRA_KEY = "cell_id_extra_key";
    public static final String NEW_PHOTO_BROADCAST_ACT = "com.kevin.new_photo_coming";
    public static final String NEW_VEDIO_BROADCAST_ACT = "com.kevin.new_vedio_coming";
}
