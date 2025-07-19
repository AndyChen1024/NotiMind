package com.andychen.notimind.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.andychen.notimind.core.database.NotiMindDatabase
import com.andychen.notimind.core.database.entity.SummaryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SummaryDaoTest {
    private lateinit var summaryDao: SummaryDao
    private lateinit var db: NotiMindDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NotiMindDatabase::class.java).build()
        summaryDao = db.summaryDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetSummary() = runBlocking {
        val today = LocalDate.now()
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val summary = SummaryEntity(
            id = "morning_${todayMillis}",
            period = "MORNING",
            date = todayMillis,
            summaryJson = """{"highlights": [{"title": "Test", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis()
        )

        summaryDao.insertSummary(summary)
        val retrievedSummary = summaryDao.getSummaryById("morning_${todayMillis}")

        assertNotNull(retrievedSummary)
        assertEquals(summary.id, retrievedSummary.id)
        assertEquals(summary.period, retrievedSummary.period)
        assertEquals(summary.date, retrievedSummary.date)
    }

    @Test
    fun getLatestSummaryByDateAndPeriod() = runBlocking {
        val today = LocalDate.now()
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val oldSummary = SummaryEntity(
            id = "morning_${todayMillis}_1",
            period = "MORNING",
            date = todayMillis,
            summaryJson = """{"highlights": [{"title": "Old", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis() - 1000
        )

        val newSummary = SummaryEntity(
            id = "morning_${todayMillis}_2",
            period = "MORNING",
            date = todayMillis,
            summaryJson = """{"highlights": [{"title": "New", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis()
        )

        summaryDao.insertSummary(oldSummary)
        summaryDao.insertSummary(newSummary)

        val latestSummary = summaryDao.getLatestSummaryByDateAndPeriod(todayMillis, "MORNING").first()

        assertNotNull(latestSummary)
        assertEquals(newSummary.id, latestSummary?.id)
    }

    @Test
    fun getSummariesByDateRange() = runBlocking {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)
        
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayMillis = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val tomorrowMillis = tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val summary1 = SummaryEntity(
            id = "morning_${yesterdayMillis}",
            period = "MORNING",
            date = yesterdayMillis,
            summaryJson = """{"highlights": [{"title": "Yesterday", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis()
        )

        val summary2 = SummaryEntity(
            id = "morning_${todayMillis}",
            period = "MORNING",
            date = todayMillis,
            summaryJson = """{"highlights": [{"title": "Today", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis()
        )

        val summary3 = SummaryEntity(
            id = "morning_${tomorrowMillis}",
            period = "MORNING",
            date = tomorrowMillis,
            summaryJson = """{"highlights": [{"title": "Tomorrow", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis()
        )

        summaryDao.insertSummary(summary1)
        summaryDao.insertSummary(summary2)
        summaryDao.insertSummary(summary3)

        val summaries = summaryDao.getSummariesByDateRange(yesterdayMillis, todayMillis).first()
        assertEquals(2, summaries.size)
    }

    @Test
    fun deleteSummariesOlderThan() = runBlocking {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)
        
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayMillis = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val twoDaysAgoMillis = twoDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val summary1 = SummaryEntity(
            id = "morning_${twoDaysAgoMillis}",
            period = "MORNING",
            date = twoDaysAgoMillis,
            summaryJson = """{"highlights": [{"title": "Two Days Ago", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis()
        )

        val summary2 = SummaryEntity(
            id = "morning_${yesterdayMillis}",
            period = "MORNING",
            date = yesterdayMillis,
            summaryJson = """{"highlights": [{"title": "Yesterday", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis()
        )

        val summary3 = SummaryEntity(
            id = "morning_${todayMillis}",
            period = "MORNING",
            date = todayMillis,
            summaryJson = """{"highlights": [{"title": "Today", "content": "Content"}]}""",
            generatedAt = System.currentTimeMillis()
        )

        summaryDao.insertSummary(summary1)
        summaryDao.insertSummary(summary2)
        summaryDao.insertSummary(summary3)

        val deletedCount = summaryDao.deleteSummariesOlderThan(yesterdayMillis)
        assertEquals(1, deletedCount)

        val remainingSummaries = summaryDao.getSummariesByDateRange(
            twoDaysAgoMillis,
            todayMillis
        ).first()
        assertEquals(2, remainingSummaries.size)
    }
}