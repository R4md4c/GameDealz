package de.r4md4c.gamedealz.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CurrencyModel(val currencyCode: String, val sign: String) : Parcelable

