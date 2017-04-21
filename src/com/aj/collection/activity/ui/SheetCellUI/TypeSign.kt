package com.aj.collection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.aj.collection.activity.WritePadDialog
import com.aj.collection.activity.tools.DialogListener
import com.aj.collection.activity.tools.FileStream
import com.aj.collection.activity.tools.SheetProtocol
import com.aj.collection.activity.tools.Util
import com.aj.collection.bean.SheetCell
import com.squareup.picasso.Picasso
import org.jetbrains.anko.onClick
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * 签名单元格(type_sign) 用于签名。单元格值、单元格是否可编辑、单元格是否可打印、单元格是否默认勾选打印属性对其无效
 * Created by kevin on 17-4-18.
 * Mail: chewenkaich@gmail.com
 */
class TypeSign(var mContext: Context, var sheetCell: SheetCell, val autoGeneratedSheetID:String): CellBaseAttributes() {
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
        return signImagePath
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
        // 将内容生成Json
        val json = JSONObject()
        json.put(SheetProtocol().CELL_NAME, get_cell_name())
        json.put(SheetProtocol().CELL_TYPE, get_cell_type())
        json.put(SheetProtocol().CELL_VALUE, signImagePath)
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
            return File(signImagePath).exists()
        }

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
    var cell_value: ImageView? = null  // 盛放照片
    var makeASignButton: Button? = null // 签名按钮
    var cell_fill_required: TextView? = null
    var cell_printable: CheckBox? = null

    var signImagePath = ""
    var sign_name = ""

    init {
        // 导入界面
        val mInflater: LayoutInflater = LayoutInflater.from(mContext)
        contentView = mInflater.inflate(R.layout.sheet_cell_sign, null)
        // LinearLayout
        linearLayout = contentView!!.findViewById(R.id.cell_edit_text_linear_layout) as LinearLayout
        // 填写单元格的名字
        cell_name = contentView!!.findViewById(R.id.cell_name) as TextView
        cell_name!!.text = sheetCell.cell_name
        // 填写单元格的内容
        cell_value = contentView!!.findViewById(R.id.cell_value) as ImageView
        Picasso.with(mContext).load(R.drawable.edit_query).into(cell_value)
        signImagePath = Util.getMediaFolder(mContext)+File.separator+autoGeneratedSheetID + File.separator +
                cell_name!!.text.toString() + File.separator + "SIGN" + "_" + (mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId + Util.getCurrentTime("yyMMddHHmmss") + ".jpg"
        sign_name = "SIGN" + "_" + (mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId + Util.getCurrentTime("yyMMddHHmmss") + ".jpg"    //图片名称
        // 定义按钮功能
        makeASignButton = contentView!!.findViewById(R.id.makeASign) as Button
        makeASignButton!!.text = "点击签名"
        makeASignButton!!.onClick {
            //初始化签字对话框
            val writePadDialog = WritePadDialog(cell_value, mContext, DialogListener { imageView, `object` ->
                val signBitmap = `object` as Bitmap        //获得签名的图片
                val options = BitmapFactory.Options()
                options.inSampleSize = 2
                val baos = ByteArrayOutputStream()
                signBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)//这里100的话表示不压缩质量
                val bm = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size(), options)
                imageView.setImageBitmap(bm)    //显示签名图片
                //imageView.setClickable(false);	//只签一次

                //保存图片到当前所有样品的文件夹下
                val fs = FileStream(mContext)
                fs.createImageFile(signBitmap, Util.getMediaFolder(mContext)+File.separator+autoGeneratedSheetID + File.separator +
                        cell_name!!.text.toString(), sign_name)    //创建图片文件
                val picasso = Picasso.with(mContext)
                picasso.invalidate(File(signImagePath))
                picasso.load(File(signImagePath)).into(cell_value!!)
            })
            writePadDialog.show()
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
        cell_printable!!.visibility = View.INVISIBLE
        cell_printable!!.isChecked = sheetCell.cell_default_print == (SheetProtocol().True)
    }
}