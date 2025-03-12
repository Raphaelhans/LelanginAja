package com.example.project

import android.os.Parcel
import android.os.Parcelable

data class AuctionItem(
    val id: Int,
    val name: String,
    val category: String,
    val currentBid: Double,
    val imageResId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeDouble(currentBid)
        parcel.writeInt(imageResId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AuctionItem> {
        override fun createFromParcel(parcel: Parcel): AuctionItem = AuctionItem(parcel)
        override fun newArray(size: Int): Array<AuctionItem?> = arrayOfNulls(size)
    }
}