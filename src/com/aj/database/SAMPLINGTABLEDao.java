package com.aj.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.aj.database.SAMPLINGTABLE;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SAMPLINGTABLE".
*/
public class SAMPLINGTABLEDao extends AbstractDao<SAMPLINGTABLE, Long> {

    public static final String TABLENAME = "SAMPLINGTABLE";

    /**
     * Properties of entity SAMPLINGTABLE.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property TaskID = new Property(1, Long.class, "taskID", false, "TASK_ID");
        public final static Property TempletID = new Property(2, Long.class, "templetID", false, "TEMPLET_ID");
        public final static Property Show_name = new Property(3, String.class, "show_name", false, "SHOW_NAME");
        public final static Property Sampling_address = new Property(4, String.class, "sampling_address", false, "SAMPLING_ADDRESS");
        public final static Property Sampling_content = new Property(5, String.class, "sampling_content", false, "SAMPLING_CONTENT");
        public final static Property Media_folder = new Property(6, String.class, "media_folder", false, "MEDIA_FOLDER");
        public final static Property Is_saved = new Property(7, Boolean.class, "is_saved", false, "IS_SAVED");
        public final static Property Is_uploaded = new Property(8, Boolean.class, "is_uploaded", false, "IS_UPLOADED");
        public final static Property Is_server_sampling = new Property(9, Boolean.class, "is_server_sampling", false, "IS_SERVER_SAMPLING");
        public final static Property Is_make_up = new Property(10, Boolean.class, "is_make_up", false, "IS_MAKE_UP");
        public final static Property Check_status = new Property(11, Integer.class, "check_status", false, "CHECK_STATUS");
        public final static Property Saved_time = new Property(12, Long.class, "saved_time", false, "SAVED_TIME");
        public final static Property Uploaded_time = new Property(13, Long.class, "uploaded_time", false, "UPLOADED_TIME");
        public final static Property Sid_of_server = new Property(14, Long.class, "sid_of_server", false, "SID_OF_SERVER");
        public final static Property Latitude = new Property(15, Double.class, "latitude", false, "LATITUDE");
        public final static Property Longitude = new Property(16, Double.class, "longitude", false, "LONGITUDE");
        public final static Property Location_mode = new Property(17, Integer.class, "location_mode", false, "LOCATION_MODE");
        public final static Property Sampling_unique_num = new Property(18, String.class, "sampling_unique_num", false, "SAMPLING_UNIQUE_NUM");
    };


    public SAMPLINGTABLEDao(DaoConfig config) {
        super(config);
    }
    
    public SAMPLINGTABLEDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SAMPLINGTABLE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"TASK_ID\" INTEGER," + // 1: taskID
                "\"TEMPLET_ID\" INTEGER," + // 2: templetID
                "\"SHOW_NAME\" TEXT," + // 3: show_name
                "\"SAMPLING_ADDRESS\" TEXT," + // 4: sampling_address
                "\"SAMPLING_CONTENT\" TEXT," + // 5: sampling_content
                "\"MEDIA_FOLDER\" TEXT," + // 6: media_folder
                "\"IS_SAVED\" INTEGER," + // 7: is_saved
                "\"IS_UPLOADED\" INTEGER," + // 8: is_uploaded
                "\"IS_SERVER_SAMPLING\" INTEGER," + // 9: is_server_sampling
                "\"IS_MAKE_UP\" INTEGER," + // 10: is_make_up
                "\"CHECK_STATUS\" INTEGER," + // 11: check_status
                "\"SAVED_TIME\" INTEGER," + // 12: saved_time
                "\"UPLOADED_TIME\" INTEGER," + // 13: uploaded_time
                "\"SID_OF_SERVER\" INTEGER," + // 14: sid_of_server
                "\"LATITUDE\" REAL," + // 15: latitude
                "\"LONGITUDE\" REAL," + // 16: longitude
                "\"LOCATION_MODE\" INTEGER," + // 17: location_mode
                "\"SAMPLING_UNIQUE_NUM\" TEXT);"); // 18: sampling_unique_num
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SAMPLINGTABLE\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, SAMPLINGTABLE entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long taskID = entity.getTaskID();
        if (taskID != null) {
            stmt.bindLong(2, taskID);
        }
 
        Long templetID = entity.getTempletID();
        if (templetID != null) {
            stmt.bindLong(3, templetID);
        }
 
        String show_name = entity.getShow_name();
        if (show_name != null) {
            stmt.bindString(4, show_name);
        }
 
        String sampling_address = entity.getSampling_address();
        if (sampling_address != null) {
            stmt.bindString(5, sampling_address);
        }
 
        String sampling_content = entity.getSampling_content();
        if (sampling_content != null) {
            stmt.bindString(6, sampling_content);
        }
 
        String media_folder = entity.getMedia_folder();
        if (media_folder != null) {
            stmt.bindString(7, media_folder);
        }
 
        Boolean is_saved = entity.getIs_saved();
        if (is_saved != null) {
            stmt.bindLong(8, is_saved ? 1L: 0L);
        }
 
        Boolean is_uploaded = entity.getIs_uploaded();
        if (is_uploaded != null) {
            stmt.bindLong(9, is_uploaded ? 1L: 0L);
        }
 
        Boolean is_server_sampling = entity.getIs_server_sampling();
        if (is_server_sampling != null) {
            stmt.bindLong(10, is_server_sampling ? 1L: 0L);
        }
 
        Boolean is_make_up = entity.getIs_make_up();
        if (is_make_up != null) {
            stmt.bindLong(11, is_make_up ? 1L: 0L);
        }
 
        Integer check_status = entity.getCheck_status();
        if (check_status != null) {
            stmt.bindLong(12, check_status);
        }
 
        Long saved_time = entity.getSaved_time();
        if (saved_time != null) {
            stmt.bindLong(13, saved_time);
        }
 
        Long uploaded_time = entity.getUploaded_time();
        if (uploaded_time != null) {
            stmt.bindLong(14, uploaded_time);
        }
 
        Long sid_of_server = entity.getSid_of_server();
        if (sid_of_server != null) {
            stmt.bindLong(15, sid_of_server);
        }
 
        Double latitude = entity.getLatitude();
        if (latitude != null) {
            stmt.bindDouble(16, latitude);
        }
 
        Double longitude = entity.getLongitude();
        if (longitude != null) {
            stmt.bindDouble(17, longitude);
        }
 
        Integer location_mode = entity.getLocation_mode();
        if (location_mode != null) {
            stmt.bindLong(18, location_mode);
        }
 
        String sampling_unique_num = entity.getSampling_unique_num();
        if (sampling_unique_num != null) {
            stmt.bindString(19, sampling_unique_num);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public SAMPLINGTABLE readEntity(Cursor cursor, int offset) {
        SAMPLINGTABLE entity = new SAMPLINGTABLE( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // taskID
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // templetID
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // show_name
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // sampling_address
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // sampling_content
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // media_folder
            cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0, // is_saved
            cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0, // is_uploaded
            cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0, // is_server_sampling
            cursor.isNull(offset + 10) ? null : cursor.getShort(offset + 10) != 0, // is_make_up
            cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11), // check_status
            cursor.isNull(offset + 12) ? null : cursor.getLong(offset + 12), // saved_time
            cursor.isNull(offset + 13) ? null : cursor.getLong(offset + 13), // uploaded_time
            cursor.isNull(offset + 14) ? null : cursor.getLong(offset + 14), // sid_of_server
            cursor.isNull(offset + 15) ? null : cursor.getDouble(offset + 15), // latitude
            cursor.isNull(offset + 16) ? null : cursor.getDouble(offset + 16), // longitude
            cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17), // location_mode
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18) // sampling_unique_num
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, SAMPLINGTABLE entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTaskID(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setTempletID(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setShow_name(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setSampling_address(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setSampling_content(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setMedia_folder(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setIs_saved(cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0);
        entity.setIs_uploaded(cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0);
        entity.setIs_server_sampling(cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0);
        entity.setIs_make_up(cursor.isNull(offset + 10) ? null : cursor.getShort(offset + 10) != 0);
        entity.setCheck_status(cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11));
        entity.setSaved_time(cursor.isNull(offset + 12) ? null : cursor.getLong(offset + 12));
        entity.setUploaded_time(cursor.isNull(offset + 13) ? null : cursor.getLong(offset + 13));
        entity.setSid_of_server(cursor.isNull(offset + 14) ? null : cursor.getLong(offset + 14));
        entity.setLatitude(cursor.isNull(offset + 15) ? null : cursor.getDouble(offset + 15));
        entity.setLongitude(cursor.isNull(offset + 16) ? null : cursor.getDouble(offset + 16));
        entity.setLocation_mode(cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17));
        entity.setSampling_unique_num(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(SAMPLINGTABLE entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(SAMPLINGTABLE entity) {
        if(entity != null) {
            return entity.getId();
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