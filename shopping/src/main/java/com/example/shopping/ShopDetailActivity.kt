package com.example.shopping

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.shopping.Dao.CartDao
import com.example.shopping.Dao.GoodsDao
import com.example.shopping.ShopDatabase.ShopDatabase
import com.example.shopping.entities.Goods
import kotlinx.coroutines.launch

class ShopDetailActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var tv_title: TextView
    private lateinit var tv_price: TextView
    private lateinit var tv_desc: TextView
    private lateinit var iv_goods: ImageView
    private var id: Long = 0

    private val goodsDao: GoodsDao by lazy {
        ShopDatabase.getDatabase(this).goodsDao()
    }
    private val cartDao: CartDao by lazy {
        ShopDatabase.getDatabase(this).cartDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shop_detail)

        tv_title = findViewById(R.id.tv_title)
        tv_price = findViewById(R.id.tv_price)
        tv_desc = findViewById(R.id.tv_desc)
        iv_goods = findViewById(R.id.iv_goods)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_cart).setOnClickListener(this)
        findViewById<Button>(R.id.bt_addcart).setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        showDetail()
    }

    private fun showDetail() {
        id = getIntent().getLongExtra("goods_id", 0)
        if(id > 0) {
            lifecycleScope.launch {
                val goods = goodsDao.queryById(id)
                tv_title.setText(goods.name)
                tv_desc.setText(goods.description)
                tv_price.setText(goods.price.toString())
                iv_goods.setImageResource(resources.getIdentifier(goods.pic_path,"drawable", packageName))
            }

        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.iv_back -> {
                finish()
            }
            R.id.iv_cart -> {
                val intent = Intent(this, ShoppingCartActivity::class.java)
                startActivity(intent)
            }
            R.id.bt_addcart -> {
                lifecycleScope.launch {
                    cartDao.addCartByGoodsId(id)
                    Toast.makeText(applicationContext, "add goods successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}