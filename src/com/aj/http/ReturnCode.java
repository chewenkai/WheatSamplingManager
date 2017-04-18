package com.aj.http;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.aj.activity.LoginActivity;
import com.aj.service.LongTermService;
import com.aj.service.MsgService;

/**
 * Created by kevin on 15-10-19.
 */
public class ReturnCode {

    /*******************************************RETURN CODE****************************************/

    /**
     * 接受信息成功
     */
    public final static String Code0 = "0";

    public final static String USER_NAME_EXIST = "10188";
    public final static String USER_NAME_EXIST_STRING = "该用户名已存在，请更换后再试";

    /**
     * 密码错误
     */
    public final static String PASSWORD_INVALIDE = "10100";
    public final static String PASSWORD_INVALIDE_STRING = "密码错误";

    /**
     * 账号不存在
     */
    public final static String NO_SUCH_ACCOUNT = "10101";
    public final static String NO_SUCH_ACCOUNT_STRING = "账号不存在";

    /**
     * 用户名或密码错误
     */
    public final static String USERNAME_OR_PASSWORD_INVALIDE = "10102";
    public final static String USERNAME_OR_PASSWORD_INVALIDE_STRING = "用户名或密码错误";

    /**
     * 用户名或密码为空
     */
    public final static String USERNAME_OR_PASSWORD_EMPTY = "10110";
    public final static String USERNAME_OR_PASSWORD_EMPTY_STRING = "用户名或密码为空";

    /**
     * 任务已经关闭
     */
    public final static String TASK_HAD_BEEN_CLOSED = "10201";
    public final static String TASK_HAD_BEEN_CLOSED_STRING = "任务已经关闭";

    /**
     * 没找到抽样单
     */
    public final static String SAMPLING_DOES_NOT_EXIST = "10111";
    public final static String SAMPLING_DOES_NOT_EXIST_STRING = "服务器无此抽样单！";

    /**
     * sid为空
     */
    public final static String EMPTY_SID = "10211";
    public final static String EMPTY_SID_STRING = "服务器获取不到抽样单ID（SID）";

    /**
     * tid为空
     */
    public final static String EMPTY_TID = "10212";
    public final static String EMPTY_TID_STRING = "服务器获取不到任务ID（TID）";

    /**
     * tname为空
     */
    public final static String EMPTY_TNAME = "10213";
    public final static String EMPTY_TNAME_STRING = "服务器获取不到任务名称TNAME";

    /**
     * SCON为空
     */
    public final static String EMPTY_SCON = "10214";
    public final static String EMPTY_SCON_STRING = "服务器获取不到抽样单内容（SCON）";

    /**
     * SNAME为空
     */
    public final static String EMPTY_SNAME = "10215";
    public final static String EMPTY_SNAME_STRING = "服务器获取不到抽样单名称（SNAME）";

    /**
     * resample标识为空
     */
    public final static String EMPTY_RESAMPLE = "10216";
    public final static String EMPTY_RESAMPLE_STRING = "服务器获取不到是否是补采RESAMPLE";

    /**
     * LATITUDE标识为空
     */
    public final static String EMPTY_LATITUDE = "10217";
    public final static String EMPTY_LATITUDE_STRING = "服务器获取不到纬度LATITUDE";

    /**
     * LONGITUDE标识为空
     */
    public final static String EMPTY_LONGITUDE = "10218";
    public final static String EMPTY_LONGITUDE_STRING = "服务器获取不到经度度LONGITUDE";

    /**
     * LOCATIONMODE标识为空
     */
    public final static String EMPTY_LOCATIONMODE = "10219";
    public final static String EMPTY_LOCATIONMODE_STRING = "服务器获取不到定位模式LOCATIONMODE";

    /**
     * SAMPLENO标识为空
     */
    public final static String EMPTY_SIMPLENO = "10220";
    public final static String EMPTY_SIMPLENO_STRING = "服务器获取不到抽样单编码SAMPLENO";

    /**
     * SAMPLEID为空
     */
    public final static String EMPTY_SAMPLEID = "10221";
    public final static String EMPTY_SAMPLEID_STRING = "服务器获取不到抽样单SAMPLEID";

    /**
     * TASKID为空
     */
    public final static String EMPTY_TASKID = "10222";
    public final static String EMPTY_TASKID_STRING = "服务器获取不到任务TASKID";

    /**
     * udid为空
     */
    public final static String EMPTY_UDID = "10088";
    public final static String EMPTY_UDID_STRING = "无法获取本机的唯一ID";

    /**
     * 账号在别处登陆
     */
    public final static String ACCOUNT_LOGIN_OTHER_DEVICE = "10099";
    public final static String ACCOUNT_LOGIN_OTHER_DEVICE_STRING = "账号在其他设备登陆，您被迫下线!";

    public ReturnCode(Context context, String returnCode, boolean showToast) {
        switch (returnCode) {
            case USER_NAME_EXIST:
                if (showToast)
                    Toast.makeText(context, USER_NAME_EXIST_STRING, Toast.LENGTH_LONG).show();
                break;
            case PASSWORD_INVALIDE:
                if (showToast)
                    Toast.makeText(context, PASSWORD_INVALIDE_STRING, Toast.LENGTH_LONG).show();
                break;
            case NO_SUCH_ACCOUNT:
                if (showToast)
                    Toast.makeText(context, NO_SUCH_ACCOUNT_STRING, Toast.LENGTH_LONG).show();
                break;
            case USERNAME_OR_PASSWORD_INVALIDE:
                if (showToast)
                    Toast.makeText(context, USERNAME_OR_PASSWORD_INVALIDE_STRING, Toast.LENGTH_LONG).show();
                break;
            case USERNAME_OR_PASSWORD_EMPTY:
                if (showToast)
                    Toast.makeText(context, USERNAME_OR_PASSWORD_EMPTY_STRING, Toast.LENGTH_LONG).show();
                break;
            case TASK_HAD_BEEN_CLOSED:
                if (showToast)
                    Toast.makeText(context, TASK_HAD_BEEN_CLOSED_STRING, Toast.LENGTH_LONG).show();
                break;
            case SAMPLING_DOES_NOT_EXIST:
                if (showToast)
                    Toast.makeText(context, SAMPLING_DOES_NOT_EXIST_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_SID:
                if (showToast)
                    Toast.makeText(context, EMPTY_SID_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_TID:
                if (showToast)
                    Toast.makeText(context, EMPTY_TID_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_TNAME:
                if (showToast)
                    Toast.makeText(context, EMPTY_TNAME_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_SCON:
                if (showToast)
                    Toast.makeText(context, EMPTY_SCON_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_SNAME:
                if (showToast)
                    Toast.makeText(context, EMPTY_SNAME_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_RESAMPLE:
                if (showToast)
                    Toast.makeText(context, EMPTY_RESAMPLE_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_LATITUDE:
                if (showToast)
                    Toast.makeText(context, EMPTY_LATITUDE_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_LONGITUDE:
                if (showToast)
                    Toast.makeText(context, EMPTY_LONGITUDE_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_LOCATIONMODE:
                if (showToast)
                    Toast.makeText(context, EMPTY_LOCATIONMODE_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_SIMPLENO:
                if (showToast)
                    Toast.makeText(context, EMPTY_SIMPLENO_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_TASKID:
                if (showToast)
                    Toast.makeText(context, EMPTY_TASKID_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_SAMPLEID:
                if (showToast)
                    Toast.makeText(context, EMPTY_SIMPLENO_STRING, Toast.LENGTH_LONG).show();
                break;
            case EMPTY_UDID:
                if (showToast)
                    Toast.makeText(context,EMPTY_UDID_STRING,Toast.LENGTH_LONG).show();
                break;
            case ACCOUNT_LOGIN_OTHER_DEVICE:
//                if (showToast)
//                    Toast.makeText(context,ACCOUNT_LOGIN_OTHER_DEVICE_STRING,Toast.LENGTH_LONG).show();

                if(getRunningActivityName(context).equals(LoginActivity.class.getName()))
                    break;

                context.stopService(new Intent(context, MsgService.class));
                context.stopService(new Intent(context, LongTermService.class));
                Intent intent=new Intent(context, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ACCOUNT_LOGIN_OTHER_DEVICE,true);
                context.startActivity(intent);
                break;
            default:
                Toast.makeText(context, "未知错误码！", Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * 获取当前运行的activity名称
     * @param context
     * @return
     */
    private String getRunningActivityName(Context context){
        ActivityManager activityManager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }

}
