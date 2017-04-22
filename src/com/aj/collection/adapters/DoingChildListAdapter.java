package com.aj.collection.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.Constant;
import com.aj.WeixinActivityMain;
import com.aj.collection.activity.CollectionApplication;
import com.aj.collection.activity.GatherActivity;
import com.aj.collection.bean.Counter;
import com.aj.collection.bean.TaskInfo;
import com.aj.collection.R;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLE;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLE;
import com.aj.database.TEMPLETTABLEDao;
import com.aj.collection.activity.http.API;
import com.aj.collection.activity.http.ReturnCode;
import com.aj.collection.activity.http.URLs;
import com.aj.collection.activity.tools.SPUtils;
import com.aj.collection.activity.tools.Util;
import com.aj.collection.activity.ui.widget.FileUtil;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.library.ExpandableLayoutItem;
import com.library.ExpandableLayoutListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.aj.collection.activity.http.URLs.UPLAODIMG;

/**
 * Created by kevin on 15-10-6.
 */
public class DoingChildListAdapter extends ArrayAdapter {
    private String LogError = "XXXXXXX";
    private String LogClass = "com.aj.collection.adapters.DoingChildListAdapter:\n";

    protected LayoutInflater mInflater;
    private static final int mLayout = R.layout.child_listview_adapter_layout;
    private Context mContext;
    private CollectionApplication application;
    private ExpandableLayoutListView parentExpandListView;//
    private ExpandableLayoutListView childExpandListView;

    public long taskID;//任务名字 也就是/Task/鹤岗采样 or /Templet/鹤岗采样 文件夹下的那个文件夹 是此次任务的名字 一个任务中可能有很多抽样单模板

    public List<Object> dataSet;

    private TaskInfo taskInfo;

    private int pageFlag;

    RequestQueue queue;

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    public DoingChildListAdapter(Context context, long taskID, ExpandableLayoutListView parentExpandListView,
                                 ExpandableLayoutListView childExpandListView, List<Object> dataSet,
                                 int pageFlag, CollectionApplication application) {

        super(context, mLayout, dataSet);//上下文环境/布局文件/填充布局文件数据

        mInflater = LayoutInflater.from(context);

        mContext = context;

        this.pageFlag = pageFlag;

        taskInfo = new TaskInfo(mContext, this.pageFlag);

        this.taskID = taskID;

        this.parentExpandListView = parentExpandListView;
        this.childExpandListView = childExpandListView;
        this.dataSet = dataSet;

        this.application = application;
        this.queue = application.getRequestQueue();

        //database init
        daoSession = ((CollectionApplication) ((Activity) mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = getConvertView(viewHolder);
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (viewHolder.item.isOpened())
            viewHolder.item.hideNow();

        preferenceView(viewHolder, position);

        return convertView;

    }

    public class ViewHolder {
        //inflater
        View inflatedHeaderView;
        View inflatedContentView;
        View inflatedAllView;
        //childlistheader xml
        ImageView child_listview_title_img;
        TextView child_listview_title;
        TextView child_listview_description;
        TextView child_listview_right_btn;
        RelativeLayout child_LL_right_btn;
        TextView child_listview_upload_status;
        TextView child_listview_save_status;
        //childlist content xml
        LinearLayout LL_edit;
        LinearLayout LL_delete;
        LinearLayout LL_upload;
        LinearLayout LL_makeup;

        TextView edit_img, delete_img, upload_img, makeup_img;
        TextView edit_text, delete_text, upload_text, makeup_text;

        ExpandableLayoutItem item;

        RelativeLayout mRelativeLayout;
    }


    /**
     * initial ViewHolder
     *
     * @param ItemView
     * @return
     */
    public View getConvertView(ViewHolder ItemView) {
        View view = mInflater.inflate(mLayout, null);
        ExpandableLayoutItem item = (ExpandableLayoutItem) view.findViewById(R.id.row);
        ItemView.item = item;

        RelativeLayout contentLayout = ItemView.item.contentLayout;
        View inflatedContent = contentLayout.getChildAt(0);
        RelativeLayout headerLayout = ItemView.item.headerLayout;
        View inflatedheader = headerLayout.getChildAt(0);
        //init child list inflate
        ItemView.inflatedHeaderView = inflatedheader;
        ItemView.inflatedContentView = inflatedContent;
        ItemView.inflatedAllView = view;

        //init child list head
        ItemView.child_listview_title = (TextView) inflatedheader.findViewById(R.id.child_listview_title);
        ItemView.child_listview_description = (TextView) inflatedheader.findViewById(R.id.child_listview_describe);
        ItemView.child_listview_upload_status = (TextView) inflatedheader.findViewById(R.id.child_listview_upload_status);
        ItemView.child_listview_save_status = (TextView) inflatedheader.findViewById(R.id.child_listview_save_status);
        ItemView.child_listview_right_btn = (TextView) inflatedheader.findViewById(R.id.child_listview_right_btn);
        ItemView.child_LL_right_btn = (RelativeLayout) inflatedheader.findViewById(R.id.child_LL_right_btn);
        ItemView.child_listview_title_img = (ImageView) inflatedheader.findViewById(R.id.child_listview_title_img);
        ItemView.mRelativeLayout = (RelativeLayout) inflatedheader.findViewById(R.id.childlist_ll);
        //init child list content
        ItemView.LL_edit = (LinearLayout) inflatedContent.findViewById(R.id.child_list_content_edit);
        ItemView.LL_delete = (LinearLayout) inflatedContent.findViewById(R.id.child_list_content_delete);
        ItemView.LL_upload = (LinearLayout) inflatedContent.findViewById(R.id.child_list_content_upload);
        ItemView.LL_makeup = (LinearLayout) inflatedContent.findViewById(R.id.child_list_content_makeup);

        ItemView.edit_img = (TextView) inflatedContent.findViewById(R.id.edit_img);
        ItemView.edit_text = (TextView) inflatedContent.findViewById(R.id.edit_text);
        ItemView.delete_img = (TextView) inflatedContent.findViewById(R.id.delete_img);
        ItemView.delete_text = (TextView) inflatedContent.findViewById(R.id.delete_text);
        ItemView.upload_img = (TextView) inflatedContent.findViewById(R.id.upload_img);
        ItemView.upload_text = (TextView) inflatedContent.findViewById(R.id.upload_text);
        ItemView.makeup_img = (TextView) inflatedContent.findViewById(R.id.makeup_img);
        ItemView.makeup_text = (TextView) inflatedContent.findViewById(R.id.makeup_text);

        view.setTag(ItemView);
        return view;
    }

    /**
     * 配置当前界面
     *
     * @param ItemView
     * @param position
     */
    public void preferenceView(final ViewHolder ItemView, int position) {
        if (dataSet.get(position) instanceof TEMPLETTABLE) {// templete view 当前位置是模板

            TEMPLETTABLE templettable = (TEMPLETTABLE) dataSet.get(position);

            int[] indicatorNum = taskInfo.getUploadIndicatorNum(templettable.getTempletID());//get indicator number
            final String templetName = templettable.getTemplet_name();//get templet name

            /*显示模板的图标和大号title  深色title 隐藏状态和按钮*/
            ItemView.child_listview_title_img.setImageResource(R.drawable.sampledoc);
            RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(30, mContext), ScreenUtils.dip2px(30, mContext));
            relLayoutParams.setMargins(ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext));
            relLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            ItemView.child_listview_title_img.setLayoutParams(relLayoutParams);

            //title
            ItemView.child_listview_title.setText(templetName + TaskInfo.LISTVIEW_TEMPLET_SUFFIX);
            ItemView.child_listview_title.setTextColor(Color.parseColor("#555555"));
            ItemView.child_listview_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

            //description
            ItemView.child_listview_description.setVisibility(View.GONE);

            //设置右侧一个状态消失，一个变成上传/总数
            ItemView.child_listview_upload_status.setVisibility(View.GONE);
            ItemView.child_listview_save_status.setVisibility(View.VISIBLE);
            ItemView.child_listview_save_status.setBackgroundResource(R.drawable.shape_upload_indicator);
            ItemView.child_listview_save_status.setText(indicatorNum[0] + "/" + indicatorNum[1]); //显示已上传/总数


            //右边按钮为上传 和布局设置
            if (indicatorNum[0] == indicatorNum[1]) {
                ItemView.child_listview_right_btn.setBackgroundResource(R.drawable.upload_templet_disable);
                ItemView.child_LL_right_btn.setEnabled(false);
            } else {
                ItemView.child_listview_right_btn.setBackgroundResource(R.drawable.wifi);
                ItemView.child_LL_right_btn.setEnabled(true);
            }
            RelativeLayout.LayoutParams rightBtnParam = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(30, mContext), ScreenUtils.dip2px(30, mContext));
            ItemView.child_listview_right_btn.setLayoutParams(rightBtnParam);
            ItemView.child_LL_right_btn.setTag(templettable);
            ItemView.child_LL_right_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TEMPLETTABLE templettable = (TEMPLETTABLE) ItemView.child_LL_right_btn.getTag();

                    //find the unuploaded sample and the saved samplings under the templet
                    List<SAMPLINGTABLE> samplingtables = samplingtableDao.queryBuilder().
                            where(SAMPLINGTABLEDao.Properties.TempletID.eq(templettable.getTempletID()),
                                    SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_HAVE_NOT_UPLOAD),
                                    SAMPLINGTABLEDao.Properties.Check_status.notEq(Constant.S_STATUS_DELETE),
                                    SAMPLINGTABLEDao.Properties.Is_saved.eq(true))
                            .orderAsc(SAMPLINGTABLEDao.Properties.Id).list();

//                    //check if have unsaved sampling
//                    for (int i = 0; i < samplingtables.size(); i++) {
//                        if (!samplingtables.get(i).getIs_saved()) {
//                            Toast.makeText(mContext, "有未完成的抽样单！", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                    }

                    String taskName;
                    if (taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(templettable.getTaskID())).list().size() != 1)
                        return;
                    else
                        taskName = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(templettable.getTaskID())).list().get(0).getTask_name();

                    uploadSamplingAndMedia(samplingtables, taskName);


//                    TEMPLETTABLE templettable = (TEMPLETTABLE) ItemView.child_LL_right_btn.getTag();
//
//                    //find the unuploaded sample under the templet
//                    QueryBuilder queryBuilder = samplingtableDao.queryBuilder();
//                    queryBuilder.where(SAMPLINGTABLEDao.Properties.TempletID.eq(templettable.getTempletID()), SAMPLINGTABLEDao.Properties.Is_uploaded.eq(false));
//                    List<SAMPLINGTABLE> samplingtables = queryBuilder.orderAsc(SAMPLINGTABLEDao.Properties.Id).list();
//
//                    for(int i=0;i<samplingtables.size();i++){
//                        samplingtables.get(i).setIs_uploaded(true);
//                    }
//
//                    samplingtableDao.insertOrReplaceInTx(samplingtables);
//
//                    //Toast
//                    Toast.makeText(mContext, "模板" + templettable.getTemplet_name() + "下所有抽烟单上传成功", Toast.LENGTH_SHORT).show();
//
//                    ((DoingParentListAdapter) parentExpandListView.getAdapter()).updateRightCommitButtonText(taskID, parentExpandListView.currentExpandableLayout);
//                    //update data set of child adapter
//                    ((WeixinActivityMain) mContext).notifyDoingChildListDataChanged();
//                    //update data set of parent adapter
//                    ((WeixinActivityMain) mContext).notifyDoingParentListDataChanged();
                }
            });

            //Templet don't need a expand view
            ItemView.inflatedContentView.setVisibility(View.GONE);

            //最后确认布局居中
            RelativeLayout.LayoutParams centerVerticalParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(50, mContext));
            centerVerticalParam.addRule(RelativeLayout.CENTER_VERTICAL);
            ItemView.mRelativeLayout.setLayoutParams(centerVerticalParam);

            /*抽样单模板的点击事件：进入抽样单*/
            ItemView.mRelativeLayout.setTag(templettable);

            ItemView.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TEMPLETTABLE templettable = (TEMPLETTABLE) ItemView.mRelativeLayout.getTag();

                    Intent startForm = new Intent((Activity) mContext, GatherActivity.class);
                    startForm.putExtra("res", templettable.getTemplet_content());    //字符串
                    startForm.putExtra("templetID", templettable.getTempletID()); //文件名不包含后缀
                    startForm.putExtra("taskID", templettable.getTaskID());    //哪个任务被点击了
                    startForm.putExtra("Mode", GatherActivity.Companion.getMODE_TEMPLATE());
                    ((Activity) mContext).startActivityForResult(startForm, Constant.WEIXINTASKREFRESHITEM_FROMDO);
                }
            });

            ItemView.mRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    TEMPLETTABLE templettable = (TEMPLETTABLE) ItemView.mRelativeLayout.getTag();
                    String itemName = templettable.getTemplet_name();
                    if (ItemView.child_listview_title.getText().length() > 10)
                        Toast.makeText(mContext, itemName, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            ItemView.mRelativeLayout.setBackgroundColor(Color.parseColor("#f8f8f8"));

        } else if (dataSet.get(position) instanceof SAMPLINGTABLE) {//written templet view   不在positionlist中的posiiton说明不是模板，是已填写的当前模板的采样记录

            SAMPLINGTABLE samplingtable = (SAMPLINGTABLE) dataSet.get(position);

            final String sampleName = samplingtable.getShow_name();

            /*显示已完成任务的图标form和小号title15  浅色title777777 暂时隐藏状态和按钮*/

            //左边的图片及其布局
            ItemView.child_listview_title_img.setImageResource(R.drawable.form);
            RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(25, mContext), ScreenUtils.dip2px(25, mContext));
            relLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relLayoutParams.setMargins(ScreenUtils.dip2px(15, mContext), ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext));
            ItemView.child_listview_title_img.setLayoutParams(relLayoutParams);

            //中间的文字及布局
            if (sampleName.isEmpty()) {//定点采样  //TODO 定点采样的抽样单界面怎样设计？？
                ItemView.child_listview_title.setText("未填写样品名称");
            } else
                ItemView.child_listview_title.setText(sampleName);
            ItemView.child_listview_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            ItemView.child_listview_title.setTextColor(Color.parseColor("#666666"));

            //description
            String addressToGO = samplingtable.getSampling_address();
            ItemView.child_listview_description.setVisibility(View.VISIBLE);
            if (addressToGO.isEmpty()) {
                ItemView.child_listview_description.setText(mContext.getString(R.string.do_not_edit_company_name));
            } else {
                ItemView.child_listview_description.setText(addressToGO);
            }

            //右侧的上传状态和保存状态及布局
            /**
             * 状态分四个：未上传、待审核、已通过、未通过
             * 操作分四个：编辑、删除、上传、补采
             * 先判断是否上传 1.未上传可以编辑、删除，不能上传、补采
             *             2.已上传判断是待审核0，还是已通过1还是未通过。待审核0：一个操作不能做
             *                                                   已通过1：一个操作不能做
             *                                                   未通过2：只能补采
             *
             */

            if (samplingtable.getCheck_status() == Constant.S_STATUS_HAVE_NOT_UPLOAD) {
                ItemView.child_listview_upload_status.setText("未上传");
                ItemView.child_listview_upload_status.setBackgroundResource(R.drawable.shape_second_status_gray);
                //enable others，disable make up button.
                ItemView.LL_edit.setEnabled(true);
                ItemView.edit_img.setBackgroundResource(R.drawable.edit_expand);

                if (samplingtable.getIs_server_sampling() || samplingtable.getIs_make_up()) {
                    ItemView.LL_delete.setEnabled(false);
                    ItemView.delete_img.setBackgroundResource(R.drawable.delete_disenable);
                } else {
                    ItemView.LL_delete.setEnabled(true);
                    ItemView.delete_img.setBackgroundResource(R.drawable.delete_expand);
                }

                if (samplingtable.getIs_saved()) {
                    ItemView.LL_upload.setEnabled(true);
                    ItemView.upload_img.setBackgroundResource(R.drawable.upload_expand);
                } else {
                    ItemView.LL_upload.setEnabled(false);
                    ItemView.upload_img.setBackgroundResource(R.drawable.upload_expand_disable);
                }

                ItemView.LL_makeup.setEnabled(false);
                ItemView.makeup_img.setBackgroundResource(R.drawable.makeup_expand_disable);
            } else if (samplingtable.getCheck_status() == Constant.S_STATUS_CHECKING) {
                ItemView.child_listview_upload_status.setText("待审核");
                ItemView.child_listview_upload_status.setBackgroundResource(R.drawable.shape_second_status_blue);
                //enable make up button.disable others
                ItemView.LL_edit.setEnabled(false);
                ItemView.edit_img.setBackgroundResource(R.drawable.edit_expand_disable);
                ItemView.LL_delete.setEnabled(false);
                ItemView.delete_img.setBackgroundResource(R.drawable.delete_disenable);
                ItemView.LL_upload.setEnabled(false);
                ItemView.upload_img.setBackgroundResource(R.drawable.upload_expand_disable);
                ItemView.LL_makeup.setEnabled(false);
                ItemView.makeup_img.setBackgroundResource(R.drawable.makeup_expand_disable);
            } else if (samplingtable.getCheck_status() == Constant.S_STATUS_PASSED) {
                ItemView.child_listview_upload_status.setText("已通过");
                ItemView.child_listview_upload_status.setBackgroundResource(R.drawable.shape_second_status_green);
                //enable make up button.disable others
                ItemView.LL_edit.setEnabled(false);
                ItemView.edit_img.setBackgroundResource(R.drawable.edit_expand_disable);
                ItemView.LL_delete.setEnabled(false);
                ItemView.delete_img.setBackgroundResource(R.drawable.delete_disenable);
                ItemView.LL_upload.setEnabled(false);
                ItemView.upload_img.setBackgroundResource(R.drawable.upload_expand_disable);
                ItemView.LL_makeup.setEnabled(false);
                ItemView.makeup_img.setBackgroundResource(R.drawable.makeup_expand_disable);
            } else if (samplingtable.getCheck_status() == Constant.S_STATUS_NOT_PASS) {
                ItemView.child_listview_upload_status.setText("未通过");
                ItemView.child_listview_upload_status.setBackgroundResource(R.drawable.shape_second_status_red);
                //enable make up button.disable others
                ItemView.LL_edit.setEnabled(false);
                ItemView.edit_img.setBackgroundResource(R.drawable.edit_expand_disable);
                ItemView.LL_delete.setEnabled(false);
                ItemView.delete_img.setBackgroundResource(R.drawable.delete_disenable);
                ItemView.LL_upload.setEnabled(false);
                ItemView.upload_img.setBackgroundResource(R.drawable.upload_expand_disable);
                ItemView.LL_makeup.setEnabled(true);
                ItemView.makeup_img.setBackgroundResource(R.drawable.makeup_expand);
            } else if (samplingtable.getCheck_status() == Constant.S_STATUS_NOT_USED) {
                ItemView.child_listview_upload_status.setText("已补采");
                ItemView.child_listview_upload_status.setBackgroundResource(R.drawable.shape_second_status_yellow);
                //enable make up button.disable others
                ItemView.LL_edit.setEnabled(false);
                ItemView.edit_img.setBackgroundResource(R.drawable.edit_expand_disable);
                ItemView.LL_delete.setEnabled(false);
                ItemView.delete_img.setBackgroundResource(R.drawable.delete_disenable);
                ItemView.LL_upload.setEnabled(false);
                ItemView.upload_img.setBackgroundResource(R.drawable.upload_expand_disable);
                ItemView.LL_makeup.setEnabled(false);
                ItemView.makeup_img.setBackgroundResource(R.drawable.makeup_expand_disable);
            }


            ItemView.child_listview_upload_status.setVisibility(View.VISIBLE);


            // 根据表格必填项有没有填完显示未保存和已保存
            ItemView.child_listview_save_status.setVisibility(View.VISIBLE);
            if (samplingtable.getIs_saved()) {
                ItemView.child_listview_save_status.setBackgroundResource(R.drawable.shape_second_status_green);
                ItemView.child_listview_save_status.setText("已完成");
            } else {
                ItemView.child_listview_save_status.setBackgroundResource(R.drawable.shape_second_status_gray);
                ItemView.child_listview_save_status.setText("未完成");
            }


            //右边按钮显示按钮菜单 和布局设置
            //Templet don't need a expand view
            ItemView.inflatedContentView.setVisibility(View.VISIBLE);
            ItemView.child_listview_right_btn.setBackgroundResource(R.drawable.ellipsis);
            RelativeLayout.LayoutParams rightBtnParam = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(35, mContext), ScreenUtils.dip2px(35, mContext));
            ItemView.child_listview_right_btn.setLayoutParams(rightBtnParam);
            ItemView.child_LL_right_btn.setTag(position);
            ItemView.child_LL_right_btn.setEnabled(true);
            ItemView.child_LL_right_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    childExpandListView.expandChildView((Integer) ItemView.child_LL_right_btn.getTag());
//                    parentExpandListView.currentExpandableLayout.refreshExpandableItemHeight();//刷新里层高度
//                    Toast.makeText(mContext,"debug:clicked",Toast.LENGTH_SHORT).show();
                }
            });

            //add edit listener
            ItemView.LL_edit.setTag(samplingtable);
            ItemView.LL_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SAMPLINGTABLE samplingtable = (SAMPLINGTABLE) ItemView.mRelativeLayout.getTag();
                    Intent startForm = new Intent(mContext, GatherActivity.class);
                    startForm.putExtra("sampleID", samplingtable.getId()); //文件名不包含后缀
                    startForm.putExtra("Mode", GatherActivity.Companion.getMODE_EDIT()); // 编辑模式，编辑模式下不可加样
                    mContext.startActivity(startForm);
                }
            });


            //add delete listener
            ItemView.LL_delete.setTag(samplingtable);
            //ItemView.child_listview_right_btn.setTag(position);
            ItemView.LL_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SAMPLINGTABLE samplingtable = (SAMPLINGTABLE) ItemView.mRelativeLayout.getTag();
                    if (samplingtable != null) {
                        samplingtableDao.delete(samplingtable);
                        //also delete the media folder
                        File mediaFolder = taskInfo.getMediaFolderBySampleTable(samplingtable);
                        if (mediaFolder != null && mediaFolder.exists())
                            FileUtil.delete(mediaFolder);

                        childExpandListView.currentExpandableLayout.hideNow();
                        Toast.makeText(mContext, "已删除" + samplingtable.getShow_name(), Toast.LENGTH_SHORT).show();

                        //refresh data
                        ((WeixinActivityMain) mContext).notifyDoingChildListDataChanged();
                        ((WeixinActivityMain) mContext).notifyDoingParentListDataChanged();


                        MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP
                                , ScreenUtils.getScreenWidth(mContext) / 2, ScreenUtils.getScreenWidth(mContext) / 2, 0);
                        childExpandListView.onTouchEvent(motionEvent);

                    } else {
                        Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //add upload listener
            ItemView.LL_upload.setTag(samplingtable);
            ItemView.LL_upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO connect upload URL,post .spms and media file 上传toUploadFile文件

                    SAMPLINGTABLE samplingtable = (SAMPLINGTABLE) ItemView.mRelativeLayout.getTag();

                    if (!samplingtable.getIs_saved()) {
                        Toast.makeText(mContext, "抽样单未完成！", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String taskName;
                    if (taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(samplingtable.getTaskID())).list().size() != 1)
                        return;
                    else
                        taskName = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(samplingtable.getTaskID())).list().get(0).getTask_name();

                    ArrayList<SAMPLINGTABLE> samplingtables = new ArrayList<SAMPLINGTABLE>();
                    samplingtables.add(samplingtable);

                    uploadSamplingAndMedia(samplingtables, taskName);

//                    SAMPLINGTABLE samplingtable = (SAMPLINGTABLE) ItemView.mRelativeLayout.getTag();
//                    samplingtable.setIs_uploaded(true);
//                    samplingtableDao.insertOrReplace(samplingtable);
//
//                    Toast.makeText(mContext, "抽样单:" + samplingtable.getShow_name() + "上传成功", Toast.LENGTH_SHORT).show();
//                    //refresh
//                    ((DoingParentListAdapter) parentExpandListView.getAdapter()).updateRightCommitButtonText(taskID, parentExpandListView.currentExpandableLayout);
//                    //update data set of parent adapterr
//                    ((WeixinActivityMain)mContext).notifyDoingParentListDataChanged();
//
//                    ((WeixinActivityMain)mContext).notifyDoingChildListDataChanged();
                }
            });

            ItemView.LL_makeup.setTag(samplingtable);
            ItemView.LL_makeup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SAMPLINGTABLE samplingtable = (SAMPLINGTABLE) ItemView.mRelativeLayout.getTag();

                    Intent startForm = new Intent(mContext, GatherActivity.class);
                    startForm.putExtra("samplingID", samplingtable.getId());//get the id of sampling
                    startForm.putExtra("Mode", GatherActivity.Companion.getMODE_MAKE_UP());
                    startForm.putExtra("sid_of_server", samplingtable.getSid_of_server());//补采的时候通过存在服务器上的id来判断这是对 哪个抽样单进行的补采
                    ((Activity) mContext).startActivityForResult(startForm, Constant.WEIXINTASKREFRESHITEM_FROMDO);
                }
            });

            //最后确认布局居中
            RelativeLayout.LayoutParams centerVerticalParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(50, mContext));
            centerVerticalParam.addRule(RelativeLayout.CENTER_VERTICAL);
            ItemView.mRelativeLayout.setLayoutParams(centerVerticalParam);

            /*完成的抽样单的点击事件：进入抽样单*/
            ItemView.mRelativeLayout.setTag(samplingtable);
            ItemView.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SAMPLINGTABLE samplingtable = (SAMPLINGTABLE) ItemView.mRelativeLayout.getTag();
                    String res = samplingtable.getSampling_content();
                    Intent startForm = new Intent(mContext, GatherActivity.class);
                    startForm.putExtra("res", res);    //字符串
                    startForm.putExtra("sampleID", samplingtable.getId()); //文件名不包含后缀
                    startForm.putExtra("Mode", GatherActivity.Companion.getMODE_LOOK_THROUGH());//设置为查看模式
                    mContext.startActivity(startForm);
                }
            });

            ItemView.mRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    SAMPLINGTABLE samplingtable = (SAMPLINGTABLE) ItemView.mRelativeLayout.getTag();
                    String itemName = samplingtable.getShow_name();
                    if (ItemView.child_listview_title.getText().length() > 10)
                        Toast.makeText(mContext, itemName, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            ItemView.mRelativeLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        }


    }

//    /**
//     * 上传单张抽样单
//     * @param samplingtable
//     * @param taskName
//     */
//    public void uploadSingleSampling(final SAMPLINGTABLE samplingtable,final String taskName){
//
//        Response.Listener<String> listener = new Response.Listener<String>() {
//            @Override
//            public void onResponse(String s) {
//                samplingtable.setIs_uploaded(true);
//                samplingtableDao.insertOrReplace(samplingtable);
//
//                Toast.makeText(mContext, "抽样单:" + samplingtable.getShow_name() + "上传成功", Toast.LENGTH_SHORT).show();
//                //refresh
//                ((DoingParentListAdapter) parentExpandListView.getAdapter()).updateRightCommitButtonText(taskID, parentExpandListView.currentExpandableLayout);
//                //update data set of parent adapterr
//                ((WeixinActivityMain)mContext).notifyDoingParentListDataChanged();
//
//                ((WeixinActivityMain) mContext).notifyDoingChildListDataChanged();
//            }
//        };
//        Response.ErrorListener errorListener = new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                Toast.makeText(mContext, "上传失败,请检查网络"+volleyError.toString(), Toast.LENGTH_LONG).show();
//                Log.e("UploadSampFail", volleyError.toString());
//            }
//        };
//
//        JSONObject jsonObject=new JSONObject();
//        try {
//            jsonObject.put("tID",String.valueOf(samplingtable.getTaskID()));
//            jsonObject.put("tname",taskName);
//            jsonObject.put("tcon", samplingtable.getSampling_content());
//            jsonObject.put("sname",samplingtable.getShow_name());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        JSONArray jsonArray=new JSONArray();
//        jsonArray.put(jsonObject);
//        StringRequest stringRequest = API.uploadSampling(listener, errorListener
//                , (String)SPUtils.get(mContext,SPUtils.LOGIN_NAME,"",SPUtils.LOGIN_VALIDATE)
//                , (String)SPUtils.get(mContext,SPUtils.LOGIN_PASSWORD,"",SPUtils.LOGIN_VALIDATE)
//                , jsonArray.toString(),String.valueOf(jsonArray.length()));
//        queue.add(stringRequest);
//    }

    public void uploadSamplingAndMedia(final List<SAMPLINGTABLE> samplingtables, final String taskName) {

        if (samplingtables.size() == 0) {
            Toast.makeText(mContext, "没有可以上传的抽样单!", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<File> files = new ArrayList<>();
        for (int i = 0; i < samplingtables.size(); i++) {
            //search media files under this sampling,add them to files list
            String mediaFileName = samplingtables.get(i).getMedia_folder();
            File[] mediaFiles = new File(Util.getMediaFolder(mContext) + File.separator + mediaFileName).listFiles();
            if (mediaFiles != null) {
                for (int j = 0; j < mediaFiles.length; j++) {
                    files.add(mediaFiles[j]);
                }
            }
        }

        if (files.size() == 0) {
            uploadSamplings(samplingtables, taskName);
        } else {
            uploadIMG(files, samplingtables, taskName);
        }


    }

    /**
     * 上传抽样单
     *
     * @param samplingtables
     * @param taskName
     */
    public void uploadSamplings(final List<SAMPLINGTABLE> samplingtables, final String taskName) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(samplingtables.size());
        progressDialog.setProgress(0);
        progressDialog.setMessage(samplingtables.get(0).getShow_name() + "上传中...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String message = resultJson.getString(URLs.KEY_MESSAGE);
                    if (errorCode.equals(ReturnCode.Code0)) {//return code mean success
                        //save sid of server
                        try {
                            samplingtables.get(progressDialog.getProgress()).setSid_of_server(Long.valueOf(message));
                            samplingtables.get(progressDialog.getProgress()).setIs_uploaded(true);
                            samplingtables.get(progressDialog.getProgress()).setCheck_status(Constant.S_STATUS_CHECKING);

                            samplingtableDao.insertOrReplace(samplingtables.get(progressDialog.getProgress()));
                        } catch (NumberFormatException e) {
                            progressDialog.dismiss();
                            Toast.makeText(mContext, "上传出错" + s, Toast.LENGTH_SHORT).show();
                            Log.e(LogError, LogClass + "706,返回错误的sid");
                            Toast.makeText(mContext, "返回错误的sid", Toast.LENGTH_LONG).show();
                            return;
                        }

                        //judge if upload the last one
                        if (progressDialog.getProgress() != progressDialog.getMax() - 1) {// not the last sampling
                            progressDialog.setProgress(progressDialog.getProgress() + 1);
                            progressDialog.setMessage(samplingtables.get(progressDialog.getProgress()).getShow_name() + "上传中...");
                            Response.ErrorListener errorListener = new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    progressDialog.dismiss();
                                    Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                                    Log.e("uploadSamFail", volleyError.toString());
                                }
                            };
                            //send to api
                            StringRequest stringRequest = API.uploadSampling(this, errorListener
                                    , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
                                    , (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)
                                    , String.valueOf(samplingtables.get(progressDialog.getProgress()).getTaskID()),
                                    taskName, samplingtables.get(progressDialog.getProgress()).getSampling_content(),
                                    samplingtables.get(progressDialog.getProgress()).getShow_name(),
                                    String.valueOf(samplingtables.get(progressDialog.getProgress()).getSid_of_server()),
                                    samplingtables.get(progressDialog.getProgress()).getLatitude(),
                                    samplingtables.get(progressDialog.getProgress()).getLongitude(),
                                    samplingtables.get(progressDialog.getProgress()).getLocation_mode(),
                                    samplingtables.get(progressDialog.getProgress()).getSampling_unique_num(),
                                    samplingtables.get(progressDialog.getProgress()).getIs_make_up());
                            queue.add(stringRequest);

                        } else { //uploaded the last one
                            progressDialog.dismiss();
                            //samplingtableDao.insertOrReplaceInTx(samplingtables);
                            //Toast
                            Toast.makeText(mContext, "抽样单上传成功", Toast.LENGTH_SHORT).show();
                            //update data set of child adapter
                            ((WeixinActivityMain) mContext).notifyDoingChildListDataChanged();
                            //update data set of parent adapter
                            ((WeixinActivityMain) mContext).notifyDoingParentListDataChanged();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "上传出错" + s, Toast.LENGTH_SHORT).show();
                        new ReturnCode(mContext, errorCode, false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(mContext, "上传出错" + s, Toast.LENGTH_SHORT).show();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                Log.e("uploadSamFail", volleyError.toString());
            }
        };

        //send to api
        StringRequest stringRequest = API.uploadSampling(listener, errorListener
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)
                , String.valueOf(samplingtables.get(0).getTaskID()), taskName, samplingtables.get(0).getSampling_content(), samplingtables.get(0).getShow_name(),
                String.valueOf(samplingtables.get(0).getSid_of_server()), samplingtables.get(0).getLatitude(), samplingtables.get(0).getLongitude(),
                samplingtables.get(0).getLocation_mode(), samplingtables.get(0).getSampling_unique_num(), samplingtables.get(0).getIs_make_up());
        queue.add(stringRequest);

    }


    public void uploadIMG(final ArrayList<File> files, final List<SAMPLINGTABLE> samplingtables, final String taskName) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(files.size());
        progressDialog.setProgress(0);
        progressDialog.setMessage(files.get(0).getName() + "上传中...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Counter counter = new Counter();
        RequestBody body = null;
        if (files.get(counter.getCounter()).getName().endsWith(".jpg")) {
            body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""), RequestBody.create(null, (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)))
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""), RequestBody.create(null, (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)))
                    .addFormDataPart("upfile", files.get(counter.getCounter()).getName(), RequestBody.create(MediaType.parse("image/jpg"), files.get(counter.getCounter())))
                    .build();
        } else if (files.get(counter.getCounter()).getName().endsWith(".mp4")) {
            body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""), RequestBody.create(null, (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)))
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""), RequestBody.create(null, (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)))
                    .addFormDataPart("upfile", files.get(counter.getCounter()).getName(), RequestBody.create(MediaType.parse("video/mp4"), files.get(counter.getCounter())))
                    .build();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.unknownFileType), Toast.LENGTH_LONG).show();
            return;
        }

        final Request request = new Request.Builder()
                .url(UPLAODIMG)
                .post(body)
                .build();

        final OkHttpClient client = new OkHttpClient();

        final Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                WeixinActivityMain.instance.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    String s = response.body().string();
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String message = resultJson.getString(URLs.KEY_MESSAGE);

                    if (!errorCode.equals(ReturnCode.Code0)) {
                        WeixinActivityMain.instance.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    if (errorCode.equals(ReturnCode.Code0) && progressDialog.getProgress() == files.size() - 1) {  // The final file
                        WeixinActivityMain.instance.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                uploadSamplings(samplingtables, taskName);
                            }
                        });

                    } else {
                        counter.increase_one_step();
                        WeixinActivityMain.instance.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setProgress(progressDialog.getProgress() + 1);
                                progressDialog.setMessage(files.get(progressDialog.getProgress()).getName() + "上传中...");
                            }
                        });

                        RequestBody body = null;
                        if (files.get(counter.getCounter()).getName().endsWith(".jpg")) {
                            body = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""), RequestBody.create(null, (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)))
                                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""), RequestBody.create(null, (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)))
                                    .addFormDataPart("upfile", files.get(counter.getCounter()).getName(), RequestBody.create(MediaType.parse("image/jpg"), files.get(counter.getCounter())))
                                    .build();
                        } else if (files.get(counter.getCounter()).getName().endsWith(".mp4")) {
                            body = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"username\""), RequestBody.create(null, (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)))
                                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"password\""), RequestBody.create(null, (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)))
                                    .addFormDataPart("upfile", files.get(counter.getCounter()).getName(), RequestBody.create(MediaType.parse("video/mp4"), files.get(counter.getCounter())))
                                    .build();
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.unknownFileType), Toast.LENGTH_LONG).show();
                            return;
                        }

                        Request request = new Request.Builder()
                                .url(UPLAODIMG)
                                .post(body)
                                .build();

                        OkHttpClient client = new OkHttpClient();
                        client.newCall(request).enqueue(this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    WeixinActivityMain.instance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(mContext, mContext.getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        client.newCall(request).enqueue(callback);
    }


    /**
     * 单个上传文件
     *
     * @param files
     */
    /*
    public void uploadIMG(final ArrayList<File> files, final List<SAMPLINGTABLE> samplingtables, final String taskName) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        final RequestQueue mSingleQueue = Volley.newRequestQueue(mContext, new MultiPartStack());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(files.size());
        progressDialog.setProgress(0);
        progressDialog.setMessage(files.get(0).getName() + "上传中...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String message = resultJson.getString(URLs.KEY_MESSAGE);
                    if (errorCode.equals(ReturnCode.Code0)) {//connected
                        if (progressDialog.getProgress() != progressDialog.getMax() - 1) {
                            progressDialog.setProgress(progressDialog.getProgress() + 1);
                            progressDialog.setMessage(files.get(progressDialog.getProgress()).getName() + "上传中...");
                            Response.ErrorListener errorListener = new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    progressDialog.dismiss();
                                    Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                                    Log.e("uploadIMGFail", volleyError.toString());
                                }
                            };
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("username", (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE));
                            map.put("password", (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE));
                            ArrayList<File> theFile = new ArrayList<File>();
                            theFile.add(files.get(progressDialog.getProgress()));
                            MultipartRequest multiPartRequest = API.uploadFiles(this, errorListener, theFile, map);
                            mSingleQueue.add(multiPartRequest);
                        } else { //uploaded all files
                            progressDialog.dismiss();
                            uploadSamplings(samplingtables, taskName);
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "上传出错" + s, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                Log.e("uploadIMGFail", volleyError.toString());
            }
        };

        Map<String, String> map = new HashMap<String, String>();
        map.put("username", (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE));
        map.put("password", (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE));
        ArrayList<File> theFile = new ArrayList<File>();
        theFile.add(files.get(0));
        MultipartRequest multiPartRequest = API.uploadFiles(listener, errorListener, theFile, map);
        mSingleQueue.add(multiPartRequest);

    }
    */


}