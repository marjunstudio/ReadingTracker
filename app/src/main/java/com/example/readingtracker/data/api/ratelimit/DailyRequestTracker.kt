package com.example.readingtracker.data.api.ratelimit

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyRequestTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val sharedPrefs = context.getSharedPreferences("api_usage", Context.MODE_PRIVATE)
    private val maxDailyRequests = 900 // 安全マージン100リクエスト
    
    fun canMakeRequest(): Boolean {
        val today = getTodayDateString()
        val todayRequests = sharedPrefs.getInt("requests_$today", 0)
        return todayRequests < maxDailyRequests
    }
    
    fun recordRequest() {
        val today = getTodayDateString()
        val currentCount = sharedPrefs.getInt("requests_$today", 0)
        sharedPrefs.edit()
            .putInt("requests_$today", currentCount + 1)
            .apply()
    }
    
    fun getRemainingRequests(): Int {
        val today = getTodayDateString()
        val todayRequests = sharedPrefs.getInt("requests_$today", 0)
        return maxDailyRequests - todayRequests
    }
    
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
    }
}