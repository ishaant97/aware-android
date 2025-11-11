package com.example.aware.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val appName: String,
    val time: LocalDateTime,
    val notificationHeading: String,
    val notificationContent: String,
    val appIcon: ByteArray?, // Store Drawable as ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationEntity

        if (id != other.id) return false
        if (appName != other.appName) return false
        if (notificationHeading != other.notificationHeading) return false
        if (notificationContent != other.notificationContent) return false
        // Don't compare byte arrays for performance, just check existence
        if ((appIcon == null) != (other.appIcon == null)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + appName.hashCode()
        result = 31 * result + notificationHeading.hashCode()
        result = 31 * result + notificationContent.hashCode()
        // Avoid expensive byte array hashing
        result = 31 * result + (appIcon?.size ?: 0)
        return result
    }
}


//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//
//@Entity
//data class User(
//    @PrimaryKey val uid: Int,
//    @ColumnInfo(name = "first_name") val firstName: String?,
//    @ColumnInfo(name = "last_name") val lastName: String?
//)