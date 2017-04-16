package com.aj.http;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by kevin on 15-9-2.
 */
public class Communication {
    Context context;
    RequestQueue mQueue;

    public Communication(Context context) {
        this.context=context;
        mQueue = Volley.newRequestQueue(context);
    }

    /**
     * 从服务器获取String型数据
     * @param url 服务器地址
     * @param successlistener 获取成功的监听 Response.Listener<String> successlistener
     * @param errorlistener 获取失败的监听 Response.ErrorListener errorlistener
     */
    public void getStringMethod(String url,Response.Listener<String> successlistener,
                                   Response.ErrorListener errorlistener){

        StringRequest stringRequest = new StringRequest(url, successlistener,errorlistener);
        mQueue.add(stringRequest);

    }

    public void getJsonMethod(String url,Response.Listener<JSONObject> successlistener,
                              Response.ErrorListener errorlistener){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,successlistener, errorlistener);
        mQueue.add(jsonObjectRequest);
    }

    /**
     * 向服务器发送参数
     * @param url 服务器接口地址
     * @param successlistener 成功的监听 Response.Listener<String> successlistener
     * @param errorlistener 失败的监听 Response.ErrorListener errorlistener
     * @param map 参数 Map<String, String> map = new HashMap<String, String>();map.put("params1", "value1");map.put("params2", "value2");
     */
    public void postParamsMethod(String url,Response.Listener<String> successlistener,
                                 Response.ErrorListener errorlistener, final Map<String, String> map){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, successlistener,errorlistener){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    public RequestQueue getmQueue() {
        return mQueue;
    }

    public void setmQueue(RequestQueue mQueue) {
        this.mQueue = mQueue;
    }
}
