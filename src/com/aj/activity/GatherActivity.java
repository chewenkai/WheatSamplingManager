package com.aj.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.Constant;
import com.aj.SystemBarTintManager;
import com.aj.adapters.SampleExelGridAdapter;
import com.aj.bean.ImageInfo;
import com.aj.collection.R;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLE;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFO;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLEDao;
import com.aj.http.API;
import com.aj.http.ReturnCode;
import com.aj.http.URLs;
import com.aj.tools.ExitApplication;
import com.aj.tools.FileStream;
import com.aj.tools.L;
import com.aj.tools.SPUtils;
import com.aj.tools.T;
import com.aj.tools.Util;
import com.aj.ui.viewimage.PictureViewActivity;
import com.aj.ui.GridViewEx;
import com.aj.ui.HeadControlPanel;
import com.aj.ui.HeadControlPanel.LeftImageOnClick;
import com.aj.ui.HeadControlPanel.RightSecondOnClick;
import com.aj.ui.HeadControlPanel.rightFirstImageOnClick;
import com.aj.ui.MyLayout;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.LocationClientOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GatherActivity extends Activity {
    public static final int REQUESTCODEFORDATE = 100001;            //接收选择的日期
    //    public static final int REQUESTCODEFORSIGN = 10001;        //接收签名后的图片
    public static final int REQUESTCODEFORPICTURE = 1001;           //接收拍照后的图片
    public static final int REQUESTCODEFORVIDEO = 2001;             //接收录像的名称

    public static final String NECCESSARY = "neccessary";         //从json中解析必填是否全部填好
    public static final String SAMPLING_JSON_CONT = "cont";       //从json中解析出抽样单界面转成的json
    public static final String SAMPLING_JSON_ITEM = "item";       //从json中解析样品名称
    public static final String SAMPLING_JSON_ITEMID = "itemID";   //从json中解析样品编号
    public static final String SAMPLING_JSON_NUM = "num";         //从json中解析出抽样单编号
    public static final String SAMPLING_JSON_GPS = "gps";         //从json中解析出GPS信息

    Intent intent;                  //传递时间的意图
    String jsonStr;                 //抽样单json
    long templetID;
    String templetName;             //抽样单模板名称
    Long samplingID = -1L;          //抽样单ID

    String root_path;               //跟目录
    String child_path;              //储存照片录像等信息的子目录目录
    boolean isFixPoint;             //是否是定点采样
    boolean isMakeUp;
    Long sid_of_server;             //原抽样单的sid
    FileStream fs;                  //文件流
    MyLayout lu;                    //布局生成类
    TextView tv_gps;                //gps显示控件
    TextView tv_num;                //抽样单编号的TextView
    TextView bt_gps;                //gps暂停按钮
    public TextView addSamplingButton;
    String num;                     //抽样单编号
    long whichTask;                 //被点击的任务
    String taskName;
    LinearLayout parentView;
    SmsManager smsManager = SmsManager.getDefault();
    private String phone_number = "15004600323";
    private boolean kaiguan = false;
    private boolean isHavePicture = false, isHaveVideo = false;

    RequestQueue queue; //init Volley;

    private Context mContext = this;

    //database part
    public DaoSession daoSession;
    public TASKINFODao taskinfoDao;
    public TEMPLETTABLEDao templettableDao;
    public SAMPLINGTABLEDao samplingtableDao;

    private List<SAMPLINGTABLE> samplingtables = null; //当前这张抽样单，应该是一张

    private TASKINFO taskinfo;

    HeadControlPanel headPanel = null;

    public void sendMes() {
        if (!kaiguan) {
            SPUtils.put(GatherActivity.this, taskName, true, SPUtils.WHICHTASK);
            T.showShort(GatherActivity.this, "保存成功");
            return;
        }
        String sms_content = "任务名：" + templetName + "\n" +
                "编号：" + num +
                tv_gps.getText().toString();
        if (sms_content.length() > 70) {
            List<String> contents = smsManager.divideMessage(sms_content);
            for (String sms : contents) {
                smsManager.sendTextMessage(phone_number, null, sms, null, null);
            }
        } else {
            smsManager.sendTextMessage(phone_number, null, sms_content, null, null);
        }
        SPUtils.put(GatherActivity.this, taskName, true, SPUtils.WHICHTASK);
        T.showShort(GatherActivity.this, "保存成功，并已发送定位短信");
    }

    private EditText etCompanyName;
    private EditText etItem, etItemID;
    private String companyName;
    public String itemName, itemID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        dialog = new Dialog(GatherActivity.this);
        ExitApplication.getInstance().addActivity(this);
        //dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.form_layout);
        queue = Volley.newRequestQueue(this);
        //init database
        daoSession = ((CollectionApplication) ((Activity) mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();

        //沉浸状态栏
        SystemBarTintManager.setStatusBarTint(GatherActivity.this, Color.argb(0, 59, 59, 59));//透明状态栏

        final String EMPTY_STRING = "";
        phone_number = (String) SPUtils.get(this, SPUtils.JIANKONG, EMPTY_STRING, (String) SPUtils.get(this, SPUtils.LOGIN_USER, EMPTY_STRING, SPUtils.USER_DATA));

        //发送短信的开关是否打开
        Object object = SPUtils.get(this, SPUtils.KAIGUAN, false, (String) SPUtils.get(this, SPUtils.LOGIN_USER, EMPTY_STRING, SPUtils.USER_DATA));
        if (object != null)
            kaiguan = (Boolean) object;
        else
            kaiguan = false;

        parentView = (LinearLayout) findViewById(R.id.form_parent);

        //Intent中包含了关于任务的信息
        intent = getIntent();

        //获取各种任务信息
        whichTask = intent.getLongExtra("whichTask", -1);               //获取任务ID
        jsonStr = intent.getStringExtra("res");                         //获得抽样单JSON
        templetID = intent.getLongExtra("templetID", -1);               //获取模板ID
        isMakeUp = intent.getBooleanExtra("isMakeUp", false);           //获取是否是补采的标识
        if (isMakeUp) { //make up have sid
            sid_of_server = intent.getLongExtra("sid_of_server", -1);   //获取SID
            samplingID = intent.getLongExtra("samplingID", -1);         //获取抽样单的ID
            if (samplingID != -1)
                samplingtables = samplingtableDao.queryBuilder().where(SAMPLINGTABLEDao.Properties.Id.eq(samplingID)).list();
        } else {
            sid_of_server = -1L;
        }

        List<TASKINFO> taskinfos = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(whichTask)).list();//查询任务
        if (taskinfos.size() != 1) {
            printLineInLog();
            finish();
        } else
            taskinfo = taskinfos.get(0);

        taskName = taskinfo.getTask_name();                              //获取任务名称
        templetName = templettableDao.queryBuilder().where(TEMPLETTABLEDao.Properties.TempletID.eq(templetID)).list().get(0).getTemplet_name();       //抽样单模板名称

        //获取并检查创建媒体文件夹
        root_path = Util.getMediaFolder(mContext);
        if (root_path.isEmpty()) {
            Toast.makeText(mContext, "没有媒体文件夹", Toast.LENGTH_LONG).show();
            finish();
        }
        File taskfolder = new File(root_path);
        if (!taskfolder.exists() && !taskfolder.mkdirs()) {
            Toast.makeText(mContext, "没有媒体文件夹", Toast.LENGTH_LONG).show();
            finish();
        }


        //设置顶部面板按钮
        headPanel = (HeadControlPanel) findViewById(R.id.head_layout);
        headPanel.setRightFirstVisible(View.VISIBLE);
        headPanel.setRightSecondVisible(View.VISIBLE);
        if (headPanel != null) {
            headPanel.initHeadPanel();
            headPanel.setMiddleTitle(getResources().getString(R.string.templetTitle));
            headPanel.setLeftImage(R.drawable.ic_menu_back);
            final LeftImageOnClick l = new LeftImageOnClick() {

                @Override
                public void onImageClickListener() {

                    AlertDialog alertDialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(GatherActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    builder.setTitle("温馨提示");
                    builder.setMessage("确定要返回吗?");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //TODO 判断新拍的照片并删除
                            setResult(Constant.WEIXINTASKREFRESHITEM_FROMDO);
                            Util.decendLocalSeq(getApplicationContext(), 1);
                            finish();    //退出
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    alertDialog = builder.create();
                    alertDialog.show();    //显示对话框
                }
            };
            headPanel.setLeftImageOnClick(l);
            headPanel.setRightFirstImage(R.drawable.save_file);
            headPanel.setRightFirstText("保存");
            rightFirstImageOnClick r = new rightFirstImageOnClick() {

                @Override
                public void onImageClickListener() {//抽样单模板的保存按钮！！

                    companyName = etCompanyName.getText().toString();
                    itemName = etItem.getText().toString().replace(" ", "");
                    itemID = etItemID.getText().toString();

                    if (tv_gps.getText().toString().equals("") || ((CollectionApplication) getApplication()).latitude == -1 || ((CollectionApplication) getApplication()).longitude == -1 ||
                            ((CollectionApplication) getApplication()).location_mode == Constant.CAN_NOT_GET_LOCATION) {
                        T.showShort(GatherActivity.this, "正在定位中...请稍后保存~");
                        return;
                    }
                    if (itemName.equals("") || itemID.equals("")) {
                        T.showShort(GatherActivity.this, R.string.should_not_keep_item_info_empty);
                        return;
                    }

                    int samplingCount = lu.samplingBlockSubitems.size();
                    final ProgressDialog saveProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                    saveProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    saveProgressDialog.setMax(samplingCount);
                    saveProgressDialog.setProgress(1);
                    saveProgressDialog.setMessage("保存中...");
                    saveProgressDialog.setCancelable(false);
                    saveProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {

                        }
                    });
                    saveProgressDialog.show();

                    for (int i = 0; i < samplingCount; i++) {

                        saveProgressDialog.setProgress(i + 1);
                        saveProgressDialog.setMessage("样品" + String.valueOf(i + 1) + "保存中...");

                        parentView.removeAllViews();
                        parentView.addView(lu.samplingBlockSubitems.get(i));

                        for (int j = 0; j < lu.otherBlockSubitems.size(); j++) {
                            parentView.addView(lu.otherBlockSubitems.get(j));
                        }

                        try {
                            String mediaPath = (String) lu.samplingBlockSubitems.get(i).getTag();

                            JSONObject samplingInfoJson = createJsonStr(false, parentView, mediaPath); //获取样品所有信息

                            //解析出样品的信息
                            final boolean isEditDone = samplingInfoJson.getBoolean(GatherActivity.NECCESSARY);
                            final String mes = samplingInfoJson.getString(GatherActivity.SAMPLING_JSON_CONT);
                            final String item = samplingInfoJson.getString(GatherActivity.SAMPLING_JSON_ITEM);
                            final String itemID = samplingInfoJson.getString(GatherActivity.SAMPLING_JSON_ITEMID);
                            final String num = samplingInfoJson.getString(GatherActivity.SAMPLING_JSON_NUM);
                            final JSONObject GPSInfo = samplingInfoJson.getJSONObject(GatherActivity.SAMPLING_JSON_GPS);


                            if (!isMakeUp) {        //正常保存抽样单

                                SAMPLINGTABLE samplingtable = new SAMPLINGTABLE(null, whichTask, templetID, item + "-" + itemID, companyName, mes, num,
                                        isEditDone, false, false, false, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(), null, null,
                                        GPSInfo.getDouble(Constant.LATITUDE), GPSInfo.getDouble(Constant.LONGITUDE),
                                        GPSInfo.getInt(Constant.LOCATION_MODE), num);
                                samplingtableDao.insertOrReplace(samplingtable);

                                List<TASKINFO> taskinfo = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(whichTask)).list();
                                if (taskinfo.size() != 1) {
                                    Log.e("XXXXXXXXXXXX", "MyLayout.createTextView 1034 line 根据任务iD查询的任务数不等于1 ");
                                }
                                sendMes();
                                finish();
                            } else {                 //补采抽样单时的保存

                                if (sid_of_server == -1) {//原抽样单的sid用于上传给服务器，服务器将原抽样单状态置位为“已补采”，不应为不存在
                                    Toast.makeText(mContext, "没有接受到sid", Toast.LENGTH_LONG).show();
                                    Log.e("XXXXXXXXXXX", "没有接受到sid");
                                    return;
                                }

                                Response.Listener<String> listener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String s) {
                                        Log.e("setSMadeUpSuc", s);
                                        try {
                                            JSONObject resultJson = new JSONObject(s);
                                            String errorCode = resultJson.getString(URLs.KEY_ERROR);
//                                            String result = resultJson.getString(URLs.KEY_MESSAGE);

                                            if (errorCode.equals(ReturnCode.Code0)) {//connected

                                                //设置当前抽样单状态为“已补采”
                                                for (int i = 0; i < samplingtables.size(); i++) {
                                                    samplingtables.get(i).setCheck_status(Constant.S_STATUS_NOT_USED);
                                                    samplingtableDao.insertOrReplace(samplingtables.get(i));
                                                }

                                                //将当前抽样单以后的抽样单ID+1（即将当前抽样单之后的抽样单往后挪一个，留出一个位置插入补采的抽样单）
                                                if (samplingID != -1) {
                                                    List<SAMPLINGTABLE> allSamplingtables = samplingtableDao.queryBuilder().where(SAMPLINGTABLEDao.Properties.Id.between(samplingID + 1, samplingtableDao.queryBuilder().list().size())).list();

                                                    for (int i = 0; i < allSamplingtables.size(); i++) {
                                                        allSamplingtables.get(i).setId(allSamplingtables.get(i).getId() + 1);
                                                        samplingtableDao.insertOrReplace(allSamplingtables.get(i));
                                                    }

                                                }

                                                //将补采抽样单插入
                                                SAMPLINGTABLE samplingtable = new SAMPLINGTABLE(samplingID + 1, whichTask, templetID, item + "-" + itemID + "-补采", companyName, mes, num,
                                                        isEditDone, false, false, true, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(), null, null,
                                                        GPSInfo.getDouble(Constant.LATITUDE), GPSInfo.getDouble(Constant.LONGITUDE), GPSInfo.getInt(Constant.LOCATION_MODE), num);
                                                samplingtableDao.insertOrReplace(samplingtable);

                                                sendMes();

                                                finish();

                                            } else {//other return code
                                                new ReturnCode(getApplicationContext(), errorCode, true);
                                            }

                                            if (saveProgressDialog.isShowing())
                                                saveProgressDialog.dismiss();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            printLineInLog();

                                            if (saveProgressDialog.isShowing())
                                                saveProgressDialog.dismiss();
                                        }
                                    }
                                };

                                Response.ErrorListener errorListener = new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {

                                        if (saveProgressDialog.isShowing())
                                            saveProgressDialog.dismiss();

                                        T.showShort(getApplicationContext(), getString(R.string.make_up_sampling_need_network));
                                    }
                                };

                                //设置服务器的当前抽样单状态为已补采
                                if (samplingtables.size() == 1)
                                    setSamplingStatusMadeUp(listener, errorListener, String.valueOf(samplingtables.get(0).getSid_of_server()));
                                else {
                                    Toast.makeText(mContext, "查询到多张原抽样单", Toast.LENGTH_LONG).show();
                                    printLineInLog();
                                    return;
                                }

                            }

                            if (saveProgressDialog.getProgress() == samplingCount)
                                saveProgressDialog.dismiss();


                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (saveProgressDialog.isShowing())
                                saveProgressDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                            T.showLong(GatherActivity.this, "生成jason错误" +
                                    "" + e.toString());
                            if (saveProgressDialog.isShowing())
                                saveProgressDialog.dismiss();
                        }
                    }
                }
            };
            headPanel.setRightFirstImageOnClick(r);
            headPanel.setRightSecondImage(R.drawable.print);
            headPanel.seRightSecondText("打印");
            RightSecondOnClick t = new RightSecondOnClick() {

                @Override
                public void onImageClickListener() {


                    String toPrint = "";

                    LinearLayout LL_All = lu.getparentView();

                    for (int i = 0; i < LL_All.getChildCount(); i++) {
                        for (int k = 0; k < ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildCount(); k++) {
                            LinearLayout myWidget = ((LinearLayout) ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildAt(k));

                            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() <= 2)//排除没有checkbox的控件
                                continue;

                            CheckBox cb = (CheckBox) ((LinearLayout) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(2)).getChildAt(0);
                            if (cb.isChecked()) {

                                //判断是否是地理信息 如果是 读取并跳过 如果不是 继续判断是否是checkbox
                                if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(lu.TYPE_GPS_STRING) && myWidget.getChildCount() > 2) {
                                    //不是多选控件的直接读取控件数据
                                    if (toPrint.equals(""))
                                        toPrint = toPrint + ">>" + ((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString() +
                                                ((TextView) myWidget.getChildAt(1)).getText().toString();
                                    else
                                        toPrint = toPrint + "\n>>" + ((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString() +
                                                ((TextView) myWidget.getChildAt(1)).getText().toString();
                                    continue;
                                }

                                //判断是否是拍照 如果是 直接跳过

                                if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(lu.TYPE_CAMERA_STRING) && myWidget.getChildCount() > 2) {
                                    continue;
                                }


                                //判断是否是多选框的控件,是多选的要遍历每个多选框是否选择
                                if (myWidget.getChildCount() > 2) {

                                    for (int l = 0; l < ((LinearLayout) myWidget.getChildAt(1)).getChildCount(); l++) {

                                        if (l == 0) {

                                            if (toPrint.equals("")) {
                                                toPrint = toPrint + ">>" + ((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString();
                                                if (((CheckBox) ((LinearLayout) myWidget.getChildAt(1)).getChildAt(l)).isChecked())
                                                    toPrint = toPrint + " " + ((CheckBox) ((LinearLayout) myWidget.getChildAt(1)).getChildAt(l)).getText().toString();
                                            } else {
                                                toPrint = toPrint + "\n>>" + ((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString();
                                                if (((CheckBox) ((LinearLayout) myWidget.getChildAt(1)).getChildAt(l)).isChecked())
                                                    toPrint = toPrint + " " + ((CheckBox) ((LinearLayout) myWidget.getChildAt(1)).getChildAt(l)).getText().toString();
                                            }

                                        } else if (((CheckBox) ((LinearLayout) myWidget.getChildAt(1)).getChildAt(l)).isChecked()) {
                                            toPrint = toPrint + " " + ((CheckBox) ((LinearLayout) myWidget.getChildAt(1)).getChildAt(l)).getText().toString();
                                        }
                                    }

                                    continue;
                                }

                                //不是多选控件的直接读取控件数据
                                if (toPrint.equals(""))
                                    toPrint = toPrint + ">>" + ((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString() +
                                            ((TextView) myWidget.getChildAt(1)).getText().toString();
                                else
                                    toPrint = toPrint + "\n>>" + ((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString() +
                                            ((TextView) myWidget.getChildAt(1)).getText().toString();
                            }
                        }

                    }

                    Intent i = new Intent(GatherActivity.this, PrintActivity.class);
                    i.putExtra("toPrint", toPrint);
                    i.putExtra("num", tv_num.getText());
                    startActivity(i);

                }


            };
            headPanel.setRightSecondOnClick(t);

            //非补采时，可以点击添加一个样品信息
            if (!isMakeUp) {
                headPanel.setRightThirdImage(R.drawable.add_sampling);
                headPanel.setRightThirdText("加样");
                HeadControlPanel.RightThirdOnClick thirdOnClick = new HeadControlPanel.RightThirdOnClick() {
                    @Override
                    public void onImageClickListener() {

                        //加样前要检查样品信息是否合法

                        companyName = etCompanyName.getText().toString();

                        itemName = etItem.getText().toString().replace(" ", "");
                        itemID = etItemID.getText().toString();

                        if (tv_gps.getText().toString().equals("") || ((CollectionApplication) getApplication()).latitude == -1 || ((CollectionApplication) getApplication()).longitude == -1 ||
                                ((CollectionApplication) getApplication()).location_mode == Constant.CAN_NOT_GET_LOCATION) {
                            T.showShort(GatherActivity.this, "正在定位中...请稍后再试");
                            return;
                        }
                        if (itemName.equals("") || itemID.equals("")) {
                            T.showShort(GatherActivity.this, getString(R.string.should_not_keep_item_info_empty));
                            return;
                        }

                        lu.addOneSampling(true);

                        //添加样品需要重新签字，对当前签字清除
                        ImageView ourSign = lu.getSamplingOurSignImaView();
                        ImageView othersSign = lu.getSamplingOtherSignImaView();

                        if (ourSign == null || othersSign == null) {
                            T.showShort(parentView.getContext(), "添加样品失败，获取不到对象！");

                            printLineInLog();
                            return;
                        }

                        String cont = "";
                        ourSign.setTag(cont);
                        ourSign.setImageBitmap(null);
                        ourSign.setImageResource(R.drawable.edit_query);
                        othersSign.setTag(cont);
                        othersSign.setImageBitmap(null);
                        othersSign.setImageResource(R.drawable.edit_query);

                        reLoadVariable();

                    }
                };
                headPanel.setRightThirdOnClick(thirdOnClick);
                headPanel.setmRightThirdVisible(View.VISIBLE);
            }
        }

        fs = new FileStream(mContext);          //写文件用到的流
        lu = new MyLayout(parentView, this, fs, root_path, MyLayout.DO_FORM, whichTask, null);    //生成布局的类
        lu.initLayout(jsonStr, true);           //初始化
        isFixPoint = lu.getIsFixPoint();        //是否是定点采样

        //初始化控件
        etCompanyName = lu.companyNameET;                                       //赋值被抽样单位名称控件

        etItem = lu.ItemsET;                                                    //赋值样品名称控件
        itemName = etItem.getText().toString().replace(" ", "");                //获取样品名称

        etItemID = lu.ItemsIDET;                                                //赋值样品ID控件
        itemID = etItemID.getText().toString();                                 //获取样品ID

        tv_num = lu.numberIdTV;                                                 //赋值抽样单编码控件

        tv_gps = lu.gpsIdTV;                                                    //赋值GPS控件
        ((CollectionApplication) getApplication()).mLocationResult = tv_gps;    //将tv_gps赋值给application中的textview
        ((CollectionApplication) getApplication()).gps_pause = false;           //查看和修改模式默认gps开启
        tv_gps.setText(getString(R.string.gpsFailed));

        bt_gps = lu.gpsBtnTV;                                                   //赋值GPS按钮控件

        tv_gps.setTextColor(getResources().getColor(R.color.text_color_gray));
        tv_num.setTextColor(getResources().getColor(R.color.text_color_gray));

        //根据是否是补采获取抽样单编码
        if (isMakeUp)
            num = tv_num.getText().toString() + getString(R.string.resample_flag);
        else
            num = Util.getSamplingNum(mContext, taskinfo);

        tv_num.setText(num);                                                    //设置抽样单编码

        addSamplingButton = lu.addSamplingButton;                               //暂时没用

        child_path = root_path + File.separator + tv_num.getText().toString();

        lu.samplingBlockSubitems.get(lu.samplingBlockSubitems.size() - 1).setTag(child_path);//将媒体文件夹的路径保存在样品情况的LinearLayout中

        File cameraSearch = new File(child_path);
        if (!cameraSearch.exists())
            cameraSearch.mkdirs();

        updateGridView();
        updateVideoGridView();
    }

    /**
     * 添加或删除完样品情况后重新为样品名称、样品编号、抽样单编号和childpath等赋值
     */
    public void reLoadVariable() {
        etItem = lu.ItemsET;

        etItemID = lu.ItemsIDET;

        tv_num = lu.numberIdTV;

        child_path = root_path + File.separator + tv_num.getText().toString();

        lu.samplingBlockSubitems.get(lu.samplingBlockSubitems.size() - 1).setTag(child_path);//将媒体文件夹的路径保存在样品情况的LinearLayout中

        updateGridView();
        updateVideoGridView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUESTCODEFORDATE)//日期选择器返回的日期
        {
            if (data != null) {
                String ymd = data.getStringExtra("ymd");
                int id = data.getIntExtra("btn_id", 0);
                Button btn = (Button) findViewById(id);
                if (ymd != null) {
                    btn.setText(ymd);
                } else {
                    btn.setText("重新获取日期");
                }
            }
        } else if (requestCode == REQUESTCODEFORVIDEO) {
            if (resultCode == RESULT_OK) {
                isHaveVideo = true;
                Uri uriVideo = data.getData();
                String video_path = uriVideo.toString();
                String video_name = new File(video_path).getName();
                updateVideoGridView();
            }
        } else if (requestCode == REQUESTCODEFORPICTURE) {
            if (data != null) {
                // 没有指定特定存储路径的时候
                T.showShort(this, "拍照成功");
                isHavePicture = true;
                int id = data.getIntExtra("view_id", 0);        // imageView id
                String pName = data.getStringExtra("pName");    //图片名称
                final String picPath = data.getStringExtra("picture_path");
                updateGridView();

            } else {
                L.d("data IS null, file saved on target position.");
            }
        }
    }

    /**
     * 由布局生成json字符串
     *
     * @param isSave 是否保存
     * @return
     * @throws JSONException
     */
    private JSONObject createJsonStr(boolean isSave, LinearLayout parentView, String media_path) throws JSONException {

        JSONObject jsonObject = new JSONObject();

        boolean neccessaryIsEdit = true;

        int count = parentView.getChildCount();    //获得条目数
        //parentView 下面是itemlayout,itemlayout下面是textview和linearlayout，linearLayout里面是具体的linearLayout
        JSONObject taskObject = new JSONObject();    //第一层task
        JSONArray taskValue = new JSONArray();    //task的值
        for (int i = 0; i < count; i++) //对应七个模块
        {
            LinearLayout module_layout = (LinearLayout) parentView.getChildAt(i);    //模块布局
            String module_key = ((TextView) ((LinearLayout) module_layout.getChildAt(0)).getChildAt(0)).getText().toString();
            LinearLayout module_value = (LinearLayout) module_layout.getChildAt(1);
            JSONObject module_object = new JSONObject();
            JSONArray module_array = new JSONArray();
            for (int j = 0; j < module_value.getChildCount(); j++) {
                JSONObject itemObject = new JSONObject();
                LinearLayout item_layout = (LinearLayout) module_value.getChildAt(j);//条目布局
                HashMap<String, Object> itemTag = (HashMap<String, Object>) item_layout.getTag();    //获得条目信息
                String type = (String) itemTag.get(MyLayout.TYPE);
                String necessary = (String) itemTag.get(MyLayout.NECESSARY);
                //类型为title/sign/camera时,直接将tag转成jsonObject即可
                if (type.equals(MyLayout.TYPE_TABLE)) {
                    if (isFixPoint) {
                        itemObject = lu.mapToJson(itemTag, null);
                    } else {
                        String cont = itemName;//将抽样单名字保存在TABLE中
                        itemObject = lu.mapToJson(itemTag, cont);
                    }
                } else if (type.equals(MyLayout.TYPE_CAMERA)) {

                    //获取本抽样单下的图片名称集合

                    ArrayList<String> savedPictureNames = new ArrayList<>();

                    File cameraSearch = new File(media_path);
                    if (!cameraSearch.exists())
                        cameraSearch.mkdirs();

                    //遍历文件夹获取文件名
                    if (cameraSearch.listFiles() != null) {
                        for (int k = 0; k < cameraSearch.listFiles().length; k++) {
                            if (cameraSearch.listFiles()[k].getName().startsWith("CAMERA_")) {
                                savedPictureNames.add(cameraSearch.listFiles()[k].getName());
                            }
                        }
                    }

                    //将名称集mapToJson
                    itemObject = lu.mapToJson(itemTag, savedPictureNames);

                    //判断是否已填
                    if (necessary.equals("T") && savedPictureNames.isEmpty()) {
                        neccessaryIsEdit = false;
                    }

                } else if (type.equals(MyLayout.TYPE_VIDEO)) {

                    ArrayList<String> savedVedioNames = new ArrayList<>();

                    File cameraSearch = new File(media_path);
                    if (!cameraSearch.exists())
                        cameraSearch.mkdirs();

                    //遍历文件夹
                    if (cameraSearch.listFiles() != null) {
                        for (int k = 0; k < cameraSearch.listFiles().length; k++) {
                            if (cameraSearch.listFiles()[k].getName().startsWith("VIDEO_")) {
                                savedVedioNames.add(cameraSearch.listFiles()[k].getName());
                            }
                        }
                    }

                    //将名称集mapToJson
                    itemObject = lu.mapToJson(itemTag, savedVedioNames);

                    //判断是否已填
                    if (necessary.equals("T") && savedVedioNames.isEmpty()) {
                        neccessaryIsEdit = false;
                    }
                } else if (type.equals(MyLayout.TYPE_AUTO) || type.equals(MyLayout.TYPE_MY_COMPANY)
                        || type.equals(MyLayout.TYPE_MY_COMPANY_ADDRESS) || type.equals(MyLayout.TYPE_SAMPLING_CONTACT)
                        || type.equals(MyLayout.TYPE_SAMPLING_PHONE)) {
                    String cont = ((TextView) item_layout.getChildAt(1)).getText().toString();    //输入的内容
                    itemObject = lu.mapToJson(itemTag, cont);
                } else if (type.equals(MyLayout.TYPE_INPUT) || type.equals(MyLayout.TYPE_ADDR)
                        || type.equals(MyLayout.TYPE_COMPANY)) {
                    EditText editText = ((EditText) item_layout.getChildAt(1));
                    String cont = editText.getText().toString();    //输入的内容
                    itemObject = lu.mapToJson(itemTag, cont);
                    if (necessary.equals("T") && cont.isEmpty()) {
                        neccessaryIsEdit = false;
                    }
                } else if (type.equals(MyLayout.TYPE_ITEMS)) {
                    EditText editText = ((EditText) item_layout.getChildAt(1));
                    String cont = editText.getText().toString();    //输入的内容
                    itemObject = lu.mapToJson(itemTag, cont);
                    if (necessary.equals("T") && cont.isEmpty()) {
                        neccessaryIsEdit = false;
                    }

                    jsonObject.put(GatherActivity.SAMPLING_JSON_ITEM, cont);//将样品名称保存并返回

                } else if (type.equals(MyLayout.TYPE_ITEM_ID)) {
                    EditText editText = ((EditText) item_layout.getChildAt(1));
                    String cont = editText.getText().toString();    //输入的内容
                    itemObject = lu.mapToJson(itemTag, cont);
                    if (necessary.equals("T") && cont.isEmpty()) {
                        neccessaryIsEdit = false;
                    }

                    jsonObject.put(GatherActivity.SAMPLING_JSON_ITEMID, cont);//将样品编号保存并返回

                } else if (type.equals(MyLayout.TYPE_NUMBER) || type.equals(MyLayout.TYPE_COLLDATE)) {
                    String cont = ((TextView) item_layout.getChildAt(1)).getText().toString();    //自动生成的编号
                    if (type.equals(MyLayout.TYPE_NUMBER)) {
                        num = cont;
                    }
                    itemObject = lu.mapToJson(itemTag, cont);

                    jsonObject.put(GatherActivity.SAMPLING_JSON_NUM, cont);//将抽样单编号保存并返回

                } else if (type.equals(MyLayout.TYPE_DATE)) {
                    String cont = ((Button) item_layout.getChildAt(1)).getText().toString();    //日期内容
                    if (necessary.equals("T") && cont.equals(MyLayout.CHOOSE_DATE)) {
                        neccessaryIsEdit = false;
                    }
                    if (cont.equals(MyLayout.CHOOSE_DATE)) {
                        cont = "";
                    }
                    itemObject = lu.mapToJson(itemTag, cont);
                } else if (type.equals(MyLayout.TYPE_SIGN)) {
                    String cont = (String) ((ImageView) item_layout.getChildAt(1)).getTag();    //签名图片名称
                    itemObject = lu.mapToJson(itemTag, cont);
                    if (necessary.equals("T") && cont.equals("none")) {
                        neccessaryIsEdit = false;
                    }
                    if (isSave) {
                        cont = "";
                        itemTag.put(MyLayout.CONT, (Object) cont);    //更换Tag为空
                        ((ImageView) item_layout.getChildAt(1)).setTag(cont);
                        ((ImageView) item_layout.getChildAt(1)).setImageBitmap(null);
                        ((ImageView) item_layout.getChildAt(1)).setImageResource(R.drawable.edit_query);
                    }

                } else if (type.equals(MyLayout.TYPE_GPS)) {
                    String cont = ((TextView) item_layout.getChildAt(1)).getText().toString();
                    itemObject = lu.mapToJson(itemTag, cont);
                    if (necessary.equals("T") && (cont.isEmpty() || cont.equals(getString(R.string.gpsFailed)))) {
                        neccessaryIsEdit = false;
                    }

                    jsonObject.put(GatherActivity.SAMPLING_JSON_GPS, ((JSONObject) ((TextView) item_layout.getChildAt(1)).getTag()));

                } else if (type.equals(MyLayout.TYPE_SELECT)) {
                    JSONArray multi_value = new JSONArray();
                    int selectedCounter = 0;
                    for (int s = 0; s < ((LinearLayout) item_layout.getChildAt(1)).getChildCount(); s++) {

                        //获得多选框内的内容
                        CheckBox checkBox = ((CheckBox) ((LinearLayout) item_layout.getChildAt(1)).getChildAt(s));
                        String multi_array_value = checkBox.getText().toString();
                        JSONObject checkboxValue = new JSONObject();
                        checkboxValue.put("value", multi_array_value);
                        checkboxValue.put("isChecked", checkBox.isChecked());
                        multi_value.put(checkboxValue);
                        if (checkBox.isChecked())
                            selectedCounter++;
                    }
                    itemObject = lu.mapToJson(itemTag, multi_value);    //这里传的是jsonArray
                    if (necessary.equals("T") && selectedCounter == 0) {
                        neccessaryIsEdit = false;
                    }

                }
                module_array.put(itemObject);
            }
            module_object.put(lu.getNameKey(module_key), module_array);
            taskValue.put(module_object);    //将itemObject 放入task里面
        }
        taskObject.put(MyLayout.TASK, taskValue);

        jsonObject.put(GatherActivity.NECCESSARY, neccessaryIsEdit);
        jsonObject.put(GatherActivity.SAMPLING_JSON_CONT, taskObject.toString());

        return jsonObject;
    }

    LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;//定位模式
    private String tempcoor = Constant.COORDINATION_STADARD;//定位坐标标准   bd09ll 百度经纬度标准    "gcj02"国家测绘局标准    "bd09"百度墨卡托标准
    //    public MyLocationListenner myListener;//定位监听器，会每隔span时间返回一次数据，后面还需要LocationClient注册定位监听器
    public Vibrator mVibrator;

    @Override
    protected void onDestroy() {
        //((CollectionApplication)getApplication()).mLocationResult=null;
//        File todelete = new File(root_path + File.separator + tv_num.getText().toString());
//        if (todelete.exists())
//            FileUtil.delete(todelete);

        lu.recycleParentView();

        System.gc();
        super.onDestroy();
    }

    /**
     * 更新最后一个样品grid中的图片
     */
    public void updateGridView() {

        LinearLayout item_layout = lu.samplingBlockSubitems.get(lu.samplingBlockSubitems.size() - 1);//最后一个样品的块条目
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() == 2) {//选出照相和录像控件
                if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(lu.TYPE_CAMERA_STRING)) {//判断这个是照相控件
                    final GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));

                    //定义照片集 并添加照片
                    final ArrayList<ImageInfo> mImages = new ArrayList<ImageInfo>();

                    mImages.add(new ImageInfo(ImageInfo.PICTURE));//第一个添加的是照相图片

                    File cameraSearch = new File(child_path);
                    if (!cameraSearch.exists())
                        cameraSearch.mkdirs();

                    //其他的照片遍历child文件夹得道
                    if (cameraSearch.listFiles() != null) {
                        for (int j = 0; j < cameraSearch.listFiles().length; j++) {
                            if (cameraSearch.listFiles()[j].getName().startsWith("CAMERA_")) {
                                mImages.add(new ImageInfo(cameraSearch.listFiles()[j].getPath()));

                            }
                        }
                    }

                    mGridView.setTag(mImages);
                    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            ArrayList<ImageInfo> mImages = (ArrayList<ImageInfo>) mGridView.getTag();

                            if (i == 0) {//点击第一个是拍照控件

                                Intent intent = new Intent(GatherActivity.this, CameraView.class);
                                intent.putExtra("root_path", child_path);    //将存储图片根目录传递过去
                                intent.putExtra("view_id", GatherActivity.REQUESTCODEFORPICTURE + 0);        //控件ID
                                intent.putExtra("location", tv_gps.getText().toString());
                                intent.putExtra("number", tv_num.getText().toString());
                                startActivityForResult(intent, GatherActivity.REQUESTCODEFORPICTURE);
                                return;
                            }

                            //其他的是查看照片
                            Intent intent = new Intent(GatherActivity.this, PictureViewActivity.class);
                            intent.putExtra("picPath", mImages.get(i).getLocalPath());
                            startActivity(intent);
                        }
                    });

                    mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                            ArrayList<ImageInfo> mImages = (ArrayList<ImageInfo>) mGridView.getTag();
                            if (i != 0) {
                                final File file = new File(mImages.get(i).getLocalPath());
                                if (dialog.isShowing()) {
                                    return false;
                                }

                                LayoutInflater inflater = (LayoutInflater) GatherActivity.this
                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                RelativeLayout layout = (RelativeLayout) inflater.inflate(
                                        R.layout.dialogview_two_button, null);
                                TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
                                TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

                                TextView positivebutton = (TextView) layout.findViewById(R.id.textview_positive_button);
                                TextView negativebutton = (TextView) layout.findViewById(R.id.textview_negative_button);

                                Title.setText("删除该图片");
                                Message.setText("确认删除？");

                                positivebutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        if (file.exists()) {
                                            file.delete();
                                            updateGridView();
                                        } else
                                            Log.e("XXXXXXXX", "长按删除文件，文件不存在，不应该");

                                    }
                                });
                                negativebutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });


                                dialog.setContentView(layout);
                                dialog.setCancelable(false);
                                dialog.show();

                            }
                            return true;
                        }
                    });

                    ((SampleExelGridAdapter) mGridView.getAdapter()).setmImages(mImages);//自动notify数据
                }
            }
        }

    }


    /**
     * 更新最后一个样品Videogrid中的图片
     */
    public void updateVideoGridView() {

        LinearLayout item_layout = lu.samplingBlockSubitems.get(lu.samplingBlockSubitems.size() - 1);//最后一个样品的块条目
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() == 2) {//选出照相和录像控件
                if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(lu.TYPE_VIDEO_STRING)) {//判断这个是录像控件
                    final GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));

                    //定义照片集 并添加照片
                    final ArrayList<ImageInfo> mImages = new ArrayList<ImageInfo>();

                    mImages.add(new ImageInfo(ImageInfo.VIDEO));//第一个添加的是

                    File cameraSearch = new File(child_path);
                    if (!cameraSearch.exists())
                        cameraSearch.mkdirs();


                    //其他的照片遍历child文件夹得道
                    if (cameraSearch.listFiles() != null) {
                        for (int j = 0; j < cameraSearch.listFiles().length; j++) {
                            if (cameraSearch.listFiles()[j].getName().startsWith("VIDEO_")) {
                                ImageInfo video_image = new ImageInfo(createVideoThumbnail(cameraSearch.listFiles()[j].getPath(), 0));
                                video_image.setMediaType(ImageInfo.VIDEOBMP);
                                video_image.setLocalPath(cameraSearch.listFiles()[j].getPath());
                                mImages.add(video_image);
                            }
                        }
                    }

                    mGridView.setTag(mImages);
                    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            ArrayList<ImageInfo> mImages = (ArrayList<ImageInfo>) mGridView.getTag();
                            if (i == 0) {//点击第一个是录像控件

                                File video_name = new File(child_path + File.separator + "VIDEO_" + ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() + Util.getCurrentTime("yyMMddHHmmss") + ".mp4");
                                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);//更改录像质量 min=0 max =1
                                //intent.putExtra("view_id", v.getId());
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(video_name));
                                startActivityForResult(intent, GatherActivity.REQUESTCODEFORVIDEO);
                                return;
                            }
                            //其他的是查看照片
                            Uri url = Uri.parse("file://" + mImages.get(i).getLocalPath());
                            String type = "video/mp4";
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(url, type);
                            startActivity(intent);

                        }
                    });

                    mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                            ArrayList<ImageInfo> mImages = (ArrayList<ImageInfo>) mGridView.getTag();
                            if (i != 0) {
                                final File file = new File(mImages.get(i).getLocalPath());
                                if (dialog.isShowing()) {
                                    return false;
                                }

                                LayoutInflater inflater = (LayoutInflater) GatherActivity.this
                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                RelativeLayout layout = (RelativeLayout) inflater.inflate(
                                        R.layout.dialogview_two_button, null);
                                TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
                                TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

                                TextView positivebutton = (TextView) layout.findViewById(R.id.textview_positive_button);
                                TextView negativebutton = (TextView) layout.findViewById(R.id.textview_negative_button);

                                Title.setText("删除该视频");
                                Message.setText("确认删除？");

                                positivebutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        if (file.exists()) {
                                            file.delete();
                                            updateVideoGridView();
                                        } else
                                            Log.e("XXXXXXXX", "长按删除文件，文件不存在，不应该");

                                    }
                                });
                                negativebutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });


                                dialog.setContentView(layout);
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                            return true;
                        }
                    });

                    ((SampleExelGridAdapter) mGridView.getAdapter()).setmImages(mImages);//自动notify数据
                }
            }
        }

    }

    /**
     * 截取视频制定时间的帧画面
     *
     * @param filePath
     * @return
     */
    public Bitmap createVideoThumbnail(String filePath, long time) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        bitmap = retriever.getFrameAtTime(time);
        return bitmap;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isHaveVideo && lu.getVidIMGObject() != null) {
            lu.getVidIMGObject().setVisibility(View.GONE);
        } else if (!isHaveVideo && lu.getVidIMGObject() != null) {
            lu.getVidIMGObject().setVisibility(View.GONE);
        }

        if (isHavePicture && lu.getPicIMGObjet() != null) {
            lu.getPicIMGObjet().setVisibility(View.GONE);
        } else if (!isHavePicture && lu.getPicIMGObjet() != null) {
            lu.getPicIMGObjet().setVisibility(View.GONE);
        }

        boolean gps = Util.isOpen(this);
        boolean isNetConnected = ((CollectionApplication) getApplication()).isNetworkConnected();
        if (!gps) {
            openGPSDialog(gps, isNetConnected);

        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {    //监控back键
            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle("温馨提示");
            builder.setMessage("确定要返回吗?");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO 判断新拍的照片并删除
                    setResult(Constant.WEIXINTASKREFRESHITEM_FROMDO);
                    Util.decendLocalSeq(getApplicationContext(), 1);
                    finish();    //退出
                }
            });
            builder.setNegativeButton("取消", null);
            alertDialog = builder.create();
            alertDialog.show();    //显示对话框
        }
        return super.onKeyDown(keyCode, event);
    }

    private Dialog dialog;

    /**
     * 打开GPS提示对话框
     */
    void openGPSDialog(final boolean isGpsOpened, boolean isNetWorkOpened) {
        if (isGpsOpened)
            return;

        if (dialog.isShowing()) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) GatherActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.dialogview_three_button, null);
        TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
        TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

        TextView positivebutton = (TextView) layout.findViewById(R.id.textview_positive_button);
        TextView openGPRSButton = (TextView) layout.findViewById(R.id.textview_openGPRS_button);
        TextView negativebutton = (TextView) layout.findViewById(R.id.textview_negative_button);

        if (isGpsOpened && !isNetWorkOpened) {
            positivebutton.setVisibility(View.GONE);
            openGPRSButton.setVisibility(View.VISIBLE);
            Title.setText("获取位置的描述信息需要开启网络");
            Message.setText("建议开启");
        } else if (!isGpsOpened && isNetWorkOpened) {
            positivebutton.setVisibility(View.VISIBLE);
            openGPRSButton.setVisibility(View.GONE);
            Title.setText("本软件需开启GPS定位开关");
            Message.setText("必须开启，否则无法打开抽样单");
        } else if (!isGpsOpened && !isNetWorkOpened) {
            positivebutton.setVisibility(View.VISIBLE);
            openGPRSButton.setVisibility(View.VISIBLE);
            Title.setText("本软件需开启GPS定位开关\n获取位置的描述信息需要开启网络");
            Message.setText("是否开启？");
        }

        positivebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        });

        openGPRSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (android.os.Build.VERSION.SDK_INT > 10) {
                    //3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                } else {
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                }
            }
        });

        negativebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (!isGpsOpened)
                    GatherActivity.this.finish();
            }
        });


        dialog.setContentView(layout);
        dialog.setCancelable(false);
        dialog.show();
    }


    /**
     * 保存的时候 设置抽样单状态为已补采
     *
     * @param listener
     * @param errorListener
     * @param sid
     */
    public void setSamplingStatusMadeUp(Response.Listener<String> listener, Response.ErrorListener errorListener, String sid) {
        StringRequest stringRequest = API.setSamplingStatusMadeUp(listener, errorListener
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE), sid);
        queue.add(stringRequest);
    }

    /**
     * 打印行数到日志
     */
    public void printLineInLog() {
        Log.e(GatherActivity.class.getName(), Util.getLineInfo());
    }
}
