package com.example.aware

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aware.database.NotificationDatabase
import com.example.aware.database.NotificationEntity
import com.example.aware.utils.NotificationAdapter
import com.example.aware.utils.NotificationItemAnimator
import com.example.aware.utils.NotificationRepository
import com.example.aware.utils.NotificationSwitchStateHelper
import com.example.aware.utils.RecentNotificationAdapter
import com.example.aware.utils.RecentNotificationRepository
import com.example.aware.utils.checkNotificationPermission
import com.example.aware.utils.getStatusBarHeight
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.get
import kotlin.jvm.java

class DashboardActivity : AppCompatActivity() {


    private lateinit var mainRecyclerView: RecyclerView
    private lateinit var recentlyClearedRecycleView : RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var adapter2: RecentNotificationAdapter
    private var hasVibrated = false
    lateinit var repository: NotificationRepository
    lateinit var repository2: RecentNotificationRepository
    private var deletedNotification: NotificationEntity? = null
    private var deletedPosition: Int = -1
    private var isHandlingDelete = false



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

        val btnSearch = findViewById<TextView>(R.id.btn_search)
        btnSearch.setOnClickListener {
            val intent = Intent(this, SearchAndFilterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_card)
        }

        val dao = NotificationDatabase.getDatabase(this).notificationDao()
        repository = NotificationRepository(dao)

        val dao2 = NotificationDatabase.getDatabase(this).recentlyClearedNotificationDao()
        repository2 = RecentNotificationRepository(dao2)

        val mainLayout = findViewById<View>(R.id.main_dashboard)
        mainLayout.setPadding(0, getStatusBarHeight(this) + 40, 0, 0)

        mainRecyclerView = findViewById(R.id.mainRecyclerView)
        recentlyClearedRecycleView = findViewById(R.id.recentlyClearedRecycleView)
        val layoutManager = LinearLayoutManager(this)
        mainRecyclerView.layoutManager = layoutManager

        val layoutManager2 = LinearLayoutManager(this)
        recentlyClearedRecycleView.layoutManager = layoutManager2

        adapter = NotificationAdapter(mutableListOf())
        mainRecyclerView.adapter = adapter

        adapter2 = RecentNotificationAdapter(mutableListOf())
        recentlyClearedRecycleView.adapter = adapter2

        // Set our custom item animator
        mainRecyclerView.itemAnimator = NotificationItemAnimator()

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            NotificationSwitchStateHelper.saveSwitchState(this, isChecked)
            if (!isChecked) {
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.deleteAllNotification()
                    repository2.deleteAllNotifications()
                }
            }
        }

        repository.notifications.observe(this) { newList ->
            if (!isHandlingDelete) {
                adapter.updateList(newList)
            }
        }

        repository2.recentNotifications.observe(this) { newList ->
            if(!isHandlingDelete) {
                adapter2.updateList(newList)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mainRecyclerView)
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return

            val notificationList = repository.notifications.value ?: return
            if (position >= notificationList.size) return

            deletedNotification = notificationList[position]
            deletedPosition = position

            // Set flag to prevent LiveData from interfering
            isHandlingDelete = true

            // Remove item directly from adapter first for immediate UI feedback
            adapter.removeItem(position)

            // Then update the database
            lifecycleScope.launch(Dispatchers.IO) {
                repository.removeNotification(deletedNotification!!)
            }

            // Show snackbar with undo option
            val snackbar = Snackbar.make(mainRecyclerView, "Notification deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO") {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            // First add to database
                            deletedNotification?.let {
                                repository.addNotification(it)
                            }
                        }

                        // Then update UI directly for immediate feedback
                        withContext(Dispatchers.Main) {
                            deletedNotification?.let {
                                // Insert at correct position in adapter
                                adapter.insertItem(it, deletedPosition)

                                // Scroll to the position if needed
                                mainRecyclerView.smoothScrollToPosition(deletedPosition)
                            }
                        }
                    }
                }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        // Reset our handling state when snackbar disappears
                        isHandlingDelete = false
                        deletedNotification = null
                        deletedPosition = -1
                    }
                })

            snackbar.show()
        }
}