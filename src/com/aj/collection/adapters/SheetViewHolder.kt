package com.aj.collection.adapters

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.aj.collection.R
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder

/**
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */
class SheetViewHolder(itemView: View) : ChildViewHolder(itemView) {
    var upperView: RelativeLayout? = null
    var sheetTitle: TextView? = null
    var sheetTitleDescription: TextView? = null
    var moreLayout: RelativeLayout? = null
    var uploadStatus: TextView? = null
    var saveStatus: TextView? = null

    var menuLayout: RelativeLayout? = null
    var edit: LinearLayout? = null
    var edit_img: TextView? = null
    var delete: LinearLayout? = null
    var delete_img: TextView? = null
    var upload: LinearLayout? = null
    var upload_img: TextView? = null
    var makeUp: LinearLayout? = null
    var makeUp_img: TextView? = null

    init {
        upperView = itemView.findViewById(R.id.childlist_ll) as RelativeLayout
        sheetTitle = itemView.findViewById(R.id.child_listview_title) as TextView
        sheetTitleDescription = itemView.findViewById(R.id.child_listview_describe) as TextView
        moreLayout = itemView.findViewById(R.id.child_LL_right_btn) as RelativeLayout
        uploadStatus = itemView.findViewById(R.id.child_listview_upload_status) as TextView
        saveStatus = itemView.findViewById(R.id.child_listview_save_status) as TextView

        menuLayout = itemView.findViewById(R.id.menuLayout) as RelativeLayout
        edit = itemView.findViewById(R.id.child_list_content_edit) as LinearLayout
        edit_img = itemView.findViewById(R.id.edit_img) as TextView
        delete = itemView.findViewById(R.id.child_list_content_delete) as LinearLayout
        delete_img = itemView.findViewById(R.id.delete_img) as TextView
        upload = itemView.findViewById(R.id.child_list_content_upload) as LinearLayout
        upload_img = itemView.findViewById(R.id.upload_img) as TextView
        makeUp = itemView.findViewById(R.id.child_list_content_makeup) as LinearLayout
        makeUp_img = itemView.findViewById(R.id.makeup_img) as TextView
    }

    fun setSheetTitle(title: String) {
        sheetTitle!!.text = title
    }

}