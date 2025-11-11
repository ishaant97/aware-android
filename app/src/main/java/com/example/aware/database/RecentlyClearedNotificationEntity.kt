package com.example.aware.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "recently_cleared_notifications",)
data class RecentlyClearedNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val appName: String,
    val time: LocalDateTime,
    val notificationHeading: String,
    val notificationContent: String,
    val appIcon: ByteArray?, // Store Drawable as ByteArray
    val clearedAt: LocalDateTime = LocalDateTime.now()
)

