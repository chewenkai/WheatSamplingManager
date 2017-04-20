package com.aj.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.aj.database.TEMPLETTABLE;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TEMPLETTABLE".
*/
public class TEMPLETTABLEDao extends AbstractDao<TEMPLETTABLE, Long> {

    public static final String TABLENAME = "TEMPLETTABLE";

    /**
     * Properties of entity TEMPLETTABLE.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property TempletID = new Property(0, Long.class, "templetID", true, "TEMPLET_ID");
        public final static Property TaskID = new Property(1, Long.class, "taskID", false, "TASK_ID");
        public final static Property Templet_name = new Property(2, String.class, "templet_name", false, "TEMPLET_NAME");
        public final static Property Templet_content = new Property(3, String.class, "templet_content", false, "TEMPLET_CONTENT");
        public final static Property Download_time = new Property(4, Long.class, "download_time", false, "DOWNLOAD_TIME");
    };


    public TEMPLETTABLEDao(DaoConfig config) {
        super(config);
    }
    
    public TEMPLETTABLEDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TEMPLETTABLE\" (" + //
                "\"TEMPLET_ID\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: templetID
                "\"TASK_ID\" INTEGER," + // 1: taskID
                "\"TEMPLET_NAME\" TEXT," + // 2: templet_name
                "\"TEMPLET_CONTENT\" TEXT," + // 3: templet_content
                "\"DOWNLOAD_TIME\" INTEGER);"); // 4: download_time
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TEMPLETTABLE\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, TEMPLETTABLE entity) {
        stmt.clearBindings();
 
        Long templetID = entity.getTempletID();
        if (templetID != null) {
            stmt.bindLong(1, templetID);
        }
 
        Long taskID = entity.getTaskID();
        if (taskID != null) {
            stmt.bindLong(2, taskID);
        }
 
        String templet_name = entity.getTemplet_name();
        if (templet_name != null) {
            stmt.bindString(3, templet_name);
        }
 
        String templet_content = entity.getTemplet_content();
        if (templet_content != null) {
            stmt.bindString(4, templet_content);
        }
 
        Long download_time = entity.getDownload_time();
        if (download_time != null) {
            stmt.bindLong(5, download_time);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public TEMPLETTABLE readEntity(Cursor cursor, int offset) {
        TEMPLETTABLE entity = new TEMPLETTABLE( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // templetID
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // taskID
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // templet_name
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // templet_content
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4) // download_time
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, TEMPLETTABLE entity, int offset) {
        entity.setTempletID(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTaskID(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setTemplet_name(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTemplet_content(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setDownload_time(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(TEMPLETTABLE entity, long rowId) {
        entity.setTempletID(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(TEMPLETTABLE entity) {
        if(entity != null) {
            return entity.getTempletID();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
