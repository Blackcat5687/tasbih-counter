package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DAILY_TARGET = "daily_target"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_THEME = "selected_theme"
        private const val KEY_CURRENT_DHIKR_INDEX = "current_dhikr_index"
        private const val KEY_DHIKR_COUNTS = "dhikr_counts"
        private const val KEY_STREAK = "current_streak"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        private const val KEY_HISTORY = "dhikr_history"
    }

    var dailyTarget: Int
        get() = prefs.getInt(KEY_DAILY_TARGET, 99)
        set(value) = prefs.edit().putInt(KEY_DAILY_TARGET, value).apply()

    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var selectedTheme: String
        get() = prefs.getString(KEY_THEME, "Emerald") ?: "Emerald"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var currentDhikrIndex: Int
        get() = prefs.getInt(KEY_CURRENT_DHIKR_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_CURRENT_DHIKR_INDEX, value).apply()

    fun getDhikrCount(dhikr: String): Int {
        val jsonStr = prefs.getString(KEY_DHIKR_COUNTS, "{}") ?: "{}"
        return try {
            val json = JSONObject(jsonStr)
            json.optInt(dhikr, 0)
        } catch (e: Exception) {
            0
        }
    }

    fun saveDhikrCount(dhikr: String, count: Int) {
        val jsonStr = prefs.getString(KEY_DHIKR_COUNTS, "{}") ?: "{}"
        try {
            val json = JSONObject(jsonStr)
            json.put(dhikr, count)
            prefs.edit().putString(KEY_DHIKR_COUNTS, json.toString()).apply()
            
            // Also update today's history
            addToHistory(1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetDhikrCount(dhikr: String) {
        val jsonStr = prefs.getString(KEY_DHIKR_COUNTS, "{}") ?: "{}"
        try {
            val json = JSONObject(jsonStr)
            json.put(dhikr, 0)
            prefs.edit().putString(KEY_DHIKR_COUNTS, json.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // History tracking: Date to count map
    fun getHistory(): Map<String, Int> {
        val jsonStr = prefs.getString(KEY_HISTORY, "{}") ?: "{}"
        val map = mutableMapOf<String, Int>()
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.getInt(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    private fun addToHistory(amount: Int) {
        val today = getTodayDateString()
        val jsonStr = prefs.getString(KEY_HISTORY, "{}") ?: "{}"
        try {
            val json = JSONObject(jsonStr)
            val currentCount = json.optInt(today, 0)
            json.put(today, currentCount + amount)
            prefs.edit().putString(KEY_HISTORY, json.toString()).apply()
            
            checkStreak()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var streak: Int
        get() = prefs.getInt(KEY_STREAK, 0)
        private set(value) = prefs.edit().putInt(KEY_STREAK, value).apply()

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    private fun checkStreak() {
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        val lastActive = prefs.getString(KEY_LAST_ACTIVE_DATE, "") ?: ""

        if (lastActive == today) {
            return // Already updated today
        }

        if (lastActive == yesterday) {
            streak += 1
        } else if (lastActive.isEmpty() || lastActive != today) {
            // Check if streak was broken (last active was before yesterday)
            streak = 1
        }
        prefs.edit().putString(KEY_LAST_ACTIVE_DATE, today).apply()
    }

    fun verifyStreakOnLaunch() {
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        val lastActive = prefs.getString(KEY_LAST_ACTIVE_DATE, "") ?: ""

        if (lastActive.isNotEmpty() && lastActive != today && lastActive != yesterday) {
            // Streak broken
            streak = 0
        }
    }
}
