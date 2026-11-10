package com.app.rumbo.core.util

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.annotation.StringRes
import com.app.rumbo.R
import java.util.Locale

enum class AppLanguage(val tag: String?, @StringRes val labelRes: Int) {
    SYSTEM(null, R.string.language_system),
    SPANISH("es", R.string.language_spanish),
    ENGLISH("en", R.string.language_english);

    companion object {
        fun fromTag(tag: String?): AppLanguage = when (tag) {
            "es" -> SPANISH
            "en" -> ENGLISH
            else -> SYSTEM
        }
    }
}

private const val PREFS_NAME = "rumbo_language"
private const val KEY_LANGUAGE = "app_language"

fun currentLanguage(context: Context): AppLanguage {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        val tag = if (locales.isEmpty) null else locales[0]?.language
        return AppLanguage.fromTag(tag)
    }
    val tag = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LANGUAGE, null)
    return AppLanguage.fromTag(tag)
}

fun applyAppLanguage(activity: Activity, language: AppLanguage) {
    activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_LANGUAGE, language.tag)
        .apply()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val locales = language.tag
            ?.let { LocaleList.forLanguageTags(it) }
            ?: LocaleList.getEmptyLocaleList()
        activity.getSystemService(LocaleManager::class.java).setApplicationLocales(locales)
    } else {
        activity.recreate()
    }
}

fun Context.withAppLanguage(): Context {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return this
    val tag = currentLanguage(this).tag ?: return this
    val locale = Locale.forLanguageTag(tag)
    Locale.setDefault(locale)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
