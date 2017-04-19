package com.aj.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.aj.tools.FileStream;
import com.aj.tools.T;
import com.aj.tools.Util;
import com.aj.ui.viewimage.PictureViewActivity;
import com.aj.ui.GridViewEx;
import com.aj.ui.HeadControlPanel;
import com.aj.ui.MyLayout;
import com.aj.ui.HeadControlPanel.LeftImageOnClick;
import com.aj.ui.HeadControlPanel.RightSecondOnClick;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
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

public class WatchActivity extends Activity {

    private long sampleID;
    private long taskID;    //被点击的任务

    private String jsonStr;            //目标字符串
    private String sampleName;            //文件夹名称
    private String root_path;        //跟目录
    private String child_path;         //储存照片录像等信息的子目录完整路径
    private String num;        //编号
    private String itemName, itemID, companyAddr;

    private boolean canUserEdit = true;//是否可以修改这个抽样单
    private boolean neccessaryIsEdit = true; //isSaved
    private boolean isHavePicture = false, isHaveVideo = false;

    private ArrayList<String> savedPictureNames = new ArrayList<>();//储存保存过的图片名字 在点击保存时存入到json中
    private ArrayList<String> savedVedioNames = new ArrayList<>();//储存保存过的视频名字 在点击保存时存入到json中

    private Context mContext = this;

    private TextView tv_num;  //自动编号
    private TextView tv_gps;        //gps显示控件
    private TextView bt_gps;        //gps暂停按钮

    private EditText etCompany;
    private EditText etCompanyAddress;
    private EditText etItem, etItemID;

    private LinearLayout parentView;

    private Dialog dialog;

    private MyLayout lu;        //布局生成类

    private HeadControlPanel headPanel = null;

    private FileStream fs;            //文件流

    //database part
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;
    private SAMPLINGTABLE samplingtable;
    private TASKINFO taskinfo;

    private Intent intent;            //传递时间的意图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        dialog = new Dialog(WatchActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.form_layout);

        //database init
        daoSession = ((CollectionApplication) ((Activity) mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();

        parentView = (LinearLayout) findViewById(R.id.form_parent);

        //沉浸状态栏
        SystemBarTintManager.setStatusBarTint(WatchActivity.this, Color.argb(0, 59, 59, 59));//透明状态栏

        fs = new FileStream(mContext);        //写文件用到的流
        intent = getIntent();
        jsonStr = intent.getStringExtra("res");        //获得文件
        sampleID = intent.getLongExtra("sampleID", -1);        //文件名称
        samplingtable = samplingtableDao.queryBuilder().where(SAMPLINGTABLEDao.Properties.Id.eq(sampleID)).list().get(0);
        sampleName = samplingtable.getShow_name();
        taskID = samplingtable.getTaskID();
        root_path = Util.getMediaFolder(mContext);
        if (root_path.isEmpty()) {
            Toast.makeText(mContext, "没有媒体文件夹", Toast.LENGTH_LONG).show();
            finish();
        }

        canUserEdit = intent.getBooleanExtra("canUserEdit", true);//是编辑模式还是查看模式

        //得到储存照片录像等信息的子目录完整路径
        child_path = root_path + File.separator + samplingtable.getMedia_folder();  //get media folder

        File cameraSearch = new File(child_path);
        if (!cameraSearch.exists())
            cameraSearch.mkdirs();

        headPanel = (HeadControlPanel) findViewById(R.id.head_layout);
        headPanel.setRightFirstVisible(View.VISIBLE);
        headPanel.setRightSecondVisible(View.VISIBLE);
        if (headPanel != null) {
            headPanel.initHeadPanel();
            if (canUserEdit)
                headPanel.setMiddleTitle(getResources().getString(R.string.writtenTempletTitle_edit) + sampleName);
            else
                headPanel.setMiddleTitle(getResources().getString(R.string.writtenTempletTitle_lookthrough) + sampleName);
            headPanel.setMiddleTitleTextSize(18);
            headPanel.setLeftImage(R.drawable.ic_menu_back);
            LeftImageOnClick l = new LeftImageOnClick() {

                @Override
                public void onImageClickListener() {
                    // TODO Auto-generated method stub
                    finish();
                }
            };
            headPanel.setLeftImageOnClick(l);
            headPanel.setRightFirstImage(R.drawable.save_file);
            headPanel.setRightFirstText("保存");
            HeadControlPanel.rightFirstImageOnClick r = new HeadControlPanel.rightFirstImageOnClick() {

                @Override
                public void onImageClickListener() {
                    itemName = etItem.getText().toString().replace(" ", "");
                    itemID = etItemID.getText().toString();
                    companyAddr = etCompanyAddress.getText().toString();

                    if (tv_gps.getText().toString().equals("")) {
                        T.showShort(WatchActivity.this, "正在定位中...请稍后保存~");
                        return;
                    }
                    if (itemName.equals("") || itemID.equals("")) {
                        T.showShort(WatchActivity.this, "样品名称和抽样编号不能为空！");
                        return;
                    }

                    try {
                        String mes = createJsonStr(true);    //生成json字符串
                        samplingtable.setSampling_content(mes);
                        samplingtable.setSaved_time(System.currentTimeMillis());
                        samplingtable.setShow_name(itemName + "-" + itemID);
                        samplingtable.setSampling_address(companyAddr);
                        samplingtable.setMedia_folder(tv_num.getText().toString());
                        samplingtable.setLatitude(((CollectionApplication) getApplication()).latitude);
                        samplingtable.setLongitude(((CollectionApplication) getApplication()).longitude);
                        samplingtable.setLocation_mode(((CollectionApplication) getApplication()).location_mode);
                        samplingtable.setSampling_unique_num(num);

                        if (neccessaryIsEdit) {
                            samplingtable.setIs_saved(true);
                        } else {
                            samplingtable.setIs_saved(false);
                        }

                        samplingtableDao.insertOrReplace(samplingtable);

                        if (!neccessaryIsEdit)
                            neccessaryIsEdit = true;

                        savedPictureNames.removeAll(savedPictureNames);
                        savedVedioNames.removeAll(savedVedioNames);
                        WatchActivity.this.finish();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };

            headPanel.setRightFirstImageOnClick(r);
            if (!canUserEdit) {
                headPanel.setRightFirstVisible(View.GONE);
                headPanel.setRightFirstText("");
            }

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

                                //判断是否是多选框的控件,是多选的要遍历每个多选框是否选择
                                if (myWidget.getChildCount() > 2) {

                                    //判断是否是地理信息 如果是 读取并跳过 如果不是 继续判断是否是checkbox
                                    if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals("地理信息: ") && myWidget.getChildCount() > 2) {
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
                                    if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals("拍照: ") && myWidget.getChildCount() > 2) {
                                        continue;
                                    }

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

                    Intent i = new Intent(WatchActivity.this, PrintActivity.class);
                    i.putExtra("num", tv_num.getText());
                    i.putExtra("toPrint", toPrint);
                    startActivity(i);

                }
            };
            headPanel.setRightSecondOnClick(t);
        }
        File task = new File(root_path);    //先创建文件夹
        if (!task.exists()) {
            task.mkdirs();
        }
        lu = new MyLayout(parentView, this, fs, root_path, MyLayout.SEE_FORM, taskID, samplingtable);    //生成布局的类
        lu.initLayout(jsonStr, canUserEdit);

        //初始化控件
//        etCompany = lu.companyET;
        etCompanyAddress = lu.companyNameET;
        etItem = lu.ItemsET;
        etItemID = lu.ItemsIDET;
        tv_num = lu.numberIdTV;
        tv_gps = lu.gpsIdTV;
        bt_gps = lu.gpsBtnTV;

        //赋值到applicaiton 激活定位信息
        ((CollectionApplication) getApplication()).mLocationResult = tv_gps;//将tv_gps赋值给application中的textview
        ((CollectionApplication) getApplication()).gps_pause = true;//查看和修改模式默认gps暂停

        //初始化GPS信息
        if (tv_gps.getText().toString().isEmpty()) {
            if (samplingtable.getIs_server_sampling())
                tv_gps.setText(getString(R.string.server_sampling_do_not_have_GPS_info));
            else
                tv_gps.setText(R.string.the_sampling_do_not_have_GPS_info);
        }

        tv_gps.setTextColor(getResources().getColor(R.color.text_color_gray));
        tv_num.setTextColor(getResources().getColor(R.color.text_color_gray));

        List<TASKINFO> taskinfos = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list();
        if (taskinfos.size() != 1) {
            Log.e("XXXXXXXXXXXX", "MyLayout.createTextView 1034 line 根据任务iD查询的任务数不等于1 ");
            finish();
        } else
            taskinfo = taskinfos.get(0);

        if (samplingtable.getSampling_unique_num().isEmpty()) {//抽样单唯一编号为空时，生成抽烟单唯一号存入数据库
            num = Util.getSamplingNum(mContext, taskinfo);
            tv_num.setText(num);
            samplingtable.setSampling_unique_num(num);
            samplingtableDao.insertOrReplace(samplingtable);
        } else {  //抽样单编号存在，直接从数据库读取
            tv_num.setText(samplingtable.getSampling_unique_num());
        }

        bt_gps.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        bt_gps.setBackgroundResource(R.drawable.background_date_choose);
        bt_gps.setTextColor(Color.argb(255, 0, 187, 212));
        bt_gps.setTextSize(20);
        bt_gps.setText("暂停GPS");
        bt_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bt_gps.getText().toString().equals("暂停GPS")) {
                    ((CollectionApplication) getApplication()).gps_pause = true;
                    bt_gps.setText("启动GPS");
                    tv_gps.setTextColor(Color.argb(255, 0, 187, 212));
                    bt_gps.setTextColor(Color.argb(255, 0, 187, 212));
                } else if (bt_gps.getText().toString().equals("启动GPS")) {
                    ((CollectionApplication) getApplication()).gps_pause = false;
                    bt_gps.setText("暂停GPS");
                    tv_gps.setTextColor(getResources().getColor(R.color.text_color_gray));
                    bt_gps.setTextColor(getResources().getColor(R.color.text_color_gray));
                }

            }
        });

        if (!canUserEdit) {
            bt_gps.setVisibility(View.GONE);
            bt_gps.setText("暂停GPS");
            tv_gps.setTextColor(getResources().getColor(R.color.text_color_gray));
            bt_gps.setTextColor(getResources().getColor(R.color.text_color_gray));
        } else {
            bt_gps.setVisibility(View.VISIBLE);
            bt_gps.setText("启动GPS");
            tv_gps.setTextColor(Color.argb(255, 0, 187, 212));
            bt_gps.setTextColor(Color.argb(255, 0, 187, 212));
        }
        updateGridView(canUserEdit);
        updateVideoGridView(canUserEdit);
        etItem = lu.ItemsET;
        etItemID = lu.ItemsIDET;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		CameraView cv=new CameraView();
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("recieve--requestCode-->" + requestCode);
        if (requestCode == GatherActivity.Companion.getREQUESTCODEFORDATE()) {
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
        } else if (requestCode == GatherActivity.Companion.getREQUESTCODEFORVIDEO()) {
            if (resultCode == RESULT_OK) {
                Uri uriVideo = data.getData();
                String video_path = uriVideo.toString();
                String video_name = new File(video_path).getName();
                savedVedioNames.add(video_name);
                updateVideoGridView(canUserEdit);
            }
        } else if (requestCode == GatherActivity.Companion.getREQUESTCODEFORPICTURE()) {
            if (data != null) {
                // 没有指定特定存储路径的时候
                T.showShort(this, "拍照成功");

                int id = data.getIntExtra("view_id", 0);        // imageView id
                String pName = data.getStringExtra("pName");    //图片名称
                savedPictureNames.add(pName);
                final String picPath = data.getStringExtra("picture_path");
                updateGridView(canUserEdit);

            }
        }
    }

    /**
     * 有布局生成json字符串
     *
     * @param isSave 是否保存
     * @return
     * @throws JSONException
     */
    private String createJsonStr(boolean isSave) throws JSONException {
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
                    itemObject = lu.mapToJson(itemTag, null);
                } else if (type.equals(MyLayout.TYPE_CAMERA)) {
                    itemObject = lu.mapToJson(itemTag, savedPictureNames);
                    if (necessary.equals("T") && savedPictureNames.isEmpty()) {
                        neccessaryIsEdit = false;
                    }

                } else if (type.equals(MyLayout.TYPE_VIDEO)) {
                    itemObject = lu.mapToJson(itemTag, savedVedioNames);
                    if (necessary.equals("T") && savedVedioNames.isEmpty()) {
                        neccessaryIsEdit = false;
                    }
                } else if (type.equals(MyLayout.TYPE_AUTO) || type.equals(MyLayout.TYPE_MY_COMPANY)
                        || type.equals(MyLayout.TYPE_MY_COMPANY_ADDRESS) || type.equals(MyLayout.TYPE_SAMPLING_CONTACT)
                        || type.equals(MyLayout.TYPE_SAMPLING_PHONE)) {
                    String cont = ((TextView) item_layout.getChildAt(1)).getText().toString();    //输入的内容
                    itemObject = lu.mapToJson(itemTag, cont);
                } else if (type.equals(MyLayout.TYPE_INPUT) || type.equals(MyLayout.TYPE_ADDR)
                        || type.equals(MyLayout.TYPE_COMPANY) || type.equals(MyLayout.TYPE_ITEMS)
                        || type.equals(MyLayout.TYPE_ITEM_ID)) {
                    EditText editText = ((EditText) item_layout.getChildAt(1));
                    String cont = editText.getText().toString();    //输入的内容
                    itemObject = lu.mapToJson(itemTag, cont);
                    if (necessary.equals("T") && cont.isEmpty()) {
                        neccessaryIsEdit = false;
                    }
                } else if (type.equals(MyLayout.TYPE_NUMBER) || type.equals(MyLayout.TYPE_COLLDATE)) {
                    String cont = ((TextView) item_layout.getChildAt(1)).getText().toString();    //自动生成的编号
                    if (type.equals(MyLayout.TYPE_NUMBER)) {
                        num = cont;
                    }
                    itemObject = lu.mapToJson(itemTag, cont);
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
//					JSONArray contArray = new JSONArray();
//					contArray.put((String)cont.substring(0,cont.indexOf(",")));	//GPS 经度
//					contArray.put((String)cont.substring(cont.indexOf(",")+1,cont.length()));	//GPS纬度
                    itemObject = lu.mapToJson(itemTag, cont);
                    if (necessary.equals("T") && (cont.isEmpty() || cont.equals(getString(R.string.gpsFailed)))) {
                        neccessaryIsEdit = false;
                    }
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
        return taskObject.toString();
    }

    /**
     * 打开GPS提示对话框
     */
    void openGPSDialog(boolean isGpsOpened, boolean isNetWorkOpened) {
        if (isGpsOpened)
            return;

        if (dialog.isShowing()) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) WatchActivity.this
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
            Message.setText("是否开启？");
        } else if (!isGpsOpened && isNetWorkOpened) {
            positivebutton.setVisibility(View.VISIBLE);
            openGPRSButton.setVisibility(View.GONE);
            Title.setText("本软件需开启GPS定位开关");
            Message.setText("是否开启？");
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
                WatchActivity.this.finish();
            }
        });


        dialog.setContentView(layout);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 更新grid中的图片
     */
    public void updateGridView(final boolean canUserEdit) {
        if (canUserEdit) {
            LinearLayout LL_All = lu.getparentView();

            for (int i = 0; i < LL_All.getChildCount(); i++) {
                for (int k = 0; k < ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildCount(); k++) {
                    LinearLayout myWidget = ((LinearLayout) ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildAt(k));

                    if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() == 2) {//排除没有checkbox的控件
                        if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals("拍照: ")) {//判断这个是照相控件
                            final GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));

                            //定义照片集 并添加照片
                            final ArrayList<ImageInfo> mImages = new ArrayList<ImageInfo>();

                            mImages.add(new ImageInfo(ImageInfo.PICTURE));//第一个添加的是照相图片

                            final File cameraSearch = new File(child_path);

                            savedPictureNames.removeAll(savedPictureNames);

                            //其他的照片遍历child文件夹得道
                            if (cameraSearch.listFiles() != null) {
                                for (int j = 0; j < cameraSearch.listFiles().length; j++) {
                                    if (cameraSearch.listFiles()[j].getName().startsWith("CAMERA_")) {
                                        mImages.add(new ImageInfo(cameraSearch.listFiles()[j].getPath()));
                                        savedPictureNames.add(cameraSearch.listFiles()[j].getName());
                                    }
                                }
                            }

                            mGridView.setTag(mImages);
                            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    ArrayList<ImageInfo> mImages = (ArrayList<ImageInfo>) mGridView.getTag();
                                    if (i == 0) {//点击第一个是拍照控件

                                        Intent intent = new Intent(WatchActivity.this, CameraView.class);
                                        intent.putExtra("root_path", child_path);    //将存储图片根目录传递过去
                                        intent.putExtra("view_id", GatherActivity.Companion.getREQUESTCODEFORPICTURE() + 0);        //控件ID
                                        intent.putExtra("location", tv_gps.getText());
                                        intent.putExtra("number", cameraSearch.getName());
                                        startActivityForResult(intent, GatherActivity.Companion.getREQUESTCODEFORPICTURE());
                                        return;
                                    }
                                    //其他的是查看照片
                                    Intent intent = new Intent(WatchActivity.this, PictureViewActivity.class);
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

                                        LayoutInflater inflater = (LayoutInflater) WatchActivity.this
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
                                                    updateGridView(canUserEdit);
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
                        continue;
                    }

                }

            }
        } else {
            LinearLayout LL_All = lu.getparentView();

            for (int i = 0; i < LL_All.getChildCount(); i++) {
                for (int k = 0; k < ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildCount(); k++) {
                    LinearLayout myWidget = ((LinearLayout) ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildAt(k));


                    if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() == 2) {//排除没有checkbox的控件
                        if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals("拍照: ")) {//判断这个是照相控件
                            GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));

                            final ArrayList<ImageInfo> mImages = new ArrayList<ImageInfo>();

                            File cameraSearch = new File(child_path);

                            if (!cameraSearch.exists())
                                return;

                            if (cameraSearch.listFiles() != null) {
                                for (int j = 0; j < cameraSearch.listFiles().length; j++) {
                                    if (cameraSearch.listFiles()[j].getName().startsWith("CAMERA_")) {
                                        mImages.add(new ImageInfo(cameraSearch.listFiles()[j].getPath()));
                                    }
                                }
                            }

                            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(WatchActivity.this, PictureViewActivity.class);
                                    intent.putExtra("picPath", mImages.get(i).getLocalPath());
                                    startActivity(intent);
                                }
                            });

                            ((SampleExelGridAdapter) mGridView.getAdapter()).setmImages(mImages);
                            ((SampleExelGridAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                        }
                        continue;
                    }

                }

            }
        }

    }

    /**
     * 更新Videogrid中的视频
     */
    public void updateVideoGridView(final boolean canUserEdit) {
        if (canUserEdit) {
            LinearLayout LL_All = lu.getparentView();

            for (int i = 0; i < LL_All.getChildCount(); i++) {
                for (int k = 0; k < ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildCount(); k++) {
                    LinearLayout myWidget = ((LinearLayout) ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildAt(k));

                    if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() == 2) {//排除没有checkbox的控件
                        if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals("录像: ")) {//判断这个是录像控件
                            final GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));

                            //定义照片集 并添加照片
                            final ArrayList<ImageInfo> mImages = new ArrayList<ImageInfo>();

                            mImages.add(new ImageInfo(ImageInfo.VIDEO));//第一个添加的是照相图片

                            File cameraSearch = new File(child_path);
                            if (!cameraSearch.exists())
                                cameraSearch.mkdirs();

                            savedVedioNames.removeAll(savedVedioNames);

                            if (cameraSearch.listFiles() != null) {
                                //其他的照片遍历child文件夹得道
                                for (int j = 0; j < cameraSearch.listFiles().length; j++) {
                                    if (cameraSearch.listFiles()[j].getName().startsWith("VIDEO_")) {
                                        ImageInfo video_image = new ImageInfo(createVideoThumbnail(cameraSearch.listFiles()[j].getPath(), 0));
                                        video_image.setMediaType(ImageInfo.VIDEOBMP);
                                        video_image.setLocalPath(cameraSearch.listFiles()[j].getPath());
                                        mImages.add(video_image);
                                        savedVedioNames.add(cameraSearch.listFiles()[j].getName());
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
                                        // /intent.putExtra("view_id", v.getId());
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(video_name));
                                        startActivityForResult(intent, GatherActivity.Companion.getREQUESTCODEFORVIDEO());
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

                                        LayoutInflater inflater = (LayoutInflater) WatchActivity.this
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
                                                    updateVideoGridView(canUserEdit);
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
                        continue;
                    }

                }

            }
        } else {
            LinearLayout LL_All = lu.getparentView();

            for (int i = 0; i < LL_All.getChildCount(); i++) {
                for (int k = 0; k < ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildCount(); k++) {
                    LinearLayout myWidget = ((LinearLayout) ((LinearLayout) ((LinearLayout) LL_All.getChildAt(i)).getChildAt(1)).getChildAt(k));

                    if (((LinearLayout) myWidget.getChildAt(0)).getChildCount() == 2) {//排除没有checkbox的控件
                        if (((TextView) ((LinearLayout) myWidget.getChildAt(0)).getChildAt(1)).getText().toString().equals("录像: ")) {//判断这个是录像控件
                            GridViewEx mGridView = ((GridViewEx) myWidget.getChildAt(2));

                            //定义集 用于添加视频
                            final ArrayList<ImageInfo> mImages = new ArrayList<ImageInfo>();

                            File cameraSearch = new File(child_path);
                            //遍历child文件夹得到以VIDEO_开头的所有视频文件
                            if (!cameraSearch.exists()) {
                                return;
                            }

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

                            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                    //产看视频
                                    Uri url = Uri.parse("file://" + mImages.get(i).getLocalPath());
                                    String type = "video/mp4";
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(url, type);
                                    startActivity(intent);

                                }
                            });

                            ((SampleExelGridAdapter) mGridView.getAdapter()).setmImages(mImages);//自动notify数据
                        }
                        continue;
                    }

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
        }

        if (isHavePicture && lu.getPicIMGObjet() != null) {
            lu.getPicIMGObjet().setVisibility(View.GONE);
        }

        boolean gps = Util.isOpen(this);
        boolean isNetConnected = ((CollectionApplication) getApplication()).isNetworkConnected();
        if (!gps) {
            openGPSDialog(gps, isNetConnected);

        }

    }

    @Override
    protected void onDestroy() {
        lu.recycleParentView();
        System.gc();
        super.onDestroy();
    }
}
