package com.example.shopping.Dao

import androidx.room.*
import com.example.shopping.entities.Cart

@Dao
interface CartDao {
    // 插入方法
    @Insert
    suspend fun insert(vararg cart: Cart)

    // 更新方法
    @Update
    suspend fun update(cart: Cart)

    // 删除所有记录
    @Query("DELETE FROM Cart")
    suspend fun deleteAll()

    // 删除特定记录
    @Delete
    suspend fun delete(vararg cart: Cart)

    // 查询所有记录
    @Query("SELECT * FROM Cart")
    suspend fun queryAll(): List<Cart>

    // 根据 goods_id 查询记录
    @Query("SELECT * FROM Cart WHERE goods_id = :goods_id")
    suspend fun queryByGoodsId(goods_id: Long): Cart?

    suspend fun addCartByGoodsId(goodsId: Long) {
        val cart = queryByGoodsId(goodsId)
        if (cart != null) {
            incrementCartCount(goodsId, 1)
        } else {
            val newCart = Cart(goods_id = goodsId, count = 1)
            insert(newCart)
        }
    }

    suspend fun incrementCartCount(goodsId: Long, incrementValue: Long) {
        val cart = queryByGoodsId(goodsId)
        if (cart != null) {
            val updatedCart = cart.copy(count = cart.count + incrementValue)
            update(updatedCart)
        }
    }
}
