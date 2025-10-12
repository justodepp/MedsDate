package com.medsdate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medsdate.data.local.dao.MedicineDao
import com.medsdate.data.local.dao.SettingsDao
import com.medsdate.data.local.entity.MedicineEntity
import com.medsdate.data.local.entity.SettingsEntity

/**
 * Main Room database for the MedsDate application.
 *
 * This database stores medicine entries and user settings with support for
 * automatic migration from the old schema to the new one.
 *
 * Version history:
 * - Version 1: Initial schema (old implementation)
 * - Version 2: Refactored schema with new column names and settings table
 * - Version 3: Added language_code column to settings table
 */
@Database(
    entities = [MedicineEntity::class, SettingsEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "medsdate_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton database instance.
         *
         * @param context Application context
         * @return The database instance
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    // Removed .fallbackToDestructiveMigration() for production safety
                    // Database will crash app if migration fails - this is safer than data loss
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Migration from version 1 to version 2.
         *
         * Changes:
         * 1. Adds new columns to medicine table (notes, created_at)
         * 2. Renames columns for consistency (expire_at -> expiry_date, category removed, image -> image_path)
         * 3. Creates new settings table for notification preferences
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new medicine table with updated schema
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS medicine_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        expiry_date INTEGER NOT NULL,
                        quantity INTEGER NOT NULL DEFAULT 1,
                        image_path TEXT,
                        notes TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Copy data from old table to new table
                // Old schema: id, category, name, quantity, image, expire_at, updated_at
                // New schema: id, name, expiry_date, quantity, image_path, notes, created_at, updated_at
                database.execSQL(
                    """
                    INSERT INTO medicine_new (id, name, expiry_date, quantity, image_path, notes, created_at, updated_at)
                    SELECT id, name, expire_at, quantity, image, NULL, updated_at, updated_at
                    FROM medicine
                    """.trimIndent()
                )

                // Drop old table
                database.execSQL("DROP TABLE medicine")

                // Rename new table to medicine
                database.execSQL("ALTER TABLE medicine_new RENAME TO medicine")

                // Create settings table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS settings (
                        id INTEGER PRIMARY KEY NOT NULL,
                        enable_notifications INTEGER NOT NULL DEFAULT 1,
                        first_notification_days INTEGER NOT NULL DEFAULT 7,
                        second_notification_days INTEGER NOT NULL DEFAULT 2,
                        enable_second_notification INTEGER NOT NULL DEFAULT 1
                    )
                    """.trimIndent()
                )

                // Insert default settings
                database.execSQL(
                    """
                    INSERT INTO settings (id, enable_notifications, first_notification_days, second_notification_days, enable_second_notification)
                    VALUES (1, 1, 7, 2, 1)
                    """.trimIndent()
                )
            }
        }

        /**
         * Migration from version 2 to version 3.
         *
         * Changes:
         * 1. Adds language_code column to settings table (defaults to Italian "it")
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add language_code column to settings table with Italian as default
                database.execSQL(
                    """
                    ALTER TABLE settings ADD COLUMN language_code TEXT NOT NULL DEFAULT 'it'
                    """.trimIndent()
                )
            }
        }
    }
}
