package com.aj.ui

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
import com.aj.bean.SheetCell
import com.aj.collection.*
import com.aj.tools.SheetProtocol

/**
 * 该类通过解析SheetCell类来产生UI控件
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
class SheetCellUI(var context: Context, var sheetCell: SheetCell) {
    var cell: CellBaseAttributes = TypeEditText(context, sheetCell)
    init {
        when (sheetCell.cell_type){
            SheetProtocol().TYPE_EDIT_TEXT -> cell = TypeEditText(context, sheetCell)
            SheetProtocol().TYPE_TEXT -> cell =  TypeText(context, sheetCell)
            SheetProtocol().TYPE_RADIO -> cell =  TypeRadio(context, sheetCell)
            SheetProtocol().TYPE_RADIO_WITH_SECONDARY_CHOICE -> {}
            SheetProtocol().TYPE_MULTI_SELECT -> {}
            SheetProtocol().TYPE_GEOGRAPHIC_COORDINATES -> cell =  TypeGeographicCoordinates(context, sheetCell)
            SheetProtocol().TYPE_ADDRESS -> cell = TypeAddress(context, sheetCell)
            SheetProtocol().TYPE_PHOTOS -> {}
            SheetProtocol().TYPE_VEDIOS -> {}
            SheetProtocol().TYPE_AUTO_RECORD_DATE -> {}
            SheetProtocol().TYPE_AUTO_RECORD_TIME -> {}
            SheetProtocol().TYPE_DATE_SELECT -> {}
            SheetProtocol().TYPE_SIGN -> {}
            else ->{
                cell =  TypeEditText(context, sheetCell)
            }
        }
    }

    fun getCellType():String{
        return sheetCell.cell_type
    }

}