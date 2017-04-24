package com.aj.collection

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.AppCompatRadioButton
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.aj.collection.bean.SheetCell
import com.aj.collection.tools.SheetProtocol
import org.jetbrains.anko.enabled
import org.json.JSONObject

/**
 * 二级单选单元格(type_radio_with_secondary_choice): 用于对单选的内容进行补充，如上述自然灾害情况，单元格值为“洪灾,涝灾;轻微,一般,严重”，即可在选择完灾害后，选择严重程度。单元格是否可编辑属性对其无效
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
class TypeRadioWithSecondaryChoice(var mContext: Context, var sheetCell: SheetCell) : CellBaseAttributes() {

    /**
     * 获取打印的内容
     */
    override fun getPrintContent(): String {
        if (sheetCell.cell_printable == SheetProtocol().True && cell_printable?.isChecked ?: false)
            return cell_name?.text.toString() + ":" + get_cell_value()
        else
            return ""
    }

    /**
     * 设置单元格为不可打印
     */
    override fun setCellNotPrint() {
        cell_printable?.isChecked = false
        cell_printable?.isClickable = false
    }

    /**
     * 将内容填到单元格
     */
    override fun setFilledContent(content: String) {
        val twoPart = content.splitKeeping(";")
        if (twoPart.isEmpty())
            return
        else if (twoPart.size == 2) {
            val primaryValue = twoPart[0]
            val secondaryValue = twoPart[1]
            setPrimarySelectedRadio(primaryValue)
            setSecondarySelectedRadio(secondaryValue)
            cell_secondary_value?.visibility = View.VISIBLE
        } else
            return
    }

    /**
     * 设置单元格为不可更改
     */
    override fun setCellDisable() {
        setAllRadioDisable()
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
        var value = ""
        if (getPrimarySelectedRadio() != null) {
            value = getPrimarySelectedRadio()!!.text.toString()
        }
        value += ";"
        if (getSecondarySelectedRadio() != null) {
            value += getSecondarySelectedRadio()?.text.toString()
        }
        return value
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
        if (get_cell_fill_required() == SheetProtocol().True) {
            return getPrimarySelectedRadio() != null
        } else
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
    var cell_value: LinearLayout? = null  // 盛放Radio Button的容器
    var cell_secondary_value: LinearLayout? = null  // 盛放二级Radio Button的容器
    var radios = ArrayList<AppCompatRadioButton>() // 用于储存多个单选选项
    var secondaryRadios = ArrayList<AppCompatRadioButton>() // 用于储存多个二级单选选项
    var cell_fill_required: TextView? = null
    var cell_printable: CheckBox? = null

    init {
        // 导入界面
        val mInflater: LayoutInflater = LayoutInflater.from(mContext)
        contentView = mInflater.inflate(R.layout.sheet_cell_radio_with_secondary_choice, null)
        // LinearLayout
        linearLayout = contentView!!.findViewById(R.id.cell_edit_text_linear_layout) as LinearLayout
        // 填写单元格的名字
        cell_name = contentView!!.findViewById(R.id.cell_name) as TextView
        cell_name!!.text = sheetCell.cell_name
        // 填写单元格的内容
        cell_value = contentView!!.findViewById(R.id.cell_value) as LinearLayout  // 一级选项
        cell_secondary_value = contentView!!.findViewById(R.id.cell_secondary_value) as LinearLayout  // 二级选项
        val radioInfo = sheetCell.cell_value.splitKeeping(";")  // 先根据;区分选项级别 干旱,洪涝,高温,低温;严重,一般,轻微
        val choices = radioInfo[0].splitKeeping(",")  // 再根据,区分各级别中的选项
        for (choice in choices) {
            val radio = AppCompatRadioButton(mContext)
            radio.setText(choice)
            cell_value!!.addView(radio)
            radios.add(radio)
            radio.setOnClickListener {
                clearPrimaryRadios()
                clearSecondaryRadios()
                cell_secondary_value?.visibility = View.VISIBLE
                radio.isChecked = true
            }
        }

        val secondaryChoices = radioInfo[1].splitKeeping(",")  // 再根据,区分各级别中的选项
        for (choice in secondaryChoices) {
            val radio = AppCompatRadioButton(mContext)
            radio.setText(choice)
            cell_secondary_value!!.addView(radio)
            secondaryRadios.add(radio)
            radio.setOnClickListener {
                clearSecondaryRadios()
                radio.isChecked = true
            }
        }
        clearSecondaryRadios()
        clearPrimaryRadios()
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

    /**
     * 分割字符串
     */
    fun String.splitKeeping(str: String): List<String> {
        return this.split(str).flatMap { listOf(it) }.dropLast(0).filterNot { it.isEmpty() }
    }

    /**
     * 清除所有一级选项
     */
    fun clearPrimaryRadios() {
        for (radio in radios)
            radio.isChecked = false
    }

    /**
     * 清除所有二级选项
     */
    fun clearSecondaryRadios() {
        for (radio in secondaryRadios)
            radio.isChecked = false
    }

    /**
     * 获取一级选项选中的选项
     */
    fun getPrimarySelectedRadio(): AppCompatRadioButton? {
        for (radio in radios) {
            if (radio.isChecked)
                return radio
        }
        return null
    }

    /**
     * 获取二级选项选中的选项
     */
    fun getSecondarySelectedRadio(): AppCompatRadioButton? {
        for (radio in secondaryRadios) {
            if (radio.isChecked)
                return radio
        }
        return null
    }

    /**
     * 设置一级选项已选中的选项
     */
    fun setPrimarySelectedRadio(radioValue: String) {
        for (radio in radios) {
            if (radio.text.toString().equals(radioValue)) {
                radio.isChecked = true
                return
            }
        }
    }

    /**
     * 设置二级选项已选中的选项
     */
    fun setSecondarySelectedRadio(radioValue: String) {
        for (radio in secondaryRadios) {
            if (radio.text.toString().equals(radioValue)) {
                radio.isChecked = true
                return
            }
        }
    }

    /**
     * 设置选项为不可点击状态
     */
    fun setAllRadioDisable() {
        for (radio in radios) {
            radio.isEnabled = false
        }
        for (radio in secondaryRadios) {
            radio.isEnabled = false
        }
    }

}