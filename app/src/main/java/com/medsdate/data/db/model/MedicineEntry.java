package com.medsdate.data.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "medicine")
public class MedicineEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String category;

    private String name;

    private int quantity;

    private String image;

    @ColumnInfo(name = "expire_at")
    private Date expireAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    public MedicineEntry() {
    }

    @Ignore
    public MedicineEntry(String category, String name, Date expireAt, int quantity, String image, Date updatedAt) {
        this.category = category;
        this.name = name;
        this.quantity = quantity;
        this.image = image;
        this.expireAt = expireAt;
        this.updatedAt = updatedAt;
    }

    public MedicineEntry(MedicineEntry medicine) {
        this.id = medicine.getId();
        this.category = medicine.getCategory();
        this.name = medicine.getName();
        this.quantity = medicine.getQuantity();
        this.image = medicine.getImage();
        this.expireAt = medicine.getExpireAt();
        this.updatedAt = medicine.getUpdatedAt();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
