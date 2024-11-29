package com.example.travel

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.travel.dao.BookDAO
import com.example.travel.database.BookDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomWriteActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var et_book : EditText
    private lateinit var et_author : EditText
    private lateinit var et_press : EditText
    private lateinit var et_price : EditText

    // 这里的 bookDao 应该正确初始化
    private val bookDao: BookDAO by lazy {
        BookDatabase.getDatabase(this).bookDao()  // 确保传递正确的上下文
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_write)
        et_book = findViewById(R.id.edit_book)
        et_author = findViewById(R.id.edit_author)
        et_press = findViewById(R.id.edit_press)
        et_price = findViewById(R.id.edit_price)

        findViewById<Button>(R.id.bt_add).setOnClickListener(this)
        findViewById<Button>(R.id.bt_modifiy).setOnClickListener(this)
        findViewById<Button>(R.id.bt_delete).setOnClickListener(this)
        findViewById<Button>(R.id.bt_query).setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        val book = et_book.text.toString()
        val author = et_author.text.toString()
        val press = et_press.text.toString()
        val price = et_price.text.toString()

        when (v?.id) {
            R.id.bt_add -> {
                var b1 = BookInfo()
                b1.book = book
                b1.author = author
                b1.press = press
                b1.price = price.toDouble()
                // 使用协程来处理数据库操作
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        bookDao.insert(b1) // 插入操作在IO线程中执行
                    }
                    // 处理完成后显示 Toast
                    Toast.makeText(applicationContext, "save successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_delete -> {
                /*val b2 = BookInfo()
                b2.id = 1
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        bookDao.delete(b2)
                    }
                    Toast.makeText(applicationContext, "delete successfully!", Toast.LENGTH_SHORT).show()
                }*/
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        bookDao.deleteAll()
                    }
                    Toast.makeText(applicationContext, "delete successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_modifiy -> {
                lifecycleScope.launch {
                    // 定义变量，等待查询操作完成后再赋值
                    val b3 = BookInfo()
                    val list: List<BookInfo>

                    // 在后台线程执行查询
                    withContext(Dispatchers.IO) {
                        list = bookDao.getBookByBook(book)
                    }

                    if (list.isNotEmpty()) {
                        // 更新数据，确保查询结果已准备好
                        val b4 = list[0]
                        b3.id = b4.id
                        b3.book = book
                        b3.author = author
                        b3.press = press
                        b3.price = price.toDouble()

                        // 在后台线程执行更新操作
                        withContext(Dispatchers.IO) {
                            bookDao.update(b3)
                        }

                        // 更新成功后在主线程显示 Toast
                        Toast.makeText(applicationContext, "Update Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        // 如果没有查询结果，提示用户
                        Toast.makeText(applicationContext, "No book found to update!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            R.id.bt_query -> {
                lifecycleScope.launch {
                    // 在后台线程执行查询操作
                    val list: List<BookInfo>
                    withContext(Dispatchers.IO) {
                        list = if (book.isNotEmpty()) {
                            bookDao.getBookByBook(book)
                        } else if(author.isNotEmpty()){
                            bookDao.getBookByAuthor(author)
                        } else {
                            bookDao.getBooks()
                        }
                    }

                    // 完成查询后返回主线程更新 UI
                    if (list.isEmpty()) {
                        Toast.makeText(this@RoomWriteActivity, "没有找到符合条件的书籍", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RoomWriteActivity, "找到 ${list.size} 条书籍", Toast.LENGTH_SHORT).show()
                        for (b in list) {
                            Log.d("zwy", b.toString())
                        }
                    }
                }
            }

        }
    }

}