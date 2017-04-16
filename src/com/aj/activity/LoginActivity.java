package com.aj.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.SystemBarTintManager;
import com.aj.WeixinActivityMain;
import com.aj.collection.R;
import com.aj.http.API;
import com.aj.http.ReturnCode;
import com.aj.http.URLs;
import com.aj.service.LongTermService;
import com.aj.service.MsgService;
import com.aj.tools.ExitApplication;
import com.aj.tools.SPUtils;
import com.aj.tools.Util;
import com.aj.ui.HeadControlPanel;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity implements OnClickListener {
    private Button login;
    private EditText userET;
    private EditText pwdET;
    private TextView newUser;
    private String user, pwd;
    private HeadControlPanel headPanel = null;
    private Context mContext = this;
    private RequestQueue queue;

    private Dialog otherLoginDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        otherLoginDialog = new Dialog(LoginActivity.this);
        otherLoginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_layout);
        ExitApplication.getInstance().addActivity(this);
        //沉浸状态栏
        SystemBarTintManager.setStatusBarTint(LoginActivity.this, Color.argb(0, 59, 59, 59));//透明状态栏
        login = (Button) findViewById(R.id.l_signin_button);
        userET = (EditText) findViewById(R.id.l_username_edit);
        pwdET = (EditText) findViewById(R.id.l_password_edit);
        newUser = (TextView) findViewById(R.id.new_user);
        login.setOnClickListener(this);
        newUser.setOnClickListener(this);

        headPanel = (HeadControlPanel) findViewById(R.id.head_layout);
        if (headPanel != null) {
            headPanel.initHeadPanel();
            headPanel.setMiddleTitle("抽样监督管理系统");
            headPanel.setLeftImage(R.drawable.ic_menu_back);
            final HeadControlPanel.LeftImageOnClick l = new HeadControlPanel.LeftImageOnClick() {

                @Override
                public void onImageClickListener() {

                    ExitApplication.getInstance().exit();

                }
            };
            headPanel.setLeftImageOnClick(l);
        }

        queue = ((CollectionApplication)getApplication()).getRequestQueue(); //init Volley

        //get sharedpreference and valide it
        user = (String) SPUtils.get(this, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE);
        pwd = (String) SPUtils.get(this, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE);

        if (getIntent()!=null&&getIntent().getBooleanExtra(ReturnCode.ACCOUNT_LOGIN_OTHER_DEVICE,false)){
            stopService(new Intent(this, MsgService.class));
            stopService(new Intent(this, LongTermService.class));
            showReLoginDialog();
            return;
        }

        //if need intent have boolean needTurnToMain,and user,pwd not null,then Turn To
        if (getIntent()!=null&&getIntent().getBooleanExtra("needTurnToMain",true)
        &&!user.isEmpty() && !pwd.isEmpty()&&!getIntent().getBooleanExtra(ReturnCode.ACCOUNT_LOGIN_OTHER_DEVICE,false)) {
            Intent intent = new Intent(mContext, WeixinActivityMain.class);
            startActivity(intent);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.l_signin_button:
                user = userET.getText().toString();
                pwd = pwdET.getText().toString();
                if (!user.isEmpty() && !pwd.isEmpty()) {
                    loginValidate();
                }else{
                    Toast.makeText(mContext,"用户名和密码不能为空！",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.new_user:
//				Intent i = new Intent(this,RegistActivity.class);
//				startActivity(i);
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            ExitApplication.getInstance().exit();
        }
        return false;
    }

    /**
     * 登录验证
     */
    private void loginValidate() {
        final ProgressDialog progressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage("登录中...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String content = resultJson.getString(URLs.KEY_MESSAGE);

                    if (errorCode.equals(ReturnCode.Code0)) {
                        JSONObject jsonObject = new JSONObject(content);
                        String samplingCompany = jsonObject.getString(URLs.COMPANY);
                        String samplingCompanyAddress = jsonObject.getString(URLs.COMPANYADDR);
                        String samplingContact = jsonObject.getString(URLs.CONTACT);
                        String samplingPhone = jsonObject.getString(URLs.CONTACTPHONE);
                        String deviceSN=jsonObject.getString(URLs.SERIAL_NUMBER);

                        String noEdit = "没有填写";
                        if (samplingCompany.isEmpty())
                            samplingCompany = noEdit;
                        if (samplingCompanyAddress.isEmpty())
                            samplingCompanyAddress = noEdit;
                        if (samplingContact.isEmpty())
                            samplingContact = noEdit;
                        if (samplingPhone.isEmpty())
                            samplingPhone = noEdit;

                        SPUtils.put(mContext, SPUtils.SAMPLING_COMPANY, samplingCompany, SPUtils.USER_INFO);
                        SPUtils.put(mContext, SPUtils.SAMPLING_ADDR, samplingCompanyAddress, SPUtils.USER_INFO);
                        SPUtils.put(mContext, SPUtils.SAMPLING_CONTACT, samplingContact, SPUtils.USER_INFO);
                        SPUtils.put(mContext, SPUtils.SAMPLING_PHONE, samplingPhone, SPUtils.USER_INFO);

                        SPUtils.put(mContext, SPUtils.LOGIN_NAME, user, SPUtils.LOGIN_VALIDATE);
                        SPUtils.put(mContext, SPUtils.LOGIN_PASSWORD, pwd, SPUtils.LOGIN_VALIDATE);

                        //设备的sn是服务器记录登陆设备的编号，属于抽样单编号的一部分
                        SPUtils.put(mContext, SPUtils.DEV_SN,Util.transformDeviceSN(deviceSN),SPUtils.SYSVARIABLE);
                        ((CollectionApplication)getApplication()).global_device_sn=Util.transformDeviceSN(deviceSN);

                        Intent intent = new Intent(mContext, WeixinActivityMain.class);
                        startActivity(intent);
                        finish();
                    }else {
                        new ReturnCode(getApplicationContext(), errorCode,true);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

             }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(mContext, mContext.getString(R.string.badNetWork), Toast.LENGTH_LONG).show();
                Log.e("LoginTestFail", volleyError.toString());
            }
        };

        StringRequest stringRequest = API.login(listener, errorListener,
                user,pwd);

        queue.add(stringRequest);
    }


    public void registTask() {
        final Context mContext = this;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, URLs.REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.e(">>>>>>>>>", s.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        Log.e(">>>>>>>>>", volleyError.toString());
                        Toast.makeText(mContext, "网络没有连接", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", "kevin");
                map.put("password", "123456789");
                map.put("email", "769776082@qq.com");
                return map;
            }

        };
        queue.add(request);
    }

    /**
     * 账号在别处登陆
     */
    void showReLoginDialog() {
        if (otherLoginDialog.isShowing()) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) LoginActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.dialogview_two_button, null);
        TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
        TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

        TextView positivebutton = (TextView) layout.findViewById(R.id.textview_positive_button);
        TextView negativebutton = (TextView) layout.findViewById(R.id.textview_negative_button);

        Title.setText(ReturnCode.ACCOUNT_LOGIN_OTHER_DEVICE_STRING);
        Message.setText("是否重新登陆？");

        positivebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otherLoginDialog.dismiss();
                // re login
                String user = (String) SPUtils.get(LoginActivity.this, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE);
                String pwd = (String) SPUtils.get(LoginActivity.this, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE);

                if (!user.isEmpty() && !pwd.isEmpty()) {
                    loginValidate();
                }else{
                    Toast.makeText(mContext,"本地账号密码不存在，请手动登陆！",Toast.LENGTH_LONG).show();
                }

            }
        });
        negativebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otherLoginDialog.dismiss();

                //防止后台有任务导致不停的跳转到login界面
                SPUtils.put(LoginActivity.this,SPUtils.LOGIN_NAME,"",SPUtils.LOGIN_VALIDATE);
                SPUtils.put(LoginActivity.this,SPUtils.LOGIN_NAME,"",SPUtils.LOGIN_VALIDATE);
            }
        });


        otherLoginDialog.setContentView(layout);
        otherLoginDialog.setCancelable(false);
        otherLoginDialog.show();
    }
}
