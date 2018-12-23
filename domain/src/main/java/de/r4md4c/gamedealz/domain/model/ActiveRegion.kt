package de.r4md4c.gamedealz.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ActiveRegion(val regionCode: String, val country: CountryModel, val currency: CurrencyModel) : Parcelable
