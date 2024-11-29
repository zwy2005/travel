package com.example.shopping.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Goods")
data class Goods(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val pic_path: String = ""
)
