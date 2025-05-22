package com.ballog.mobile.data.util

import android.content.Context

object OnboardingPrefs {
    private const val PREF_NAME = "onboarding_prefs"
    private const val KEY_ONBOARDING = "onboarding_completed"
    private const val KEY_PERMISSION = "permission_completed"
    private const val KEY_GUIDE = "guide_completed"

    fun setOnboardingCompleted(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()
    }
    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING, false)
    }
    fun setPermissionCompleted(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PERMISSION, value).apply()
    }
    fun isPermissionCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PERMISSION, false)
    }
    fun setGuideCompleted(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_GUIDE, value).apply()
    }
    fun isGuideCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_GUIDE, false)
    }
    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
} 