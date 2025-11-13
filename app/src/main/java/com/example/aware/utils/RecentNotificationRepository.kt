package com.example.aware.utils

import androidx.lifecycle.LiveData
import com.example.aware.database.RecentlyClearedNotificationDao
import com.example.aware.database.RecentlyClearedNotificationEntity


class RecentNotificationRepository(val dao: RecentlyClearedNotificationDao) {

    val recentNotifications: LiveData<MutableList<RecentlyClearedNotificationEntity>> = dao.getAllRecentlyCleared()

    suspend fun insertNotificaions(recent: RecentlyClearedNotificationEntity){
        dao.insert(recent)
    }

    suspend fun deleteAllNotifications() {
        dao.deleteAllNotifications()
    }
}