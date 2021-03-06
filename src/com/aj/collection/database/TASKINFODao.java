package com.aj.collection.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TASKINFO".
*/
public class TASKINFODao extends AbstractDao<TASKINFO, Void> {

    public static final String TABLENAME = "TASKINFO";

    /**
     * Properties of entity TASKINFO.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property TaskID = new Property(0, Long.class, "taskID", false, "TASK_ID");
        public final static Property Task_name = new Property(1, String.class, "task_name", false, "TASK_NAME");
        public final static Property Task_letter = new Property(2, String.class, "task_letter", false, "TASK_LETTER");
        public final static Property Is_finished = new Property(3, Boolean.class, "is_finished", false, "IS_FINISHED");
        public final static Property Is_new_task = new Property(4, Boolean.class, "is_new_task", false, "IS_NEW_TASK");
        public final static Property Download_time = new Property(5, Long.class, "download_time", false, "DOWNLOAD_TIME");
        public final static Property Description = new Property(6, String.class, "description", false, "DESCRIPTION");
    }


    public TASKINFODao(DaoConfig config) {
        super(config);
    }
    
    public TASKINFODao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TASKINFO\" (" + //
                "\"TASK_ID\" INTEGER UNIQUE ," + // 0: taskID
                "\"TASK_NAME\" TEXT," + // 1: task_name
                "\"TASK_LETTER\" TEXT," + // 2: task_letter
                "\"IS_FINISHED\" INTEGER," + // 3: is_finished
                "\"IS_NEW_TASK\" INTEGER," + // 4: is_new_task
                "\"DOWNLOAD_TIME\" INTEGER," + // 5: download_time
                "\"DESCRIPTION\" TEXT);"); // 6: description
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TASKINFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, TASKINFO entity) {
        stmt.clearBindings();
 
        Long taskID = entity.getTaskID();
        if (taskID != null) {
            stmt.bindLong(1, taskID);
        }
 
        String task_name = entity.getTask_name();
        if (task_name != null) {
            stmt.bindString(2, task_name);
        }
 
        String task_letter = entity.getTask_letter();
        if (task_letter != null) {
            stmt.bindString(3, task_letter);
        }
 
        Boolean is_finished = entity.getIs_finished();
        if (is_finished != null) {
            stmt.bindLong(4, is_finished ? 1L: 0L);
        }
 
        Boolean is_new_task = entity.getIs_new_task();
        if (is_new_task != null) {
            stmt.bindLong(5, is_new_task ? 1L: 0L);
        }
 
        Long download_time = entity.getDownload_time();
        if (download_time != null) {
            stmt.bindLong(6, download_time);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(7, description);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, TASKINFO entity) {
        stmt.clearBindings();
 
        Long taskID = entity.getTaskID();
        if (taskID != null) {
            stmt.bindLong(1, taskID);
        }
 
        String task_name = entity.getTask_name();
        if (task_name != null) {
            stmt.bindString(2, task_name);
        }
 
        String task_letter = entity.getTask_letter();
        if (task_letter != null) {
            stmt.bindString(3, task_letter);
        }
 
        Boolean is_finished = entity.getIs_finished();
        if (is_finished != null) {
            stmt.bindLong(4, is_finished ? 1L: 0L);
        }
 
        Boolean is_new_task = entity.getIs_new_task();
        if (is_new_task != null) {
            stmt.bindLong(5, is_new_task ? 1L: 0L);
        }
 
        Long download_time = entity.getDownload_time();
        if (download_time != null) {
            stmt.bindLong(6, download_time);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(7, description);
        }
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public TASKINFO readEntity(Cursor cursor, int offset) {
        TASKINFO entity = new TASKINFO( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // taskID
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // task_name
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // task_letter
            cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0, // is_finished
            cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0, // is_new_task
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // download_time
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // description
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, TASKINFO entity, int offset) {
        entity.setTaskID(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTask_name(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setTask_letter(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setIs_finished(cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0);
        entity.setIs_new_task(cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0);
        entity.setDownload_time(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setDescription(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    @Override
    protected final Void updateKeyAfterInsert(TASKINFO entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(TASKINFO entity) {
        return null;
    }

    @Override
    public boolean hasKey(TASKINFO entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
