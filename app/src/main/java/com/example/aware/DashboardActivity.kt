package com.example.aware

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.aware.utils.NotificationSwitchStateHelper
import com.example.aware.utils.checkNotificationPermission

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        checkNotificationPermission(this)

        val notificationSwitch = findViewById<Switch>(R.id.switch_button_notification)
        notificationSwitch.isChecked = NotificationSwitchStateHelper.getSwitchState(this)



    }
}