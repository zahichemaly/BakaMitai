package com.zc.bakamitai.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.zc.bakamitai.R

class PreferenceUtil(private val context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private fun getValue(@StringRes keyRes: Int, @ArrayRes entriesRes: Int): String {
        val key = context.getString(keyRes)
        val entries = context.resources.getStringArray(entriesRes)
        val defaultValue = entries[0]
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun getFirstDayOfWeek(): FirstDayOfWeek = FirstDayOfWeek.valueOf(getValue(R.string.key_calendar, R.array.calendar_values))
    fun getTimeFormat() = TimeFormat.valueOf(getValue(R.string.key_time_format, R.array.time_values))
}

enum class FirstDayOfWeek {
    Sunday, Monday
}

enum class TimeFormat {
    TF_24, TF_12
}
