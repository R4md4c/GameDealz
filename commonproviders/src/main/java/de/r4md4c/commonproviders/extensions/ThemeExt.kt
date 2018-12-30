package de.r4md4c.commonproviders.extensions

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

fun Context.resolveThemeAttribute(@AttrRes resId: Int): TypedValue =
    TypedValue().apply { theme.resolveAttribute(resId, this, true) }
