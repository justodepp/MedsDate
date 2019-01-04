package com.medsdate.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import com.medsdate.R
import java.io.IOException
import java.util.*

object Utility {

    private val TAG = "Utility"

    fun getSpan(context: Context): Int {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || isTablet(context)) {
            2
        } else 1
    }

    fun isTablet(context: Context): Boolean {
        return context.resources
                .getBoolean(R.bool.isTablet)
    }

    fun getDatePart(date: Date): Long {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.timeInMillis
    }

    fun loadImage(context: Context, name: String): Drawable? {
        try {
            // get input stream
            val ims = context.assets.open("img_meds/$name")
            // load image as Drawable
            // set image to ImageView
            return Drawable.createFromStream(ims, null)
        } catch (ex: IOException) {
            return null
        }
    }
}
