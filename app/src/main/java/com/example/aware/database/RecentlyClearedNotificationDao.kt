package com.example.aware.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecentlyClearedNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recentlyClearedNotification: RecentlyClearedNotificationEntity)

    @Query("SELECT * FROM recently_cleared_notifications ORDER BY clearedAt DESC LIMIT 5")
    fun getAllRecentlyCleared(): LiveData<MutableList<RecentlyClearedNotificationEntity>>

    @Query("DELETE FROM recently_cleared_notifications")
    suspend fun deleteAllNotifications()
}
