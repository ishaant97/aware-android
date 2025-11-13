package com.example.aware.utils

import androidx.lifecycle.LiveData
import com.example.aware.database.NotificationDao
import com.example.aware.database.NotificationEntity
import java.time.LocalDateTime

class NotificationRepository(val dao: NotificationDao) {

    val notifications: LiveData<MutableList<NotificationEntity>> = dao.getNotifications()

    suspend fun addNotification(notification: NotificationEntity) {
        dao.insertNotification(notification)
    }

    suspend fun removeNotification(notification: NotificationEntity) {
        dao.deleteNotification(notification)
    }

    suspend fun addNotificationAtIndex(notification: NotificationEntity) {
        dao.insertNotification(notification)
    }

    suspend fun deleteAllNotification() {
        dao.deleteAllNotifications()
    }

    suspend fun deleteOldNotifications(cutoff: LocalDateTime) {
        dao.deleteOldNotifications(cutoff)
    }
}
