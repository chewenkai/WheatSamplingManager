package com.aj.collection.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by kevin on 17-4-22.
 * Mail: chewenkaich@gmail.com
 */
class Sheet(var id: Long?, var taskID: Long?, var templetID: Long?,
            var show_name: String?, var sampling_address: String?, var sampling_content: String?,
            var media_folder: String?, var is_saved: Boolean?, var is_uploaded: Boolean?,
            var is_server_sampling: Boolean?, var is_make_up: Boolean?, var check_status: Int?,
            var saved_time: Long?, var uploaded_time: Long?, var sid_of_server: Long?,
            var latitude: Double?, var longitude: Double?, var location_mode: Int?,
            var sampling_unique_num: String?, var isTemplate: Boolean?, var isHidden: Boolean?) : Parcelable, ChildItem() {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Sheet> = object : Parcelable.Creator<Sheet> {
            override fun createFromParcel(source: Parcel): Sheet = Sheet(source)
            override fun newArray(size: Int): Array<Sheet?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readLong(), source.readLong(), source.readLong(), source.readString(), source.readString(), source.readString(), source.readString(), 1.equals(source.readInt()), 1.equals(source.readInt()), 1.equals(source.readInt()), 1.equals(source.readInt()), source.readInt(), source.readLong(), source.readLong(), source.readLong(), source.readDouble(), source.readDouble(), source.readInt(), source.readString(), 1.equals(source.readInt()), 1.equals(source.readInt()))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(id!!)
        dest?.writeLong(taskID!!)
        dest?.writeLong(templetID!!)
        dest?.writeString(show_name!!)
        dest?.writeString(sampling_address!!)
        dest?.writeString(sampling_content!!)
        dest?.writeString(media_folder)
        dest?.writeInt((if (is_saved!!) 1 else 0))
        dest?.writeInt((if (is_uploaded!!) 1 else 0))
        dest?.writeInt((if (is_server_sampling!!) 1 else 0))
        dest?.writeInt((if (is_make_up!!) 1 else 0))
        dest?.writeInt(check_status!!)
        dest?.writeLong(saved_time!!)
        dest?.writeLong(uploaded_time!!)
        dest?.writeLong(sid_of_server!!)
        dest?.writeDouble(latitude!!)
        dest?.writeDouble(longitude!!)
        dest?.writeInt(location_mode!!)
        dest?.writeString(sampling_unique_num!!)
        dest?.writeInt((if (isTemplate!!) 1 else 0))
        dest?.writeInt((if (isHidden!!) 1 else 0))
    }
}