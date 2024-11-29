package com.example.travel.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.travel.BookInfo

@Dao
interface BookDAO {
    @Insert
    suspend fun insert(vararg books: BookInfo)

    @Delete
    suspend fun delete(vararg books: BookInfo)

    @Update
    suspend fun update(book: BookInfo)

    @Query("select * from Book_Info where book == :book")
    suspend fun getBookByBook(book: String): List<BookInfo>

    @Query("select * from Book_Info where author == :author")
    suspend fun getBookByAuthor(author: String): List<BookInfo>

    @Query("select * from Book_Info")
    suspend fun getBooks(): List<BookInfo>

    @Query("delete from Book_Info where 1=1")
    suspend fun deleteAll()

}