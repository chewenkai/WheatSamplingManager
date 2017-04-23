package com.aj.collection.adapters

import android.annotation.SuppressLint
import com.aj.collection.bean.Sheet
import com.aj.collection.bean.Task
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup


@SuppressLint("ParcelCreator")
/**
 * TaskData是对ExpandableRecycleView中每一个组类的数据封装，其中title是第一层显示的名字，items是该名字下的第二层数据
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */

class TaskData(items: List<Sheet>, var taskID: Long, var task_name: String, var task_letter: String,
               var is_finished: Boolean, var is_new_task: Boolean, var download_time: Long, var description: String) : ExpandableGroup<Sheet>(task_name, items) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Task) return false

        return task_name == (o as TaskData).task_name
    }

    override fun hashCode(): Int {
        return task_name.hashCode()
    }
}