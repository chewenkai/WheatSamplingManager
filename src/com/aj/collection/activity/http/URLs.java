package com.aj.collection.activity.http;

/**
 * 应用服务网址
 */
public class URLs {


    //正式版
    public final static String HOST = "120.25.69.38/heiljcydc";

    //测试版
//    public final static String HOST = "222.171.78.156:32002/heiljcydctest";


    public final static String HTTP = "http://";
    public final static String HTTPS = "https://";

    private final static String URL_SPLITTER = "/";
    private final static String URL_UNDERLINE = "_";

    private final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;

    //result constant
    public final static String KEY_ERROR = "error";
    public final static String KEY_MESSAGE = "msg";

    /**
     * 应用更新
     */
    public final static String UPDATEAPP = URL_API_HOST + "version/get";
    /**
     * 登录 get act=login  $username $password
     */
    public final static String LOGIN = URL_API_HOST + "jiekou.php";
    public final static String LOGINACT = "login";
    public final static String COMPANY = "samplingcompany";
    public final static String COMPANYADDR = "samplingaddr";
    public final static String CONTACT = "samplingcontact";
    public final static String CONTACTPHONE = "samplingphone";
    public final static String SERIAL_NUMBER = "devSN";
    public final static String POST_CODE = "postcode";
    public final static String IDENTITY_NUMBER = "idcode";
    public final static String BANK_CARD_POSSESSOR = "cardholder";
    public final static String BANK_CARD_NUMBER = "accountnumber";
    public final static String BANK_NAME = "depositbank";
    //	 $list = array('depositbank'=>$result['depositbank']);

    /**
     * 注销
     */
    public final static String LOGOUT = URL_API_HOST + "user/logout";
    /**
     * 注册 get $username         $password         $email
     */
    public final static String REGISTER = URL_API_HOST + "user.php";
    public final static String REGISTERACT = "act_register";
    /**
     * 验证码
     */
    public final static String VERIFY = URL_API_HOST + "user/verify";
    /**
     * 重置密码,获取验证码
     */
    public final static String RESETVERIFY = URL_API_HOST + "user/verify/resetpwd/1";
    /**
     * 重置密码
     */
    public final static String RESETPWD = URL_API_HOST + "user/find_reset_pwd";

    /**
     * 注册
     */

    public final static String JIEKOU = URL_API_HOST + "jiekou.php";
    public final static String GET_COMPANY_INFO = "getCompanyInfo";
    public final static String REGIST_NEW_USER = "registerNewUser";

    /**
     * 修改用户信息
     */
    public final static String EDIT_USER_INFO="editUser";
    /***************************DOWNLOAD**********************/

    /**
     * 查询是否有新任务 $act=getNewTaskFlag  $username=ceshi11111
     */
    public final static String HAVENEWTASK = URL_API_HOST + "jiekou.php";
    public final static String HAVENEWTASKACT = "getNewTaskFlag";
    public final static String RESULT_NEWTASK = "T";
    public final static String RESULT_NOTHING = "F";

    /**
     * 获取新任务 act=getNewTask  username=用户名  password = 密码
     */
    public final static String GETNEWTASK = URL_API_HOST + "jiekou.php";
    public final static String GETNEWTASKACT = "getNewTask";
    public final static String KEY_TASKID = "taskid";
    public final static String KEY_TASKNAME = "taskname";
    public final static String KEY_TASK_INI_LETTER = "task_initial_letter";
    public final static String KEY_TASKDISCRIPTION = "taskdiscription";
    public final static String KEY_TASKCONT = "taskcont";//目前指模板 一个任务只有一个模板
    public final static String KEY_SAMPLING = "sampling";//定点采样的抽样单

    public final static String KEY_SAMPLINGID = "samplingid";
    public final static String KEY_SAMPLINGCONT = "samplingcont";
    public final static String KEY_ITEMS = "samplingitems";//定点采样的样品名称
    public final static String KEY_ITEMSID = "samplingitemid";//定点采样的样品编号
    public final static String KEY_SAMPLING_COMPANY_NAME = "samplingcompanyname";//定点采样的抽样单位名称

    /**
     * 任务接受完成接口 act=setReceived     username=用户名
     */
    public final static String RECEIVED = URL_API_HOST + "jiekou.php";
    public final static String RECEIVEDACT = "setReceived";
    public final static String RESULT_RECEIVEDOK = "ok";

    /***************************UPLOAD***********************/

    /**
     * 上传图片接口
     */
    public final static String UPLAODIMG = URL_API_HOST + "uploadfile.php";

    /**
     * 上传抽样单 post $act username password tid tname scon sname sid
     */
    public final static String UPLOADSAMPLING = URL_API_HOST + "jiekou.php";
    public final static String UPLOADSAMPLINGACT = "uploadSample";

    /**
     * 任务完成
     */
    public final static String FINISHTASK = URL_API_HOST + "jiekou.php";
    public final static String FINISHTASKACT = "finishTask";

    /**
     * 上传位置
     */
    public final static String UPLOAD_LOCATION = URL_API_HOST + "jiekou.php";
    public final static String UPLOAD_LOCATION_ACT = "location";

    /*************************QUERY*************************/
    /**
     * 获取抽样单审核状态 act username password sid
     */
    public final static String GETSAMPLINGSTATUS = URL_API_HOST + "jiekou.php";
    public final static String GETSAMPLINGSTATUSACT = "isPassed";

    /**
     * 获取任务状态 act username password taskids
     */
    public final static String GETTASKSTATUS = URL_API_HOST + "jiekou.php";
    public final static String GETTASKSTATUSACT = "isTaskComplete";

    /*************************SAMPLING**********************/
    /**
     * 设置抽样单状态为已补采
     */
    public final static String SET_SAMPLING_STATUS_MADE_UP = URL_API_HOST + "jiekou.php";
    public final static String SET_SAMPLING_STATUS_MADE_UP_ACT = "setSamplingStatusMadeUp";
}
