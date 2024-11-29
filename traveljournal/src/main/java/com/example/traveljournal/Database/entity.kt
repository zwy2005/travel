package com.example.traveljournal.database

import androidx.room.*

@Entity
data class TravelRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var location: String,
    var date: String
)
@Entity(
    tableName = "ContentItem",
    foreignKeys = [
        ForeignKey(
            entity = TravelRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recordId"])] // 为 recordId 创建索引
)
data class ContentItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long, // 外键字段
    val type: Int, // 0 - 文本, 1 - 图片
    val content: String,
    val orderIndex: Int // 新增字段，表示内容的顺序
)