package com.example.readingtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.readingtracker.ui.theme.ReadingTrackerTheme
import com.example.readingtracker.ui.ReadingTrackerApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadingTrackerTheme {
                ReadingTrackerApp()
            }
        }
    }
}