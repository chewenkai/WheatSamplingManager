package com.aj.collection

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatRadioButton
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.aj.collection.bean.SheetCell
import com.aj.collection.activity.tools.SheetProtocol
import org.json.JSONObject

/**
 * 多选单元格(type_multi_select): 用于进行多项选择，如单元格名称为“喷洒农药名称”，可能喷洒多种农药，单元格值为“A药,B药”。单元格是否可编辑属性对其无效
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
class TypeMultiSelect(var mContext: Context, var sheetCell: SheetCell): CellBaseAttributes() {
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
        return getSelectedRadio()
    }

    /**
     * 获取单元格是否可编辑(cell_editable)
     */
    override fun get_cell_editable(): Boolean {
        return sheetCell.cell_editable == SheetProtocol().True
    }

    /**
     * 获取单元格是否为必填(cell_fill_required)
     */
    override fun get_cell_fill_required(): Boolean {
        return sheetCell.cell_fill_required == SheetProtocol().True
    }

    /**
     * 获取单元格是否可打印(cell_printable)
     */
    override fun get_cell_printable(): Boolean {
        return sheetCell.cell_printable == SheetProtocol().True
    }

    /**
     * 获取单元格是否默认勾选打印(cell_default_print)
     */
    override fun get_cell_default_print(): Boolean {
        return sheetCell.cell_default_print == SheetProtocol().True
    }

    /**
     * 获取单元格可否被加样(cell_copyable)
     */
    override fun get_cell_copyable(): Boolean {
        return sheetCell.cell_copyable == SheetProtocol().True
    }

    /**
     * 根据单元格内容生成Json
     */
    override fun getJsonContent(): JSONObject {
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
        if (get_cell_fill_required()){
            return getSelectedRadio() != null
        }else
            return true

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
    var cell_value: LinearLayout? = null  // 盛放Checkbox的容器
    var checkBoxes = ArrayList<AppCompatCheckBox>() // 用于储存多选选项
    var cell_fill_required: TextView? = null
    var cell_printable: CheckBox? = null

    init {
        // 导入界面
        val mInflater: LayoutInflater = LayoutInflater.from(mContext)
        contentView = mInflater.inflate(R.layout.sheet_cell_radio, null)
        // LinearLayout
        linearLayout = contentView!!.findViewById(R.id.cell_edit_text_linear_layout) as LinearLayout
        // 填写单元格的名字
        cell_name = contentView!!.findViewById(R.id.cell_name) as TextView
        cell_name!!.text = sheetCell.cell_name
        // 填写单元格的内容
        cell_value = contentView!!.findViewById(R.id.cell_value) as LinearLayout
        val choices = sheetCell.cell_value.splitKeeping(",")
        for (choice in choices){
            val radio = AppCompatCheckBox(mContext)
            radio.setText(choice)
            cell_value!!.addView(radio)
            checkBoxes.add(radio)
        }
        // 设置单元格可编辑状态(不受该属性影响)
        // ？@##￥￥%%%@#￥！
        // 设置单元格必填状态
        cell_fill_required= contentView!!.findViewById(R.id.cell_fill_required) as TextView
        if (sheetCell.cell_fill_required==(SheetProtocol().False))
            cell_fill_required!!.visibility= View.INVISIBLE
        // 设置单元格默认打印状态
        cell_printable = contentView!!.findViewById(R.id.cell_printable) as CheckBox
        cell_printable!!.setBackgroundResource(R.drawable.selector_checkbox_print)
        cell_printable!!.setButtonDrawable(ColorDrawable(Color.TRANSPARENT))
        if (sheetCell.cell_printable==(SheetProtocol().False))
            cell_printable!!.visibility = View.INVISIBLE
        cell_printable!!.isChecked = sheetCell.cell_default_print == (SheetProtocol().True)
    }

    /**
     * 分割字符串
     */
    fun String.splitKeeping(str: String): List<String> {
        return this.split(str).flatMap {listOf(it)}.dropLast(0).filterNot {it.isEmpty()}
    }

    /**
     * 清除所有选项
     */
    fun clearAllCheckBoxes(){
        for (checkBox in checkBoxes)
            checkBox.isChecked = false
    }

    /**
     * 获取选中的选项,多个选项之间通过英文逗号分割
     */
    fun getSelectedRadio():String{
        var result = ""
        for (radio in checkBoxes) {
            if(radio.isChecked){
                if (result.isEmpty())
                    result = radio.text.toString()
                else
                    result = result + "," + radio.text.toString()
            }
        }
        return result
    }

}