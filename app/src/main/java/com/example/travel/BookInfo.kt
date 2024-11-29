package com.example.travel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Book_Info")
data class BookInfo(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var book: String = "",
    var author: String = "",
    var press: String = "",
    var price: Double = 0.0
)
