package com.example.aware

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.aware.database.NotificationDatabase
import com.example.aware.utils.NotificationRepository
import com.example.aware.utils.NotificationSwitchStateHelper
import com.example.aware.utils.RecentNotificationRepository
import com.example.aware.utils.checkNotificationPermission
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    lateinit var repository: NotificationRepository
    lateinit var repository2: RecentNotificationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        checkNotificationPermission(this)

        val notificationSwitch = findViewById<SwitchMaterial>(R.id.switch_button_notification)
        notificationSwitch.isChecked = NotificationSwitchStateHelper.getSwitchState(this)

        findViewById<Button>(R.id.btnOpenSummary).setOnClickListener {
            val intent = Intent(this, AiSummaryActivity::class.java)
            startActivity(intent)
        }

        val dao = NotificationDatabase.getDatabase(this).notificationDao()
        repository = NotificationRepository(dao)

        val dao2 = NotificationDatabase.getDatabase(this).recentlyClearedNotificationDao()
        repository2 = RecentNotificationRepository(dao2)

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            NotificationSwitchStateHelper.saveSwitchState(this, isChecked)
            if (!isChecked) {
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.deleteAllNotification()
                    repository2.deleteAllNotifications()
                }
            }
        }

        // Setup ViewPager2 with Tabs
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        
        viewPager.adapter = NotificationsPagerAdapter(this)
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Active"
                1 -> "Cleared"
                else -> null
            }
        }.attach()
    }
    
    inner class NotificationsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ActiveNotificationsFragment()
                1 -> ClearedNotificationsFragment()
                else -> ActiveNotificationsFragment()
            }
        }
    }
}