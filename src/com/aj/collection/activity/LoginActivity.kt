package com.aj.collection.activity

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatRadioButton
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.widget.*
import com.aj.WeixinActivityMain
import com.aj.collection.R
import com.aj.collection.bean.Superior
import com.aj.collection.http.API
import com.aj.collection.http.ReturnCode
import com.aj.collection.http.URLs
import com.aj.collection.service.LongTermService
import com.aj.collection.service.MsgService
import com.aj.collection.tools.ExitApplication
import com.aj.collection.tools.SPUtils
import com.aj.collection.tools.Util
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), OnClickListener {
    private var login: Button? = null
    private var signUp: Button? = null
    private var userET: EditText? = null
    private var pwdET: EditText? = null
    private var newUser: TextView? = null
    private var user: String? = null
    private var pwd: String? = null
    private val mContext = this
    private var queue: RequestQueue? = null

    private var otherLoginDialog: Dialog? = null
    private var selectedSuperior: Superior = Superior()
    private var allSuperiorRadioButtons = ArrayList<AppCompatRadioButton>()
    private var allSignUpEditText = ArrayList<EditText>()
    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState)
        otherLoginDialog = Dialog(this@LoginActivity)
        otherLoginDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.login_layout)
        supportActionBar?.title = resources.getString(R.string.app_name)
        ExitApplication.getInstance().addActivity(this)

        login = findViewById(R.id.l_signin_button) as Button
        signUp = findViewById(R.id.l_signup_button) as Button
        userET = findViewById(R.id.l_username_edit) as EditText
        pwdET = findViewById(R.id.l_password_edit) as EditText
        newUser = findViewById(R.id.new_user) as TextView
        login!!.setOnClickListener(this)
        newUser!!.setOnClickListener(this)

        signUp!!.setOnClickListener {
            allSignUpEditText.removeAll(allSignUpEditText)
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.sign_up_layout, null)
            dialogBuilder.setView(dialogView)

            val userName = dialogView.findViewById(R.id.farmer_user_name) as EditText
            allSignUpEditText.add(userName)
            val passwd = dialogView.findViewById(R.id.farmer_password) as EditText
            allSignUpEditText.add(passwd)
            val name = dialogView.findViewById(R.id.farmer_name) as EditText
            allSignUpEditText.add(name)
            val phone = dialogView.findViewById(R.id.farmer_phone) as EditText
            allSignUpEditText.add(phone)
            val identity = dialogView.findViewById(R.id.farmer_identity_number) as EditText
            allSignUpEditText.add(identity)
            val address = dialogView.findViewById(R.id.farmer_address) as EditText
            allSignUpEditText.add(address)
            val post = dialogView.findViewById(R.id.farmer_post_number) as EditText
            allSignUpEditText.add(post)
            val possessor = dialogView.findViewById(R.id.farmer_bank_card_possessor) as EditText
            allSignUpEditText.add(possessor)
            val band_card_number = dialogView.findViewById(R.id.farmer_band_card_number) as EditText
            allSignUpEditText.add(band_card_number)
            val band_name = dialogView.findViewById(R.id.farmer_bank_name) as EditText
            allSignUpEditText.add(band_name)
            val saveButton = dialogView.findViewById(R.id.dialog_ok) as Button
            val cancleButton = dialogView.findViewById(R.id.dialog_cancle) as Button

            val superiorListLL = dialogView.findViewById(R.id.superior_list) as LinearLayout
            getSuperiorInfo(superiorListLL)

            dialogBuilder.setTitle("请详细填写用户信息")
            val b = dialogBuilder.create()
            b.show()

            saveButton.setOnClickListener {
                for (et:EditText in allSignUpEditText){
                    if (et.text.isEmpty()){
                        toast("请将信息填写完整")
                        return@setOnClickListener
                    }
                }
                registUser(b, userName.text.toString(), passwd.text.toString(), selectedSuperior.company,selectedSuperior.user_id,
                        name.text.toString(), phone.text.toString(), identity.text.toString(), address.text.toString(), post.text.toString(), possessor.text.toString(),
                        band_card_number.text.toString(), band_name.text.toString())}

            cancleButton.setOnClickListener { b.dismiss() }
        }
        queue = (application as CollectionApplication).requestQueue //init Volley

        //get sharedpreference and valide it
        user = SPUtils.get(this, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String
        pwd = SPUtils.get(this, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String

        if (intent != null && intent.getBooleanExtra(ReturnCode.ACCOUNT_LOGIN_OTHER_DEVICE, false)) {
            stopService(Intent(this, MsgService::class.java))
            stopService(Intent(this, LongTermService::class.java))
            showReLoginDialog()
            return
        }

        //if need intent have boolean needTurnToMain,and user,pwd not null,then Turn To
        if (intent != null && intent.getBooleanExtra("needTurnToMain", true)
                && !user!!.isEmpty() && !pwd!!.isEmpty() && !intent.getBooleanExtra(ReturnCode.ACCOUNT_LOGIN_OTHER_DEVICE, false)) {
            val intent = Intent(mContext, WeixinActivityMain::class.java)
            startActivity(intent)
        }

        //        Intent intent = new Intent(mContext, WeixinActivityMain.class);
        //        startActivity(intent);
        //        finish();
    }

    override fun onClick(v: View) {
        // TODO Auto-generated method stub
        when (v.id) {
            R.id.l_signin_button -> {
                user = userET!!.text.toString()
                pwd = pwdET!!.text.toString()
                if (!user!!.isEmpty() && !pwd!!.isEmpty()) {
                    loginValidate()
                } else {
                    Toast.makeText(mContext, "用户名和密码不能为空！", Toast.LENGTH_LONG).show()
                }
            }
            R.id.new_user -> {
            }

            else -> {
            }
        }
        //				Intent i = new Intent(this,RegistActivity.class);
        //				startActivity(i);
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ExitApplication.getInstance().exit()
        }
        return false
    }

    private fun registUser(b:AlertDialog, user:String, pwd:String, company:String, company_id:String, farmer_name:String,
                           farmer_phone:String, farmer_identity:String, farmer_address:String,
                           farmer_post:String, farmer_possessor:String, farmer_bank_number:String,
                           farmer_bank_name:String){
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("注册中...")
        progressDialog.show()

        val listener = Response.Listener<String> { s ->
            progressDialog.dismiss()
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val content = resultJson.getString(URLs.KEY_MESSAGE)

                if (errorCode == ReturnCode.Code0) {
                    toast("注册成功")
                    SPUtils.put(this@LoginActivity,SPUtils.SAMPLING_COMPANY, company, SPUtils.USER_INFO)
                    SPUtils.put(this@LoginActivity,SPUtils.SAMPLING_COMPANY_ID, company_id, SPUtils.USER_INFO)
                    b.dismiss()

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
            Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show()
            Log.e("LoginTestFail", volleyError.toString())
        }

        val stringRequest = API.registUser(listener, errorListener, user, pwd, company, company_id, farmer_name,
                farmer_phone, farmer_identity, farmer_address,
                farmer_post, farmer_possessor, farmer_bank_number,
                farmer_bank_name)

        queue!!.add(stringRequest)
    }
    private fun getSuperiorInfo(ll_superiors:LinearLayout){
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("查询上级单位...")
        progressDialog.show()

        val listener = Response.Listener<String> { s ->
            progressDialog.dismiss()
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val content = resultJson.getString(URLs.KEY_MESSAGE)

                if (errorCode == ReturnCode.Code0) {
                    val gson = Gson()
                    gson.fromJson(s, Superior::class.java)
                    val turnsType = object : TypeToken<List<Superior>>() {}.type
                    var superiorList:List<Superior> = gson.fromJson(content, turnsType)
                    for(superior in superiorList){
                        val rb = AppCompatRadioButton(this@LoginActivity)
                        rb.setText(superior.company)
                        rb.setOnClickListener {
                            clearAllRadioButtons()
                            rb.isChecked=true
                            selectedSuperior = superior
                        }
                        ll_superiors.addView(rb)
                        allSuperiorRadioButtons.add(rb)
                    }

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
        }

        val stringRequest = API.getSuperiorInfo(listener, errorListener)

        queue!!.add(stringRequest)
    }

    private fun clearAllRadioButtons(){
        for (rb:AppCompatRadioButton in allSuperiorRadioButtons){
            rb.isChecked=false
        }
    }
    /**
     * 登录验证
     */
    private fun loginValidate() {
        val progressDialog = ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT)
        progressDialog.setMessage("登录中...")
        progressDialog.show()

        val listener = Response.Listener<String> { s ->
            progressDialog.dismiss()
            try {
                val resultJson = JSONObject(s)
                val errorCode = resultJson.getString(URLs.KEY_ERROR)
                val content = resultJson.getString(URLs.KEY_MESSAGE)

                if (errorCode == ReturnCode.Code0) {
                    val jsonObject = JSONObject(content)
                    var samplingCompany = jsonObject.getString(URLs.COMPANY)
                    var samplingCompanyAddress = jsonObject.getString(URLs.COMPANYADDR)
                    var samplingContact = jsonObject.getString(URLs.CONTACT)
                    var samplingPhone = jsonObject.getString(URLs.CONTACTPHONE)
                    var deviceSN = jsonObject.getString(URLs.SERIAL_NUMBER)
                    var postCode = jsonObject.getString(URLs.POST_CODE)
                    var identityNumber = jsonObject.getString(URLs.IDENTITY_NUMBER)
                    var bankPossessor = jsonObject.getString(URLs.BANK_CARD_POSSESSOR)
                    var bankNumber = jsonObject.getString(URLs.BANK_CARD_NUMBER)
                    var bankName = jsonObject.getString(URLs.BANK_NAME)

                    val noEdit = "没有填写"
                    if (samplingCompany.isEmpty())
                        samplingCompany = noEdit
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

                    SPUtils.put(mContext, SPUtils.SAMPLING_COMPANY, samplingCompany, SPUtils.USER_INFO)
                    SPUtils.put(mContext, SPUtils.FARMER_ADDRESS, samplingCompanyAddress, SPUtils.USER_INFO)
                    SPUtils.put(mContext, SPUtils.FARMER_NAME, samplingContact, SPUtils.USER_INFO)
                    SPUtils.put(mContext, SPUtils.FARMER_PHONE_NUMBER, samplingPhone, SPUtils.USER_INFO)
                    SPUtils.put(mContext, SPUtils.FARMER_POST_NUMBER, postCode, SPUtils.USER_INFO)
                    SPUtils.put(mContext, SPUtils.FARMER_IDENTITY_NUMBER, identityNumber, SPUtils.USER_INFO)
                    SPUtils.put(mContext, SPUtils.FARMER_DEPOSIT_CARD_NAME, bankPossessor, SPUtils.USER_INFO)
                    SPUtils.put(mContext, SPUtils.FARMER_DEPOSIT_CARD_NUMBER, bankNumber, SPUtils.USER_INFO)
                    SPUtils.put(mContext, SPUtils.FARMER_BANK_NAME, bankName, SPUtils.USER_INFO)

                    SPUtils.put(mContext, SPUtils.LOGIN_NAME, user, SPUtils.LOGIN_VALIDATE)
                    SPUtils.put(mContext, SPUtils.LOGIN_PASSWORD, pwd, SPUtils.LOGIN_VALIDATE)

                    //设备的sn是服务器记录登陆设备的编号，属于抽样单编号的一部分
                    SPUtils.put(mContext, SPUtils.DEV_SN, Util.transformDeviceSN(deviceSN), SPUtils.SYSVARIABLE)
                    (application as CollectionApplication).global_device_sn = Util.transformDeviceSN(deviceSN)

                    val intent = Intent(mContext, WeixinActivityMain::class.java)
                    startActivity(intent)
                    finish()
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
        }

        val stringRequest = API.login(listener, errorListener,
                user, pwd)

        queue!!.add(stringRequest)
    }



    /**
     * 账号在别处登陆
     */
    internal fun showReLoginDialog() {
        if (otherLoginDialog!!.isShowing) {
            return
        }

        val inflater = this@LoginActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(
                R.layout.dialogview_two_button, null) as RelativeLayout
        val Title = layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu) as TextView
        val Message = layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu) as TextView

        val positivebutton = layout.findViewById(R.id.textview_positive_button) as TextView
        val negativebutton = layout.findViewById(R.id.textview_negative_button) as TextView

        Title.text = ReturnCode.ACCOUNT_LOGIN_OTHER_DEVICE_STRING
        Message.text = "是否重新登陆？"

        positivebutton.setOnClickListener {
            otherLoginDialog!!.dismiss()
            // re login
            val user = SPUtils.get(this@LoginActivity, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE) as String
            val pwd = SPUtils.get(this@LoginActivity, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE) as String

            if (!user.isEmpty() && !pwd.isEmpty()) {
                loginValidate()
            } else {
                Toast.makeText(mContext, "本地账号密码不存在，请手动登陆！", Toast.LENGTH_LONG).show()
            }
        }
        negativebutton.setOnClickListener {
            otherLoginDialog!!.dismiss()

            //防止后台有任务导致不停的跳转到login界面
            SPUtils.put(this@LoginActivity, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
            SPUtils.put(this@LoginActivity, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
        }


        otherLoginDialog!!.setContentView(layout)
        otherLoginDialog!!.setCancelable(false)
        otherLoginDialog!!.show()
    }
}
