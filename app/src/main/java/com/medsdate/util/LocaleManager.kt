package com.medsdate.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * App language on top of AppCompat per-app locales.
 *
 * AppCompat persists the choice, recreates the activity and, on API 33+, keeps it in sync with the
 * system "App languages" setting. With no choice made the app follows the system language and
 * falls back to the default resources (Italian).
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
        ITALIAN("it", "Italiano", "🇮🇹"), // 🇮🇹
        ENGLISH("en", "English", "🇬🇧"); // 🇬🇧

        companion object {
            fun fromCode(code: String): Language {
                return entries.find { it.code == code } ?: ITALIAN
            }
        }
    }

    /** Applies and persists [language]; AppCompat recreates the activity. */
    fun setLanguage(language: Language) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
    }

    /** Language in use: the app choice if any, else the system one. */
    fun currentLanguage(): Language {
        val code = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
        return Language.fromCode(code)
    }

    /**
     * One-shot: moves the language saved by the old SharedPreferences implementation into
     * AppCompat, then clears it. Users who never picked a language have no saved value.
     */
    fun migrateLegacyLanguage(context: Context) {
        val prefs = context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
        val legacy = prefs.getString("language_code", null) ?: return
        if (AppCompatDelegate.getApplicationLocales().isEmpty) setLanguage(Language.fromCode(legacy))
        prefs.edit().clear().apply()
    }
}
