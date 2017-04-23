package com.aj.collection.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */
class Task(var taskID: Long, var task_name: String, var task_letter: String,
           var is_finished: Boolean, var is_new_task: Boolean, var download_time: Long, var description: String) : Parcelable {
    companion object {
        val CREATOR: Parcelable.Creator<Task> = object : Parcelable.Creator<Task> {
            override fun createFromParcel(source: Parcel): Task = Task(source)
            override fun newArray(size: Int): Array<Task?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readLong(), source.readString(), source.readString(), 1.equals(source.readInt()), 1.equals(source.readInt()), source.readLong(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(taskID)
        dest?.writeString(task_name)
        dest?.writeString(task_letter)
        dest?.writeInt((if (is_finished) 1 else 0))
        dest?.writeInt((if (is_new_task) 1 else 0))
        dest?.writeLong(download_time)
        dest?.writeString(description)
    }
}