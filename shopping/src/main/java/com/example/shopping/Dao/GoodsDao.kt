package com.example.shopping.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.shopping.entities.Goods

@Dao
interface GoodsDao {
    @Insert
    suspend fun insert(goods: List<Goods>)

    @Query("delete from Goods where 1=1")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(vararg  goods: Goods)

    @Update
    suspend fun update(good : Goods)

    @Query("select * from Goods order by price desc")
    suspend fun queryAll(): List<Goods>

    @Query("select * from Goods where name = :name")
    suspend fun queryByName(name: String): List<Goods>

    @Query("select * from Goods where id = :id")
    suspend fun queryById(id: Long): Goods

}