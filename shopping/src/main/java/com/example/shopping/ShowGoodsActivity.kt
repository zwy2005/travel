package com.example.shopping

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shopping.Dao.CartDao
import com.example.shopping.Dao.GoodsDao
import com.example.shopping.ShopDatabase.ShopDatabase
import com.example.shopping.entities.Goods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowGoodsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var tv_title: TextView
    private lateinit var gl_channel: GridLayout
    private lateinit var iv_cart: ImageView
    private lateinit var iv_back: ImageView


    private val goodsDao: GoodsDao by lazy {
        ShopDatabase.getDatabase(this).goodsDao()
    }

    private val cartDao: CartDao by lazy {
        ShopDatabase.getDatabase(this).cartDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_goods)

        tv_title = findViewById(R.id.tv_title)
        tv_title.setText("商城")
        gl_channel = findViewById(R.id.gl_channel)
        iv_back = findViewById(R.id.iv_back)
        iv_cart = findViewById(R.id.iv_cart)

        findViewById<Button>(R.id.bt_add).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_cart).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_back).setOnClickListener(this)
        showGoods()

    }

    private fun showGoods() {
        //gl_channel.removeAllViews()
        val screenWidth = resources.displayMetrics.widthPixels
        val params = LinearLayout.LayoutParams(screenWidth / 2, LinearLayout.LayoutParams.WRAP_CONTENT)

        lifecycleScope.launch {
            val goodsList = withContext(Dispatchers.IO) {
                goodsDao.queryAll() // 在 IO 线程调用 DAO 方法
            }
            for (goods in goodsList) {
                val view = LayoutInflater.from(applicationContext).inflate(R.layout.item_goods, null)
                val iv_thumb = view.findViewById<ImageView>(R.id.iv_thumb)
                val tv_name = view.findViewById<TextView>(R.id.tv_name)
                val tv_price = view.findViewById<TextView>(R.id.tv_price)
                val bt_addcart = view.findViewById<Button>(R.id.bt_addcart)

                // 设置商品信息
                iv_thumb.setImageResource(resources.getIdentifier(goods.pic_path, "drawable", packageName))
                tv_name.text = goods.name
                tv_price.text = String.format("%.2f￥", goods.price)

                // 将商品 ID 绑定到按钮的 tag 属性
                bt_addcart.tag = goods.id

                // 设置点击事件
                bt_addcart.setOnClickListener {
                    val goodsId = it.tag as Long
                    lifecycleScope.launch {
                        addToCart(goodsId)
                    }
                }

                gl_channel.addView(view, params)
            }
        }
    }


    private suspend fun addToCart(goods_id: Long) {
        cartDao.addCartByGoodsId(goods_id)
        Toast.makeText(this, "add goods successfully!", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.bt_add -> {
                // 准备一系列数据
                val goodsList = listOf(
                    Goods(name = "Laptop", description = "High-performance laptop", price = 1200.0, pic_path = "laptop"),
                    Goods(name = "Xiaomi", description = "Latest smartphone model", price = 800.0, pic_path = "xiaomi"),
                    Goods(name = "huawei", description = "Noise-canceling headphones", price = 250.0, pic_path = "huawei"),
                    Goods(name = "Laptop", description = "High-performance laptop", price = 1200.0, pic_path = "laptop"),
                    Goods(name = "Xiaomi", description = "Latest smartphone model", price = 800.0, pic_path = "xiaomi"),
                    Goods(name = "huawei", description = "Noise-canceling headphones", price = 250.0, pic_path = "huawei")
                )

                // 使用协程来处理数据库操作
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        goodsDao.insert(goodsList) // 批量插入操作
                    }
                    // 插入完成后在主线程显示 Toast
                    Toast.makeText(applicationContext, "Goods added successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.iv_back->{
                finish()
            }
            R.id.iv_cart->{
                val intent = Intent(this, ShoppingCartActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }
}