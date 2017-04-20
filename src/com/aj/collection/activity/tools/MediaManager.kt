package com.aj.collection.activity.tools

import android.content.Context
import java.io.File

/**
 * 该类用于管理抽样系统产生的媒体文件，包括拍摄的照片，视频，签名图片等
 * Created by kevin on 17-4-20.
 * Mail: chewenkaich@gmail.com
 */
class MediaManager {
    /**
     * 获取图片文件夹中名字以"CAMERA_"开头的照片
     */
    fun getLatestCameraFiles(mContext: Context, autoGeneratedSheetID: String): ArrayList<File>{
        val files = ArrayList<File>()
        val mediaFolder = File(Util.getMediaFolder(mContext) + File.separator + autoGeneratedSheetID)
        if (!mediaFolder.exists())
            mediaFolder.mkdirs()

        //其他的照片遍历child文件夹得道
        if (mediaFolder.listFiles() != null) {
            (0..mediaFolder.listFiles().size - 1)
                    .filter { mediaFolder.listFiles()[it].name.startsWith("CAMERA_") }
                    .mapTo(files) { mediaFolder.listFiles()[it] }
        }
        return files
    }
}