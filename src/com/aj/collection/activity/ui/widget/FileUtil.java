package com.aj.collection.activity.ui.widget;

import java.io.File;

/**
 * Created by kevin on 15-9-27.
 */
public class FileUtil {

    /**
     * 删除文件和文件夹
     * @param file
     */
    public static void delete(File file){
        if(file.isFile()){
            file.delete();
            return;
        }

        if(file.isDirectory()){
            File[] childFiles=file.listFiles();
            if(childFiles ==null || childFiles.length==0){
                file.delete();
                return;
            }
            for(int i=0;i<childFiles.length;i++){
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
}
