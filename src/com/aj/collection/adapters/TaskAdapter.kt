package com.aj.collection.adapters

import android.app.Activity
import android.content.Context
import android.support.annotation.UiThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aj.collection.R
import com.aj.collection.activity.CollectionApplication
import com.aj.collection.bean.TaskData
import com.aj.collection.database.*
import com.android.volley.RequestQueue
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter
import org.jetbrains.annotations.NotNull


/**
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */
class TaskAdapter(val mContext: Context, @NotNull var taskList: ArrayList<TaskData>) : ExpandableRecyclerAdapter<TaskData, SAMPLINGTABLE, TaskViewHolder, SheetViewHolder>(taskList) {

    private var daoSession: DaoSession? = null
    private var taskinfoDao: TASKINFODao? = null
    private var templettableDao: TEMPLETTABLEDao? = null
    private var samplingtableDao: SAMPLINGTABLEDao? = null
    internal var queue: RequestQueue? = null

    init {
        //database init
        daoSession = ((mContext as Activity).application as CollectionApplication).getDaoSession(mContext)
        taskinfoDao = daoSession?.getTASKINFODao()
        templettableDao = daoSession?.getTEMPLETTABLEDao()
        samplingtableDao = daoSession?.getSAMPLINGTABLEDao()
        queue = (mContext.application as CollectionApplication).requestQueue
    }

    @UiThread
    override fun onCreateChildViewHolder(parentViewGroup: ViewGroup, viewType: Int): SheetViewHolder {
        var recipeView: View? = null
        when (viewType) {
            TYPE_TEMPLATE -> recipeView = LayoutInflater.from(mContext).inflate(R.layout.child_listview_header_template, parentViewGroup, false)
            TYPE_SHEET -> recipeView = LayoutInflater.from(mContext).inflate(R.layout.child_listview_solitary_sheet, parentViewGroup, false)
        }
        return SheetViewHolder(mContext, recipeView!!, viewType)
    }

    @UiThread
    override fun onBindChildViewHolder(childViewHolder: SheetViewHolder, parentPosition: Int, childPosition: Int, child: SAMPLINGTABLE) {
        childViewHolder.onBind(child)
    }

    @UiThread
    override fun onCreateParentViewHolder(parentViewGroup: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.parent_listview_header, parentViewGroup, false)
        return TaskViewHolder(mContext, view)
    }

    @UiThread
    override fun onBindParentViewHolder(parentViewHolder: TaskViewHolder, parentPosition: Int, parent: TaskData) {
        parentViewHolder.onBind(parent)
    }

    override fun getParentViewType(parentPosition: Int): Int {
        return TYPE_TASK
    }

    override fun getChildViewType(parentPosition: Int, childPosition: Int): Int {
        if (taskList[parentPosition].childList[childPosition].id == -1L) {
            return TYPE_TEMPLATE
        } else {
            return TYPE_SHEET
        }
    }

    override fun isParentViewType(viewType: Int): Boolean {
        return viewType == TYPE_TASK
    }

    companion object {
        val TYPE_TASK = 3
        val TYPE_TEMPLATE = 4
        val TYPE_SHEET = 5
    }


}