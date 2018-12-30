package de.r4md4c.gamedealz.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HistoricalLowModel(
    val shop: ShopModel,
    val price: Float,
    val priceCutPercentage: Short,
    val added: Long
) : Parcelable
