package com.example.readingtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReadingTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}