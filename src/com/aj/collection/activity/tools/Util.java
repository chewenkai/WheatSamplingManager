package com.aj.collection.activity.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aj.Constant;
import com.aj.collection.activity.CollectionApplication;
import com.aj.database.TASKINFO;
import com.aj.collection.activity.service.LongTermService;
import com.aj.collection.activity.service.MsgService;
import com.aj.collection.activity.ui.MyLayout;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.widget.Toast;

public class Util {

    public static final String TIME_FORMAT_DEFAOUT = "yyyyMMddHHmmss";
    public static final String KEY_SEQUENCE = "local_sequence";

    /**
     * 获得当前系统时间
     *
     * @param timeformate 如"yyyyMMddHHmmss"
     * @return string
     */
    public static String getCurrentTime(String timeformate) {
        String time = null;
        time = new SimpleDateFormat(timeformate, Locale.getDefault()).format(new Date(System
                .currentTimeMillis()));
        return time;
    }

    /**
     * toast
     *
     * @param context current context
     * @param message for display
     */
    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示指定路径的图片
     *
     * @param path
     * @return
     */
    public static Bitmap getBitmap(String path, int sampleSize) {
        System.out.println("sign_path-->" + path);
        File file = new File(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        System.out.println("file-->" + file.exists());
        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(file.getPath(), options);
            System.out.println("bm-->" + bm);
            return bm;
        }
        return null;
    }

    /**
     * 将bitmap转换为字符串
     *
     * @param bitmap
     * @return
     */
    public static String bitmaptoString(Bitmap bitmap) {

        //将Bitmap转换成字符串
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }

    /**
     * 將字符串转换为bitmap
     *
     * @param string
     * @return
     */
    public static Bitmap stringtoBitmap(String string) {
        //将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 获得全局流水
     *
     * @param context
     * @param inc     true 则+1
     * @return
     */

    public static String getLocalSeq(Context context, boolean inc) {
        String num = "暂未实现";
        int sequence = 0;
        sequence = (int)SPUtils.get(context, SPUtils.SHEET_SERIAL_NUMBER_KEY, 0, SPUtils.SHEET_SERIAL_NUMBER_FILE);
        if (inc) {
            sequence = sequence + 1;
            if (sequence >= 10000) //1~99999循环
//			if(sequence > 1000) //1~999循环
            {
                sequence = 1;
            }
            SPUtils.put(context, SPUtils.SHEET_SERIAL_NUMBER_KEY, sequence, SPUtils.SHEET_SERIAL_NUMBER_FILE);
        }
        num = Integer.toString(sequence);
        while (num.length() != 4) {
            num = "0" + num;
        }
        return num;
    }

    /**
     * 使sn长度至少为5位
     * @param sn
     * @return
     */
    public static String transformDeviceSN(String sn){

        while(sn.length()<6){
            sn="0"+sn;
        }

        return sn;
    }

    /**
     * 全局流水加1写入文件
     *
     * @param context
     * @return
     */
    public static boolean writeLocalSeq(Context context) {
        try {
            int sequence = 0;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            sequence = sp.getInt(KEY_SEQUENCE, 0);
            sequence = sequence + 1;
            editor.putInt(KEY_SEQUENCE, sequence);
            editor.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean decendLocalSeq(Context context,int num) {
        try {
            int sequence = 0;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            sequence = sp.getInt(KEY_SEQUENCE, 0);
            sequence = sequence - num;
            editor.putInt(KEY_SEQUENCE, sequence);
            editor.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断GPS是否开启
     *
     * @param context
     * @return
     */
    public static final boolean isOpen(final Context context) {
        LocationManager locationManager = (LocationManager
                ) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//		boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps) {
            return true;
        }
        return false;
    }

    /**
     * 强制开启GPS
     *
     * @param context
     */
    public static final void openGps(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得被检单位的地理位置信息
     *
     * @param jsonStr
     * @return
     */
    public static String getAddressFromJson(String jsonStr) {
        try {
            JSONObject task = new JSONObject(jsonStr);  //第一层的对象
            JSONArray task_value = task.getJSONArray(MyLayout.TASK); //通过key取得value  key--"task".


            JSONObject jsonObject = task_value.optJSONObject(2);    //第二层的对象 被检单位
            JSONArray item_value = jsonObject.getJSONArray(MyLayout.getName(2));
            for (int j = 0; j < item_value.length(); j++)    //循环取出第二层的值
            {
                JSONObject itemObject = item_value.optJSONObject(j);    //第三层的对象
                System.out.println(jsonObject.names());     //打印出第二层所包含的
                String type = itemObject.optString(MyLayout.TYPE);
                if (!type.equals(MyLayout.TYPE_ADDR)) {
                    continue;
                }
                String cont = itemObject.getString(MyLayout.CONT);
                return cont;
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }


    /**
     * 像素转化dp
     *
     * @param context
     * @param px
     * @return
     */
    public int Px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * dp>px
     *
     * @param context
     * @param dp
     * @return
     */
    public int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * unicode转ch
     *
     * @param utfString
     * @return
     */
    public static String convert(String utfString) {
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;

        while ((i = utfString.indexOf("\\u", pos)) != -1) {
            sb.append(utfString.substring(pos, i));
            if (i + 5 < utfString.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(utfString.substring(i + 2, i + 6), 16));
            }
        }

        return sb.toString();
    }

    /**
     * 获取抽样单的编号
     *Formate:抽样单唯一编号：字母（5）+设备号（6）+年月日（6）+流水号（4）=21
     * @param context
     * @param taskinfo
     * @return
     */
    public static String getSamplingNum(Context context, TASKINFO taskinfo) {

        if(((CollectionApplication)context.getApplicationContext()).global_device_sn!= Constant.CAN_NOT_GET_SERIES_NUMBER){
            return taskinfo.getTask_letter() + ((CollectionApplication)context.getApplicationContext()).global_device_sn + Util.getCurrentTime("yyMMdd") + Util.getLocalSeq(context, true);
        }else if(((CollectionApplication)context.getApplicationContext()).global_device_sn== Constant.CAN_NOT_GET_SERIES_NUMBER&&
                !((String)SPUtils.get(context,SPUtils.DEV_SN,"",SPUtils.SYSVARIABLE)).isEmpty()){
            ((CollectionApplication)context.getApplicationContext()).global_device_sn=(String)SPUtils.get(context,SPUtils.DEV_SN,"",SPUtils.SYSVARIABLE);
            return taskinfo.getTask_letter() + ((CollectionApplication)context.getApplicationContext()).global_device_sn + Util.getCurrentTime("yyMMdd") + Util.getLocalSeq(context, true);
        }else
            return taskinfo.getTask_letter() + ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() + Util.getCurrentTime("yyMMdd") + Util.getLocalSeq(context, true);
    }

    /**
     * 获取抽样单的编号
     *Formate:抽样单唯一编号：字母（5）+设备号（6）+年月日（6）+流水号（4）=21
     * @param context
     * @param taskLetter 五位任务字母编码
     * @return
     */
    public static String getSamplingNum(Context context, String taskLetter) {

        if(((CollectionApplication)context.getApplicationContext()).global_device_sn!= Constant.CAN_NOT_GET_SERIES_NUMBER){
            return taskLetter + ((CollectionApplication)context.getApplicationContext()).global_device_sn + Util.getCurrentTime("yyMMdd") + Util.getLocalSeq(context, true);
        }else if(((CollectionApplication)context.getApplicationContext()).global_device_sn== Constant.CAN_NOT_GET_SERIES_NUMBER&&
                !((String)SPUtils.get(context,SPUtils.DEV_SN,"",SPUtils.SYSVARIABLE)).isEmpty()){
            ((CollectionApplication)context.getApplicationContext()).global_device_sn=(String)SPUtils.get(context,SPUtils.DEV_SN,"",SPUtils.SYSVARIABLE);
            return taskLetter + ((CollectionApplication)context.getApplicationContext()).global_device_sn + Util.getCurrentTime("yyMMdd") + Util.getLocalSeq(context, true);
        }else
            return taskLetter + ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() + Util.getCurrentTime("yyMMdd") + Util.getLocalSeq(context, true);
    }

    /**
     * 获取存取图片、录像、签名的文件夹路径
     * @param context
     * @return
     */
    public static String getMediaFolder(Context context) {
        File file = context.getExternalCacheDir();
        if (file != null)
            return file.getPath() + File.separator + "mediaFolder";
        else
            return "";
    }

    /**
     * 判断后台服务是否运行
     * @param serviceName
     * @param context
     * @return
     */
    public static boolean isServiceRunning(String serviceName,Context context) {
        ActivityManager manager = (ActivityManager) context.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (service
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 开启后台接受任务服务
     * @param context
     */
    public static void startMsgService(Context context) {

        context.startService(new Intent(context, MsgService.class));

    }

    /**
     * 开启后台位置上传
     * @param context
     */
    public static void startLongTermService(Context context) {

        context.startService(new Intent(context, LongTermService.class));

    }

    /**
     * 关闭后台位置上传
     * @param context
     */
    public static void stopLongTermService(Context context) {

        context.stopService(new Intent(context, LongTermService.class));

    }

    /**
     * 获取当前的代码行数
     * @return
     */
    public static String getLineInfo()
    {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return ste.getFileName() + ": Line " + ste.getLineNumber();
    }

    /**
     * 获取SD卡路径
     */
    public static String sdPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    /**
     * 获取Bitmap图像的Uri
     * 由于Picasso不能Load Bitmap图像，所以将Bitmap转为Uri后再Load
     * @param inContext
     * @param inImage
     * @return
     */
    public static Uri getBitmapUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
