package com.aj.collection

import android.content.Context
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
import com.aj.collection.activity.SheetActivity
import com.aj.collection.bean.SheetCell
import com.aj.collection.database.TASKINFO
import com.aj.collection.http.API
import com.aj.collection.http.ReturnCode
import com.aj.collection.http.URLs
import com.aj.collection.tools.KotlinUtil
import com.aj.collection.tools.SPUtils
import com.aj.collection.tools.SheetProtocol
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject

/**
 * **抽样序列号单元格(type_serial_number):** 用户标识抽样单的唯一编号，同时用于将抽样单和样品关联。格式如“W170001”，是最为常用的类型，*是否可编辑为否，可否被加样为是*
 * 为了保证序列号唯一而且不浪费，需要做以下操作：
 * --填写抽样单时
 *   -- 进入抽样单，获取SID为X
 *   -- 退出抽样单界面时，判断是否已保存抽样单（搜索数据库中是否已有以SID编号的样品），若已保存则setT，否则setF
 *   -- 为了防止用户申请到SID后（SID申请后在服务器默认状态为T，即已被使用），程序意外退出，未来得及置位服务端为F。需要
 *      在申请到SID时将其存入本地保存的SID列表，在退出界面时清除本地保存的SID列表，这样在Application退出时或打
 *      开时，判断本地是否保存有SID，若有，将其setF，并清除本地记录.
 *
 * --编辑抽样单，查看抽样单和补采抽样单
 *   -- 不需要申请SID
 *   -- 退出界面时，不需要setT或setF
 *   -- 不需要保存和清除SID到本地
 *
 * --删除抽样单
 *   --抽样单在服务器setF后，删除本地记录
 *
 * Created by kevin on 17-5-12.
 * Mail: chewenkaich@gmail.com
 */
class TypeSerialNumber(var mContext: Context, var sheetCell: SheetCell, var taskinfo: TASKINFO?) : CellBaseAttributes() {
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
        return cell_value?.text?.toString() ?: ""
    }

    /**
     * 获取单元格是否可编辑(cell_editable)
     */
    override fun get_cell_editable(): String {
        return SheetProtocol().False
    }

    /**
     * 获取单元格是否为必填(cell_fill_required)
     */
    override fun get_cell_fill_required(): String {
        return SheetProtocol().True
    }

    /**
     * 获取单元格是否可打印(cell_printable)
     */
    override fun get_cell_printable(): String {
        return sheetCell.cell_printable
    }

    /**
     * 获取单元格是否默认勾选打印(cell_default_print)
     */
    override fun get_cell_default_print(): String {
        return sheetCell.cell_default_print
    }

    /**
     * 获取单元格可否被加样(cell_copyable)
     */
    override fun get_cell_copyable(): String {
        return SheetProtocol().True
    }

    /**
     * 获取LinearLayout的界面
     */
    override fun getView(): LinearLayout {
        return linearLayout!!
    }

    /**
     * 必填的内容是否已经填写
     */
    override fun isFilled(): Boolean {
        return get_cell_value().isNotEmpty() && get_cell_value().isNotBlank()
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
     * 获取打印的内容
     */
    override fun getPrintContent(): String {
        if (sheetCell.cell_printable == SheetProtocol().True && cell_printable?.isChecked ?: false)
            return cell_name?.hint.toString() + ":" + get_cell_value()
        else
            return ""
    }

    /**
     * 将内容填到单元格
     */
    override fun setFilledContent(content: String) {
        cell_value!!.setText(content)
    }

    /**
     * 设置单元格为不可更改
     */
    override fun setCellDisable() {
    }

    /**
     * 设置单元格为不可打印
     */
    override fun setCellNotPrint() {
        cell_printable?.isChecked = false
        cell_printable?.isClickable = false
    }

    var contentView: View? = null  // 设计的界面
    var linearLayout: LinearLayout? = null
    var cell_name: TextInputLayout? = null
    var cell_value: TextInputEditText? = null
    var cell_fill_required: TextView? = null
    var cell_printable: CheckBox? = null
    var defaultSampleSN = Constant.DEFAULT_SAMPLE_SN
    var sampleSN: String = defaultSampleSN  // 抽样单序列号
    // 网络部分
    internal var queue: RequestQueue? = null

    init {
        // 初始化网络框架
        queue = Volley.newRequestQueue(mContext) //init Volley
        // 导入界面
        val mInflater: LayoutInflater = LayoutInflater.from(mContext)
        contentView = mInflater.inflate(R.layout.sheet_cell_edit_text, null)
        // LinearLayout
        linearLayout = contentView!!.findViewById(R.id.cell_edit_text_linear_layout) as LinearLayout
        // 填写单元格的名字
        cell_name = contentView!!.findViewById(R.id.cell_name) as TextInputLayout
        cell_name!!.hint = sheetCell.cell_name
        cell_name!!.isHintEnabled = true
        // 填写单元格的内容
        cell_value = contentView!!.findViewById(R.id.cell_value) as TextInputEditText
        cell_value!!.isEnabled = false
        // 设置单元格可编辑状态(不受该属性影响)
        // ？@##￥￥%%%@#￥！
        // 设置单元格必填状态(不受该属性影响，默认隐藏红星号)
        cell_fill_required = contentView!!.findViewById(R.id.cell_fill_required) as TextView
        cell_fill_required!!.visibility = View.INVISIBLE
        // 设置单元格默认打印状态
        cell_printable = contentView!!.findViewById(R.id.cell_printable) as CheckBox
        cell_printable!!.setBackgroundResource(R.drawable.selector_checkbox_print)
        cell_printable!!.buttonDrawable = ColorDrawable(Color.TRANSPARENT)
        if (sheetCell.cell_printable == (SheetProtocol().False))
            cell_printable!!.visibility = View.INVISIBLE
        cell_printable!!.isChecked = sheetCell.cell_default_print == (SheetProtocol().True)
    }

    fun fetchId() {
        val listener = Response.Listener<String> { s ->

            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val message = resultJson.getString(URLs.KEY_MESSAGE)
                if (errorCode == ReturnCode.Code0) {
                    sampleSN = message
                    val jsonObject = JSONObject("{\"id\":$sampleSN,\"task_id\":${taskinfo?.taskID}}")
                    // 查询并添加本地缓存的sid
                    val cachedSID = KotlinUtil.getLocalSIds(mContext)
                    cachedSID.put(jsonObject)
                    SPUtils.put(mContext, SPUtils.SAMPLING_CACHED_SID, cachedSID.toString(),
                            SPUtils.SAMPLING_CACHED_SID_NAME)

                    while (sampleSN.length != 4) {
                        sampleSN = "0" + sampleSN
                    }
                    cell_value!!.setText(taskinfo?.task_letter + sampleSN)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener {
            mContext.toast("请打开网络连接")
            if (SheetActivity::class.java.isInstance(mContext)){
                (mContext as SheetActivity).finish()
            }
        }
        val stringRequest = API.fetchSID(listener, errorListener,
                SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String?,
                SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String?,
                taskinfo?.taskID?.toString())
        queue?.add(stringRequest)
    }
}