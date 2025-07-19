package com.andychen.notimind.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "notifications",
    indices = [
        Index("packageName"),
        Index("timestamp"),
        Index("category")
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String?,
    val content: String?,
    val timestamp: Long,
    val category: String?,
    val isRemoved: Boolean = false
)