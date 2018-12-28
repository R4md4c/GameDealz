package de.r4md4c.gamedealz.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShopModel(val id: String, val name: String, val rgbColor: String) : Parcelable
