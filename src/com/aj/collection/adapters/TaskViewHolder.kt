package com.aj.collection.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.aj.Constant
import com.aj.collection.R
import com.aj.collection.activity.CollectionApplication
import com.aj.collection.activity.TaskDescriptionActivity
import com.aj.collection.bean.TaskData
import com.aj.collection.database.*
import com.android.volley.RequestQueue
import com.bignerdranch.expandablerecyclerview.ParentViewHolder
import org.jetbrains.anko.onClick
import org.jetbrains.anko.toast

/**
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */

class TaskViewHolder(val mContext:Context, itemView: View): ParentViewHolder<TaskData, SAMPLINGTABLE>(itemView){
    var layout:RelativeLayout? = null
    var isOpenArrow:ImageView? = null
    var taskTitle:TextView? = null
    var counterNotUploaded:TextView? = null
    var counterChecking:TextView? = null
    var counterPassed:TextView? = null
    var counterNotPassed:TextView? = null
    var counterNotUsed:TextView? = null
    var taskDescription:TextView? =null
    var newFlag:TextView? = null

    private var daoSession: DaoSession? = null
    private var taskinfoDao: TASKINFODao? = null
    private var templettableDao: TEMPLETTABLEDao? = null
    private var samplingtableDao: SAMPLINGTABLEDao? = null
    internal var queue: RequestQueue? = null

    init {
        layout = itemView.findViewById(R.id.relative_layout_task_item) as RelativeLayout
        isOpenArrow = itemView.findViewById(R.id.taskIfOpendImg) as ImageView
        taskTitle = itemView.findViewById(R.id.header_text) as TextView
        counterNotUploaded = itemView.findViewById(R.id.counter_not_uploaded) as TextView
        counterChecking = itemView.findViewById(R.id.counter_checking) as TextView
        counterPassed = itemView.findViewById(R.id.counter_passed) as TextView
        counterNotPassed = itemView.findViewById(R.id.counter_not_passed) as TextView
        counterNotUsed = itemView.findViewById(R.id.counter_not_used) as TextView
        taskDescription = itemView.findViewById(R.id.task_description) as TextView
        newFlag = itemView.findViewById(R.id.newFlag) as TextView
        //database init
        daoSession = ((mContext as Activity).application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession?.taskinfoDao
        templettableDao = daoSession?.templettableDao
        samplingtableDao = daoSession?.samplingtableDao
        queue = (mContext.application as CollectionApplication).requestQueue
    }

    fun onBind(parent: TaskData){
        val taskID = parent.taskID
        // 查询任务
        val tasks = taskinfoDao?.queryBuilder()?.where(TASKINFODao.Properties.TaskID.eq(taskID))?.list()
        if (tasks?.size !=1){
            mContext.toast("找不到任务")
            return
        }
        val task = tasks[0]
        //New字符相关
        if (parent.is_new_task)
            newFlag?.visibility = (View.VISIBLE)
        else
            newFlag?.visibility = (View.GONE)

        //status
        val notUplaodedSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Is_uploaded.eq(false))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
        val checkingSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_CHECKING))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
        val passedSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_PASSED))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
        val notPassedSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_NOT_PASS))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()
        val notUsedSampling = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.TaskID.eq(taskID),
                SAMPLINGTABLEDao.Properties.Check_status.eq(Constant.S_STATUS_NOT_USED))?.orderAsc(SAMPLINGTABLEDao.Properties.Id)?.list()

        taskTitle?.text = parent.task_name
        counterNotUploaded?.text = notUplaodedSampling?.size.toString() + "个"
        counterChecking?.text = checkingSampling?.size.toString() + "个"
        counterPassed?.text = passedSampling?.size.toString() + "个"
        counterNotPassed?.text = notPassedSampling?.size.toString() + "个"
        counterNotUsed?.text = notUsedSampling?.size.toString() + "个"

        // 任务详情
        taskDescription?.onClick {
            val intent = Intent(mContext, TaskDescriptionActivity::class.java)
            intent.putExtra(Constant.TASK_DES_EXTRA, task.description)
            mContext.startActivity(intent)
        }
    }

    override fun onExpansionToggled(expanded: Boolean) {
        if (expanded)
            animateCollapse()
        else
            animateExpand()
        super.onExpansionToggled(expanded)
    }

    private fun animateExpand() {
        val rotate = RotateAnimation(0f, 90f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.setDuration(300)
        rotate.setFillAfter(true)
        isOpenArrow?.setAnimation(rotate)
    }

    private fun animateCollapse() {
        val rotate = RotateAnimation(90f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.setDuration(300)
        rotate.setFillAfter(true)
        isOpenArrow?.setAnimation(rotate)
    }
}