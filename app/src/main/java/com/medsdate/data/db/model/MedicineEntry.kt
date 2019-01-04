package com.medsdate.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "medicine")
class MedicineEntry {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var category: String? = null

    var name: String? = null

    var quantity: Int = 0

    var image: String? = null

    @ColumnInfo(name = "expire_at")
    var expireAt: Date? = null

    @ColumnInfo(name = "updated_at")
    var updatedAt: Date? = null

    constructor() {}

    @Ignore
    constructor(category: String, name: String, expireAt: Date, quantity: Int, image: String, updatedAt: Date) {
        this.category = category
        this.name = name
        this.quantity = quantity
        this.image = image
        this.expireAt = expireAt
        this.updatedAt = updatedAt
    }

    constructor(medicine: MedicineEntry) {
        this.id = medicine.id
        this.category = medicine.category
        this.name = medicine.name
        this.quantity = medicine.quantity
        this.image = medicine.image
        this.expireAt = medicine.expireAt
        this.updatedAt = medicine.updatedAt
    }
}
