package com.example.sportoapppit

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object UserPreferences {
    private const val PREF_NAME = "user_data"
    private const val KEY_NAME = "user_name"

    fun saveUserName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putString(KEY_NAME, name) }
    }

    fun getUserName(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NAME, "Vartotojas") ?: "Vartotojas"
    }

    private const val KEY_AVATAR_URI = "avatar_uri"

    fun saveAvatarUri(context: Context, uri: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putString(KEY_AVATAR_URI, uri) }
    }

    fun getAvatarUri(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AVATAR_URI, null)
    }

    private const val KEY_STEP_GOAL = "step_goal"

    fun saveStepGoal(context: Context, steps: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putInt(KEY_STEP_GOAL, steps) }
    }

    fun getStepGoal(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_STEP_GOAL, 18000) // default to 18000
    }

    private const val KEY_REMINDER_ENABLED = "reminder_enabled"

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit() { putBoolean(KEY_REMINDER_ENABLED, enabled) }
    }

    fun isReminderEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REMINDER_ENABLED, false)
    }


}
