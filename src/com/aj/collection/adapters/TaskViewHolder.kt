package com.aj.collection.adapters

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.aj.collection.R
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder

/**
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */
class TaskViewHolder(itemView: View): GroupViewHolder(itemView){
    var layout:RelativeLayout? = null
    var isOpenArrow:ImageView? = null
    var taskTitle:TextView? = null
    var counterNotUploaded:TextView? = null
    var counterChecking:TextView? = null
    var counterPassed:TextView? = null
    var counterNotPassed:TextView? = null
    var counterNotUsed:TextView? = null
    var newFlag:TextView? = null

    init {
        layout = itemView.findViewById(R.id.relative_layout_task_item) as RelativeLayout
        isOpenArrow = itemView.findViewById(R.id.taskIfOpendImg) as ImageView
        taskTitle = itemView.findViewById(R.id.header_text) as TextView
        counterNotUploaded = itemView.findViewById(R.id.counter_not_uploaded) as TextView
        counterChecking = itemView.findViewById(R.id.counter_checking) as TextView
        counterPassed = itemView.findViewById(R.id.counter_passed) as TextView
        counterNotPassed = itemView.findViewById(R.id.counter_not_passed) as TextView
        counterNotUsed = itemView.findViewById(R.id.counter_not_used) as TextView
        newFlag = itemView.findViewById(R.id.newFlag) as TextView
    }

    override fun expand() {
        animateExpand()
    }

    override fun collapse() {
        animateCollapse()
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