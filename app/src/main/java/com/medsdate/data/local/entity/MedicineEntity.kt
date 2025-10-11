package com.medsdate.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room entity representing a medicine entry in the database.
 *
 * This entity stores all information about a medicine including its name,
 * expiration date, quantity, image path, and optional notes.
 *
 * @property id Unique identifier for the medicine (auto-generated)
 * @property name Name of the medicine
 * @property expiryDate The expiration date of the medicine
 * @property quantity Quantity/amount of medicine available
 * @property imagePath File path or URI to the medicine image (optional)
 * @property notes Additional notes about the medicine (optional)
 * @property createdAt Timestamp when the entry was created
 * @property updatedAt Timestamp when the entry was last updated
 */
@Entity(tableName = "medicine")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "expiry_date")
    val expiryDate: Date,

    @ColumnInfo(name = "quantity")
    val quantity: Int = 1,

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)
