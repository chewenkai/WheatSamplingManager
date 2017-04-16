package com.aj.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.Constant;
import com.aj.activity.CollectionApplication;
import com.aj.activity.GatherActivity;
import com.aj.activity.WatchActivity;
import com.aj.bean.TaskInfo;
import com.aj.collection.R;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLE;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLE;
import com.aj.database.TEMPLETTABLEDao;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.library.ExpandableLayoutItem;
import com.library.ExpandableLayoutListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 15-10-6.
 */
public class DoneChildListAdapter extends ArrayAdapter {
    public DoneChildListAdapter doingChildListAdapter = this;
    protected LayoutInflater mInflater;
    private static final int mLayout = R.layout.child_listview_adapter_layout;
    private Context mContext;
    private ExpandableLayoutListView parentExpandListView;
    private ExpandableLayoutListView childExpandListView;
    private File templetDir, taskDir;
    private File[] templetFiles = new File[0], taskFils = new File[0];

    public long taskID;//任务名字 也就是/Task/鹤岗采样 or /Templet/鹤岗采样 文件夹下的那个文件夹 是此次任务的名字 一个任务中可能有很多抽样单模板

    public List<Object> dataSet;//record templet file and written templet file
    public ArrayList<Integer> templatePositionListSet = new ArrayList<Integer>();//record templet position of child listview

    private TaskInfo taskInfo;

    private int currentPosition = 0;

    private int count = 0;

    private int pageFlag;

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    public DoneChildListAdapter(Context context, long taskID, ExpandableLayoutListView parentExpandListView,
                                ExpandableLayoutListView childExpandListView, List<Object> dataSet,
                                int pageFlag) {

        super(context, mLayout, dataSet);//上下文环境/布局文件/填充布局文件数据
        mInflater = LayoutInflater.from(context);

        mContext = context;
        this.pageFlag=pageFlag;
        taskInfo = new TaskInfo(mContext,pageFlag);

        this.taskID = taskID;

        this.parentExpandListView = parentExpandListView;
        this.childExpandListView = childExpandListView;
        this.dataSet = dataSet;

        //database init
        daoSession =((CollectionApplication)((Activity)mContext).getApplication()).getDaoSession(mContext);
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
        ViewHolder viewHolder=null;

        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= getConvertView(viewHolder);
            viewHolder= (ViewHolder) convertView.getTag();
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }

        preferenceView(viewHolder,position);

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
        TextView child_listview_upload_status;
        TextView child_listview_save_status;
        //childlist content xml
        LinearLayout LL_edit;
        LinearLayout LL_delete;
        LinearLayout LL_upload;
        LinearLayout LL_makeup;

        ExpandableLayoutItem item;

        TextView edit_img,delete_img,upload_img,makeup_img;
        TextView edit_text,delete_text,upload_text,makeup_text;

        public RelativeLayout getmRelativeLayout() {
            return mRelativeLayout;
        }

        public TextView getChild_listview_upload_status() {
            return child_listview_upload_status;
        }

        public TextView getChild_listview_right_btn() {
            return child_listview_right_btn;
        }

        public TextView getChild_listview_title() {
            return child_listview_title;
        }

        public ImageView getChild_listview_title_img() {
            return child_listview_title_img;
        }

        public View getInflatedAllView() {
            return inflatedAllView;
        }

        public View getInflatedContentView() {
            return inflatedContentView;
        }

        public View getInflatedHeaderView() {
            return inflatedHeaderView;
        }

        RelativeLayout mRelativeLayout;
    }


    /**
     * 初始化ListView中的Item 讲它们保存在saveItemView这个ViewHolder集合中，用于getView的时候取出。解决getView中position错乱的问题
     *
     * @param
     */
    public View getConvertView(ViewHolder viewHolder) {
        View view = mInflater.inflate(mLayout, null);
        ExpandableLayoutItem item = (ExpandableLayoutItem) view.findViewById(R.id.row);

        RelativeLayout contentLayout = item.contentLayout;
        View inflatedContent = contentLayout.getChildAt(0);
        RelativeLayout headerLayout = item.headerLayout;
        View inflatedheader = headerLayout.getChildAt(0);

        final ViewHolder ItemView = new ViewHolder();
        //init child list inflate
        ItemView.inflatedHeaderView = inflatedheader;
        ItemView.inflatedContentView = inflatedContent;
        ItemView.inflatedAllView = view;
        //init child list head
        ItemView.child_listview_title = (TextView) inflatedheader.findViewById(R.id.child_listview_title);
        ItemView.child_listview_description=(TextView) inflatedheader.findViewById(R.id.child_listview_describe);
        ItemView.child_listview_upload_status = (TextView) inflatedheader.findViewById(R.id.child_listview_upload_status);
        ItemView.child_listview_save_status = (TextView) inflatedheader.findViewById(R.id.child_listview_save_status);
        ItemView.child_listview_right_btn = (TextView) inflatedheader.findViewById(R.id.child_listview_right_btn);
        ItemView.child_listview_title_img = (ImageView) inflatedheader.findViewById(R.id.child_listview_title_img);
        ItemView.mRelativeLayout = (RelativeLayout) inflatedheader.findViewById(R.id.childlist_ll);
        //init child list content
        ItemView.LL_edit = (LinearLayout) inflatedContent.findViewById(R.id.child_list_content_edit);
        ItemView.LL_delete = (LinearLayout) inflatedContent.findViewById(R.id.child_list_content_delete);
        ItemView.LL_upload = (LinearLayout) inflatedContent.findViewById(R.id.child_list_content_upload);
        ItemView.LL_makeup = (LinearLayout) inflatedContent.findViewById(R.id.child_list_content_makeup);

        ItemView.edit_img=(TextView)inflatedContent.findViewById(R.id.edit_img);
        ItemView.edit_text=(TextView)inflatedContent.findViewById(R.id.edit_text);
        ItemView.delete_img=(TextView)inflatedContent.findViewById(R.id.delete_img);
        ItemView.delete_text=(TextView)inflatedContent.findViewById(R.id.delete_text);
        ItemView.upload_img=(TextView)inflatedContent.findViewById(R.id.upload_img);
        ItemView.upload_text=(TextView)inflatedContent.findViewById(R.id.upload_text);
        ItemView.makeup_img=(TextView)inflatedContent.findViewById(R.id.makeup_img);
        ItemView.makeup_text=(TextView)inflatedContent.findViewById(R.id.makeup_text);

        view.setTag(ItemView);
        return view;
    }


    /**
     * 配置当前界面
     * @param ItemView
     * @param position
     */
    public void preferenceView(final ViewHolder ItemView ,int position){
        if (dataSet.get(position) instanceof TEMPLETTABLE) {// templete view 当前位置是模板

            TEMPLETTABLE templettable =(TEMPLETTABLE)dataSet.get(position);

            int[] indicatorNum=taskInfo.getUploadIndicatorNum(templettable.getTempletID());//get indicator number
            String templetName = templettable.getTemplet_name();//get templet name

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
            ItemView.child_listview_upload_status.setVisibility(View.VISIBLE);
            ItemView.child_listview_upload_status.setBackgroundDrawable(null);
            ItemView.child_listview_upload_status.setText("");

            RelativeLayout.LayoutParams saveStatusParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            saveStatusParam.addRule(RelativeLayout.CENTER_VERTICAL);
            saveStatusParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            saveStatusParam.setMargins(ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(10, mContext), ScreenUtils.dip2px(0, mContext));

            ItemView.child_listview_save_status.setVisibility(View.VISIBLE);
            ItemView.child_listview_save_status.setBackgroundResource(R.drawable.shape_upload_indicator);
            ItemView.child_listview_save_status.setTextColor(Color.parseColor("#ffffff"));
            ItemView.child_listview_save_status.setText("共" + indicatorNum[1] + "个"); //显示总数
            ItemView.child_listview_save_status.setLayoutParams(saveStatusParam);


            RelativeLayout.LayoutParams rightBtnParam = new RelativeLayout.LayoutParams(1,1);
            rightBtnParam.addRule(RelativeLayout.CENTER_VERTICAL);
            rightBtnParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rightBtnParam.setMargins(ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext));
            ItemView.child_listview_right_btn.setBackgroundDrawable(null);
            ItemView.child_listview_right_btn.setEnabled(false);
            ItemView.child_listview_right_btn.setVisibility(View.VISIBLE);
            ItemView.child_listview_right_btn.setLayoutParams(rightBtnParam);

            //最后确认布局居中
            RelativeLayout.LayoutParams centerVerticalParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(50, mContext));
            centerVerticalParam.addRule(RelativeLayout.CENTER_VERTICAL);
            ItemView.mRelativeLayout.setLayoutParams(centerVerticalParam);
            ItemView.mRelativeLayout.setTag(templettable);

            ItemView.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TEMPLETTABLE templettable = (TEMPLETTABLE) ItemView.mRelativeLayout.getTag();

                    Intent startForm = new Intent((Activity) mContext, GatherActivity.class);
                    startForm.putExtra("res", templettable.getTemplet_content());    //字符串
                    startForm.putExtra("templetID", templettable.getTempletID()); //文件名不包含后缀
                    startForm.putExtra("whichTask", templettable.getTaskID());    //哪个任务被点击了
                    startForm.putExtra("isMakeUp", false);
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

        } else {//written templet view   不在positionlist中的posiiton说明不是模板，是已填写的当前模板的采样记录

            SAMPLINGTABLE samplingtable =(SAMPLINGTABLE)dataSet.get(position);

            final String sampleName = samplingtable.getShow_name();

            /*显示已完成任务的图标form和小号title15  浅色title777777 暂时隐藏状态和按钮*/

            //左边的图片及其布局
            ItemView.child_listview_title_img.setImageResource(R.drawable.form);
            RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(20, mContext), ScreenUtils.dip2px(20, mContext));
            relLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relLayoutParams.setMargins(ScreenUtils.dip2px(15, mContext), ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext));
            ItemView.child_listview_title_img.setLayoutParams(relLayoutParams);

            //中间的文字及布局
            ItemView.child_listview_title.setText(sampleName);
            ItemView.child_listview_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            ItemView.child_listview_title.setTextColor(Color.parseColor("#666666"));

            //description
            ItemView.child_listview_description.setVisibility(View.GONE);

            //右侧的上传状态和保存状态及布局
            ItemView.child_listview_upload_status.setText("");
            ItemView.child_listview_upload_status.setBackgroundDrawable(null);
            RelativeLayout.LayoutParams uploadstatus_param = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext));
            uploadstatus_param.addRule(RelativeLayout.CENTER_VERTICAL);
            uploadstatus_param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            uploadstatus_param.setMargins(ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext));
            ItemView.child_listview_upload_status.setLayoutParams(uploadstatus_param);


            // 根据表格必填项有没有填完显示未保存和已保存
            ItemView.child_listview_save_status.setBackgroundDrawable(null);
            ItemView.child_listview_save_status.setTextColor(Color.parseColor("#666666"));
            long time=samplingtable.getSaved_time();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            ItemView.child_listview_save_status.setText(format.format(time));
            RelativeLayout.LayoutParams saveStatusParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            saveStatusParam.addRule(RelativeLayout.CENTER_VERTICAL);
            saveStatusParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            saveStatusParam.setMargins(ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext));
            ItemView.child_listview_save_status.setLayoutParams(saveStatusParam);


            //右边按钮显示按钮菜单 和布局设置
            RelativeLayout.LayoutParams rightBtnParam = new RelativeLayout.LayoutParams(1,1);
            rightBtnParam.addRule(RelativeLayout.CENTER_VERTICAL);
            rightBtnParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rightBtnParam.setMargins(ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext), ScreenUtils.dip2px(0, mContext));
            ItemView.child_listview_right_btn.setBackgroundDrawable(null);
            ItemView.child_listview_right_btn.setEnabled(false);
            ItemView.child_listview_right_btn.setVisibility(View.VISIBLE);
            ItemView.child_listview_right_btn.setLayoutParams(rightBtnParam);


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
                    Intent startForm = new Intent(mContext, WatchActivity.class);
                    startForm.putExtra("res", res);    //字符串
                    startForm.putExtra("sampleID", samplingtable.getId()); //文件名不包含后缀
                    startForm.putExtra("canUserEdit", false);//设置为不能修改
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



}