package com.aj.collection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.aj.Constant
import com.aj.collection.bean.SheetCell
import com.aj.collection.tools.SheetProtocol
import com.baidu.location.BDLocation
import com.baidu.navisdk.comapi.mapcontrol.MapParams
import org.jetbrains.anko.toast
import org.json.JSONObject

/**
 * 地址单元格(type_address): 用于显示采样人员所在的地址信息，如“XX市，XX区，XX县”。
 * Created by kevin on 17-4-19.
 * Mail: chewenkaich@gmail.com
 */
class TypeAddress(var mContext: Context, var sheetCell: SheetCell): CellBaseAttributes() {
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
    override fun setCellNotPrint() {
        cell_printable?.isChecked = false
        cell_printable?.isClickable = false
    }

    /**
     * 将内容填到单元格
     */
    override fun setFilledContent(content: String) {
        country_province_city?.text = content

    }

    /**
     * 设置单元格为不可更改
     */
    override fun setCellDisable() {
        locationPause = true
        streetET?.visibility = View.GONE
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
        if (streetET!!.text.toString().isEmpty())
            return country_province_city!!.text.toString()
        else
            return country_province_city!!.text.toString() + "，补充位置：" + streetET?.text.toString()
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
        return if(sheetCell.cell_fill_required == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 获取单元格是否可打印(cell_printable)
     */
    override fun get_cell_printable(): String {
        return if(sheetCell.cell_printable == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 获取单元格是否默认勾选打印(cell_default_print)
     */
    override fun get_cell_default_print(): String {
        return if(sheetCell.cell_default_print == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 获取单元格可否被加样(cell_copyable)
     */
    override fun get_cell_copyable(): String {
        return if(sheetCell.cell_copyable == SheetProtocol().True) SheetProtocol().True else SheetProtocol().False
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
        if (sheetCell.cell_fill_required==(SheetProtocol().False))
            return true
        else{
            return location_info_type == BDLocation.TypeGpsLocation||
                    location_info_type == BDLocation.TypeNetWorkLocation
        }
    }

    /**
     * 获取LinearLayout的界面
     */
    override fun getView(): LinearLayout {
        return linearLayout!!
    }

    /**
     * 注册位置信息广播接收器
     */
    fun registerLocationBroadcast(){
        val filter = IntentFilter(Constant.LOCATION_BROADCAST_ACT)
        mContext.registerReceiver(locationReceiver, filter)
    }

    /**
     * 取消注册位置信息广播接收器
     */
    fun unregisterLocationBroadcast(){
        mContext.unregisterReceiver(locationReceiver)
    }

    var contentView: View? = null  // 设计的界面
    var linearLayout: LinearLayout? = null
    var cell_name: TextView? = null
    var cell_value: LinearLayout? = null
    var country_province_city: TextView? = null
    var streetET:EditText? = null
    var cell_fill_required: TextView? = null
    var cell_printable: CheckBox? = null

    // Location Info
    var locationPause = false  // 是否暂停定位
    var country = ""  // 国家
    var province = ""  // 省
    var city = ""  // 市
    var district = ""  // 区域
    var street = ""  // 街道
    var location_info_type = BDLocation.TypeOffLineLocation  // 定位类型

    /**
     * 接受位置信息
     */
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Constant.LOCATION_BROADCAST_ACT == action) {
                if (locationPause)
                    return
                val location = intent.getParcelableExtra<BDLocation>(Constant.LOCATION_BROADCAST_VALUE)
                country = location?.country?:""
                province = location?.province?:""
                city = location?.city?:""
                district = location?.district?:""
                street = location?.street?:""

                location_info_type = location?.locType?:BDLocation.TypeNone  // 定位类型
                country_province_city!!.text = "$province$city$district$street"
            }
        }
    }

    init {
        // 导入界面
        val mInflater: LayoutInflater = LayoutInflater.from(mContext)
        contentView = mInflater.inflate(R.layout.sheet_cell_address, null)
        // LinearLayout
        linearLayout = contentView!!.findViewById(R.id.cell_edit_text_linear_layout) as LinearLayout
        // 填写单元格的名字
        cell_name = contentView!!.findViewById(R.id.cell_name) as TextView
        cell_name!!.text = sheetCell.cell_name
        // 填写单元格的内容(不受内容的影响)
        cell_value = contentView!!.findViewById(R.id.cell_value) as LinearLayout
        country_province_city = contentView!!.findViewById(R.id.country_province_city) as TextView
        streetET = contentView!!.findViewById(R.id.street) as EditText
        country_province_city!!.text = "未知区域"
        streetET!!.hint = "补充地址信息"
        streetET!!.setText("")
        // 设置单元格可编辑状态(不受内容的影响)
        cell_value!!.isEnabled=false
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
        // 注册位置广播接收器
        registerLocationBroadcast()
    }
}