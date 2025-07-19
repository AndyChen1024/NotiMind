package com.andychen.notimind.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_extras",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["notificationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("notificationId"),
        Index("key")
    ]
)
data class NotificationExtraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val notificationId: Long,
    val key: String,
    val value: String
)