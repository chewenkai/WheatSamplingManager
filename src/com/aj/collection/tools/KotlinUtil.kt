package com.aj.collection.tools

import android.content.Context
import com.aj.Constant
import com.aj.collection.bean.SheetCell
import com.aj.collection.database.*
import com.aj.collection.http.URLs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Long

/**
 * Created by kevin on 17-5-13.
 * Mail: chewenkaich@gmail.com
 */
class KotlinUtil {
    companion object {
        fun getLocalSIds(mContext: Context): ArrayList<String> {
            val localSID = SPUtils.get(mContext, SPUtils.SAMPLING_CACHED_SID, "",
                    SPUtils.SAMPLING_CACHED_SID_NAME) as String

            var cachedSID = ArrayList<String>()
            if (localSID != null && localSID.isNotEmpty() && localSID.isNotBlank()) {
                // 利用Gson将Json文本转为SheetCell列表
                try {
                    val turnsType = object : TypeToken<ArrayList<String>>() {}.type
                    cachedSID = Gson().fromJson(localSID, turnsType)
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }

            }
            return cachedSID
        }

        /**
         * 返回结果为是否有新任务
         */
        fun parseTaskDataIntoDatabase(mContext: Context, taskJsonObject: JSONObject, taskinfoDao: TASKINFODao?,
                                      templettableDao: TEMPLETTABLEDao?, samplingtableDao: SAMPLINGTABLEDao?):Boolean {
            var hasNewTask=false
            try {
                val taskID = taskJsonObject.getString(URLs.KEY_TASKID)
                val taskName = taskJsonObject.getString(URLs.KEY_TASKNAME)
                val taskLetter = taskJsonObject.getString(URLs.KEY_TASK_INI_LETTER)
                val taskDes = taskJsonObject.getString(URLs.KEY_TASKDISCRIPTION)
                val taskCont = taskJsonObject.getString(URLs.KEY_TASKCONT)
                val sampling = taskJsonObject.getString(URLs.KEY_SAMPLING)//定点采样的抽样单

                //任务id已存在，则不存入数据
                val searchTasks = taskinfoDao!!.queryBuilder().where(TASKINFODao.Properties.TaskID.eq(taskID)).list()
                var theTask: TASKINFO? = null
                var templettable: TEMPLETTABLE? = null
                if (searchTasks.size != 1) {  // 数据库中没有该任务，插入
                    theTask = TASKINFO(taskID.toLong(), taskName, taskLetter,
                            false, true, System.currentTimeMillis(), taskDes)
                    taskinfoDao?.insertOrReplace(theTask)
                    //insert Templet
                    templettable = TEMPLETTABLE(null, theTask.taskID, theTask.task_name, taskCont, System.currentTimeMillis())
                    templettableDao?.insertOrReplace(templettable)
                    templettable = templettableDao?.queryBuilder()?.where(TEMPLETTABLEDao.Properties.TaskID.eq(theTask.taskID))?.list()?.get(0)
                    hasNewTask = true
                } else {
                    theTask = searchTasks[0]
                    templettable = templettableDao?.queryBuilder()?.where(TEMPLETTABLEDao.Properties.TaskID.eq(theTask.taskID))?.list()?.get(0)
                }

                //获取该用户在该任务下的抽样单
                val samplingsArray = JSONArray(sampling)

                for (j in 0..samplingsArray.length() - 1) {
                    val samplingID = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLINGID)
                    val samplingCont = samplingsArray.getJSONObject(j).getString(URLs.KEY_SAMPLINGCONT)
                    val samplingName = samplingsArray.getJSONObject(j).getString(URLs.KEY_ITEMS)
                    val samplingNum = samplingsArray.getJSONObject(j).getString(URLs.KEY_ITEMSID)
                    val mediaFolderChild = Util.getSamplingNum(mContext, theTask)
                    val searchSamples = samplingtableDao?.queryBuilder()?.where(SAMPLINGTABLEDao.Properties.Sid_of_server.eq(samplingID))?.list()
                    // 判断本地是否存在该抽样单
                    if (searchSamples?.size != 1) {  // 抽样单不存在,插入抽样单
                        // 提取抽样单Json文本
                        var sheetCells = JSONObject(samplingCont).getString(SheetProtocol().SHEET_JSON_KEY)
                        // 利用Gson将Json文本转为SheetCell列表
                        val turnsType = object : TypeToken<List<SheetCell>>() {}.type
                        val sheetCellList: ArrayList<SheetCell> = Gson().fromJson(sheetCells, turnsType)

                        val samplingtable = SAMPLINGTABLE(null, Long.valueOf(taskID), templettable?.templetID, samplingName, sheetCellList[1].cell_value,
                                samplingCont, mediaFolderChild, false, false, true, false, Constant.S_STATUS_HAVE_NOT_UPLOAD, System.currentTimeMillis(), null, Long.valueOf(samplingID), null, null, null, samplingNum, false)

                        samplingtableDao!!.insertOrReplace(samplingtable)
                    }
                    // 抽样单存在，不做任何操作
                }
                return hasNewTask
            }catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }
}