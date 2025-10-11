package com.medsdate.data.local.dao

import androidx.room.*
import com.medsdate.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for settings operations.
 *
 * Provides methods to interact with the settings table in the Room database.
 * There is only one settings record (id = 1) that stores user preferences.
 */
@Dao
interface SettingsDao {

    /**
     * Retrieves the user settings.
     *
     * @return Flow of settings entity
     */
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    /**
     * Inserts or updates the settings.
     *
     * @param settings The settings entity to save
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    /**
     * Updates existing settings.
     *
     * @param settings The settings entity with updated values
     */
    @Update
    suspend fun updateSettings(settings: SettingsEntity)
}
