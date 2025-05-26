package com.example.project.ui.auction

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.project.R

data class AuctionItem(
    val id: String,
    val name: String,
    val category: String,
    val currentBid: Double,
    val imageResId: String,
    val sellerId: Int,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeDouble(currentBid)
        parcel.writeString(imageResId)
        parcel.writeInt(sellerId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AuctionItem> {
        override fun createFromParcel(parcel: Parcel): AuctionItem = AuctionItem(parcel)
        override fun newArray(size: Int): Array<AuctionItem?> = arrayOfNulls(size)
    }
}

class AuctionItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auctiondetail)

        val auctionItem = intent.getParcelableExtra<AuctionItem>("auction_item")

    }
}
