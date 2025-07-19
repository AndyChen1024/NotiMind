package com.andychen.notimind.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "summaries",
    indices = [
        Index("period"),
        Index("date")
    ]
)
data class SummaryEntity(
    @PrimaryKey val id: String,
    val period: String,
    val date: Long,
    val summaryJson: String, // Serialized summary data
    val generatedAt: Long
)