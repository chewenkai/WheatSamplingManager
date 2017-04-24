package com.aj.collection.activity.tools

/**
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
class SheetProtocol {
    val SHEET_JSON_KEY = "specimen_sheet"

    //单元格二值类属性的值
    val True = "T"
    val False = "F"

    // 单元格属性列表
    val CELL_NAME = "cell_name"
    val CELL_TYPE = "cell_type"
    val CELL_VALUE = "cell_value"
    val CELL_EDITABLE = "cell_editable"
    val CELL_FILL_REQUIRED = "cell_fill_required"
    val CELL_PRINTABLE = "cell_printable"
    val CELL_DEFAULT_PRINT = "cell_default_print"
    val CELL_COPYABLE = "cell_copyable"

    // SheetCell Type List
    val TYPE_EDIT_TEXT = "type_edit_text"  // <填写单元格>用 于手机端填写信息。最为常用的类型，所有单元格属性对其生效。
    val TYPE_TEXT = "type_text"  // <显示单元格> 仅用于显示信息，单元格是否可编辑属性、单元格是否为必填属性对其无效
    val TYPE_RADIO = "type_radio"  // <单选单元格> 用于进行单项选择，如单元格名称为“自然灾害情况”，单元格值为“洪灾,涝灾”。单元格是否可编辑属性对其无效
    val TYPE_PRIMARY_TITLE = "type_primary_title"  // 一级标题单元格(type_primary_title): 用于显示一个粗体的标题，如”抽样情况”。单元格是否可编辑、单元格值，单元格是否为必填属性对其无效
    val TYPE_RADIO_WITH_SECONDARY_CHOICE = "type_radio_with_secondary_choice"  // <二级单选单元格> 用于对单选的内容进行补充，如上述自然灾害情况，单元格值为“洪灾,涝灾;轻微,一般,严重”，即可在选择完灾害后，选择严重程度。单元格是否可编辑属性对其无效
    val TYPE_MULTI_SELECT = "type_multi_select"  // <多选单元格> 用于进行多项选择，如单元格名称为“喷洒农药名称”，可能喷洒多种农药，单元格值为“A药,B药”。单元格是否可编辑属性对其无效
    val TYPE_GEOGRAPHIC_COORDINATES = "type_geographic_coordinates"  // <地理位置坐标单元格> 显示采样人员所在的经纬度。单元格值、单元格是否可编辑属性对其无效
    val TYPE_ADDRESS = "type_address"  // <地址单元格> 用于显示采样人员所在的地址信息，如“XX市，XX区，XX县”
    val TYPE_PHOTOS = "type_photos"  // <拍照单元格> 用于采集照片。单元格值、单元格是否可打印、单元格是否默认勾选打印属性对其无效
    val TYPE_VEDIOS = "type_vedios"  // <录像单元格> 用于拍摄视频。单元格值、单元格是否可打印、单元格是否默认勾选打印属性对其无效
    val TYPE_AUTO_RECORD_DATE = "type_auto_record_date"  // <自动记录日期单元格> 用于自动记录日期，如”XXXX年XX月XX日”。单元格值、单元格是否可编辑属性对其无效
    val TYPE_AUTO_RECORD_TIME = "type_auto_record_time"  // <自动记录时间单元格> 用于自动记录时间，如“XX时XX分”。单元格、单元格是否可编辑属性对其无效
    val TYPE_DATE_SELECT = "type_date_select"  // <日期选择单元格> 用于用于手动选择日期。单元格值、单元格是否可编辑属性对其无效
    val TYPE_SIGN = "type_sign"  // <签名单元格> 用于签名。单元格值、单元格是否可编辑、单元格是否可打印、单元格是否默认勾选打印属性对其无效
}