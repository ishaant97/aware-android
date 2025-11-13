package com.example.aware.utils

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.aware.database.RecentlyClearedNotificationEntity
import java.util.concurrent.Executors
import com.example.aware.R;


class RecentNotificationAdapter(private var recentNotifications: MutableList<RecentlyClearedNotificationEntity>) :
    RecyclerView.Adapter<RecentNotificationAdapter.NotificationViewHolder>(){

    // Background thread for calculating diffs
    private val diffExecutor = Executors.newSingleThreadExecutor()

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var appIcon: ImageView = itemView.findViewById(R.id.img_notification_icon)
        var appName: TextView = itemView.findViewById(R.id.txt_app_name)
        var time: TextView = itemView.findViewById(R.id.txt_time)
        var notificationHeading: TextView = itemView.findViewById(R.id.notification_heading)
        var notificationContent: TextView = itemView.findViewById(R.id.notification_content)
    }

    init {
        // Set stable IDs to help with animations
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): com.example.aware.utils.RecentNotificationAdapter.NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return com.example.aware.utils.RecentNotificationAdapter.NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: com.example.aware.utils.RecentNotificationAdapter.NotificationViewHolder, position: Int) {
        val notification = recentNotifications[position]

        // Set app icon safely
        val byteArrayAppIcon = notification.appIcon
        if (byteArrayAppIcon != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArrayAppIcon, 0, byteArrayAppIcon.size)
            holder.appIcon.setImageBitmap(bitmap)
        } else {
            holder.appIcon.setImageResource(R.drawable.ic_archive) // fallback icon
        }

        holder.appName.text = notification.appName
        holder.time.text = buildString {
            append(notification.time.hour.toString().padStart(2, '0'))
            append(":")
            append(notification.time.minute.toString().padStart(2, '0'))
        }
        holder.notificationHeading.text = notification.notificationHeading
        holder.notificationContent.text = notification.notificationContent

        // No manual animations here - let the item animator handle it
    }

    override fun getItemId(position: Int): Long {
        return recentNotifications[position].id.toLong()
    }

    override fun getItemCount(): Int = recentNotifications.size

    // Use a better update mechanism for the list
    fun updateList(newList: List<RecentlyClearedNotificationEntity>) {
        // Calculate diff in background thread
        diffExecutor.execute {
            val oldList = ArrayList(recentNotifications)
            val diffCallback = NotificationDiffCallback(oldList, newList)
            val diffResult = DiffUtil.calculateDiff(diffCallback, true) // true = detect moves

            // Apply changes on main thread
            holder.get()?.post {
                recentNotifications.clear()
                recentNotifications.addAll(newList)
                diffResult.dispatchUpdatesTo(this@RecentNotificationAdapter)
            }
        }
    }

    // For single item updates
    fun insertItem(notification: RecentlyClearedNotificationEntity, position: Int) {
        recentNotifications.add(position, notification)
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < recentNotifications.size) {
            recentNotifications.removeAt(position)
            notifyItemRemoved(position)
            // Important: This triggers the move animation for items below
            notifyItemRangeChanged(position, recentNotifications.size - position)
        }
    }

    // DiffUtil implementation
    private inner class NotificationDiffCallback(
        private val oldList: List<RecentlyClearedNotificationEntity>,
        private val newList: List<RecentlyClearedNotificationEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]

            return old.id == new.id &&
                    old.appName == new.appName &&
                    old.notificationHeading == new.notificationHeading &&
                    old.notificationContent == new.notificationContent
        }

        // Detect if item moved positions - helps with animations
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return Any() // Non-null indicates the item changed but doesn't need full rebind
        }
    }

    // ThreadLocal holder for main thread operations
    companion object {
        private val holder = object : ThreadLocal<android.os.Handler>() {
            override fun initialValue(): android.os.Handler {
                return android.os.Handler(android.os.Looper.getMainLooper())
            }
        }
    }

}