package com.aj.collection

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import cn.qqtheme.framework.picker.DatePicker
import com.aj.collection.activity.GatherActivity
import com.aj.collection.activity.tools.SheetProtocol
import com.aj.collection.bean.SheetCell
import org.jetbrains.anko.onClick
import org.json.JSONObject


/**
 * 日期选择单元格(type_date_select): 用于用于手动选择日期。单元格值、单元格是否可编辑属性对其无效
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
class TypeDateSelect(var mContext: Context, var sheetCell: SheetCell) : CellBaseAttributes() {

    /**
     * 获取打印的内容
     */
    override fun getPrintContent(): String {
        if (sheetCell.cell_printable == SheetProtocol().True && cell_printable?.isChecked?:false)
            return cell_name?.text.toString() + ":" + get_cell_value()
        else
            return ""
    }

    /**
     * 设置单元格为不可打印
     */
    override fun setCellNotPrinte() {
        cell_printable?.isChecked = false
        cell_printable?.isClickable = false
    }

    /**
     * 将内容填到单元格
     */
    override fun setFilledContent(content: String) {
        cell_value?.text = content
    }

    /**
     * 设置单元格为不可更改
     */
    override fun setCellDisable() {
        pickDate?.visibility = View.GONE
    }

    /**
     *获取单元格名称(cell_name)
     */
    override fun get_cell_name(): String {
        return sheetCell.cell_name
    }

    /**
     * 获取单元格类型(cell_type)
     */
    override fun get_cell_type(): String {
        return sheetCell.cell_type
    }

    /**
     * 获取单元格值(cell_value)
     */
    override fun get_cell_value(): String {
        return cell_value?.text?.toString()?:""
    }

    /**
     * 获取单元格是否可编辑(cell_editable)
     */
    override fun get_cell_editable(): String {
        return if (sheetCell.cell_editable == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 获取单元格是否为必填(cell_fill_required)
     */
    override fun get_cell_fill_required(): String {
        return if (sheetCell.cell_fill_required == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 获取单元格是否可打印(cell_printable)
     */
    override fun get_cell_printable(): String {
        return if (sheetCell.cell_printable == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 获取单元格是否默认勾选打印(cell_default_print)
     */
    override fun get_cell_default_print(): String {
        return if (sheetCell.cell_default_print == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 获取单元格可否被加样(cell_copyable)
     */
    override fun get_cell_copyable(): String {
        return if (sheetCell.cell_copyable == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 根据单元格内容生成Json
     */
    override fun getJsonContent(): JSONObject {
        // 将内容生成Json
        val json = JSONObject()
        json.put(SheetProtocol().CELL_NAME, get_cell_name())
        json.put(SheetProtocol().CELL_TYPE, get_cell_type())
        json.put(SheetProtocol().CELL_VALUE, get_cell_value())
        json.put(SheetProtocol().CELL_EDITABLE, get_cell_editable())
        json.put(SheetProtocol().CELL_FILL_REQUIRED, get_cell_fill_required())
        json.put(SheetProtocol().CELL_PRINTABLE, get_cell_printable())
        json.put(SheetProtocol().CELL_DEFAULT_PRINT, get_cell_default_print())
        json.put(SheetProtocol().CELL_COPYABLE, get_cell_copyable())
        return json
    }

    /**
     * 必填的内容是否已经填写
     */
    override fun isFilled(): Boolean {
        if (sheetCell.cell_fill_required == (SheetProtocol().False))
            return true
        else {
            return !selected_month.isEmpty() && !selected_year.isEmpty() && !selected_day.isEmpty()
        }

    }

    /**
     * 获取LinearLayout的界面
     */
    override fun getView(): LinearLayout {
        return linearLayout!!
    }

    var contentView: View? = null  // 设计的界面
    var linearLayout: LinearLayout? = null
    var cell_name: TextView? = null
    var cell_value: TextView? = null  // 用于显示日期
    var pickDate: Button? = null // 选择日期
    var cell_fill_required: TextView? = null
    var cell_printable: CheckBox? = null

    var selected_month = ""
    var selected_year = ""
    var selected_day = ""
    var isDatePickerOpen = false

    init {
        // 导入界面
        val mInflater: LayoutInflater = LayoutInflater.from(mContext)
        contentView = mInflater.inflate(R.layout.sheet_cell_date_select, null)
        // LinearLayout
        linearLayout = contentView!!.findViewById(R.id.cell_edit_text_linear_layout) as LinearLayout
        // 填写单元格的名字
        cell_name = contentView!!.findViewById(R.id.cell_name) as TextView
        cell_name!!.text = sheetCell.cell_name
        // 填写单元格的内容
        cell_value = contentView!!.findViewById(R.id.cell_value) as TextView
        // 定义按钮功能
        pickDate = contentView!!.findViewById(R.id.pickDate) as Button
        pickDate!!.text = "选择日期"
        var datePicker: DatePicker? = null
        if (GatherActivity::class.java.isInstance(mContext))
            datePicker = DatePicker(mContext as GatherActivity, DatePicker.YEAR_MONTH_DAY)
        pickDate!!.onClick {
            datePicker!!.setOnWheelListener(object : DatePicker.OnWheelListener {
                override fun onMonthWheeled(index: Int, month: String?) {
                    selected_month = month!!
                    cell_value!!.text = "$selected_year-$selected_month-$selected_day"
                }

                override fun onYearWheeled(index: Int, year: String?) {
                    selected_year = year!!
                    cell_value!!.text = "$selected_year-$selected_month-$selected_day"
                }

                override fun onDayWheeled(index: Int, day: String?) {
                    selected_day = day!!
                    cell_value!!.text = "$selected_year-$selected_month-$selected_day"
                }

            })
            if (isDatePickerOpen) {
                linearLayout!!.removeView(datePicker!!.contentView)
                isDatePickerOpen = false
                pickDate!!.text = "选择日期"
            } else {
                linearLayout!!.addView(datePicker!!.contentView)
                isDatePickerOpen = true
                pickDate!!.text = "完成选择"
            }
        }
        // 设置单元格可编辑状态(不受该属性影响)
        // ？@##￥￥%%%@#￥！
        // 设置单元格必填状态
        cell_fill_required = contentView!!.findViewById(R.id.cell_fill_required) as TextView
        if (sheetCell.cell_fill_required == (SheetProtocol().False))
            cell_fill_required!!.visibility = View.INVISIBLE
        // 设置单元格默认打印状态
        cell_printable = contentView!!.findViewById(R.id.cell_printable) as CheckBox
        cell_printable!!.setBackgroundResource(R.drawable.selector_checkbox_print)
        cell_printable!!.setButtonDrawable(ColorDrawable(Color.TRANSPARENT))
        if (sheetCell.cell_printable == (SheetProtocol().False))
            cell_printable!!.visibility = View.INVISIBLE
        cell_printable!!.isChecked = sheetCell.cell_default_print == (SheetProtocol().True)
    }
}