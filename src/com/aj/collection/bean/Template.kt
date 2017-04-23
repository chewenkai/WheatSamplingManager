package com.aj.collection.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */
class Template(var templetID: Long, var taskID: Long, var templet_name: String,
               var templet_content: String, var download_time: Long) : Parcelable, ChildItem() {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Template> = object : Parcelable.Creator<Template> {
            override fun createFromParcel(source: Parcel): Template = Template(source)
            override fun newArray(size: Int): Array<Template?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readLong(), source.readLong(), source.readString(), source.readString(), source.readLong())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(templetID)
        dest?.writeLong(taskID)
        dest?.writeString(templet_name)
        dest?.writeString(templet_content)
        dest?.writeLong(download_time)
    }
}
