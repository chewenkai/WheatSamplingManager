package com.aj.bean;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aj.Constant;
import com.aj.activity.CollectionApplication;
import com.aj.database.DaoMaster;
import com.aj.database.DaoSession;
import com.aj.database.SAMPLINGTABLE;
import com.aj.database.SAMPLINGTABLEDao;
import com.aj.database.TASKINFODao;
import com.aj.database.TEMPLETTABLE;
import com.aj.database.TEMPLETTABLEDao;

import java.io.File;
import java.util.List;

/**
 * Created by kevin on 15-10-8.
 * This class contain the method of processing the files
 * and file name which can extract some name information or file status.
 */
public class TaskInfo {

    /*about written templet files name*/
    final public static String TEMPLETNAME_HEAD = "<templet>";
    final public static String TEMPLETNAME_TAIL = "<templet|>";
    final public static String SAMPLENAME_HEAD = "<tablename>";
    final public static String SAMPLENAME_TAIL = "<tablename|>";

    final public static String STATUS_SAVE_HEAD = "<Ss>";
    final public static String STATUS_SAVE__TAIL = "<Ss|>";
    final public static String STATUS_UPLOAD_HEAD = "<Su>";
    final public static String STATUS_UPLOAD_TAIL = "<Su|>";

    final public static String KEVIN = "kevin";
    final public static String TEMPLET_FILE_SUFFIX = ".spms";

    //child list adapter
    final public static String LISTVIEW_TEMPLET_SUFFIX = "-模板";

    private Context mContext;
    private int PAGEFLAG;

    //database part
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private TASKINFODao taskinfoDao;
    private TEMPLETTABLEDao templettableDao;
    private SAMPLINGTABLEDao samplingtableDao;

    private Cursor cursor;

    public TaskInfo(Context context, int PAGEFLAG) {
        mContext = context;
        this.PAGEFLAG = PAGEFLAG;

        //database init
        daoSession =((CollectionApplication)((Activity)mContext).getApplication()).getDaoSession(mContext);
        taskinfoDao = daoSession.getTASKINFODao();
        templettableDao = daoSession.getTEMPLETTABLEDao();
        samplingtableDao = daoSession.getSAMPLINGTABLEDao();
    }

    /**
     * getAllTempletFiles
     *
     * @return
     */
    public List<TEMPLETTABLE> getAllTempletByTaskID(long taskID) {
        List<TEMPLETTABLE> templettables = templettableDao.queryBuilder().where(TEMPLETTABLEDao.Properties.TaskID.eq(taskID)).orderAsc(TEMPLETTABLEDao.Properties.TempletID).list();
        return templettables;
    }




    public List<SAMPLINGTABLE> getSampleTablesByTempletID(long templetID) {
        List<SAMPLINGTABLE> samplingtables = samplingtableDao.queryBuilder().
                where(SAMPLINGTABLEDao.Properties.TempletID.eq(templetID)).
                orderAsc(SAMPLINGTABLEDao.Properties.Id).list();
        return samplingtables;
    }


    public File getMediaFolderBySampleTable(SAMPLINGTABLE samplingtable){
        if(mContext.getPackageResourcePath()==null)
            return null;

        File toReturn=new File(mContext.getExternalCacheDir()+ File.separator + "mediaFolder"+File.separator+samplingtable.getMedia_folder());
        return toReturn;
    }





    public int[] getUploadIndicatorNum(long templetID) {
        int writtenTempletNum = 0;
        int updatedWrittenTempletNum = 0;

        List<SAMPLINGTABLE> samplingtables = getSampleTablesByTempletID(templetID);

        writtenTempletNum = samplingtables.size();
        for (int i = 0; i < writtenTempletNum; i++) {
            if (samplingtables.get(i).getCheck_status()!=Constant.S_STATUS_HAVE_NOT_UPLOAD) {
                updatedWrittenTempletNum++;
            }
        }


        int[] toReturn = {updatedWrittenTempletNum, writtenTempletNum};
        return toReturn;
    }


    public int[] getRightCommitButtonNum(long taskID) {
        int uploadedNum = 0;
        int allTemplet = 0;

        List<TEMPLETTABLE> templets = getAllTempletByTaskID(taskID);

        allTemplet = templets.size();

        for (int i = 0; i < allTemplet; i++) {
            int[] indicatorNum = getUploadIndicatorNum(templets.get(i).getTempletID());
            if (indicatorNum[0] == indicatorNum[1] && indicatorNum[0] != 0)
                uploadedNum++;
        }


        int[] toRetrun = {uploadedNum, allTemplet};
        return toRetrun;
    }

}
