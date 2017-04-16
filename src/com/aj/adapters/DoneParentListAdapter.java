package com.aj.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aj.activity.CollectionApplication;
import com.aj.bean.TaskInfo;
import com.aj.collection.R;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLE;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFO;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLE;
import com.aj.database.TEMPLETTABLEDao;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.library.ExpandableLayoutItem;
import com.library.ExpandableLayoutListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 15-10-6.
 */
public class DoneParentListAdapter extends ArrayAdapter<ParentListItem> {
    private List<ParentListItem> adapterDataSet;
    protected LayoutInflater mInflater;
    private static final int mLayout = R.layout.parent_done_listview_adapter_layout;
    private Context mContext;
    private ExpandableLayoutListView parentListView;
    int thePosition = -1;
    private TaskInfo taskInfo;
    private int pageFlag;

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    public DoneParentListAdapter(Context context, List<ParentListItem> items, ExpandableLayoutListView parentListView, int pageFlag) {

        super(context, mLayout, items);//上下文环境/布局文件/填充布局文件数据
        mInflater = LayoutInflater.from(context);
        adapterDataSet = items;
        this.mContext = context;
        this.parentListView = parentListView;
        this.pageFlag = pageFlag;
        taskInfo = new TaskInfo(mContext, pageFlag);

        //database init
        daoSession =((CollectionApplication)((Activity)mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();
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
        getHeadImg(holder.item).setImageResource(R.drawable.taskcompleted);
        getHeadText(holder.item).setText(getItem(position).getTitle());

        holder.item.headerLayout.setTag(position);
//        holder.item.contentLayout.setTag(-1);
        holder.item.headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (int) holder.item.headerLayout.getTag();
                TASKINFO taskinfo = taskinfoDao.queryBuilder().
                        where(TASKINFODao.Properties.Is_finished.eq(true)).
                        orderAsc(TASKINFODao.Properties.TaskID).
                        list().get(position);
                parentListView.expandChildView(position);
                parentListView.currentExpandableLayout.setTaskID(taskinfo.getTaskID());

                initChildListView(parentListView, taskinfo.getTaskID(), pageFlag);

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
    public DoneChildListAdapter doingChildrenAdapter;
    List<Object> dataSet = new ArrayList<>();

    /**
     * 初始化用于显示采样单的listview
     */
    public void initChildListView(final ExpandableLayoutListView parentExpandListView, long taskID, int pageFlag) {
//database init
        daoSession =((CollectionApplication)((Activity)mContext).getApplication()).getDaoSession(mContext);
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
                    where(SAMPLINGTABLEDao.Properties.TempletID.eq(templettable.getTempletID())).
                    orderAsc(SAMPLINGTABLEDao.Properties.Id).list();
            for (int j = 0; j < samplingtables.size(); j++) {
                dataSet.add(samplingtables.get(j));
            }
        }


        //init adapter
        doingChildrenAdapter = new DoneChildListAdapter(mContext, taskID, parentExpandListView, doingChildlistView,
                dataSet, pageFlag);

        doingChildlistView.setAdapter(doingChildrenAdapter);

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
//            //调整listview的高度
//            int Height = 0;//外层listviewitem的高度
//            for (int i = 0; i < ((DoingParentListAdapter) expandList.getAdapter()).getCount(); i++) {
//                View listItem = ((DoingParentListAdapter) expandList.getAdapter()).getView(i, null, expandList);
//                listItem.measure(0, 0);
//                Height += listItem.getMeasuredHeight();
//                if (Height > ScreenUtils.dip2px(100, mContext)) {
//                    break;
//                }
//            }
//
//            int maxheight = expandList.getMeasuredHeight() - Height;
//
//            for (int i = 0; i < adapter.getCount(); i++) {
//                View listItem = adapter.getView(i, null, childlistview);
////                if(listItem==null)
////                    continue;
//                listItem.measure(0, 0);
//                totalHeight += listItem.getMeasuredHeight();
//                if (totalHeight >= maxheight) {
//                    totalHeight = maxheight;
//                    break;
//                }
//            }
//            totalHeight = totalHeight + (childlistview.getDividerHeight() * (adapter.getCount() - 1));
            if(doingChildrenAdapter.getCount()==0)
                return 0;
            View listItem = getView(parentListView.currentPosition, null, parentListView);
            if (listItem == null)
                return parentListView.getMeasuredHeight();

            listItem.measure(0, 0);
            totalHeight = parentListView.getMeasuredHeight() - listItem.getMeasuredHeight();

        }
        return totalHeight;
    }

    /**
     * 刷新子view的高度
     */
    public void refreshExpandableItemHeight() {
        ViewGroup.LayoutParams params = doingChildlistView.getLayoutParams();
        params.height = getTotalHeightofItem();
        doingChildlistView.setLayoutParams(params);
    }

//    /**
//     * 更新已上传的任务数
//     */
//    public void updateRightCommitButtonText(String taskName, ExpandableLayoutItem mExpandableLayoutItem) {
//        TextView rightText = getHeadRightCommitButton(mExpandableLayoutItem);
//        int[] rightCommitButtonNum = taskInfo.getRightCommitButtonNum(taskName);
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

}