package com.medsdate.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/** Date formatter with the pattern the locale uses for [skeleton] (e.g. "yMMMd": "Nov 16, 2026" / "16 nov 2026"). */
fun localizedDateFormat(locale: Locale, skeleton: String = "yMMMd"): SimpleDateFormat =
    SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
