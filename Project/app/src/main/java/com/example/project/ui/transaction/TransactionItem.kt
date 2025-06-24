package com.example.project.ui.transaction

import android.os.Parcel
import android.os.Parcelable

data class TransactionItem(
    val id: String,
    val type: String,
    val itemName: String,
    val itemId: String,
    val date: String,
    val status: String,
    val info: String,
    val amount: Int,
    val typeIconResId: Int,
    val itemImageResId: Int,
    val lastBid: Int

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(type)
        parcel.writeString(itemName)
        parcel.writeString(itemId)
        parcel.writeString(date)
        parcel.writeString(status)
        parcel.writeString(info)
        parcel.writeInt(amount)
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