package com.medsdate.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Utility object for managing app localization.
 *
 * This manager handles runtime locale changes and provides a consistent
 * way to apply language settings across the app.
 */
object LocaleManager {

    /**
     * Available languages in the app.
     *
     * @property code ISO 639-1 language code
     * @property displayName Language name in its native form
     * @property flag Emoji flag for the language
     */
    enum class Language(val code: String, val displayName: String, val flag: String) {
        ITALIAN("it", "Italiano", "\uD83C\uDDEE\uD83C\uDDF9"), // 🇮🇹
        ENGLISH("en", "English", "\uD83C\uDDEC\uD83C\uDDE7"); // 🇬🇧

        companion object {
            fun fromCode(code: String): Language {
                return entries.find { it.code == code } ?: ITALIAN
            }
        }
    }

    /**
     * Applies the given locale to the context.
     *
     * @param context The context to update
     * @param languageCode The ISO 639-1 language code (e.g., "it", "en")
     * @return Updated context with the new locale
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Gets the currently applied locale from context.
     *
     * @param context The context
     * @return The current locale
     */
    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }

    /**
     * Applies locale and recreates the activity to reflect changes.
     *
     * @param activity The activity to recreate
     * @param languageCode The ISO 639-1 language code (e.g., "it", "en")
     */
    fun applyLocaleAndRecreate(activity: Activity, languageCode: String) {
        setLocale(activity, languageCode)
        activity.recreate()
    }
}
