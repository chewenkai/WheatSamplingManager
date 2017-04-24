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
 * 地理位置坐标单元格(type_geographic_coordinates): 显示采样人员所在的经纬度。单元格值、单元格是否可编辑属性对其无效
 * Created by kevin on 17-4-19.
 * Mail: chewenkaich@gmail.com
 */
class TypeGeographicCoordinates(var mContext: Context, var sheetCell: SheetCell): CellBaseAttributes() {

    /**
     * 获取打印的内容
     */
    override fun getPrintContent(): String {
        if (sheetCell.cell_printable == SheetProtocol().True && cell_printable?.isChecked?:false)
            return cell_name?.hint.toString() + ":" + get_cell_value()
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
            cell_value?.setText(content)
    }

    /**
     * 设置单元格为不可更改
     */
    override fun setCellDisable() {
        locationPause = true
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
        return cell_value!!.text.toString()
    }

    /**
     * 获取单元格是否可编辑(cell_editable)
     */
    override fun get_cell_editable(): String {
        return if(sheetCell.cell_editable == SheetProtocol().True)SheetProtocol().True else SheetProtocol().False
    }

    /**
     * 获取单元格是否为必填(cell_fill_required)
     */
    override fun get_cell_fill_required(): String {
        return if(sheetCell.cell_fill_required == SheetProtocol().True)SheetProtocol().True else SheetProtocol().False
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

    fun registerLocationBroadcast(){
        // 注册位置信息广播接收器
        val filter = IntentFilter(Constant.LOCATION_BROADCAST_ACT)
        mContext.registerReceiver(locationReceiver, filter)
    }

    fun unregisterLocationBroadcast(){
        mContext.unregisterReceiver(locationReceiver)
    }

    var contentView: View? = null  // 设计的界面
    var linearLayout: LinearLayout? = null
    var cell_name: TextInputLayout? = null
    var cell_value: TextInputEditText? = null
    var cell_fill_required: TextView? = null
    var cell_printable: CheckBox? = null

    // Location Info
    var locationPause = false  // 是否暂停定位
    var longitude = ""  // 经度信息
    var latitude = ""  // 纬度信息
    var location_info_type = BDLocation.TypeOffLineLocation  // 定位类型
    var location_type_str = ""  // 定位类型字符

    /**
     * Receive notification of scanned cards
     */
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Constant.LOCATION_BROADCAST_ACT == action) {
                if (locationPause)
                    return
                val location = intent.getParcelableExtra<BDLocation>(Constant.LOCATION_BROADCAST_VALUE)
                longitude = location.longitude.toString()  // 经度信息
                latitude = location.latitude.toString()  // 纬度信息
                location_info_type = location.locType  // 定位类型
                when (location_info_type){
                    BDLocation.TypeGpsLocation -> location_type_str="GPS定位"
                    BDLocation.TypeCacheLocation -> location_type_str="缓存的位置(此状态无法保存)"
                    BDLocation.TypeNetWorkLocation-> location_type_str="网络定位"
                    BDLocation.TypeNone-> location_type_str="无(此状态无法保存)"
                    BDLocation.TypeOffLineLocation-> location_type_str="离线定位(此状态无法保存)"
                }
                cell_value!!.setText("经度:$longitude\n纬度:$latitude\n定位类型：$location_type_str")
            }
        }
    }

    init {
        // 导入界面
        val mInflater: LayoutInflater = LayoutInflater.from(mContext)
        contentView = mInflater.inflate(R.layout.sheet_cell_geographic_coordinates, null)
        // LinearLayout
        linearLayout = contentView!!.findViewById(R.id.cell_edit_text_linear_layout) as LinearLayout
        // 填写单元格的名字
        cell_name = contentView!!.findViewById(R.id.cell_name) as TextInputLayout
        cell_name!!.hint = sheetCell.cell_name
        cell_name!!.isHintEnabled =true
        // 填写单元格的内容(不受内容的影响)
        cell_value = contentView!!.findViewById(R.id.cell_value) as TextInputEditText
        cell_value!!.setText(mContext.getString(R.string.search_signal))
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