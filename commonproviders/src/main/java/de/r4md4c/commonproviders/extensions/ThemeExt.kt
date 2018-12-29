package de.r4md4c.commonproviders.extensions

import android.content.Context
import android.util.TypedValue

fun Context.resolveThemeAttribute(resId: Int): TypedValue =
    TypedValue().apply { theme.resolveAttribute(resId, this, true) }
