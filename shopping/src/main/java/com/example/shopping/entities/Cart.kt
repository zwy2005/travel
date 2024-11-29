package com.example.shopping.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cart")
data class Cart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goods_id: Long = 0,
    @ColumnInfo(defaultValue = "1") val count: Long = 1
)
