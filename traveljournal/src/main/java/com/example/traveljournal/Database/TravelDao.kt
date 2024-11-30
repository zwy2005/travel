package com.example.traveljournal.database

import android.util.Log
import androidx.room.*

@Dao
interface TravelDao {
    @Insert
    suspend fun insertRecord(record: TravelRecord): Long

    @Insert
    suspend fun insertRecords(records: List<TravelRecord>): List<Long>

    @Insert
    suspend fun insertContentItems(contentItems: List<ContentItem>)

    @Transaction
    @Query("SELECT * FROM TravelRecord WHERE id = :recordId")
    suspend fun getRecordWithContents(recordId: Long): RecordWithContents

    @Query("DELETE FROM TravelRecord WHERE id = :recordId")
    suspend fun deleteRecord(recordId: Long)

    @Query("""
    SELECT TravelRecord.id, TravelRecord.location, TravelRecord.date, ContentItem.content AS firstImageUri 
    FROM TravelRecord 
    LEFT JOIN ContentItem 
    ON TravelRecord.id = ContentItem.recordId 
    AND ContentItem.type = 1 
    GROUP BY TravelRecord.id
    ORDER BY TravelRecord.id DESC
""")
    suspend fun getAllRecordsWithFirstImage(): List<RecordWithFirstImage>


    // 获取单条记录的详细信息
    @Query("""
    SELECT TravelRecord.id, TravelRecord.location, TravelRecord.date, ContentItem.content AS firstImageUri 
    FROM TravelRecord 
    LEFT JOIN ContentItem 
    ON TravelRecord.id = ContentItem.recordId 
    WHERE TravelRecord.id = :recordId
    """)
    suspend fun getRecordWithFirstImageById(recordId: Long): RecordWithFirstImage?

    // 删除 TravelRecord，会级联删除关联的 ContentItem
    @Query("DELETE FROM TravelRecord WHERE id = :recordId")
    fun deleteRecordById(recordId: Long)

    // 插入新的内容项
    @Insert
    suspend fun insertContentItem(contentItem: ContentItem): Long

    // 删除单个内容项
    @Query("DELETE FROM ContentItem WHERE id = :contentItemId")
    suspend fun deleteContentItemById(contentItemId: Long)

    // 更新内容项（如修改文本或图片路径）
    @Update
    suspend fun updateContentItem(contentItem: ContentItem)

    // 调整内容项顺序
    @Query("UPDATE ContentItem SET orderIndex = :newOrderIndex WHERE id = :contentItemId")
    suspend fun updateContentOrder(contentItemId: Long, newOrderIndex: Int)

    @Update
    suspend fun updateContentItems(contentItems: List<ContentItem>):Int

    @Update
    fun updateRecord(record: TravelRecord)

    // 新增方法：获取单个 TravelRecord
    @Query("SELECT * FROM TravelRecord WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): TravelRecord

    @Query("DELETE FROM TravelRecord")
    suspend fun clearAllRecords()

    @Query("DELETE FROM ContentItem")
    suspend fun clearAllContentItems()

    // 查询指定 recordId 下的所有 ContentItem
    @Query("SELECT * FROM ContentItem WHERE recordId = :recordId")
    suspend fun getItemsByRecordId(recordId: Long): List<ContentItem>

}

data class RecordWithFirstImage(
    val id: Long,
    val location: String,
    val date: String,
    val firstImageUri: String?
)

data class RecordWithContents(
    @Embedded val record: TravelRecord,
    @Relation(
        parentColumn = "id",
        entityColumn = "recordId"
    )
    val contents: List<ContentItem>
)
