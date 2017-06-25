package com.aj.collection.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import cn.qqtheme.framework.entity.Province
import cn.qqtheme.framework.picker.AddressPicker
import cn.qqtheme.framework.util.ConvertUtils

import com.aj.collection.R
import com.aj.collection.http.API
import com.aj.collection.http.ReturnCode
import com.aj.collection.http.URLs
import com.aj.collection.tools.SPUtils
import com.aj.collection.tools.T
import com.aj.collection.ui.HeadControlPanel
import com.aj.collection.ui.HeadControlPanel.LeftImageOnClick
import com.alibaba.fastjson.JSON
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap

class RegistActivity : AppCompatActivity() {
    private var allSignUpEditText = ArrayList<EditText>()
    private var queue: RequestQueue? = null
    var addressPicker: AddressPicker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_layout)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "用户注册"
        queue = (application as CollectionApplication).requestQueue //init Volley
        init()
    }

    private fun init() {
        val userName = findViewById(R.id.farmer_user_name) as EditText
        allSignUpEditText.add(userName)
        val passwd = findViewById(R.id.farmer_password) as EditText
        allSignUpEditText.add(passwd)
        val name = findViewById(R.id.farmer_name) as EditText
        allSignUpEditText.add(name)
        val phone = findViewById(R.id.farmer_phone) as EditText
        allSignUpEditText.add(phone)
        val identity = findViewById(R.id.farmer_identity_number) as EditText
//        allSignUpEditText.add(identity)
        val address = findViewById(R.id.farmer_address) as EditText
//        allSignUpEditText.add(address)
        val post = findViewById(R.id.farmer_post_number) as EditText
//        allSignUpEditText.add(post)
        val possessor = findViewById(R.id.farmer_bank_card_possessor) as EditText
//        allSignUpEditText.add(possessor)
        val band_card_number = findViewById(R.id.farmer_band_card_number) as EditText
//        allSignUpEditText.add(band_card_number)
        val band_name = findViewById(R.id.farmer_bank_name) as EditText
//        allSignUpEditText.add(band_name)
        val saveButton = findViewById(R.id.dialog_ok) as Button

        val superiorListLL = findViewById(R.id.superior_list) as LinearLayout
        var data = ArrayList<Province>()
        // TODO 放到后台线程执行，添加ProgressDialog
        val progress = ProgressDialog(this)
        progress.setMessage("加载城市数据")
        progress.setCancelable(false)
        if(!progress.isShowing)
            progress.show()
        doAsync {
            val json: String = ConvertUtils.toString(getAssets().open("city2.json"))
            data.addAll(JSON.parseArray(json, Province::class.java))
            uiThread {
                addressPicker = AddressPicker(this@RegistActivity, data)
                addressPicker?.setShadowVisible(false)
                addressPicker?.setHideProvince(false)
                addressPicker?.setHideCounty(false)
                superiorListLL.addView(addressPicker?.contentView)
                if(progress.isShowing)
                    progress.dismiss()
            }
        }

        saveButton.setOnClickListener {
            for (et: EditText in allSignUpEditText) {
                if (et.text.isEmpty()) {
                    toast("请将必填信息填写完整")
                    return@setOnClickListener
                }
            }
            registUser(userName.text.toString(), passwd.text.toString(), addressPicker?.selectedProvince?.areaName ?: "",
                    addressPicker?.selectedCity?.areaName ?: "", addressPicker?.selectedCounty?.areaName ?: "",
                    name.text.toString(), phone.text.toString(), identity.text.toString(), address.text.toString(), post.text.toString(), possessor.text.toString(),
                    band_card_number.text.toString(), band_name.text.toString())
        }
    }

    private fun registUser(user: String, pwd: String, province: String, city: String, country: String, farmer_name: String,
                           farmer_phone: String, farmer_identity: String, farmer_address: String,
                           farmer_post: String, farmer_possessor: String, farmer_bank_number: String,
                           farmer_bank_name: String) {
        val progressDialog = ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("注册中...")
        progressDialog.show()

        val listener = Response.Listener<String> { s ->
            progressDialog.dismiss()
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val content = resultJson.getString(URLs.KEY_MESSAGE)

                if (errorCode == ReturnCode.Code0) {
                    toast("注册成功,请登录")
                    finish()
                    SPUtils.put(this@RegistActivity, SPUtils.FARMER_PROVINCE, province, SPUtils.USER_INFO)
                    SPUtils.put(this@RegistActivity, SPUtils.FARMER_CITY, city, SPUtils.USER_INFO)
                    SPUtils.put(this@RegistActivity, SPUtils.FARMER_COUNTRY, country, SPUtils.USER_INFO)

                } else {
                    ReturnCode(applicationContext, errorCode, true)
                }

            } catch (e: JSONException) {
                toast("注册出错，错误信息：" + e.message)
                e.printStackTrace()
            }
        }
        val errorListener = Response.ErrorListener { volleyError ->
            progressDialog.dismiss()
            Toast.makeText(this, getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
            Log.e("LoginTestFail", volleyError.toString())
        }

        val stringRequest = API.registUser(listener, errorListener, user, pwd, province, city, country, farmer_name,
                farmer_phone, farmer_identity, farmer_address,
                farmer_post, farmer_possessor, farmer_bank_number,
                farmer_bank_name)

        queue!!.add(stringRequest)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
