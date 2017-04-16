package com.aj.http;


import com.aj.Constant;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * All net work action
 * Created by kevin on 15-10-28.
 */
public class API {

    /***************************ABOUT USER**********************/

    /**
     * 登录验证
     */
    public static StringRequest login(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                      final String user, final String pwd) {

        StringRequest request = new StringRequest(Request.Method.POST, URLs.LOGIN, listener, errorListener) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.LOGINACT);
                map.put("username", user);
                map.put("password", pwd);
                map.put("UDID", Constant.DeviceID);
                return map;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {

                    Map<String, String> responseHeaders = response.headers;
                    String rawCookies = responseHeaders.get("Set-Cookie");
                    String dataString = new String(response.data, "UTF-8");
                    return Response.success(dataString, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
//                return super.parseNetworkResponse(response);
            }
        };

        return request;
    }

    /**
     * 用户注册
     */
    public static StringRequest regist(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                       final String user, final String pwd, final String email) {
        StringRequest request = new StringRequest(Request.Method.GET, URLs.REGISTER, listener, errorListener) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.REGISTERACT);
                map.put("username", user);
                map.put("password", pwd);
                map.put("email", email);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };

        return request;
    }

    /***************************DOWNLOAD**********************/

    /**
     * 检查是否有新任务
     *
     * @param listener
     * @param errorListener
     * @return
     */
    public static StringRequest haveNewTask(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                            final String user) {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.HAVENEWTASK, listener, errorListener) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.HAVENEWTASKACT);
                map.put("username", user);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };
        return request;
    }

    /**
     * 获取新任务x
     * jason like this: array('taskid' => $tid,'taskname'=>$task_name,'task_initial_letter'=>'ABCDE','sampling'=>$samplinglistarray , 'taskdiscription' => $task_remark , 'taskcont' => $cont);
     * 其中 sampling的json形式是 'samplingid'=>$value['id'],'samplingcont'=>$value['t_con']
     *
     * @param listener
     * @param errorListener
     * @param user
     * @param pwd
     * @return
     */
    public static StringRequest getNewTask(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                           final String user, final String pwd) {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.GETNEWTASK, listener, errorListener) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.GETNEWTASKACT);
                map.put("username", user);
                map.put("password", pwd);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };
        return request;
    }

    /**
     * 新任务接收成功
     *
     * @param listener
     * @param errorListener
     * @param user
     * @return
     */
    public static StringRequest setTaskReceived(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                                final String user) {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.RECEIVED, listener, errorListener) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.RECEIVEDACT);
                map.put("username", user);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };
        return request;
    }

    /***************************UPLOAD***********************/

    /**
     * 上传图片x
     *
     * @param listener
     * @param errorListener
     * @param files
     * @param params
     * @return
     */
    public static MultipartRequest uploadFiles(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                               final ArrayList<File> files, final Map<String, String> params) {
//        RequestQueue mSingleQueue = Volley.newRequestQueue(context, new MultiPartStack());

        MultipartRequest multiPartRequest = new MultipartRequest(URLs.UPLAODIMG, errorListener
                , listener, "upfile", files, params);
        return multiPartRequest;
    }


    /**
     * 上传抽样单OK
     *
     * @param listener
     * @param errorListener
     * @param username
     * @param password
     * @param samplingSets
     * @return
     */
    public static StringRequest uploadSampling(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                               final String username, final String password, final String samplingSets,
                                               final String tname, final String samplecontent, final String samplingname,
                                               final String sid,final double latitude,final double longitude,final int mode,
                                               final String samplingnumber,final boolean isMadeUp) {

        StringRequest request = new StringRequest(Request.Method.POST, URLs.UPLOADSAMPLING, listener, errorListener) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                //判断是否是补采
                int samplingType = Constant.UNKOWN_SAMPLING_TYPE;
                if(isMadeUp)
                    samplingType=Constant.RESAMPLE_SAMPLING_TYPE;
                else
                    samplingType=Constant.NORMAL_SAMPLING_TYPE;

                //判断是否有sid
                String serverid=Constant.DO_NOT_HAVE_SID;
                if (!sid.equals("null"))
                    serverid=sid;

                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.UPLOADSAMPLINGACT);
                map.put("username", username);
                map.put("password", password);
                map.put("tid", samplingSets);
                map.put("tname", tname);
                map.put("scon", samplecontent);
                map.put("sname", samplingname);
                map.put("latitude",String.valueOf(latitude));
                map.put("longitude",String.valueOf(longitude));
                map.put("mode",String.valueOf(mode));
                map.put("samplingnumber",samplingnumber);
                map.put("UDID", Constant.DeviceID);
                map.put("sid", serverid);
                map.put("resample", String.valueOf(samplingType));
                return map;
            }
        };
        return request;
    }

    /**
     * 完成任务
     *
     * @param listener
     * @param errorListener
     * @param username
     * @param password
     * @param tID
     * @return
     */
    public static StringRequest finishTask(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                           final String username, final String password, final String tID) {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.FINISHTASK, listener, errorListener) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.FINISHTASKACT);
                map.put("username", username);
                map.put("password", password);
                map.put("tid", tID);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };
        return request;
    }

    /**
     * upload user location
     *
     * @param listener
     * @param errorListener
     * @param username
     * @param password
     * @param longitude
     * @param latitude
     * @param mode
     * @return
     */
    public static StringRequest uploadLocation(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                               final String username, final String password, final String longitude, final String latitude,
                                               final String mode) {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.UPLOAD_LOCATION, listener, errorListener) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.UPLOAD_LOCATION_ACT);
                map.put("username", username);
                map.put("password", password);
                map.put("longitude", longitude);
                map.put("latitude", latitude);
                map.put("mode", mode);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };
        return request;
    }

    /*************************QUERY*************************/

    /**
     * 获取抽样单的审核状态
     *
     * @param listener
     * @param errorListener
     * @param username
     * @param password
     * @param sids
     * @return
     */
    public static StringRequest getSamplingStatus(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                                  final String username, final String password, final String sids) {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.GETSAMPLINGSTATUS, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.GETSAMPLINGSTATUSACT);
                map.put("username", username);
                map.put("password", password);
                map.put("sampleid", sids);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };
        return request;
    }

    /**
     * 获取任务状态接口
     *
     * @param listener
     * @param errorListener
     * @param username
     * @param password
     * @param taskids
     * @return
     */
    public static StringRequest getTaskStatus(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                              final String username, final String password, final String taskids) {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.GETTASKSTATUS, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.GETTASKSTATUSACT);
                map.put("username", username);
                map.put("password", password);
                map.put("taskid", taskids);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };
        return request;
    }

    /***********************************抽样单**************************************/

    /**
     * 设置抽样单状态为已补采
     * @param listener
     * @param errorListener
     * @param username
     * @param password
     * @param sid 原抽样单的sid，用于服务器给原抽样单置位“已补采”
     * @return
     */
    public static StringRequest setSamplingStatusMadeUp(Response.Listener<String> listener, Response.ErrorListener errorListener,
                                                  final String username, final String password, final String sid){
        StringRequest request = new StringRequest(Request.Method.POST, URLs.SET_SAMPLING_STATUS_MADE_UP, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("act", URLs.SET_SAMPLING_STATUS_MADE_UP_ACT);
                map.put("username", username);
                map.put("password", password);
                map.put("sid", sid);
                map.put("UDID", Constant.DeviceID);
                return map;
            }
        };
        return request;
    }

}
