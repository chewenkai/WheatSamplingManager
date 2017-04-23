package com.aj.collection.adapters

import android.view.View
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.aj.collection.R
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder


/**
 * 模板在可展开循环界面中的UI
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */
class SheetTemplateViewHolder(itemView: View) : ChildViewHolder(itemView) {
    val templateLayout:RelativeLayout
    val templateTitle: TextView
    val uploadIndicator: TextView
    val uploadAllSheetsLayout: RelativeLayout

    init {
        templateLayout = itemView.findViewById(R.id.childlist_ll) as RelativeLayout
        templateTitle = itemView.findViewById(R.id.child_listview_title) as TextView
        uploadIndicator = itemView.findViewById(R.id.child_listview_upload_status) as TextView
        uploadAllSheetsLayout = itemView.findViewById(R.id.child_LL_right_btn) as RelativeLayout
    }

    fun setTemplateTitle(title: String){
        templateTitle.text = title
    }

    fun setUploadIndicator(uploadText: String){
        uploadIndicator.text = uploadText
    }

    fun expand() {
        animateExpand()
    }

    fun collapse() {
        animateCollapse()
    }

    private fun animateExpand() {
        val rotate = RotateAnimation(360f, 180f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
        rotate.setDuration(300)
        rotate.setFillAfter(true)
        uploadIndicator.setAnimation(rotate)
    }

    private fun animateCollapse() {
        val rotate = RotateAnimation(180f, 360f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
        rotate.setDuration(300)
        rotate.setFillAfter(true)
        uploadIndicator.setAnimation(rotate)
    }
}