package com.aj.collection.bean

import com.aj.collection.database.SAMPLINGTABLE
import com.bignerdranch.expandablerecyclerview.model.Parent

/**
 * ListItemTask是对ExpandableRecycleView中每一个组类的数据封装
 * Created by kevin on 17-4-25.
 * Mail: chewenkaich@gmail.com
 */
class TaskData(var childList: ArrayList<SAMPLINGTABLE>, var taskID: Long, var task_name: String, var task_letter: String,
               var is_finished: Boolean, var is_new_task: Boolean, var download_time: Long, var description: String):Parent<SAMPLINGTABLE> {
    override fun isInitiallyExpanded(): Boolean {
        return false
    }

    override fun getChildList(): MutableList<SAMPLINGTABLE> {
        return childList
    }
}