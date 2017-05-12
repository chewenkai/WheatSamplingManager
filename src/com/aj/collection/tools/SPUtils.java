package com.aj.collection.tools;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by kevin on 15-8-26.
 */
public class SPUtils {
    public SPUtils()
    {
		/* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 保存在手机里面的文件名
     */
    // Farmer information
    public static final String FARMER_PROVINCE = "farmer_province";
    public static final String FARMER_CITY = "farmer_city";
    public static final String FARMER_COUNTRY = "farmer_country";
    public static final String FARMER_NAME = "farmer_name";
    public static final String FARMER_PHONE_NUMBER = "farmer_phone_number";
    public static final String FARMER_IDENTITY_NUMBER = "farmer_identity_number";
    public static final String FARMER_ADDRESS = "farmer_address";
    public static final String FARMER_POST_NUMBER = "farmer_postnumber";
    public static final String FARMER_DEPOSIT_CARD_NAME = "bank_card_name";
    public static final String FARMER_DEPOSIT_CARD_NUMBER = "bank_card_number";
    public static final String FARMER_BANK_NAME = "band_name";
    public static final String SAMPLING_COMPANY_ID="company_id";

    public static final String SAMPLING_CACHED_SID_NAME="cached_sid";
    public static final String SAMPLING_CACHED_SID="sampling_cached_sid";

    public static final String USER_DATA = "user_data";
    public static final String UNIT_NAME = "unit_name";
    public static final String UNIT_PHONE = "unit_phone";
    public static final String UNIT_ADDR = "unit_addr";
    public static final String UNIT_CZ = "unit_cz";
    public static final String UNIT_POST = "unit_post";
    public static final String UNIT_USER = "unit_user";
    public static final String LOGIN_USER = "login_user";
    public static final String JIANKONG = "jiankong_dianhua" ;
    public static final String KAIGUAN = "kai_guan";
    public static final String WHICHTASK = "which_task";
    //------after 2015.10.19-----
    public static final String LOGIN_VALIDATE = "login_validate";
    public static final String LOGIN_NAME = "login_name";
    public static final String LOGIN_PASSWORD = "login_password";

    public static final String USER_INFO="user_info";
    public static final String SAMPLING_COMPANY="sampling_company";
    public static final String SAMPLING_ADDR="sampling_addr";
    public static final String SAMPLING_CONTACT="sampling_contact";
    public static final String SAMPLING_PHONE="sampling_phone";

    public static final String RECEIVED_TASK="received_task";
    public static final String TEMPORARY_SAVE="temporary";

    //if need keep service when exit?
    public static final String KEEPSERVICE="keep_service";

    //system variable
    public static final String SYSVARIABLE="system_variable";
    public static final String DEV_SN="device_serial_number";

    public static final String SHEET_SERIAL_NUMBER_KEY = "sheet_serial_number";
    public static final String SHEET_SERIAL_NUMBER_FILE = "sheet_serial_number_file";
    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void put(Context context, String key, Object object,String fileName)
    {

        SharedPreferences sp = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String)
        {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer)
        {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean)
        {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float)
        {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long)
        {
            editor.putLong(key, (Long) object);
        } else
        {
            editor.putString(key, object.toString());
        }

        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object get(Context context, String key, Object defaultObject,String fileName)
    {
        SharedPreferences sp = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);

        if (defaultObject instanceof String)
        {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer)
        {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean)
        {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float)
        {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long)
        {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key,String fileName)
    {
        SharedPreferences sp = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context,String fileName)
    {
        SharedPreferences sp = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key,String fileName)
    {
        SharedPreferences sp = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context,String fileName)
    {
        SharedPreferences sp = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return sp.getAll();
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     *
     */
    private static class SharedPreferencesCompat
    {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static Method findApplyMethod()
        {
            try
            {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e)
            {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor)
        {
            try
            {
                if (sApplyMethod != null)
                {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e)
            {
            } catch (IllegalAccessException e)
            {
            } catch (InvocationTargetException e)
            {
            }
            editor.commit();
        }
    }
}
