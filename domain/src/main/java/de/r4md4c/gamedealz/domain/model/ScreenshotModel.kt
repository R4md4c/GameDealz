package de.r4md4c.gamedealz.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScreenshotModel(val thumbnail: String, val full: String) : Parcelable
