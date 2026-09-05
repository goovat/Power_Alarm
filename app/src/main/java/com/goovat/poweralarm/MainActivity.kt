package com.goovat.poweralarm

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = TextView(this).apply {
            text = "Power Alarm\n\nMonitoring system ready."
            textSize = 22f
            setPadding(48, 48, 48, 48)
        }

        setContentView(view)
    }
}
