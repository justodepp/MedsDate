package com.medsdate.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for storing and retrieving locale preferences.
 *
 * Uses SharedPreferences for synchronous access, which is needed
 * for applying locale in attachBaseContext().
 */
object LocalePreferences {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE_CODE = "language_code"
    private const val DEFAULT_LANGUAGE = "it" // Italian as default

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves the language code to SharedPreferences.
     */
    fun saveLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit()
            .putString(KEY_LANGUAGE_CODE, languageCode)
            .apply()
    }

    /**
     * Retrieves the saved language code, or returns Italian as default.
     */
    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE_CODE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
}
