package com.example.project.ui.transaction

import android.os.Parcel
import android.os.Parcelable

data class TransactionItem(
    val id: Int,
    val type: String,
    val itemName: String,
    val date: String,
    val status: String,
    val typeIconResId: Int,
    val itemImageResId: Int,
    val lastBid: Int

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(type)
        parcel.writeString(itemName)
        parcel.writeString(date)
        parcel.writeString(status)
        parcel.writeInt(typeIconResId)
        parcel.writeInt(lastBid)
    }

    companion object CREATOR : Parcelable.Creator<TransactionItem> {
        override fun createFromParcel(parcel: Parcel): TransactionItem {
            return TransactionItem(parcel)
        }

        override fun newArray(size: Int): Array<TransactionItem?> {
            return arrayOfNulls(size)
        }
    }
}