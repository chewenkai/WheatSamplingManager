package com.aj.collection.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aj.Constant;
import com.aj.WeixinActivityMain;
import com.aj.collection.activity.CollectionApplication;
import com.aj.collection.bean.TaskInfo;
import com.aj.collection.R;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLE;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFO;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLE;
import com.aj.database.TEMPLETTABLEDao;
import com.android.volley.RequestQueue;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.library.ExpandableLayoutItem;
import com.library.ExpandableLayoutListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 15-10-6.
 */
public class DoingParentListAdapter extends ArrayAdapter<ParentListItem> {
    public List<ParentListItem> adapterDataSet;
    protected LayoutInflater mInflater;
    private static final int mLayout = R.layout.parent_listview_adapter_layout;
    private Context mContext;
    private CollectionApplication application;
    private ExpandableLayoutListView parentListView;
    private TaskInfo taskInfo;
    private int pageFlag;

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    RequestQueue queue;

    public DoingParentListAdapter(Context context, List<ParentListItem> items, ExpandableLayoutListView parentListView, int pageFlag,CollectionApplication application) {

        super(context, mLayout, items);//上下文环境/布局文件/填充布局文件数据
        mInflater = LayoutInflater.from(context);
        adapterDataSet = items;
        this.mContext = context;
        this.parentListView = parentListView;
        this.pageFlag = pageFlag;
        taskInfo = new TaskInfo(mContext, pageFlag);

        //database init
        daoSession = ((CollectionApplication) ((Activity) mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();

        this.application=application;
        this.queue = application.getRequestQueue(); //init Volley
    }

    @Override
    public int getCount() {
        return adapterDataSet.size();
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {

//        TextView title;
//        TextView rightText;
//        ImageView leftImg;

//        final ExpandableLayoutItem item;
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(mLayout, null);

            holder.item = (ExpandableLayoutItem) convertView.findViewById(R.id.row);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        holder = new ViewHolder();
//        convertView = mInflater.inflate(mLayout, null);
//        holder.item = (ExpandableLayoutItem) convertView.findViewById(R.id.row);
//        getHeadRightText(holder.item).setText(getItem(position).getmRightText());

        getHeadText(holder.item).setText(getItem(position).getTitle());

        //process new flag
        TASKINFO taskinfo = taskinfoDao.queryBuilder().
                where(TASKINFODao.Properties.Is_finished.notEq(true)).
                orderAsc(TASKINFODao.Properties.TaskID).
                list().get(position);
        if (taskinfo.getIs_new_task())
            getHeadRightText(holder.item).setVisibility(View.VISIBLE);
        else
            getHeadRightText(holder.item).setVisibility(View.GONE);

        //status
        List notUplaodedSampling = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskinfo.getTaskID()), SAMPLINGTABLEDao.Properties.Is_uploaded.eq(false)).
                orderAsc(SAMPLINGTABLEDao.Properties.Id).list();

        List checkingSampling = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskinfo.getTaskID()), SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_CHECKING)).
                orderAsc(SAMPLINGTABLEDao.Properties.Id).list();

        List passedSampling = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskinfo.getTaskID()), SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_PASSED)).
                orderAsc(SAMPLINGTABLEDao.Properties.Id).list();

        List notPassedSampling = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskinfo.getTaskID()), SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_NOT_PASS)).
                orderAsc(SAMPLINGTABLEDao.Properties.Id).list();

        List notUsedSampling = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskinfo.getTaskID()), SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_NOT_USED)).
                orderAsc(SAMPLINGTABLEDao.Properties.Id).list();

        getCountNotUploaded(holder.item).setText(notUplaodedSampling.size() + "个");
        getCountChecking(holder.item).setText(checkingSampling.size() + "个");
        getCountPassed(holder.item).setText(passedSampling.size() + "个");
        getCountNotPassed(holder.item).setText(notPassedSampling.size() + "个");
        getCountNotUsed(holder.item).setText(notUsedSampling.size() + "个");
//        getHeadRightCommitButton(holder.item).setText(getItem(position).getmRightText());
//        if (getItem(position).getmRightText().equals(mContext.getResources().getString(R.string.commit_task))) {
//            getHeadRightCommitButton(holder.item).setEnabled(true);
//            getHeadRightCommitButton(holder.item).setBackgroundResource(R.drawable.selector_taskcommit_style);
//            getHeadRightCommitButton(holder.item).setTextColor(Color.parseColor("#3ba61d"));
//        } else {
//            getHeadRightCommitButton(holder.item).setEnabled(false);
//            getHeadRightCommitButton(holder.item).setBackgroundResource(R.drawable.shape_taskcommit_pressed);
//            getHeadRightCommitButton(holder.item).setTextColor(Color.parseColor("#A0A0A0"));
//        }
//        // parent list right button > commit task
//        getHeadRightCommitButton(holder.item).setTag(position);
//        getHeadRightCommitButton(holder.item).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //update taskinfo >isfinished==true
//
//                int position = (int) getHeadRightCommitButton(holder.item).getTag();
//                finishTask(position);
//
//            }
//        });
        getRefreshButton(holder.item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((WeixinActivityMain) mContext).updateTaskStatus(true);
                ((WeixinActivityMain) mContext).updateSamplingStatus(true);

            }
        });

        holder.item.headerLayout.setTag(position);
//        holder.item.contentLayout.setTag(-1);
        holder.item.headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (int) holder.item.headerLayout.getTag();
                TASKINFO taskinfo = taskinfoDao.queryBuilder().
                        where(TASKINFODao.Properties.Is_finished.notEq(true)).
                        orderAsc(TASKINFODao.Properties.TaskID).
                        list().get(position);
                parentListView.expandChildView(position);
                parentListView.currentExpandableLayout.setTaskID(taskinfo.getTaskID());

                initChildListView(parentListView, taskinfo.getTaskID(), pageFlag);
                taskinfo.setIs_new_task(false);
                taskinfoDao.insertOrReplace(taskinfo);
                getHeadRightText(holder.item).setVisibility(View.GONE);

                ((WeixinActivityMain) mContext).refreshBadgeView1();
            }
        });

        return convertView;
    }

    public class ViewHolder {
        ExpandableLayoutItem item;
    }

    public TextView getHeadText(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;

        return ((TextView) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.header_text));
    }

    public ImageView getHeadImg(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;
        return ((ImageView) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.taskIfOpendImg));
    }

    public TextView getHeadRightText(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;
        return ((TextView) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.newFlag));
    }

    public TextView getCountNotUploaded(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;
        return ((TextView) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.counter_not_uploaded));
    }

    public TextView getCountNotUsed(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;
        return ((TextView) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.counter_not_used));
    }

    //textview show samplings which are checking
    public TextView getCountChecking(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;
        return ((TextView) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.counter_checking));
    }

    //textview show samplings which passed
    public TextView getCountPassed(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;
        return ((TextView) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.counter_passed));
    }

    //textview show samplings which not passed
    public TextView getCountNotPassed(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;
        return ((TextView) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.counter_not_passed));
    }

    //get the button in the right:refresh task and sampling status
    public LinearLayout getRefreshButton(ExpandableLayoutItem mExpandableLayoutItem) {
        if (mExpandableLayoutItem.headerLayout == null)
            return null;
        return ((LinearLayout) mExpandableLayoutItem.headerLayout.getChildAt(0).findViewById(R.id.LL_refreshBTN));
    }

    public void setTaskClosedImg(ExpandableLayoutItem mExpandableLayoutItem) {
        getHeadImg(mExpandableLayoutItem).setImageResource(R.drawable.taskclosed);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(20, mContext), ScreenUtils.dip2px(20, mContext));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMargins(ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext),
                ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext));
        getHeadImg(mExpandableLayoutItem).setLayoutParams(params);
    }

    public void setTaskOpenedImg(ExpandableLayoutItem mExpandableLayoutItem) {
        getHeadImg(mExpandableLayoutItem).setImageResource(R.drawable.taskopened);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ScreenUtils.dip2px(20, mContext), ScreenUtils.dip2px(20, mContext));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMargins(ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext),
                ScreenUtils.dip2px(5, mContext), ScreenUtils.dip2px(5, mContext));
        getHeadImg(mExpandableLayoutItem).setLayoutParams(params);
    }

    public ExpandableLayoutListView doingChildlistView;
    public DoingChildListAdapter doingChildrenAdapter;

    List<Object> dataSet = new ArrayList<>();

    /**
     * 初始化用于显示采样单的listview
     */
    public void initChildListView(final ExpandableLayoutListView parentExpandListView, long taskID, int pageFlag) {
        //database init
        daoSession = ((CollectionApplication) ((Activity) mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();

        doingChildlistView = (ExpandableLayoutListView) parentExpandListView.currentExpandableLayout.contentLayout.getChildAt(0).findViewById(R.id.child_listview);
        doingChildlistView.isParentListView = false;
        doingChildlistView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        //repare data ----> fileDataSet:all file need to show. templatePositionListSet:all templet postition in fileDataSet
        //read database .get all templet and samples belong to the task
        if (!dataSet.isEmpty())
            dataSet.removeAll(dataSet);

        List<TEMPLETTABLE> templettables = templettableDao.queryBuilder().
                where(TEMPLETTABLEDao.Properties.TaskID.eq(taskID)).
                orderAsc(TEMPLETTABLEDao.Properties.TempletID).list();

        for (int i = 0; i < templettables.size(); i++) {
            TEMPLETTABLE templettable = templettables.get(i);
            if (templettable == null)
                continue;
            dataSet.add(templettable);
            List<SAMPLINGTABLE> samplingtables = samplingtableDao.queryBuilder().
                    where(SAMPLINGTABLEDao.Properties.TempletID.eq(templettable.getTempletID()),
                            SAMPLINGTABLEDao.Properties.Check_status.notEq(Constant.S_STATUS_DELETE)).
                    orderAsc(SAMPLINGTABLEDao.Properties.Id).list();
            for (int j = 0; j < samplingtables.size(); j++) {
                dataSet.add(samplingtables.get(j));
            }
        }

        //init adapter
        doingChildrenAdapter = new DoingChildListAdapter(mContext, taskID, parentExpandListView, doingChildlistView,
                dataSet, pageFlag,application);

        doingChildlistView.setAdapter(doingChildrenAdapter);

//        } else {
//            //refreshChildListData();
//        }
    }

    /**
     * 计算子Item的高度
     *
     * @return
     */
    public int getTotalHeightofItem() {
        //调整listview的高度
        int totalHeight = 0;
        if (parentListView.currentExpandableLayout.isOpened()) {

            if (getCount() == 0 && parentListView.currentPosition == 0)
                return 0;

            View listItem;

            try {

                if (parentListView.currentPosition >= getCount())
                    listItem = getView(getCount() - 1, null, parentListView);
                else if (parentListView.currentPosition < 0)
                    listItem = getView(0, null, parentListView);
                else
                    listItem = getView(parentListView.currentPosition, null, parentListView);

                if (listItem == null)
                    return parentListView.getMeasuredHeight();

            }catch (ArrayIndexOutOfBoundsException e){
                return 0;
            }

            listItem.measure(0, 0);
            totalHeight = parentListView.getMeasuredHeight() - listItem.getMeasuredHeight();

        }
        return totalHeight;
    }

    /**
     * 刷新子view的高度
     */
    public void refreshExpandableItemHeight() {
        if (doingChildlistView != null) {
            ViewGroup.LayoutParams params = doingChildlistView.getLayoutParams();
            params.height = getTotalHeightofItem();
            doingChildlistView.setLayoutParams(params);
        }
    }

//    /**
//     * 更新已上传的任务数
//     */
//    public void updateRightCommitButtonText(long taskID, ExpandableLayoutItem mExpandableLayoutItem) {
//        TextView rightText = getHeadRightCommitButton(mExpandableLayoutItem);
//        int[] rightCommitButtonNum = taskInfo.getRightCommitButtonNum(taskID);
//        if (rightCommitButtonNum[0] == rightCommitButtonNum[1] && rightCommitButtonNum[0] != 0) {
//            rightText.setText(R.string.commit_task);
//            rightText.setEnabled(true);
//            rightText.setBackgroundResource(R.drawable.selector_taskcommit_style);
//            rightText.setTextColor(Color.parseColor("#3ba61d"));
//        } else {
//            rightText.setText("上传" + rightCommitButtonNum[0] + "/模板" + rightCommitButtonNum[1]);
//            rightText.setEnabled(false);
//            rightText.setBackgroundResource(R.drawable.shape_taskcommit_pressed);
//            rightText.setTextColor(Color.parseColor("#A0A0A0"));
//        }
//    }

//    public void finishTask(final int position) {
//        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
//        progressDialog.setMessage("正在提交任务...");
//        progressDialog.show();
//
//        final TASKINFO taskinfo = taskinfoDao.queryBuilder()
//                .where(TASKINFODao.Properties.Is_finished.notEq(true)).
//                        orderAsc(TASKINFODao.Properties.TaskID)
//                .list().get(position);
//
//        Response.Listener<String> listener = new Response.Listener<String>() {
//            @Override
//            public void onResponse(String s) {
//                progressDialog.dismiss();
//                try {
//                    JSONObject resultJson = new JSONObject(s);
//                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
//                    String message = resultJson.getString(URLs.KEY_MESSAGE);
//                    if (errorCode.equals(ReturnCode.Code0)) {//connected
//                        taskinfo.setIs_finished(true);
//                        taskinfoDao.insertOrReplace(taskinfo);
//
//                        if (parentListView.currentExpandableLayout != null)
//                            parentListView.currentExpandableLayout.hide();
//
//                        ((WeixinActivityMain) mContext).notifyDoingChildListDataChanged();
//                        ((WeixinActivityMain) mContext).notifyDoingParentListDataChanged();
//                        Toast.makeText(mContext, "任务已移到完成标签中", Toast.LENGTH_LONG).show();
//                    } else if (errorCode.equals(ReturnCode.USERNAME_OR_PASSWORD_INVALIDE)) {
//                        Toast.makeText(mContext, ReturnCode.USERNAME_OR_PASSWORD_INVALIDE_STRING, Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(mContext, LoginActivity.class);
//                        mContext.startActivity(intent);
//                    } else {
//                        Toast.makeText(mContext, "未知错误码" + errorCode, Toast.LENGTH_LONG).show();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        Response.ErrorListener errorListener = new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                progressDialog.dismiss();
//                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
//                Log.e("finishTaskFial", volleyError.toString());
//            }
//        };
//        StringRequest stringRequest = API.finishTask(listener, errorListener
//                , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
//                , (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)
//                , String.valueOf(taskinfo.getTaskID()));
//        queue.add(stringRequest);
//    }

}