package com.aj.collection.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.Constant;
import com.aj.collection.R;
import com.aj.collection.database.DaoMaster;
import com.aj.collection.database.DaoSession;
import com.aj.collection.database.SAMPLINGTABLE;
import com.aj.collection.database.SAMPLINGTABLEDao;
import com.aj.collection.database.TASKINFO;
import com.aj.collection.database.TASKINFODao;
import com.aj.collection.database.TEMPLETTABLEDao;
import com.aj.collection.http.API;
import com.aj.collection.http.URLs;
import com.aj.collection.tools.SPUtils;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DebugActivity extends Activity {
    TextView login, haveNewTask, getNewTask, setReceived, uploadFile, uploadSampling, uploadLocation, getSampleTask, getTaskStatus, output;

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    ProgressDialog progressDialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        queue = Volley.newRequestQueue(this); //init Volley
        //database init
        daoSession = ((CollectionApplication) ((Activity) mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();

        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);

        initUI();

        setListener();
    }

    private void setListener() {

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("登陆中...");
                progressDialog.show();

                Response.Listener<String> listener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressDialog.dismiss();
                        output.setText(s);
                        try {
                            JSONObject resultJson = new JSONObject(s);
                            String errorCode = resultJson.getString(URLs.KEY_ERROR);
                            String message = resultJson.getString(URLs.KEY_MESSAGE);
                            output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        output.setText(volleyError.toString());
                    }
                };
                StringRequest stringRequest = API.login(listener, errorListener, "kevin", "123456");
                queue.add(stringRequest);
            }
        });

        haveNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("检查有无新任务...");
                progressDialog.show();
                Response.Listener<String> listener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressDialog.dismiss();
                        output.setText(s);
                        try {
                            JSONObject resultJson = new JSONObject(s);
                            String errorCode = resultJson.getString(URLs.KEY_ERROR);
                            String message = resultJson.getString(URLs.KEY_MESSAGE);
                            output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        output.setText(volleyError.toString());
                    }
                };
                StringRequest stringRequest = API.haveNewTask(listener, errorListener, "kevin");
                queue.add(stringRequest);
            }
        });

        getNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("获取新任务...");
                progressDialog.show();
                Response.Listener<String> listener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressDialog.dismiss();
                        output.setText(s);
                        try {
                            JSONObject resultJson = new JSONObject(s);
                            String errorCode = resultJson.getString(URLs.KEY_ERROR);
                            String message = resultJson.getString(URLs.KEY_MESSAGE);
                            output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        output.setText(volleyError.toString());
                    }
                };
                StringRequest stringRequest = API.getNewTask(listener, errorListener, "kevin", "123456");
                queue.add(stringRequest);
            }
        });

        setReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testReceived();
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testUploadIMG();
            }
        });

        uploadSampling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testUploadSampling();
            }
        });

        uploadLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testUploadLocation();
            }
        });

        getSampleTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testGetSamplingStatus();
            }
        });

        getTaskStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testGetTaskStatus();
            }
        });
    }

    private void initUI() {
        login = (TextView) findViewById(R.id.debug_login);
        haveNewTask = (TextView) findViewById(R.id.debug_have_new_task);
        getNewTask = (TextView) findViewById(R.id.debug_get_new_task);
        setReceived = (TextView) findViewById(R.id.debug_set_received);
        uploadFile = (TextView) findViewById(R.id.debug_upload_file);
        uploadSampling = (TextView) findViewById(R.id.debug_upload_sampling);
        uploadLocation = (TextView) findViewById(R.id.debug_upload_location);
        getSampleTask = (TextView) findViewById(R.id.debug_get_sampling_status);
        getTaskStatus = (TextView) findViewById(R.id.debug_get_task_status);

        output = (TextView) findViewById(R.id.debug_output_info);
    }

    /***************************************************************************
     * *****************************接口测试**************************************
     **************************************************************************/
    RequestQueue queue;
    Context mContext = this;

    public void testReceived() {
        progressDialog.setMessage("设置接受...");
        progressDialog.show();
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                output.setText(s);
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String message = resultJson.getString(URLs.KEY_MESSAGE);
                    output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                output.setText(volleyError.toString());
            }
        };
        StringRequest stringRequest = API.setTaskReceived(listener, errorListener, "kevin");
        queue.add(stringRequest);
    }

    public void testUploadIMG() {
//        progressDialog.setMessage("上传文件...");
//        progressDialog.show();
//        RequestQueue mSingleQueue = Volley.newRequestQueue(mContext, new MultiPartStack());
//        Response.Listener<String> listener = new Response.Listener<String>() {
//            @Override
//            public void onResponse(String s) {
//                progressDialog.dismiss();
//                output.setText(s);
//                try {
//                    JSONObject resultJson = new JSONObject(s);
//                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
//                    String message = resultJson.getString(URLs.KEY_MESSAGE);
//                    output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        Response.ErrorListener errorListener = new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                progressDialog.dismiss();
//                output.setText(volleyError.toString());
//            }
//        };
//
//        File file = null;
//        File sdcard = Environment.getExternalStorageDirectory();
//        for (int i = 0; i < sdcard.listFiles().length; i++) {
//            if (sdcard.listFiles()[i].isFile()) {
//                file = sdcard.listFiles()[i];
//                break;
//            }
//        }
//
//
//        if (file == null || !file.exists()) {
//            output.setText("sdcard下没有文件！");
//            return;
//        }
//        ArrayList<File> files = new ArrayList<>();
//        files.add(file);
//
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("act", URLs.UPLOADSAMPLINGACT);
//
//        MultipartRequest multiPartRequest = API.uploadFiles(listener, errorListener, files, map);
//        mSingleQueue.add(multiPartRequest);

    }


    public void testUploadSampling() {
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                output.setText(s);
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String message = resultJson.getString(URLs.KEY_MESSAGE);
                    output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                output.setText(volleyError.toString());
            }
        };
        List<SAMPLINGTABLE> samplingtables = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.Is_uploaded.eq(false),
                        SAMPLINGTABLEDao.Properties.Check_status.notEq(Constant.S_STATUS_DELETE))
                .orderAsc(SAMPLINGTABLEDao.Properties.Id).list();
        if(samplingtables.size()==0){
            Toast.makeText(mContext,"无抽样单",Toast.LENGTH_LONG).show();
            return;
        }
        final SAMPLINGTABLE samplingtable = samplingtables.get(0);
        String taskName;
        if (taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(samplingtable.getTaskID())).list().size() != 1)
            return;
        else
            taskName = taskinfoDao.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(samplingtable.getTaskID())).list().get(0).getTask_name();

        //send to api
        StringRequest stringRequest = API.uploadSampling(listener, errorListener
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE)
                , (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE)
                , String.valueOf(samplingtable.getTaskID()), taskName, samplingtable.getSampling_content(), samplingtable.getShow_name(),
                String.valueOf(samplingtable.getSid_of_server()),88.8888d,88.8888d,1,"测试num",false);
        queue.add(stringRequest);
        progressDialog.setMessage("登陆中...");
        progressDialog.show();
    }

    public void testUploadLocation() {
        progressDialog.setMessage("上传位置...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                output.setText(s);
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String message = resultJson.getString(URLs.KEY_MESSAGE);
                    output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                output.setText(volleyError.toString());
            }
        };
        StringRequest stringRequest = API.uploadLocation(listener, errorListener,"kevin","123456",
                "12.22154", "15.12321", "1");

        queue.add(stringRequest);
    }

    public void testGetSamplingStatus() {
        progressDialog.setMessage("获取抽样单状态...");
        progressDialog.show();
        RequestQueue queue;
        queue = Volley.newRequestQueue(this); //init Volley

        JSONArray jsonArray = new JSONArray();
        List<SAMPLINGTABLE> allSamplingtables = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.Sid_of_server.isNotNull()).orderAsc().list();
        try {
            for (int i = 0; i < allSamplingtables.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("sid", allSamplingtables.get(i).getSid_of_server());
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                output.setText(s);
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String message = resultJson.getString(URLs.KEY_MESSAGE);
                    output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                output.setText(volleyError.toString());
            }
        };
        StringRequest stringRequest = API.getSamplingStatus(listener, errorListener, "kevin", "123456", jsonArray.toString());
        queue.add(stringRequest);
    }

    public void testGetTaskStatus() {
        progressDialog.setMessage("获取任务状态...");
        progressDialog.show();
        JSONArray jsonArray = new JSONArray();
        List<TASKINFO> alltaskinfos = taskinfoDao.queryBuilder().
                where(TASKINFODao.Properties.TaskID.isNotNull()).orderAsc().list();
        try {
            for (int i = 0; i < alltaskinfos.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tid", alltaskinfos.get(i).getTaskID());
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progressDialog.dismiss();
                output.setText(s);
                try {
                    JSONObject resultJson = new JSONObject(s);
                    String errorCode = resultJson.getString(URLs.KEY_ERROR);
                    String message = resultJson.getString(URLs.KEY_MESSAGE);
                    output.setText(s+"\n错误码："+errorCode+"\n消息："+message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                output.setText(volleyError.toString());
            }
        };
        StringRequest stringRequest = API.getTaskStatus(listener, errorListener,
                (String) SPUtils.get(mContext, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE),
                (String) SPUtils.get(mContext, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE), jsonArray.toString());

        queue.add(stringRequest);
    }
}
