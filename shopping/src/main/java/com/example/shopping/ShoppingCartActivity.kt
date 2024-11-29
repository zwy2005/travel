package com.example.shopping

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.shopping.Dao.CartDao
import com.example.shopping.Dao.GoodsDao
import com.example.shopping.ShopDatabase.ShopDatabase
import com.example.shopping.entities.Cart
import com.example.shopping.entities.Goods
import kotlinx.coroutines.launch

class ShoppingCartActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var tv_title: TextView
    private lateinit var ll_cart: LinearLayout
    private lateinit var carts: List<Cart>
    private lateinit var tv_sum: TextView
    private lateinit var tv_nogoods: LinearLayout
    private val goodsMap: MutableMap<Long, Goods> = mutableMapOf()
    private var totalPrice: Double = 0.0

    private val goodsDao: GoodsDao by lazy {
        ShopDatabase.getDatabase(this).goodsDao()
    }

    private val cartDao: CartDao by lazy {
        ShopDatabase.getDatabase(this).cartDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shopping_cart)
        tv_title = findViewById(R.id.tv_title)
        tv_title.setText("购物车")
        ll_cart = findViewById(R.id.ll_cart)
        tv_sum = findViewById(R.id.tv_sum)
        tv_nogoods = findViewById(R.id.tv_nogoods)


        findViewById<ImageView>(R.id.iv_back).setOnClickListener(this)
        findViewById<Button>(R.id.bt_clear).setOnClickListener(this)
        findViewById<Button>(R.id.bt_buy).setOnClickListener(this)
        findViewById<Button>(R.id.bt_shop).setOnClickListener(this)
        findViewById<Button>(R.id.bt_buy).setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        // 在生命周期范围内启动协程
        lifecycleScope.launch {
            showCart()
        }
    }

    private suspend fun showCart() {
        ll_cart.removeAllViews()
        carts = cartDao.queryAll()
        if(carts.isEmpty()) {
            tv_nogoods.visibility = View.VISIBLE
            return
        } else {
            tv_nogoods.visibility = View.GONE
        }
        for(cart in carts) {
            val goods = goodsDao.queryById(cart.goods_id)
            goodsMap.put(cart.goods_id, goods)

            val view = LayoutInflater.from(this).inflate(R.layout.item_cart, null)
            val iv_thumb = view.findViewById<ImageView>(R.id.iv_thumb)
            val tv_name = view.findViewById<TextView>(R.id.tv_name)
            val tv_desc = view.findViewById<TextView>(R.id.tv_desc)
            val tv_price = view.findViewById<TextView>(R.id.tv_price)
            val tv_count = view.findViewById<TextView>(R.id.tv_count)
            val tv_sum = view.findViewById<TextView>(R.id.tv_sum)

            iv_thumb.setImageResource(resources.getIdentifier(goods.pic_path, "drawable", packageName))
            tv_name.setText(goods.name)
            tv_desc.setText(goods.description)
            tv_price.setText(goods.price.toString())
            tv_count.setText(cart.count.toString())
            tv_sum.setText((goods.price*cart.count).toString())

            iv_thumb.setOnClickListener{v->
                val intent = Intent(this, ShopDetailActivity::class.java)
                intent.putExtra("goods_id", goods.id)
                startActivity(intent)
            }


            ll_cart.addView(view)

            view.setOnLongClickListener { v ->
                // 创建 AlertDialog.Builder
                val builder = androidx.appcompat.app.AlertDialog.Builder(this@ShoppingCartActivity)
                builder.setMessage("是否从购物车中删除商品 ${goods.name}？")

                // 设置“确定”按钮的逻辑
                builder.setPositiveButton("确定") { dialog, _ ->
                    // 删除商品逻辑
                    lifecycleScope.launch {
                        cartDao.delete(cart)
                        ll_cart.removeView(v)
                        goodsMap.remove(goods.id)
                        updateTotalPrice()
                    }

                    dialog.dismiss()
                }

                // 设置“取消”按钮的逻辑
                builder.setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss() // 关闭对话框
                }

                // 显示对话框
                builder.show()

                // 返回 true 表示事件已被消费
                true
            }


        }
        updateTotalPrice()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.iv_back->{
                finish()
            }
            R.id.bt_clear -> {
                lifecycleScope.launch {
                    cartDao.deleteAll()
                    ll_cart.removeAllViews() // 清空视图
                    goodsMap.clear() // 清空内存中的数据
                    updateTotalPrice() // 更新总价
                    tv_nogoods.visibility = View.VISIBLE
                }
            }
            R.id.bt_buy -> {
                val builder = androidx.appcompat.app.AlertDialog.Builder(this@ShoppingCartActivity)
                builder.setTitle("结算商品")
                builder.setMessage("支付功能尚未开通")
                builder.setPositiveButton("我知道了", null)
                builder.create().show()
            }
            R.id.bt_shop -> {
                val intent = Intent(this, ShowGoodsActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }

        }
    }

    private fun updateTotalPrice() {
        totalPrice = carts.sumOf { cart ->
            val goods = goodsMap[cart.goods_id]
            goods?.price?.times(cart.count) ?: 0.0
        }
        tv_sum.text = totalPrice.toString()
    }


}