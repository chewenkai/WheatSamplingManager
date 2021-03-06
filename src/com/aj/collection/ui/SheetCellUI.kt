package com.aj.collection.ui

import android.content.Context
import com.aj.collection.*
import com.aj.collection.bean.SheetCell
import com.aj.collection.tools.SheetProtocol

/**
 * 该类通过解析SheetCell类来产生UI控件
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
class SheetCellUI(var context: Context, var sheetCell: SheetCell, var autoGeneratedSheetID:String) {
    var cell: CellBaseAttributes = TypeEditText(context, sheetCell)
    init {
        when (sheetCell.cell_type){
            SheetProtocol().TYPE_EDIT_TEXT -> cell = TypeEditText(context, sheetCell)
            SheetProtocol().TYPE_TEXT -> cell =  TypeText(context, sheetCell)
            SheetProtocol().TYPE_RADIO -> cell =  TypeRadio(context, sheetCell)
            SheetProtocol().TYPE_RADIO_WITH_SECONDARY_CHOICE -> cell = TypeRadioWithSecondaryChoice(context, sheetCell)
            SheetProtocol().TYPE_MULTI_SELECT -> cell = TypeMultiSelect(context, sheetCell)
            SheetProtocol().TYPE_MULTI_THEN_SINGLE_CHOICE -> cell = TypeMultiThenSingleChoice(context, sheetCell)
            SheetProtocol().TYPE_GEOGRAPHIC_COORDINATES -> cell =  TypeGeographicCoordinates(context, sheetCell)
            SheetProtocol().TYPE_ADDRESS -> cell = TypeAddress(context, sheetCell)
            SheetProtocol().TYPE_PHOTOS -> cell = TypePhotos(context, sheetCell, autoGeneratedSheetID)
            SheetProtocol().TYPE_VEDIOS -> cell = TypeVedios(context, sheetCell, autoGeneratedSheetID)
            SheetProtocol().TYPE_AUTO_RECORD_DATE -> cell = TypeAutoRecordDate(context, sheetCell)
            SheetProtocol().TYPE_AUTO_RECORD_TIME -> cell = TypeAutoRecordTime(context, sheetCell)
            SheetProtocol().TYPE_DATE_SELECT -> cell = TypeDateSelect(context, sheetCell)
            SheetProtocol().TYPE_SIGN -> cell = TypeSign(context, sheetCell, autoGeneratedSheetID)
            else ->{
                cell =  TypeEditText(context, sheetCell)
            }
        }
    }

    fun getCellType():String{
        return sheetCell.cell_type
    }

}