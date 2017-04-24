package com.aj.collection

import android.content.Context
import android.widget.LinearLayout
import org.json.JSONObject
import java.io.File

/**
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
abstract class CellBaseAttributes {
    //单元格相关属性的获取

    /**
     *获取单元格名称(cell_name)
     */
    abstract fun get_cell_name(): String

    /**
     * 获取单元格类型(cell_type)
     */
    abstract fun get_cell_type(): String

    /**
     * 获取单元格值(cell_value)
     */
    abstract fun get_cell_value(): String

    /**
     * 获取单元格是否可编辑(cell_editable)
     */
    abstract fun get_cell_editable(): String

    /**
     * 获取单元格是否为必填(cell_fill_required)
     */
    abstract fun get_cell_fill_required(): String

    /**
     * 获取单元格是否可打印(cell_printable)
     */
    abstract fun get_cell_printable(): String

    /**
     * 获取单元格是否默认勾选打印(cell_default_print)
     */
    abstract fun get_cell_default_print(): String

    /**
     * 获取单元格可否被加样(cell_copyable)
     */
    abstract fun get_cell_copyable(): String

    //获取其他信息

    /**
     * 获取LinearLayout的界面
     */
    abstract fun getView(): LinearLayout

    /**
     * 必填的内容是否已经填写
     */
    abstract fun isFilled(): Boolean

    /**
     * 根据单元格内容生成Json
     */
    abstract fun getJsonContent(): JSONObject

    /**
     * 获取打印的内容
     */
    abstract fun getPrintContent(): String

    /**
     * 将内容填到单元格
     */
    abstract fun setFilledContent(content: String)

    /**
     * 设置单元格为不可更改
     */
    abstract fun setCellDisable()

    /**
     * 设置单元格为不可打印
     */
    abstract fun setCellNotPrinte()

}