package com.aj.collection.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import cn.qqtheme.framework.entity.Province
import cn.qqtheme.framework.picker.AddressPicker
import cn.qqtheme.framework.util.ConvertUtils
import com.aj.collection.R
import com.aj.collection.http.API
import com.aj.collection.http.ReturnCode
import com.aj.collection.http.URLs
import com.aj.collection.tools.SPUtils
import com.aj.collection.ui.HeadControlPanel
import com.aj.collection.ui.HeadControlPanel.LeftImageOnClick
import com.alibaba.fastjson.JSON
import com.android.volley.RequestQueue
import com.android.volley.Response
import kotlinx.android.synthetic.main.set_base_info.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject

class DetecedUnit : AppCompatActivity()  {
    var province = ""
    var city = ""
    var country = ""
    private var kaiguan: Switch? = null
    private var userTV: TextView? = null
    private var userName: EditText? = null
    private var userPhoneNum: EditText? = null
    private var userIdentity: EditText? = null
    private var userAddress: EditText? = null
    private var userPostNum: EditText? = null
    private var userBankCardPossessor: EditText? = null
    private var userBankCardNumber: EditText? = null
    private var userBankName: EditText? = null
    private var saveInfo: AppCompatButton?=null
    var addressPicker: AddressPicker? = null
    internal var login_user: String=""
    internal var ll: LinearLayout?=null
    private var queue: RequestQueue? = null
    private val mContext:Context = this@DetecedUnit
    val dv = "没有填写"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.set_base_info)
        supportActionBar?.title = "个人信息"

        queue = (application as CollectionApplication).requestQueue //init Volley
        ll = findViewById(R.id.ll_jiankong) as LinearLayout
        userName = findViewById(R.id.user_real_name) as EditText
        userPhoneNum = findViewById(R.id.phone_number) as EditText
        userIdentity = findViewById(R.id.identity_number) as EditText
        userAddress = findViewById(R.id.user_address) as EditText
        userPostNum = findViewById(R.id.post_number) as EditText
        userBankCardPossessor = findViewById(R.id.bank_card_possessor) as EditText
        userBankCardNumber = findViewById(R.id.bank_card_number) as EditText
        userBankName = findViewById(R.id.bank_name) as EditText
        saveInfo = findViewById(R.id.save_info) as AppCompatButton

        userTV = findViewById(R.id.user_set) as TextView
        kaiguan = findViewById(R.id.switch_phone) as Switch

        var data = ArrayList<Province>()
        val json: String = ConvertUtils.toString(assets.open("city2.json"))
        data.addAll(JSON.parseArray(json, Province::class.java))
        addressPicker = AddressPicker(this@DetecedUnit, data)
        addressPicker?.setShadowVisible(false)
        addressPicker?.setHideProvince(false)
        addressPicker?.setHideCounty(false)
        change_region.onClick {
            if (addressPicker?.isShowing?:true) {
                addressPicker?.dismiss()
            }
            else {
                addressPicker?.show()
                addressPicker?.submitButton?.onClick {
                    province = addressPicker?.selectedProvince?.areaName?:""
                    city = addressPicker?.selectedCity?.areaName?:""
                    country = addressPicker?.selectedCounty?.areaName?:""
                    region_text.text = province + "-" +  city + "-" + country
                    addressPicker?.dismiss()
                }
            }
        }

        login_user = SPUtils.get(this, SPUtils.LOGIN_NAME, dv, SPUtils.LOGIN_VALIDATE) as String//登录的用户名
        val passwd = SPUtils.get(this, SPUtils.LOGIN_PASSWORD, dv, SPUtils.LOGIN_VALIDATE) as String//登录密码
        userTV!!.text = login_user
        kaiguan!!.isChecked = SPUtils.get(this, SPUtils.KAIGUAN, false, login_user) as Boolean
        getUserInfo(userName, userPhoneNum, userIdentity, userAddress, userPostNum,
                userBankCardPossessor, userBankCardNumber, userBankName)

        saveInfo!!.setOnClickListener {
            editUser(login_user, passwd, province,
                    city, country,
                    userName!!.text.toString(),
                    userPhoneNum!!.text.toString(), userIdentity!!.text.toString(),
                    userAddress!!.text.toString(), userPostNum!!.text.toString(),
                    userBankCardPossessor!!.text.toString(), userBankCardNumber!!.text.toString(),
                    userBankName!!.text.toString())
        }
    }

    /**
     * 获取用户信息
     */
    private fun getUserInfo(userName: EditText?, userPhoneNum: EditText?, userIdentity: EditText?,
                            userAddress: EditText?, userPostNum: EditText?,
                            userBankCardPossessor: EditText?, userBankCardNumber: EditText?,
                            userBankName: EditText?) {
        val progressDialog = ProgressDialog(this@DetecedUnit, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("获取用户信息...")
        progressDialog.show()

        val listener = Response.Listener<String> { s ->
            progressDialog.dismiss()
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val content = resultJson.getString(URLs.KEY_MESSAGE)

                if (errorCode == ReturnCode.Code0) {
                    val jsonObject = JSONObject(content)
                    var province = jsonObject.getString(URLs.PROVINCE)
                    var city = jsonObject.getString(URLs.CITY)
                    var country = jsonObject.getString(URLs.COUNTRY)
                    this.province = province
                    this.city = city
                    this.country = country
                    var samplingCompanyAddress = jsonObject.getString(URLs.ADDRESS)
                    var samplingContact = jsonObject.getString(URLs.CONTACT)
                    var samplingPhone = jsonObject.getString(URLs.CONTACTPHONE)
                    var deviceSN = jsonObject.getString(URLs.SERIAL_NUMBER)
                    var postCode = jsonObject.getString(URLs.POST_CODE)
                    var identityNumber = jsonObject.getString(URLs.IDENTITY_NUMBER)
                    var bankPossessor = jsonObject.getString(URLs.BANK_CARD_POSSESSOR)
                    var bankNumber = jsonObject.getString(URLs.BANK_CARD_NUMBER)
                    var bankName = jsonObject.getString(URLs.BANK_NAME)

                    val noEdit = "没有填写"
                    if (province.isEmpty())
                        province = noEdit
                    if (city.isEmpty())
                        city = noEdit
                    if (country.isEmpty())
                        country = noEdit
                    if (samplingCompanyAddress.isEmpty())
                        samplingCompanyAddress = noEdit
                    if (samplingContact.isEmpty())
                        samplingContact = noEdit
                    if (samplingPhone.isEmpty())
                        samplingPhone = noEdit
                    if (postCode.isEmpty())
                        postCode = noEdit
                    if (identityNumber.isEmpty())
                        identityNumber = noEdit
                    if (bankPossessor.isEmpty())
                        bankPossessor = noEdit
                    if (bankNumber.isEmpty())
                        bankNumber = noEdit
                    if (bankName.isEmpty())
                        bankName = noEdit

                    userName!!.setText(samplingContact)
                    userPhoneNum!!.setText(samplingPhone)
                    userIdentity!!.setText(identityNumber)
                    userAddress!!.setText(samplingCompanyAddress)
                    userPostNum!!.setText(postCode)
                    userBankCardPossessor!!.setText(bankPossessor)
                    userBankCardNumber!!.setText(bankNumber)
                    userBankName!!.setText(bankName)
                    region_text.text = province+city+country

                } else {
                    ReturnCode(applicationContext, errorCode, true)
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog.dismiss()
            Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
            Log.e("LoginTestFail", volleyError.toString())
            userName!!.setText(SPUtils.get(this, SPUtils.FARMER_NAME, dv, SPUtils.USER_INFO) as String)
            userPhoneNum!!.setText(SPUtils.get(this, SPUtils.FARMER_PHONE_NUMBER, dv, SPUtils.USER_INFO) as String)
            userIdentity!!.setText(SPUtils.get(this, SPUtils.FARMER_IDENTITY_NUMBER, dv, SPUtils.USER_INFO) as String)
            userAddress!!.setText(SPUtils.get(this, SPUtils.FARMER_ADDRESS, dv, SPUtils.USER_INFO) as String)
            userPostNum!!.setText(SPUtils.get(this, SPUtils.FARMER_POST_NUMBER, dv, SPUtils.USER_INFO) as String)
            userBankCardPossessor!!.setText(SPUtils.get(this, SPUtils.FARMER_DEPOSIT_CARD_NAME, dv, SPUtils.USER_INFO) as String)
            userBankCardNumber!!.setText(SPUtils.get(this, SPUtils.FARMER_DEPOSIT_CARD_NUMBER, dv, SPUtils.USER_INFO) as String)
            userBankName!!.setText(SPUtils.get(this, SPUtils.FARMER_BANK_NAME, dv, SPUtils.USER_INFO) as String)

        }

        val stringRequest = API.login(listener, errorListener,
                (SPUtils.get(this@DetecedUnit, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)) as String,
                SPUtils.get(this@DetecedUnit, SPUtils.LOGIN_PASSWORD, "",SPUtils.LOGIN_VALIDATE) as String)

        queue!!.add(stringRequest)
    }

    private fun editUser(user:String, pwd:String, province:String, city:String, country:String, farmer_name:String,
                           farmer_phone:String, farmer_identity:String, farmer_address:String,
                           farmer_post:String, farmer_possessor:String, farmer_bank_number:String,
                           farmer_bank_name:String){
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("保存中...")
        progressDialog.show()

        val listener = Response.Listener<String> { s ->
            progressDialog.dismiss()
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val content = resultJson.getString(URLs.KEY_MESSAGE)

                if (errorCode == ReturnCode.Code0) {
                    toast("保存成功")
                    SPUtils.put(this@DetecedUnit, SPUtils.FARMER_PROVINCE, province, SPUtils.USER_INFO)
                    SPUtils.put(this@DetecedUnit, SPUtils.FARMER_CITY, city, SPUtils.USER_INFO)
                    SPUtils.put(this@DetecedUnit, SPUtils.FARMER_COUNTRY, country, SPUtils.USER_INFO)
                } else {
                    ReturnCode(applicationContext, errorCode, true)
                }

            } catch (e: JSONException) {
                toast("用户信息修改出错，错误信息：" + e.message)
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog.dismiss()
            Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
            Log.e("LoginTestFail", volleyError.toString())
        }

        val stringRequest = API.editUser(listener, errorListener, user, pwd, province, city, country, farmer_name,
                farmer_phone, farmer_identity, farmer_address,
                farmer_post, farmer_possessor, farmer_bank_number,
                farmer_bank_name)

        queue!!.add(stringRequest)
    }
}
