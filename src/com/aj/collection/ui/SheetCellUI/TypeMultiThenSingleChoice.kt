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
import com.aj.collection.tools.SheetProtocol
import com.aj.collection.bean.RadiosView
import org.jetbrains.anko.enabled
import org.json.JSONObject

/**
 * 二级单选单元格(type_radio_with_secondary_choice): 用于对单选的内容进行补充，如上述自然灾害情况，单元格值为“洪灾,涝灾;轻微,一般,严重”，即可在选择完灾害后，选择严重程度。单元格是否可编辑属性对其无效
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
class TypeMultiThenSingleChoice(var mContext: Context, var sheetCell: SheetCell) : CellBaseAttributes() {

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
        val nCheckBoxesStr = content.splitKeeping(";")
        if (nCheckBoxesStr.isEmpty())
            return
        for (checkBox in nCheckBoxesStr){
            setSelectedCheckBox(checkBox)
        }
    }

    /**
     * 设置单元格为不可更改
     */
    override fun setCellDisable() {
        setAllCheckBoxDisable()
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
     * TODO 分号分割
     * 获取单元格值(cell_value)
     */
    override fun get_cell_value(): String {
        val checkBoxes = getSelectedCheckBox()
        var value = ""
        for (checkBox in checkBoxes){
            value += checkBox.text.toString()+","
            value+=(checkBox.tag as RadiosView).getSelectedRadio()?.text.toString()
            if (checkBoxes.indexOf(checkBox) != checkBoxes.size){
                value += ";"
            }
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
            return !getSelectedCheckBox().isEmpty() && (getSelectedCheckBox()[0].tag as RadiosView).getSelectedRadio()!=null
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
    var checkBoxes = ArrayList<AppCompatCheckBox>() // 用于储存多个单选选项
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
            val checkBox = AppCompatCheckBox(mContext)
            checkBox.text = choice
            cell_value!!.addView(checkBox)
            checkBoxes.add(checkBox)
            // 添加二级单选
            val secondaryLinearLL = LinearLayout(mContext)
            secondaryLinearLL.orientation = LinearLayout.VERTICAL
            cell_value!!.addView(secondaryLinearLL)
            val radiosView = RadiosView(secondaryLinearLL, radioInfo[1].splitKeeping(","), mContext)
            checkBox.tag = radiosView
            radiosView.hiddenRadios()
            checkBox.setOnClickListener {
                if (checkBox.isChecked)  // 如果被选择，就显示二级选项
                    radiosView.showRadios()
                else {  // 不被选择则清除二级选项并隐藏之
                    radiosView.clearRadios()
                    radiosView.hiddenRadios()
                }
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

    /**
     * 分割字符串
     */
    fun String.splitKeeping(str: String): List<String> {
        return this.split(str).flatMap { listOf(it) }.dropLast(0).filterNot { it.isEmpty() }
    }

    /**
     * 获取选中的选项
     */
    fun getSelectedCheckBox(): ArrayList<AppCompatCheckBox> {
        val selectedCB = ArrayList<AppCompatCheckBox>()
        for (checkBox in checkBoxes) {
            if (checkBox.isChecked)
                selectedCB.add(checkBox)
        }
        return selectedCB
    }

    /**
     * 设置一级选项已选中的选项
     * 洪涝,轻微
     */
    fun setSelectedCheckBox(radioValue: String) {
        var content = radioValue.splitKeeping(",")
        for (radio in checkBoxes) {
            if (radio.text.toString().equals(content[0])) {
                radio.isChecked = true
                (radio.tag as RadiosView).setSelectedRadio(content[1])
                (radio.tag as RadiosView).showRadios()
            }
        }
    }

    /**
     * 设置选项为不可点击状态
     */
    fun setAllCheckBoxDisable() {
        for (radio in checkBoxes) {
            radio.isEnabled = false
            (radio.tag as RadiosView).setRadiosDisable()
        }
    }

}