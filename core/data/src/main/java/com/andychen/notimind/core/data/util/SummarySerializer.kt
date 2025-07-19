package com.andychen.notimind.core.data.util

import com.andychen.notimind.core.model.HighlightImportance
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.SummaryHighlight
import com.andychen.notimind.core.model.TimePeriod
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object SummarySerializer {
    
    fun serializeNotificationSummary(summary: NotificationSummary): String {
        val jsonObject = JSONObject()
        
        jsonObject.put("id", summary.id)
        jsonObject.put("period", summary.period.name)
        jsonObject.put("date", summary.date.toString())
        jsonObject.put("totalCount", summary.totalCount)
        
        // 序列化分类统计
        val categoriesJson = JSONObject()
        summary.categories.forEach { (category, count) ->
            categoriesJson.put(category.name, count)
        }
        jsonObject.put("categories", categoriesJson)
        
        // 序列化亮点
        val highlightsJson = JSONArray()
        summary.highlights.forEach { highlight ->
            val highlightJson = JSONObject()
            highlightJson.put("title", highlight.title)
            highlightJson.put("content", highlight.content)
            highlightJson.put("category", highlight.category.name)
            highlightJson.put("importance", highlight.importance.name)
            highlightsJson.put(highlightJson)
        }
        jsonObject.put("highlights", highlightsJson)
        
        return jsonObject.toString()
    }
    
    fun deserializeNotificationSummary(json: String): NotificationSummary {
        val jsonObject = JSONObject(json)
        
        val id = jsonObject.getString("id")
        val period = TimePeriod.valueOf(jsonObject.getString("period"))
        val date = LocalDate.parse(jsonObject.getString("date"))
        val totalCount = jsonObject.getInt("totalCount")
        
        // 反序列化分类统计
        val categoriesJson = jsonObject.getJSONObject("categories")
        val categories = mutableMapOf<NotificationCategory, Int>()
        categoriesJson.keys().forEach { key ->
            val category = NotificationCategory.valueOf(key)
            val count = categoriesJson.getInt(key)
            categories[category] = count
        }
        
        // 反序列化亮点
        val highlightsJson = jsonObject.getJSONArray("highlights")
        val highlights = mutableListOf<SummaryHighlight>()
        for (i in 0 until highlightsJson.length()) {
            val highlightJson = highlightsJson.getJSONObject(i)
            val title = highlightJson.getString("title")
            val content = highlightJson.getString("content")
            val category = NotificationCategory.valueOf(highlightJson.getString("category"))
            val importance = HighlightImportance.valueOf(highlightJson.getString("importance"))
            
            highlights.add(
                SummaryHighlight(
                    title = title,
                    content = content,
                    category = category,
                    importance = importance
                )
            )
        }
        
        return NotificationSummary(
            id = id,
            period = period,
            date = date,
            categories = categories,
            highlights = highlights,
            totalCount = totalCount
        )
    }
    
    fun generateSummaryId(date: LocalDate, period: TimePeriod): String {
        return "${date}_${period.name}"
    }
    
    fun dateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    fun timestampToDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}