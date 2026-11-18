package com.medsdate.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Builds legacy databases with raw SQL (schemas of v1 and v3 as shipped) and opens them with the
 * full migration chain. Room validates the final schema against the entities on open.
 * Uses its own DB name: never touches the real app database.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "migration_test_db"

    @Before @After
    fun cleanUp() {
        context.deleteDatabase(name)
    }

    private fun legacyDb(version: Int, vararg sql: String) {
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null).use { db ->
            sql.forEach(db::execSQL)
            db.version = version
        }
    }

    private fun open() = Room.databaseBuilder(context, AppDatabase::class.java, name)
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
        .build()

    @Test
    fun migrate1To4_keepsDataAndSurvivesNullColumns() {
        legacyDb(
            1,
            "CREATE TABLE medicine (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, category TEXT, name TEXT, " +
                "quantity INTEGER NOT NULL, image TEXT, expire_at INTEGER, updated_at INTEGER)",
            "INSERT INTO medicine VALUES (1, 'c', 'Aspirin', 3, '/img.jpg', 1000, 500)",
            "INSERT INTO medicine VALUES (2, NULL, NULL, 1, NULL, NULL, NULL)"
        )
        val db = open()
        runBlocking {
            val medicines = db.medicineDao().getAllMedicines().first().sortedBy { it.id }
            assertEquals(2, medicines.size)
            assertEquals("Aspirin", medicines[0].name)
            assertEquals(3, medicines[0].quantity)
            assertEquals("/img.jpg", medicines[0].imagePath)
            assertEquals(1000L, medicines[0].expiryDate.time)
            assertEquals("", medicines[1].name)
            val settings = db.settingsDao().getSettings().first()
            assertEquals(7, settings?.expiringSoonDays)
            assertEquals("it", settings?.languageCode)
        }
        db.close()
    }

    @Test
    fun migrate3To4_keepsUserSettings() {
        legacyDb(
            3,
            "CREATE TABLE medicine (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, " +
                "expiry_date INTEGER NOT NULL, quantity INTEGER NOT NULL DEFAULT 1, image_path TEXT, notes TEXT, " +
                "created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)",
            "CREATE TABLE settings (id INTEGER PRIMARY KEY NOT NULL, enable_notifications INTEGER NOT NULL DEFAULT 1, " +
                "first_notification_days INTEGER NOT NULL DEFAULT 7, second_notification_days INTEGER NOT NULL DEFAULT 2, " +
                "enable_second_notification INTEGER NOT NULL DEFAULT 1, language_code TEXT NOT NULL DEFAULT 'it')",
            "INSERT INTO medicine VALUES (1, 'Moment', 2000, 2, NULL, 'n', 100, 200)",
            "INSERT INTO settings VALUES (1, 0, 14, 3, 0, 'en')"
        )
        val db = open()
        runBlocking {
            val medicines = db.medicineDao().getAllMedicines().first()
            assertEquals(1, medicines.size)
            assertEquals("Moment", medicines[0].name)
            assertEquals("n", medicines[0].notes)
            val s = db.settingsDao().getSettings().first()!!
            assertEquals(false, s.enableNotifications)
            assertEquals(14, s.firstNotificationDays)
            assertEquals("en", s.languageCode)
            assertEquals(7, s.expiringSoonDays)
        }
        db.close()
    }
}
