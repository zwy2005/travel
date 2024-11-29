package com.example.traveljournal

import android.util.Log
import com.example.traveljournal.database.ContentItem

sealed class Item(open val id: Long) { // 基类增加 id
    data class TextItem(override val id: Long, var text: String) : Item(id)
    data class ImageItem(override val id: Long, val imageUri: String) : Item(id)
}

// 定义一个扩展函数，转换 Item 到 ContentItem
fun Item.toContentItem(recordId: Long, orderIndex: Int): ContentItem {
    Log.d("ItemConversion", "Converting item: $this")
    return when (this) {
        is Item.TextItem -> ContentItem(
            id = if (this.id == 0L) 0L else this.id, // 如果 id 为 0，表示新内容；否则，保持原有 id
            recordId = recordId,
            type = 0, // 类型为文本
            content = this.text,
            orderIndex = orderIndex
        )
        is Item.ImageItem -> ContentItem(
            id = if (this.id == 0L) 0L else this.id, // 如果 id 为 0，表示新内容；否则，保持原有 id
            recordId = recordId,
            type = 1, // 类型为图片
            content = this.imageUri,
            orderIndex = orderIndex
        )
        else -> throw IllegalArgumentException("Unknown item type")
    }
}

