package com.aj.collection.bean

import android.content.Context
import android.support.v7.widget.AppCompatRadioButton
import android.view.View
import android.widget.LinearLayout

/**
 * Created by kevin on 17-4-24.
 * Mail: chewenkaich@gmail.com
 */
class RadiosView(val groupView: LinearLayout, val content: List<String>, val mContext: Context) {
    var radios = ArrayList<AppCompatRadioButton>() // 用于储存多个单选选项
    init {
        for (choice in content){
            val radio = AppCompatRadioButton(mContext)
            radio.text = choice
            groupView.addView(radio)
            radios.add(radio)
            radio.setOnClickListener {
                clearRadios()
                radio.isChecked = true
            }
        }
    }

    /**
     * 清除所有一级选项
     */
    fun clearRadios() {
        for (radio in radios)
            radio.isChecked = false
    }

    /**
     * 获取选中的选项
     */
    fun getSelectedRadio(): AppCompatRadioButton? {
        for (radio in radios) {
            if (radio.isChecked)
                return radio
        }
        return null
    }

    /**
     * 设置一级选项已选中的选项
     */
    fun setSelectedRadio(radioValue: String) {
        for (radio in radios) {
            if (radio.text.toString().equals(radioValue)) {
                radio.isChecked = true
                return
            }
        }
    }

    /**
     * 设置选项为不可点击
     */
    fun setRadiosDisable(){
        for (radio in radios)
            radio.isEnabled = false
    }
    /**
     * 隐藏选项
     */
    fun hiddenRadios(){
        for (radio in radios)
            radio.visibility = View.GONE
    }

    /**
     * 显示选项
     */
    fun showRadios(){
        for (radio in radios)
            radio.visibility = View.VISIBLE
    }

}