package com.aj.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.activity.Calendar_View;
import com.aj.activity.CameraView;
import com.aj.activity.CollectionApplication;
import com.aj.activity.GatherActivity;
import com.aj.activity.WritePadDialog;
import com.aj.adapters.SampleExelGridAdapter;
import com.aj.bean.ImageInfo;
import com.aj.collection.R;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLE;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFO;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLEDao;
import com.aj.tools.DialogListener;
import com.aj.tools.FileStream;
import com.aj.tools.L;
import com.aj.tools.SPUtils;
import com.aj.tools.T;
import com.aj.tools.Util;
import com.aj.ui.viewimage.PictureViewActivity;
import com.aj.ui.widget.AutoCompleteEditText;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;

public class MyLayout {
    public static final String TASK = "task";            //第一层的键
    public static final String NAME = "name";            //条目名称
    public static final String TYPE = "type";            //条目类型
    public static final String CONT = "cont";            //条目内容
    public static final String CHAN = "chan";            //条目是否可以改变
    public static final String PRINT = "print";            //条目是否打印
    public static final String SIZE = "size";            //蓝牙打印机中的字体大小
    public static final String NECESSARY = "necessary";    //是否是必填选项

    public static final String CHOOSE_DATE = "点击选择日期";    //date button

    //	public static final String TYPE_TITLE = "TITLE";	//1.类型为标题
    public static final String TYPE_COLLDATE = "COLLDATE";//11.类型为采集日期
    public static final String TYPE_GPS = "GPS";        //7.类型为地理位置信息
    public static final String TYPE_CAMERA = "CAMERA";    //6.类型为拍照
    public static final String TYPE_VIDEO = "VIDEO";    //13.类型为录像
    public static final String TYPE_NUMBER = "NUM";        //8.类型为编号,文件流水号
    public static final String TYPE_ITEMS = "ITEMS";    //16.类型为样品名称
    public static final String TYPE_ITEM_ID = "ITEMID";  //样品编号

    public static final String TYPE_INPUT = "INPUT";    //2.类型为直接输入
    public static final String TYPE_DATE = "DATE";        //3.类型为日期
    public static final String TYPE_SELECT = "SELECT";    //4.类型为选择
    public static final String TYPE_SIGN = "SIGN";        //5.类型为签名
    //	public static final String TYPE_NAME = "NAME";		//9.类型为采集人
//	public static final String TYPE_DEVNUM = "DEVNUM";	//10.类型为设备编号
    public static final String TYPE_TABLE = "TABLE";        //12.类型为表头
    public static final String TYPE_AUTO = "CLIENTAUTOGENERATION";    //14.类型为客户端自动生成
    public static final String TYPE_COMPANY = "COMPANY";    //15.类型为被抽样单位名称
    public static final String TYPE_MY_COMPANY = "SAMPLINGCOMPANY";    //抽样单位名称
    public static final String TYPE_MY_COMPANY_ADDRESS = "SAMPLINGADDR";  //抽样单位地址
    public static final String TYPE_SAMPLING_CONTACT = "SAMPLINGCONTACT";  //抽样人
    public static final String TYPE_SAMPLING_PHONE = "SAMPLINGPHONE";  //抽样人电话

    //add by 2015-08-16
    public static final String TYPE_ADDR = "ADDRESS";  //17.类型为定点的地理位置

    public static final int SIZE_TABLE = 22;            //表头字体大小
    public static final int SIZE_FIRST = 35;            //一级标题字体大小
    public static final int SIZE_SECOND = 27;            //二级标题字体大小
    public static final int SIZE_THRIRD = 21;            //蓝字大小
    public static final int SIZE_CONTENT_TITLE = 21;            //条目title字体大小
    public static final int SIZE_CONTENT_TEXT = 18;            //条目内容字体大小

    public static final int DO_FORM = 100;    //填表模式
    public static final int SEE_FORM = 101; //看表模式


    public ImageView picture, video;

    private LinearLayout parentView; //整个抽样单界面
    private Activity activity;
    private FileStream fs;
    private String root_path;    //存储图片和录像的根路径
    private int doORwatch;
    private boolean isFixPoint;

    private long taskID;
    SAMPLINGTABLE samplingtable;

    //公共控件
//    public EditText companyET;
    public EditText companyNameET;
    public EditText ItemsET, ItemsIDET;
    public TextView numberIdTV, gpsIdTV, gpsBtnTV;
    public ImageView imageViewP;
    public TextView addSamplingButton;

    public String TYPE_COLLDATE_STRING = "";
    public String TYPE_GPS_STRING = "";
    public String TYPE_CAMERA_STRING = "";
    public String TYPE_VIDEO_STRING = "";
    public String TYPE_NUMBER_STRING = "";
    public String TYPE_ITEMS_STRING = "";
    public String TYPE_ITEM_ID_STRING = "";
    //写死这两个 因为是统一类型的
    public String TYPE_OUR_SING_STRING = "抽样单位: ";
    public String TYPE_OTHER_SING_STRING = "被抽样单位: ";

    //块条目集合
    public ArrayList<LinearLayout> samplingBlockSubitems = new ArrayList<>();//样品情况块条目集合
    public ArrayList<LinearLayout> otherBlockSubitems = new ArrayList<>();//其他的（被抽样单位、抽样单位、备注等。。。）块条目集合

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    private JSONArray jsonArray_Sampling;

    public LinearLayout getparentView() {
        return parentView;
    }

    public void recycleParentView() {
        if (parentView != null)
            parentView = null;
    }

    /**
     * 是否是定点采样
     *
     * @return
     */
    public boolean getIsFixPoint() {
        return isFixPoint;
    }

    /**
     * 布局的构造函数
     *
     * @param linearLayout
     * @param activity
     * @param fs
     * @param root_path
     * @param doORwatch
     */
    public MyLayout(LinearLayout linearLayout, Activity activity,
                    FileStream fs, String root_path, int doORwatch, long taskID, SAMPLINGTABLE samplingtable) {
        parentView = linearLayout;
        this.activity = activity;
        this.fs = fs;
        this.root_path = root_path;
        this.doORwatch = doORwatch;
        this.taskID = taskID;
        this.samplingtable = samplingtable;
        //init database
        daoSession = ((CollectionApplication) activity.getApplication()).getDaoSession(activity.getApplicationContext());
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();
    }

//    public void initLayout(String sheetJsonStr) {
//        try {
//            JSONObject task = new JSONObject(sheetJsonStr);    //第一层的对象
//            JSONArray task_value = task.getJSONArray(TASK);    //通过key取得value	key--"task".
//            for (int i = 0; i < task_value.length(); i++)    //循环取出第一层的值
//            {
//                JSONObject jsonObject = task_value.optJSONObject(i);    //第二层的对象
//                JSONArray item_value = jsonObject.getJSONArray(getName(i));
//                LinearLayout item_layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.item_layout, null);//第一层的布局文件 块条目 包括两部分 分别是名 和内容
//                TextView module_key = (TextView) item_layout.getChildAt(0);//第一层的大字“基本信息”
//                System.out.println(jsonObject.names() + "***  " + i);
//                module_key.setText(getNameInfo(getName(i)));
//                module_key.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_THRIRD);
//                LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);
//                for (int j = 0; j < item_value.length(); j++)    //循环取出第二层的值
//                {
//                    JSONObject itemObject = item_value.optJSONObject(j);    //第三层的对象
//                    HashMap<String, Object> item = new HashMap<String, Object>();    //将取出的值全部放进hashmap里
//                    System.out.println(jsonObject.names());        //打印出第二层所包含的
//                    item.put(NAME, itemObject.optString(NAME));        //条目名称
//                    item.put(TYPE, itemObject.optString(TYPE));        //条目类型
//                    item.put(CONT, itemObject.opt(CONT));                //条目内容
//                    item.put(CHAN, itemObject.optString(CHAN));        //条目内容是否可以改写
//                    item.put(PRINT, itemObject.optString(PRINT));        //条目是否可以打印
//                    item.put(SIZE, itemObject.optString(SIZE));        //条目的字体大小
//                    item.put(NECESSARY, itemObject.optString(NECESSARY)); //条目必填项
//                    System.out.println(item);
//                    LinearLayout parentLayout = createWidget(item, j, true);    //生成一个linearLayout（一个条目）
//
//                    module_value.addView(parentLayout);    //将条目添加至父类视图
//
//                }
//                parentView.addView(item_layout);
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            L.d("解析json失败 MyLayout >line:175");
//            Toast.makeText(activity.getApplicationContext(), "解析json失败", Toast.LENGTH_LONG).show();
//        }
//    }

    public void initLayout(String jsonStr, final boolean canUserEdit) {

        try {
            JSONObject task = new JSONObject(jsonStr);    //第一层的json对象
            JSONArray task_value = task.getJSONArray(TASK);    //通过key取得value	key--"task".
            for (int i = 0; i < task_value.length(); i++)    //循环取出json第一层的值
            {
                final JSONObject jsonObject = task_value.optJSONObject(i);    //json第二层的对象
                final JSONArray item_value = jsonObject.getJSONArray(getName(i));

                //第一个块条目
                LinearLayout item_layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.item_layout, null);//第一层的布局View  大块条目 包括两部分 分别是名：基本信息、抽样单位等 和条目块VIew

                TextView module_key = (TextView) ((LinearLayout) item_layout.getChildAt(0)).getChildAt(0); //用来显示块条目标题的TextView
                module_key.setText(getNameInfo(getName(i))); //设置块条目的标题
                module_key.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_THRIRD); //设置块条目标题的字体大小

                //基本信息旁显示加号，其他不显示
                final TextView addButton = (TextView) ((LinearLayout) item_layout.getChildAt(0)).getChildAt(1); //标题旁边的添加按钮
                if (jsonObject.has("specimencondition")) {
                    addButton.setVisibility(View.GONE);
                    module_key.setTag(1);

                    samplingBlockSubitems.add(item_layout);//样品情况的块条目view添加到样品情况集合

                    addSamplingButton = addButton;

                    jsonArray_Sampling = item_value;//保存样品情况的jsonArray，用于后续动态添加样品情况
                } else if (jsonObject.has("samplingdivision")) {
                    addButton.setVisibility(View.GONE);
                    item_layout.setTag("抽样单位");

                    otherBlockSubitems.add(item_layout);//其他块条目view的添加到其他集合

                } else if (jsonObject.has("sampeddivision")) {
                    addButton.setVisibility(View.GONE);
                    item_layout.setTag("被抽样单位");

                    otherBlockSubitems.add(item_layout);//其他块条目view的添加到其他集合

                } else if (jsonObject.has("samplingcondition")) {
                    addButton.setVisibility(View.GONE);
                    item_layout.setTag("抽样情况");

                    otherBlockSubitems.add(item_layout);//其他块条目view的添加到其他集合

                } else if (jsonObject.has("samplingdivisionsign")) {
                    addButton.setVisibility(View.GONE);
                    item_layout.setTag("抽样单位签字");

                    otherBlockSubitems.add(item_layout);//其他块条目view的添加到其他集合

                } else if (jsonObject.has("sampleddivisionsign")) {
                    addButton.setVisibility(View.GONE);
                    item_layout.setTag("被抽样单位签字");

                    otherBlockSubitems.add(item_layout);//其他块条目view的添加到其他集合

                } else if (jsonObject.has("extra")) {
                    addButton.setVisibility(View.GONE);
                    item_layout.setTag("备注");

                    otherBlockSubitems.add(item_layout);//其他块条目view的添加到其他集合

                } else {
                    addButton.setVisibility(View.GONE);
                    item_layout.setTag("");

                    otherBlockSubitems.add(item_layout);//其他块条目view的添加到其他集合

                }

                //设置加号的监听 生成一个带减号的布局 并将Applicaition中的GPS切换到最新的布局
                addButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addOneSampling(canUserEdit);
                    }
                });

                LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1); //用于显示块条目内容的View
                for (int j = 0; j < item_value.length(); j++)    //循环取出第二层的值
                {
                    JSONObject itemObject = item_value.optJSONObject(j);    //第三层的对象
                    HashMap<String, Object> item = new HashMap<String, Object>();    //将取出的值全部放进hashmap里
                    System.out.println(jsonObject.names());        //打印出第二层所包含的
                    item.put(NAME, itemObject.optString(NAME));        //条目名称
                    item.put(TYPE, itemObject.optString(TYPE));        //条目类型
                    item.put(CONT, itemObject.opt(CONT));                //条目内容
                    item.put(CHAN, itemObject.optString(CHAN));        //条目内容是否可以改写
                    item.put(PRINT, itemObject.optString(PRINT));        //条目是否可以打印
                    item.put(SIZE, itemObject.optString(SIZE));        //条目的字体大小
                    item.put(NECESSARY, itemObject.optString(NECESSARY)); //条目必填项
                    System.out.println(item);
                    LinearLayout parentLayout = createWidget(item, j, canUserEdit);    //生成一个linearLayout（一个条目）条目View 包括两部分 名和内容View

                    module_value.addView(parentLayout);    //将条目添加至块条目视图

                }

                parentView.addView(item_layout);//将块条目添加至总布局
                parentView.setTag(1);//设置标志位指针，指向最后一个样品情况+1
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            L.d("解析json失败 MyLayout >line:347");
            Toast.makeText(activity.getApplicationContext(), "解析json失败", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * 在界面中添加一个样品情况
     *
     * @param canUserEdit
     * @return
     */
    public boolean addOneSampling(boolean canUserEdit) {
        //暂停GPS 隐藏暂停按钮
        gpsBtnTV.setVisibility(View.GONE);
        gpsIdTV.setTextColor(Color.argb(255, 0, 187, 212));

        //要添加View的号码 既第几个样品情况--样品情况n
        int numOfAddedView = (int)getSamplingModuleKeyTextView(samplingBlockSubitems.size()-1).getTag() + 1;

        //开始生成新的块条目VIEW

        if (jsonArray_Sampling == null) {
            return false;
        }

        JSONArray item_value = jsonArray_Sampling;

        //样品情况的块条目
        LinearLayout item_layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.item_layout, null); //第一层的布局View  大块条目 包括两部分 分别是名：基本信息、抽样单位等 和条目块VIew

        final TextView module_key = (TextView) ((LinearLayout) item_layout.getChildAt(0)).getChildAt(0); //用来显示块条目标题的TextView
        module_key.setText("样品"+numOfAddedView+"情况"); //设置块条目的标题
        module_key.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_THRIRD); //设置块条目标题的字体大小
        module_key.setTag(numOfAddedView);

        //获取减号
        final TextView reduceButton = (TextView) ((LinearLayout) item_layout.getChildAt(0)).getChildAt(1); //标题旁边的添加按钮
        reduceButton.setBackgroundResource(R.drawable.desentsize);
        reduceButton.setVisibility(View.VISIBLE);
        reduceButton.setTag(item_layout);//将当前的VIEW保存下来
        reduceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                parentView.removeView((LinearLayout) reduceButton.getTag());//移除当前的VIEW

                samplingBlockSubitems.remove((LinearLayout) reduceButton.getTag());//从样品情况块条目集合中移除

                parentView.setTag((int) parentView.getTag() - 1);           //标志位-1

                //设置GPS信息TextView的颜色为灰色，并激活（将其赋值给CollectionApplicaiton中的mLocaitonResult）
                gpsIdTV = getSamplingGpsIdTV(samplingBlockSubitems.size() - 1);
                if(gpsIdTV==null){
                    T.showShort(parentView.getContext(),"删除样品失败，获取不到之前控件！");
                    Log.e(MyLayout.class.getName(),"line:"+Util.getLineInfo());
                    return;
                }
                gpsIdTV.setTextColor(parentView.getContext().getResources().getColor(R.color.text_color_gray));
                ((CollectionApplication) parentView.getContext().getApplicationContext()).mLocationResult = gpsIdTV;

                //显示暂停按钮
                TextView gpsPauseButton=getSamplingGPSBtnTV(samplingBlockSubitems.size() - 1);
                if (gpsPauseButton==null){
                    T.showShort(parentView.getContext(),"删除样品失败，获取不到之前控件！");
                    Log.e(MyLayout.class.getName(),"line:"+Util.getLineInfo());
                    return;
                }
                gpsPauseButton.setVisibility(View.VISIBLE);

                //设置变量为最后一个块条目的样品名称 样品编号 抽样单编号
                ItemsET = getSamplingItemsET(samplingBlockSubitems.size() - 1);
                ItemsIDET = getSamplingItemsIDET(samplingBlockSubitems.size() - 1);
                numberIdTV = getSamplingNumberIdTV(samplingBlockSubitems.size() - 1);

                if (ItemsET == null ||ItemsIDET == null ||numberIdTV == null) {
                    T.showShort(parentView.getContext(),"删除样品失败，获取不到之前控件！");
                    Log.e(MyLayout.class.getName(),"line:"+Util.getLineInfo());
                    return;
                }

                if (activity instanceof GatherActivity) {
                    ((GatherActivity) activity).reLoadVariable();
                }

                //设置最后一个块条目的照相和录像为的添加按钮显示
                setAddPhotoVideoButtonVisible(samplingBlockSubitems.size() - 1);


                T.showShort(parentView.getContext(), "删除样品");
            }
        });

        //获取块条目的内容
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1); //用于显示块条目内容的View
        for (int j = 0; j < item_value.length(); j++)    //循环取出第二层的值
        {
            JSONObject itemObject = item_value.optJSONObject(j);    //第三层的对象
            HashMap<String, Object> item = new HashMap<String, Object>();        //将取出的值全部放进hashmap里
            item.put(NAME, itemObject.optString(NAME));             //条目名称
            item.put(TYPE, itemObject.optString(TYPE));             //条目类型
            item.put(CONT, itemObject.opt(CONT));                   //条目内容
            item.put(CHAN, itemObject.optString(CHAN));             //条目内容是否可以改写
            item.put(PRINT, itemObject.optString(PRINT));           //条目是否可以打印
            item.put(SIZE, itemObject.optString(SIZE));             //条目的字体大小
            item.put(NECESSARY, itemObject.optString(NECESSARY));   //条目必填项
            System.out.println(item);
            LinearLayout parentLayout = null;                       //生成一个linearLayout（一个条目）条目View
            // 包括两部分 名和内容View
            try {
                parentLayout = createWidget(item, j, canUserEdit);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            module_value.addView(parentLayout);                     //将条目添加至块条目视图

        }

        setAddPhotoVideoButtonGone(samplingBlockSubitems.size() - 1); //隐藏照片和录像的加号

        parentView.addView(item_layout, (Integer) parentView.getTag());//在标志位指向处加入带减号的View
        parentView.setTag((Integer) parentView.getTag() + 1);//标志位+1

        samplingBlockSubitems.add(item_layout);                       //将块条目view添加到样品情况块条目集合中

        T.showShort(parentView.getContext(), "添加样品" + numOfAddedView);

        return true;
    }

    /**
     * 保存下面七个条目的条目名，用于以后获取条目时的判断
     *
     * @param type
     * @param name
     */
    public void saveTypeString(String type, String name) {
        switch (type) {
            case TYPE_COLLDATE:
                TYPE_COLLDATE_STRING = name;
                break;
            case TYPE_GPS:
                TYPE_GPS_STRING = name;
                break;
            case TYPE_CAMERA:
                TYPE_CAMERA_STRING = name;
                break;
            case TYPE_VIDEO:
                TYPE_VIDEO_STRING = name;
                break;
            case TYPE_NUMBER:
                TYPE_NUMBER_STRING = name;
                break;
            case TYPE_ITEMS:
                TYPE_ITEMS_STRING = name;
                break;
            case TYPE_ITEM_ID:
                TYPE_ITEM_ID_STRING = name;
                break;
            default:
                break;
        }
    }

    /**
     * @param item 条目集合/条目父类视图的Tag
     * @param id   通过id来确认是哪个按钮触发的事件
     * @return
     * @throws JSONException
     */
    private LinearLayout createWidget(HashMap<String, Object> item, int id, boolean canUserEdit) throws JSONException {
        //要生成的条目父类视图linearLayout myWidget 构成是LinearLayout--->CheckBox\TextView
        //                                                       ---->TextView
        final LinearLayout myWidget = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.linearlayout_input, null);//具体条目的layout文件
        myWidget.setTag(item);    //设置好F的标签
        String type = (String) item.get(TYPE);    //取出条目类型

        if (type.equals(TYPE_TABLE)) { //一个抽样单只有一个表头
            LinearLayout LL_element = new LinearLayout(myWidget.getContext());
            TextView tvv = new TextView(myWidget.getContext());    //表头用TextView显示
            tvv.setText((String) item.get(NAME));
            tvv.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_TABLE);
            tvv.setTextColor(activity.getResources().getColor(R.color.text_color_gray));//设置表头的字体颜色
            LL_element.addView(tvv);
            myWidget.addView(LL_element);
        } else//创建是否必填 条目名称和是否打印的控件
        {
            LinearLayout LL_element = new LinearLayout(myWidget.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            LL_element.setGravity(Gravity.CENTER_VERTICAL);
            LL_element.setLayoutParams(params);

            TextView ifNecessary = new TextView(myWidget.getContext());    //是否是必填选项
            String nec = (String) item.get(NECESSARY);
            if (nec.equals("T"))
                nec = "*";
            else if (nec.equals("F"))
                nec = "";
            ifNecessary.setText(nec);    //取出条目名
            ifNecessary.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TITLE);
            ifNecessary.setTextColor(Color.RED);//设置条目名称的字体颜色
            LL_element.addView(ifNecessary);

            TextView tv = new TextView(myWidget.getContext());    //条目名称用TextView显示
            String name = (String) item.get(NAME);
            tv.setText(name + ": ");    //取出条目名
            saveTypeString(type, name + ": ");
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TITLE);
            tv.setTextColor(activity.getResources().getColor(R.color.text_color_gray));//设置条目名称的字体颜色
            LinearLayout.LayoutParams tv_param = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            tv_param.weight = 4.0f;
            tv.setLayoutParams(tv_param);
            tv.setGravity(Gravity.LEFT);
            LL_element.addView(tv);

            //签名、照相、录像不需要打印 其他都添加是否打印的选择框
            if (!type.equals(TYPE_SIGN) && !type.equals(TYPE_CAMERA) && !type.equals(TYPE_VIDEO)) {
                LinearLayout LL_checkbox = new LinearLayout(myWidget.getContext());
                final CheckBox ifPrint = new CheckBox(myWidget.getContext());
                ifPrint.setBackgroundResource(R.drawable.selector_checkbox_print);
                ifPrint.setHeight(ScreenUtils.dip2px(20, myWidget.getContext()));
                ifPrint.setWidth(ScreenUtils.dip2px(20, myWidget.getContext()));
                ifPrint.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
                if (((String) item.get(PRINT)).equals("T"))
                    ifPrint.setChecked(true);
                else
                    ifPrint.setChecked(false);

                LinearLayout.LayoutParams LL_ceckbox_params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                LL_ceckbox_params.weight = 0.1f;
                LL_checkbox.setLayoutParams(LL_ceckbox_params);
                LL_checkbox.addView(ifPrint);
                LL_checkbox.setGravity(Gravity.RIGHT);
                LL_element.addView(LL_checkbox);
            }

            myWidget.addView(LL_element);        //添加至F
        }

        //下面是条目中的不同控件

        if (type.equals(TYPE_GPS)) {        //类型为GPS时
            final TextView tv_gps = createTextView(myWidget.getContext(), item, type, canUserEdit);
            tv_gps.setTextColor(activity.getResources().getColor(R.color.text_color_gray));
            tv_gps.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            tv_gps.setMinHeight(activity.getResources().getDimensionPixelSize(R.dimen.gps_view_height));
            tv_gps.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TEXT);
            tv_gps.setPadding(Dp2Px(myWidget.getContext(), 25), 0, 0, 0);
            tv_gps.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // 得到剪贴板管理器
                    ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(tv_gps.getText().toString());
                    Toast.makeText(activity, "已复制文字到剪贴板", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            gpsIdTV = tv_gps;
            ((CollectionApplication) myWidget.getContext().getApplicationContext()).mLocationResult = gpsIdTV;
            myWidget.addView(tv_gps);

            final TextView bt_gps = new TextView(myWidget.getContext());
            bt_gps.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            bt_gps.setPadding(0, Dp2Px(myWidget.getContext(), 5), 0, Dp2Px(myWidget.getContext(), 5));
            bt_gps.setGravity(Gravity.CENTER);

            bt_gps.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            bt_gps.setBackgroundResource(R.drawable.background_date_choose);
            bt_gps.setTextColor(myWidget.getContext().getResources().getColor(R.color.text_color_gray));
            bt_gps.setTextSize(20);
            bt_gps.setText("暂停GPS");
            bt_gps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bt_gps.getText().toString().equals("暂停GPS")) {
                        ((CollectionApplication) myWidget.getContext().getApplicationContext()).gps_pause = true;
                        bt_gps.setText("启动GPS");
                        tv_gps.setTextColor(Color.argb(255, 0, 187, 212));
                        bt_gps.setTextColor(Color.argb(255, 0, 187, 212));
                    } else if (bt_gps.getText().toString().equals("启动GPS")) {
                        ((CollectionApplication) myWidget.getContext().getApplicationContext()).gps_pause = false;
                        bt_gps.setText("暂停GPS");
                        tv_gps.setTextColor(myWidget.getContext().getResources().getColor(R.color.text_color_gray));
                        bt_gps.setTextColor(myWidget.getContext().getResources().getColor(R.color.text_color_gray));
                    }

                }
            });
            if (canUserEdit)
                bt_gps.setVisibility(View.VISIBLE);
            else
                bt_gps.setVisibility(View.GONE);

            gpsBtnTV = bt_gps;
            myWidget.addView(bt_gps);

        } else {

            switch (doORwatch) {
                case SEE_FORM:
                    if (type.equals(TYPE_INPUT) || type.equals(TYPE_ADDR)
                            || type.equals(TYPE_COMPANY) || type.equals(TYPE_ITEMS) || type.equals(TYPE_ITEM_ID)) {//类型为输入框
                        EditText editText = createEditText(myWidget.getContext(), item, canUserEdit);
                        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TEXT);
                        myWidget.addView(editText);
                    } else if (type.equals(TYPE_NUMBER)
                            || type.equals(TYPE_COLLDATE) || type.equals(TYPE_AUTO)
                            || type.equals(TYPE_MY_COMPANY) || type.equals(TYPE_MY_COMPANY_ADDRESS)
                            || type.equals(TYPE_SAMPLING_CONTACT) || type.equals(TYPE_SAMPLING_PHONE)
                            ) {//textView
                        TextView textView = createTextView(myWidget.getContext(), item, type, canUserEdit);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TEXT);
                        myWidget.addView(textView);
                    } else if (type.equals(TYPE_DATE))    //类型为选择日期
                    {
                        Button dateChoose = createDateButton(myWidget.getContext(), item, id);
                        if (canUserEdit) {
                            myWidget.addView(dateChoose);
                        } else {
                            dateChoose.setClickable(false);
                            myWidget.addView(dateChoose);
                        }
//                    myWidget.addView(createDateButton(myWidget.getContext(), item, id));
                    } else if (type.equals(TYPE_SIGN))    //类型为签名
                    {
                        View sign = createImageView(myWidget.getContext(), item);
                        if (canUserEdit) {
                            myWidget.addView(sign);
                        } else {
                            sign.setClickable(false);
                            myWidget.addView(sign);
                        }
//                    myWidget.addView(createImageView(myWidget.getContext(), item));

                    } else if (type.equals(TYPE_SELECT))    //类型为选择时，全部当成多选来处理
                    {
                        createMultiCheckBox(myWidget, item, true, canUserEdit);
                    } else if (type.equals(TYPE_CAMERA))    //类型为拍照时
                    {
                        JSONArray camera_value = (JSONArray) item.get(CONT);
                        ImageView iv = createCameraImageView(myWidget.getContext(), id);
                        ImageView ivv = new ImageView(myWidget.getContext());
                        ivv.setId(GatherActivity.Companion.getREQUESTCODEFORPICTURE() + id);
                        if (camera_value.length() > 0) {
                            String ipath = root_path + File.separator + camera_value.getString(camera_value.length() - 1);
                            Bitmap bm = Util.getBitmap(ipath, 2);
                            ivv.setImageBitmap(bm);
                            iv.setVisibility(View.GONE);
                            final String picPath = root_path + File.separator + camera_value.getString(camera_value.length() - 1);
                            ivv.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(myWidget.getContext(), PictureViewActivity.class);
                                    intent.putExtra("picPath", picPath);
                                    myWidget.getContext().startActivity(intent);
                                }
                            });
                            iv.setVisibility(View.GONE);
                        } else {
                            iv.setVisibility(View.GONE);
                        }

                        myWidget.addView(iv);

                        myWidget.addView(createImageGridView(myWidget.getContext()));

                    } else if (type.equals(TYPE_VIDEO))    //类型为录像时
                    {
                        JSONArray video_value = (JSONArray) item.get(CONT);
                        ImageView iv = createVideoImageView(myWidget.getContext(), id);
                        TextView tvv = new TextView(myWidget.getContext());
                        tvv.setId(GatherActivity.Companion.getREQUESTCODEFORVIDEO() + 1);
                        tvv.setTextColor(activity.getResources().getColor(R.color.text_color_gray));
                        if (video_value.length() > 0) {
                            tvv.setText(video_value.getString(video_value.length() - 1));
                            iv.setVisibility(View.GONE);
                        } else {
                            iv.setVisibility(View.GONE);
                        }

                        myWidget.addView(iv);

                        myWidget.addView(createVideoGridView(myWidget.getContext()));

                    }
                    break;
                case DO_FORM:
                    if (type.equals(TYPE_INPUT) || type.equals(TYPE_ADDR)
                            || type.equals(TYPE_COMPANY) || type.equals(TYPE_ITEMS) || type.equals(TYPE_ITEM_ID))    //类型为直接输入
                    {
                        EditText editText = createEditText(myWidget.getContext(), item, canUserEdit);
                        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TEXT);
                        myWidget.addView(editText);
                    } else if (type.equals(TYPE_AUTO) || type.equals(TYPE_MY_COMPANY) || type.equals(TYPE_MY_COMPANY_ADDRESS)
                            || type.equals(TYPE_SAMPLING_CONTACT) || type.equals(TYPE_SAMPLING_PHONE) || type.equals(TYPE_NUMBER)
                            || type.equals(TYPE_COLLDATE)) {//textview
                        TextView textView = createTextView(myWidget.getContext(), item, type, canUserEdit);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TEXT);
                        myWidget.addView(textView);
                    } else if (type.equals(TYPE_DATE))    //类型为选择日期
                    {
                        myWidget.addView(createDateButton(myWidget.getContext(), item, id));
                    } else if (type.equals(TYPE_SIGN))    //类型为签名
                    {
                        myWidget.addView(createSignImage(myWidget.getContext()));
                    } else if (type.equals(TYPE_SELECT))    //类型为选择时，全部当成多选来处理
                    {
                        createMultiCheckBox(myWidget, item, false, canUserEdit);
                    } else if (type.equals(TYPE_CAMERA))    //类型为拍照时
                    {
                        JSONArray camera_value = new JSONArray();
                        item.put(CONT, camera_value);    //初始为空
                        myWidget.setTag(item);    //重新设置标签
                        ImageView iv = createCameraImageView(myWidget.getContext(), id);
                        ImageView ivv = new ImageView(myWidget.getContext());
                        ivv.setId(GatherActivity.Companion.getREQUESTCODEFORPICTURE() + id);
                        myWidget.addView(iv);
                        myWidget.addView(createImageGridView(myWidget.getContext()));

                    } else if (type.equals(TYPE_VIDEO))    //类型为录像时
                    {
                        JSONArray video_value = new JSONArray();
                        item.put(CONT, video_value);    //初始为空
                        myWidget.setTag(item);    //重新设置标签
                        ImageView iv = createVideoImageView(myWidget.getContext(), id);
                        TextView tvv = new TextView(myWidget.getContext());
                        tvv.setTextColor(activity.getResources().getColor(R.color.text_color_gray));
                        tvv.setId(GatherActivity.Companion.getREQUESTCODEFORVIDEO() + 1);
//					TextView tvvv = new TextView(myWidget.getContext());
//					tvvv.setId(GatherActivity.REQUESTCODEFORVIDEO+2);
                        myWidget.addView(iv);
                        myWidget.addView(createVideoGridView(myWidget.getContext()));
//					myWidget.addView(tvvv);
                    }
                    break;

                default:
                    break;
            }

        }

        return myWidget;
    }


    /**
     * 获取指定index处的块条目名称TextView
     * @param index
     * @return
     */
    public TextView getSamplingModuleKeyTextView(int index){
        LinearLayout item_layout = samplingBlockSubitems.get(index);
        LinearLayout module_key = (LinearLayout) item_layout.getChildAt(0);

        return (TextView) module_key.getChildAt(0);
    }

    /**
     * 获取index处的GPS信息的的TextView控件
     *
     * @param index
     * @return
     */
    public TextView getSamplingGpsIdTV(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                continue;

            if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_GPS_STRING)) {//判断这个是GPS控件
                return ((TextView) myWidget.getChildAt(1));
            }
        }

        return null;
    }

    /**
     * 获取index处的GPS信息按钮的的TextView控件
     *
     * @param index
     * @return
     */
    public TextView getSamplingGPSBtnTV(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                continue;

            if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_GPS_STRING)) {//判断这个是GPS控件
                return ((TextView) myWidget.getChildAt(2));
            }
        }

        return null;
    }

    /**
     * 获取index处的照相的的GridView控件
     *
     * @param index
     */
    public GridView getSamplingCameraGridView(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);//指定的块条目
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                continue;

            if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_CAMERA_STRING)) {//判断这个是照相控件
                return ((GridViewEx) myWidget.getChildAt(2));
            }

        }

        return null;
    }

    /**
     * 获取index处的录像的的GridView控件
     *
     * @param index
     */
    public GridView getSamplingVedioGridView(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);//指定的块条目
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                continue;

            if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_VIDEO_STRING)) {//判断这个是录相控件
                return ((GridViewEx) myWidget.getChildAt(2));
            }

        }

        return null;
    }

    /**
     * 获取index处的抽样单编号的TextView控件
     *
     * @param index
     * @return
     */
    public TextView getSamplingNumberIdTV(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                continue;

            if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_NUMBER_STRING)) {//判断这个是抽样单编号控件
                return ((TextView) myWidget.getChildAt(1));
            }
        }

        return null;
    }

    /**
     * 获取index处的样品名称EditText控件
     *
     * @param index
     * @return
     */
    public EditText getSamplingItemsET(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                continue;

            if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_ITEMS_STRING)) {//判断这个是样品名称控件
                return ((EditText) myWidget.getChildAt(1));
            }
        }

        return null;
    }

    /**
     * 获取index处的样品编号的EditText控件
     *
     * @param index
     * @return
     */
    public EditText getSamplingItemsIDET(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                continue;

            if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_ITEM_ID_STRING)) {//判断这个是样品编号控件
                return ((EditText) myWidget.getChildAt(1));
            }
        }

        return null;
    }

    /**
     * 获取抽样单的唯一抽样单位签字的ImageView控件
     *
     * @return
     */
    public ImageView getSamplingOurSignImaView() {
        for (int j = 0; j < otherBlockSubitems.size(); j++) {
            LinearLayout item_layout = otherBlockSubitems.get(j);
            LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

            for (int i = 0; i < module_value.getChildCount(); i++) {
                LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

                if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                    continue;

                if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_OUR_SING_STRING)) {//判断这个是样品编号控件
                    return ((ImageView) myWidget.getChildAt(1));
                }
            }
        }

        return null;
    }

    /**
     * 获取抽样单的唯一被抽样单位签字的ImageView控件
     *
     * @return
     */
    public ImageView getSamplingOtherSignImaView() {
        for (int j = 0; j < otherBlockSubitems.size(); j++) {
            LinearLayout item_layout = otherBlockSubitems.get(j);
            LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

            for (int i = 0; i < module_value.getChildCount(); i++) {
                LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

                if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() < 2)
                    continue;

                if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_OTHER_SING_STRING)) {//判断这个是样品编号控件
                    return ((ImageView) myWidget.getChildAt(1));
                }
            }
        }

        return null;
    }

    /**
     * 设置指定index的照相和录像控件的添加按钮为隐藏
     *
     * @param index
     */
    public void setAddPhotoVideoButtonGone(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);//指定的块条目
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() == 2) {//选出照相和录像控件
                if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_CAMERA_STRING)) {//判断这个是照相控件
                    final GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));
                    mGridView.getChildAt(0).setVisibility(View.GONE);

                } else if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_VIDEO_STRING)) {//判断这个是录像控件
                    final GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));
                    mGridView.getChildAt(0).setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 设置指定index的照相和录像控件的添加按钮为显示
     *
     * @param index
     */
    public void setAddPhotoVideoButtonVisible(int index) {
        LinearLayout item_layout = samplingBlockSubitems.get(index);//指定的块条目
        LinearLayout module_value = (LinearLayout) item_layout.getChildAt(1);

        for (int i = 0; i < module_value.getChildCount(); i++) {
            LinearLayout myWidget = (LinearLayout) module_value.getChildAt(i);

            if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() == 2) {//选出照相和录像控件
                if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_CAMERA_STRING)) {//判断这个是照相控件
                    final GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));
                    mGridView.getChildAt(0).setVisibility(View.VISIBLE);
                } else if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals(TYPE_VIDEO_STRING)) {//判断这个是录像控件
                    final GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));
                    mGridView.getChildAt(0).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * 创建用于显示图片的控件
     */
    private void createBrowserPic(LinearLayout myWidget,
                                  HashMap<String, Object> item) throws JSONException {
        // TODO Auto-generated method stub
        JSONArray ja = (JSONArray) item.get(CONT);
        int len = ja.length();
        for (int i = 0; i < 3 && i < len; i++) {
            ImageView iv = new ImageView(myWidget.getContext());
            String ipath = root_path + File.separator + ja.getString(i);
            Bitmap bm = Util.getBitmap(ipath, 2);
            iv.setImageBitmap(bm);
            myWidget.addView(iv);
        }
    }

    /**
     * 创建拍照控件/用ImageView
     *
     * @param context
     * @param id
     * @return
     */
    private ImageView createCameraImageView(final Context context, final int id) {
        final ImageView imageView = new ImageView(context);
        imageView.setTag("none");    //设置控件tag为none
//		imageView.setId(FillForm.REQUESTCODEFORPICTURE+id);		//设置触发控件ID
//		imageView.setLayoutParams(new LayoutParams(60,60));	//设置控件大小
        imageView.setImageResource(R.drawable.ic_menu_camera);    //设置控件默认图片
        imageView.setVisibility(View.GONE);
        picture = imageView;
        imageView.setTag(root_path);
        imageView.setOnClickListener(new View.OnClickListener()    //监听控件
        {
            @Override
            public void onClick(View v) {
                String root_path = (String) imageView.getTag();

                TextView tv_gps = gpsIdTV;
                TextView tv_num = numberIdTV;

                tv_gps.setTextColor(activity.getResources().getColor(R.color.text_color_gray));
                tv_num.setTextColor(activity.getResources().getColor(R.color.text_color_gray));

//				if(tv_gps.getText().equals(""))
//				{
//					Toast.makeText(context, "定位中，稍后拍照。。。", Toast.LENGTH_SHORT).show();
//					return;
//				}
                Intent intent = new Intent(activity, CameraView.class);
                intent.putExtra("root_path", root_path);    //将存储图片根目录传递过去
                intent.putExtra("view_id", GatherActivity.Companion.getREQUESTCODEFORPICTURE() + id);        //控件ID
                intent.putExtra("location", tv_gps.getText().toString());
                intent.putExtra("number", tv_num.getText().toString());
                activity.startActivityForResult(intent, GatherActivity.Companion.getREQUESTCODEFORPICTURE());
                //imageView.setVisibility(View.GONE);
            }
        });
        return imageView;
    }


    /**
     * 创建用于展示照片的gridview
     *
     * @param context
     * @return
     */
    private GridViewEx createImageGridView(final Context context) {
        final GridViewEx imageView = new GridViewEx(context);
        imageView.setTag("none");    //设置控件tag为none
        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        imageView.setGravity(Gravity.CENTER);
        imageView.setHorizontalSpacing(ScreenUtils.dip2px(4, context));
        imageView.setVerticalSpacing(ScreenUtils.dip2px(4, context));
        imageView.setNumColumns(3);
//        imageView.setColumnWidth(ScreenUtils.dip2px(100, context));

        imageView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

        SampleExelGridAdapter imgAdapter = new SampleExelGridAdapter(activity.getApplicationContext(),
                new ArrayList<ImageInfo>());

        imageView.setAdapter(imgAdapter);
        return imageView;

    }


    /**
     * 创建一个摄像控件
     *
     * @param context
     * @param id
     * @return
     */
    private ImageView createVideoImageView(final Context context, final int id) {
        final ImageView imageView = new ImageView(context);
        imageView.setTag("none");    //设置控件tag为none
        imageView.setImageResource(R.drawable.ic_menu_vedio);    //设置控件默认图片
        imageView.setVisibility(View.GONE);
        video = imageView;
        imageView.setTag(root_path);
        imageView.setOnClickListener(new View.OnClickListener()    //监听控件
        {

            @Override
            public void onClick(View v) {
                String root_path = (String) imageView.getTag();
                File file = new File(root_path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                File video_name = new File(root_path + File.separator + "VIDEO_" + ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() + Util.getCurrentTime("yyMMddHHmmss") + ".mp4");
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                intent.putExtra("view_id", v.getId());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(video_name));
                activity.startActivityForResult(intent, GatherActivity.Companion.getREQUESTCODEFORVIDEO());
                //imageView.setVisibility(View.GONE);
            }
        });
        imageViewP = imageView;
        return imageView;
    }

    /**
     * 创建用于展示录像的gridview
     *
     * @param context
     * @return
     */
    private GridViewEx createVideoGridView(final Context context) {
        final GridViewEx imageView = new GridViewEx(context);
        imageView.setTag("none");    //设置控件tag为none
        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        imageView.setGravity(Gravity.CENTER);
        imageView.setHorizontalSpacing(ScreenUtils.dip2px(4, context));
        imageView.setVerticalSpacing(ScreenUtils.dip2px(4, context));
        imageView.setNumColumns(3);
//        imageView.setColumnWidth(ScreenUtils.dip2px(100, context));

        imageView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

        SampleExelGridAdapter imgAdapter = new SampleExelGridAdapter(activity.getApplicationContext(),
                new ArrayList<ImageInfo>());

        imageView.setAdapter(imgAdapter);
        return imageView;

    }


    /**
     * 创建输入控件
     *
     * @param context
     * @param item
     * @return
     */
    private EditText createEditText(Context context, HashMap<String, Object> item, boolean canUserEdit) {
        String chan = (String) item.get(CHAN);
        String cont = (String) item.get(CONT);

        final ArrayList<String> values = new ArrayList<String>();//用于保存edittext历史记录

        final AutoCompleteEditText et = new AutoCompleteEditText(context);
        et.setTextColor(activity.getResources().getColor(R.color.theme_primary_dark2));
        et.setHintTextColor(activity.getResources().getColor(R.color.hint_color_text));
        et.setHint("请输入信息");
        et.setBackgroundResource(android.support.design.R.drawable.abc_textfield_search_activated_mtrl_alpha);

        //et.setPadding(80, 0, 0, 0);
        if (!cont.equals("")) {
            et.setText(cont);
        }
        if (canUserEdit) {
            if (chan.equals("F")) {
                et.setClickable(false);
                et.setFocusableInTouchMode(false);
                et.setFocusable(false);
            }
        } else {
            et.setClickable(false);
            et.setFocusableInTouchMode(false);
            et.setFocusable(false);
            et.setEnabled(false);
        }

//        values.add("sumile.cn");
//        values.add("sunile.cn");
//        values.add("sunule.cn");
//        values.add("samile.cn");
//        values.add("sakile.cn");
        et.setResultsValues(values);

//        et.requestFocus();
//        et.requestFocusFromTouch();
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);

        String type = (String) item.get(TYPE);
//        if (type.equals(TYPE_COMPANY)) companyET = et;
//        if (type.equals(TYPE_ADDR)) companyNameET=et;
        if (type.equals(TYPE_ADDR)) companyNameET = et;
        if (type.equals(TYPE_ITEMS)) ItemsET = et;
        if (type.equals(TYPE_ITEM_ID)) {
            ItemsIDET = et;
            //TODO if need limit input type ,modify here
        }
        return et;
    }


    /**
     * 创建显示日期的控件
     *
     * @param context
     * @param item
     * @param id      触发的按钮
     * @return
     */
    private Button createDateButton(Context context, HashMap<String, Object> item, int id) {
        final Button btn = new Button(context);
        btn.setBackgroundResource(R.drawable.background_date_choose);
        btn.setTextColor(Color.argb(255, 0, 187, 212));
        btn.setTextSize(20);
        btn.setId(GatherActivity.Companion.getREQUESTCODEFORDATE() + id);    //设置触发按钮ID
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, Calendar_View.class);
                i.putExtra("btn_id", btn.getId());    //将触发按钮ID传入下个activity
                activity.startActivityForResult(i, GatherActivity.Companion.getREQUESTCODEFORDATE());
            }
        });
        String cont = (String) item.get(CONT);
        if (!cont.equals("")) {
            btn.setText(cont);    //设置按钮内容
        } else {
            btn.setText(CHOOSE_DATE);
        }
        return btn;
    }

    /**
     * 创建显示签名的控件
     *
     * @param context
     * @return
     */
    private ImageView createSignImage(final Context context) {
//		Button btn = new Button(context);
//		btn.setText("签名");
        ImageView imageView = new ImageView(context);
        imageView.setTag("none");    //设置控件标签为none
//		imageView.setLayoutParams(new LayoutParams(60,40));	//设置控件的长和宽

        imageView.setImageResource(R.drawable.edit_query);        //设置控件的背景图片
        imageView.setOnClickListener(new View.OnClickListener()    //监听控件
        {
            @Override
            public void onClick(View v) {
                //初始化签字对话框
                WritePadDialog writePadDialog = new WritePadDialog((ImageView) v, activity, new DialogListener() {
                    @Override
                    public void refreshActivity(ImageView imageView, Object object) {
                        Bitmap signBitmap = (Bitmap) object;        //获得签名的图片
                        String sing_name = "SIGN" + "_" + ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() + Util.getCurrentTime("yyMMddHHmmss") + ".jpg";    //图片名称
                        imageView.setTag(sing_name);        //签名生成的图片名称保存在控件的tag标签里
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        signBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里100的话表示不压缩质量
                        Bitmap bm = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size(), options);
                        imageView.setImageBitmap(bm);    //显示签名图片
//						imageView.setClickable(false);	//只签一次

                        //保存图片到当前所有样品的文件夹下
                        for (int i = 0; i < samplingBlockSubitems.size(); i++) {
                            fs.createImageFile(signBitmap, root_path + File.separator + getSamplingNumberIdTV(i).getText(), sing_name);    //创建图片文件
                        }
                    }
                });
                writePadDialog.show();
            }
        });
        return imageView;
    }

    /**
     * 创建多选的控件	/	单选也应用此控件
     *
     * @param myWidget
     * @param item
     * @throws JSONException
     */
    private void createMultiCheckBox(final LinearLayout myWidget, HashMap<String, Object> item, boolean fromSeeFragment, boolean canUserEdit) throws JSONException {
        myWidget.setOrientation(LinearLayout.VERTICAL);
        LinearLayout checkboxs = new LinearLayout(myWidget.getContext());
        LinearLayout checkboxsFlag = new LinearLayout(myWidget.getContext());
        checkboxs.setOrientation(LinearLayout.VERTICAL);
        checkboxs.setPadding(Dp2Px(myWidget.getContext(), 25), 0, 0, 0);
        Object cont = item.get(CONT);    //取得内容
        JSONArray chiose = (JSONArray) cont;
        String chan = (String) item.get(CHAN);
        int size = chiose.length();    //获得内容长度
        for (int i = 0; i < size; i++) {
            final CheckBox checkBox = new CheckBox(myWidget.getContext());

//            checkBox.setText(((JSONObject)((JSONArray) cont).get(i)).getString("value"));    //获取具体内容

            checkBox.setText(chiose.getJSONObject(i).getString("value"));


            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TEXT); //设置内容字体大小
            checkBox.setTextColor(activity.getResources().getColor(R.color.text_color_gray));
            Boolean isChecked = chiose.getJSONObject(i).getBoolean("isChecked");
            if (isChecked != null) {
                checkBox.setChecked(isChecked);
            } else {
                checkBox.setChecked(false);
            }
            checkBox.setButtonDrawable(activity.getResources().getDrawable(R.drawable.selector_checkbox));
            //checkBox.set(80,0,0,0);
            if (canUserEdit) {
                if (chan.endsWith("F")) {
                    checkBox.setClickable(false);    //不可更改
                }
            } else {
                checkBox.setClickable(false);    //不可更改
            }

            checkboxs.addView(checkBox);
        }

        myWidget.addView(checkboxs);
        myWidget.addView(checkboxsFlag);
    }

    /**
     * 创建用于显示图片的控件
     *
     * @param context
     * @param item
     * @return
     */
    private View createImageView(final Context context, HashMap<String, Object> item) {
        // TODO Auto-generated method stub
        ImageView iv = new ImageView(context);
        iv.setTag(item.get(CONT));
        String imagePath = root_path + File.separator + numberIdTV.getText() + File.separator + (String) item.get(CONT);
        System.out.println("imagePath -->" + imagePath + "\nroot_path" + root_path);
        Bitmap bm = Util.getBitmap(imagePath, 2);
        if (bm == null) {
            iv.setImageResource(R.drawable.edit_query);
            iv.setOnClickListener(new View.OnClickListener()    //监听控件
            {
                @Override
                public void onClick(View v) {
                    //初始化签字对话框
                    WritePadDialog writePadDialog = new WritePadDialog((ImageView) v, activity, new DialogListener() {
                        @Override
                        public void refreshActivity(ImageView imageView, Object object) {
                            // TODO Auto-generated method stub
                            Bitmap signBitmap = (Bitmap) object;        //获得签名的图片
                            String sing_name = "SIGN" + "_" + ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() + Util.getCurrentTime("yyMMddHHmmss") + ".jpg";    //图片名称
                            imageView.setTag(sing_name);        //签名生成的图片名称保存在控件的tag标签里
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            signBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里100的话表示不压缩质量
                            Bitmap bm = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size(), options);
                            imageView.setImageBitmap(bm);    //显示签名图片
//							imageView.setClickable(false);	//只签一次
                            fs.createImageFile(signBitmap, root_path + File.separator + numberIdTV.getText(), sing_name);    //创建图片文件
                        }
                    });
                    writePadDialog.show();
                }
            });
        } else {
            iv.setImageBitmap(bm);
//			iv.setClickable(false);	//只签一次
        }

        return iv;
    }

    /**
     * 创建用于显示的textview
     *
     * @param context
     * @param item    传的是item
     * @param type    INPUT/SELECT/DATE/GPS
     * @return
     * @throws JSONException
     */
    private TextView createTextView(Context context, HashMap<String, Object> item, String type, boolean canUserEdit) throws JSONException {
        TextView tv = new TextView(context);
        tv.setPadding(Dp2Px(context, 25), 0, 0, 0);
        tv.setTextColor(activity.getResources().getColor(R.color.text_color_gray));
        System.out.println(type);
        if (type.equals(TYPE_SELECT) /*|| type.equals(TYPE_GPS) */ || type.equals(TYPE_VIDEO)) {
            JSONArray cont = (JSONArray) item.get(CONT);
            StringBuffer str = new StringBuffer();
            int len = cont.length();
            if (type.equals(TYPE_VIDEO)) {
                len = Math.min(len, 2);
            }
            for (int k = 0; k < len; k++) {
                str.append(cont.get(k));
                str.append(";");
            }
            tv.setText(str.toString());
        } else if (type.equals(TYPE_AUTO)) {
            switch (doORwatch) {
                case DO_FORM:
                    String name = (String) item.get(NAME);
                    tv.setText(getAutoName(context, name));
                    break;
                case SEE_FORM:
                    tv.setText((String) item.get(CONT));
                    break;
                default:
                    break;
            }
        } else if (type.equals(TYPE_MY_COMPANY)) {// is my company name
            String cont = (String) item.get(CONT);
            if (cont.equals("")) {  //if contains nothing ,show preference
                tv.setText((String) SPUtils.get(context, SPUtils.SAMPLING_COMPANY, "没有填写", SPUtils.USER_INFO));
            } else {    //if contains content ,show it
                tv.setText(cont);
            }
        } else if (type.equals(TYPE_MY_COMPANY_ADDRESS)) {// is my company address
            String cont = (String) item.get(CONT);
            if (cont.equals("")) {  //if contains nothing ,show preference
                tv.setText((String) SPUtils.get(context, SPUtils.SAMPLING_ADDR, "没有填写", SPUtils.USER_INFO));
            } else {    //if contains content ,show it
                tv.setText(cont);
            }
        } else if (type.equals(TYPE_SAMPLING_CONTACT)) {// is user name
            String cont = (String) item.get(CONT);
            if (cont.equals("")) {  //if contains nothing ,show preference
                tv.setText((String) SPUtils.get(context, SPUtils.SAMPLING_CONTACT, "没有填写", SPUtils.USER_INFO));
            } else {    //if contains content ,show it
                tv.setText(cont);
            }
        } else if (type.equals(TYPE_SAMPLING_PHONE)) {// is user phone
            String cont = (String) item.get(CONT);
            if (cont.equals("")) {  //if contains nothing ,show preference
                tv.setText((String) SPUtils.get(context, SPUtils.SAMPLING_PHONE, "没有填写", SPUtils.USER_INFO));
            } else {    //if contains content ,show it
                tv.setText(cont);
            }
        } else if (type.equals(TYPE_GPS)) {
            tv.setText((String) item.get(CONT));
            gpsIdTV = tv;
        } else if (type.equals(TYPE_NUMBER)) {
            String cont = (String) item.get(CONT);
            if (cont.equals("") && samplingtable == null) {//抽样单模板
                List<TASKINFO> taskinfo = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list();
                if (taskinfo.size() != 1) {
                    Log.e("XXXXXXXXXXXX", "MyLayout.createTextView 1034 line 根据任务iD查询的任务数不等于1 ");
                }
                tv.setText(Util.getSamplingNum(context, taskinfo.get(0)));//taskname+imei+date+liushui
                isFixPoint = false;
            } else if (cont.equals("") && samplingtable != null) {//抽样单
                if (samplingtable.getMedia_folder().isEmpty()) {//无NUM内容的抽样单和无mediafolder
                    List<TASKINFO> taskinfo = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list();
                    if (taskinfo.size() != 1) {
                        Log.e("XXXXXXXXXXXX", "MyLayout.createTextView 1034 line 根据任务iD查询的任务数不等于1 ");
                    }
                    tv.setText(Util.getSamplingNum(context, taskinfo.get(0)));//taskname+imei+date+liushui
                    isFixPoint = false;
                } else { //定点采样
                    List<TASKINFO> taskinfo = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list();
                    if (taskinfo.size() != 1) {
                        Log.e("XXXXXXXXXXXX", "MyLayout.createTextView 1034 line 根据任务iD查询的任务数不等于1 ");
                    }
                    tv.setText(samplingtable.getMedia_folder());//taskname+imei+date+liushui
                    isFixPoint = true;//是定点采样，既服务器发来的抽样单
                }
            } else { //有内容的抽样单
                tv.setText(cont);
                isFixPoint = false;
            }
            numberIdTV = tv;

        } else if (type.equals(TYPE_COLLDATE)) {
            tv.setText(Util.getCurrentTime("yyyy-MM-dd"));
        } else {
            tv.setText((String) item.get(CONT));
        }
//		if(type.equals(TYPE_GPS))
//		{
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE_CONTENT_TEXT);
//		}
        return tv;
    }

    private String getAutoName(Context context, String name) {
        String dv = "";
        String login_user = (String) SPUtils.get(context, SPUtils.LOGIN_USER, dv, SPUtils.USER_DATA);
        // TODO Auto-generated method stub
        if (name.equals("名称")) {
            return (String) SPUtils.get(context, SPUtils.UNIT_NAME, dv, login_user);
        } else if (name.equals("地址")) {
            return (String) SPUtils.get(context, SPUtils.UNIT_ADDR, dv, login_user);
        } else if (name.equals("邮编")) {
            return (String) SPUtils.get(context, SPUtils.UNIT_POST, dv, login_user);
        } else if (name.equals("采样人")) {
            return login_user;
        } else if (name.equals("联系电话")) {
            return (String) SPUtils.get(context, SPUtils.UNIT_PHONE, dv, login_user);
        } else if (name.equals("传真")) {
            return (String) SPUtils.get(context, SPUtils.UNIT_CZ, dv, login_user);
        }
        return name;
    }

    /**
     * 将hashmap转换成jsonObject
     *
     * @param item 条目集合
     * @param cont 条目内容
     * @return
     * @throws JSONException
     */
    public JSONObject mapToJson(HashMap<String, Object> item, Object cont) throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put(NAME, (String) item.get(NAME));
        itemObject.put(TYPE, (String) item.get(TYPE));
        itemObject.put(CHAN, (String) item.get(CHAN));
        itemObject.put(PRINT, (String) item.get(PRINT));
        itemObject.put(SIZE, (String) item.get(SIZE));
        itemObject.put(NECESSARY, (String) item.get(NECESSARY));
        String type = (String) item.get(TYPE);
        if (type.equals(TYPE_TABLE)) {
            if (cont == null) {
                itemObject.put(CONT, (String) item.get(CONT));
            } else {
                itemObject.put(CONT, (String) cont);
            }

        }
        // input/date/number/sign 内容皆为字符串
        else if (type.equals(TYPE_INPUT) || type.equals(TYPE_DATE) || type.equals(TYPE_NUMBER)
                || type.equals(TYPE_SIGN) || type.equals(TYPE_COLLDATE) || type.equals(TYPE_COMPANY)
                || type.equals(TYPE_ITEMS) || type.equals(TYPE_AUTO) || type.equals(TYPE_MY_COMPANY)
                || type.equals(TYPE_MY_COMPANY_ADDRESS) || type.equals(TYPE_SAMPLING_CONTACT)
                || type.equals(TYPE_SAMPLING_PHONE) || type.equals(TYPE_ADDR) || type.equals(TYPE_ITEM_ID)) {
            itemObject.put(CONT, (String) cont);
        }
        // select/gps 为JSONArray数组
        else if (type.equals(TYPE_GPS)) {
            itemObject.put(CONT, (String) cont);
//            switch (doORwatch) {
//                case DO_FORM:
//                    itemObject.put(CONT, (String) cont);
//                    break;
//                case SEE_FORM:
//                    itemObject.put(CONT, (String) item.get(CONT));
//                    break;
//
//                default:
//                    break;
//            }
        } else if (type.equals(TYPE_SELECT)) {
            itemObject.put(CONT, (JSONArray) cont);
        }
        // camera直接将图片名称放入ArrayList里面了
        else if (type.equals(TYPE_CAMERA) || type.equals(TYPE_VIDEO)) {
            ArrayList<String> savedName = (ArrayList<String>) cont;
            JSONArray jsonArray = new JSONArray();
            if (savedName != null) {
                for (int i = 0; i < savedName.size(); i++) {
                    jsonArray.put(i, savedName.get(i));
                }
            }
            itemObject.put(CONT, jsonArray);
        }

        return itemObject;
    }

    /**
     * Get key from json.
     *
     * @param id
     * @return
     */
    public static String getName(int id) {
        switch (id) {
            case 0:
                return "specimencondition";//样品信息
            case 1:
                return "samplingdivision";//抽样单位
            case 2:
                return "sampeddivision";    //被抽样单位
            case 3:
                return "samplingcondition";//抽样情况
            case 4:
                return "samplingdivisionsign";//抽样单位签字
            case 5:
                return "sampleddivisionsign";//被抽样单位签字
            case 6:
                return "extra";//备注
            default:
                break;
        }
        return null;
    }

    private HashMap<String, String> getModuleKeyMap() {
        HashMap<String, String> moduleKeyMap = new HashMap<String, String>();
//        moduleKeyMap.put("autogeneration", "基本信息");
        moduleKeyMap.put("samplingdivision", "抽样单位");
        moduleKeyMap.put("sampeddivision", "被抽样单位");
        moduleKeyMap.put("specimencondition", "样品1情况");
        moduleKeyMap.put("samplingcondition", "抽样情况");
        moduleKeyMap.put("samplingdivisionsign", "抽样单位签字");
        moduleKeyMap.put("sampleddivisionsign", "被抽样单位签字");
        moduleKeyMap.put("extra", "备注");
        return moduleKeyMap;
    }

    private String getNameInfo(String key) {
        HashMap<String, String> map = getModuleKeyMap();
        if (!map.containsKey(key)) {
            return key;
        }
        return map.get(key);
    }

    /**
     * Get key from value.
     *
     * @param value
     * @return
     */
    public String getNameKey(String value) {
        HashMap<String, String> map = getModuleKeyMap();
        if (!map.containsValue(value)) {
            return "specimencondition";
        }
        for (String key : map.keySet()) {
            if (map.get(key).equals(value)) {
                return key;
            }
        }
        return value;
    }


    /**
     * 获取照相图标的ImageView
     */
    public ImageView getPicIMGObjet() {
        return picture;
    }

    /**
     * 获取摄影图标的ImageView
     */
    public ImageView getVidIMGObject() {
        return video;
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
}
