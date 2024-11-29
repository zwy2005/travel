package com.example.shopping.ShopDatabase

import android.content.Context
import android.provider.CalendarContract.Instances
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shopping.Dao.CartDao
import com.example.shopping.Dao.GoodsDao
import com.example.shopping.entities.Cart
import com.example.shopping.entities.Goods

@Database(entities = [Goods::class, Cart::class], version = 1)
abstract class ShopDatabase : RoomDatabase(){
    abstract fun cartDao(): CartDao
    abstract fun goodsDao(): GoodsDao

    companion object {
        @Volatile
        private var INSTANCE: ShopDatabase? = null

        fun getDatabase(context: Context): ShopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}