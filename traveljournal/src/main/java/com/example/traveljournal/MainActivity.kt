package com.example.traveljournal

import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.RecordWithFirstImage
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.database.TravelRecord
import com.example.traveljournal.database.ContentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var recordAdapter: RecordAdapter // 适配器声明
    private lateinit var db: TravelDatabase // 数据库声明

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES
        )

        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            // 请求权限
            ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), 101)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        checkAndRequestPermissions()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvRecords = findViewById<RecyclerView>(R.id.rv_records)
        rvRecords.layoutManager = LinearLayoutManager(this)

        db = TravelDatabase.getDatabase(this) // 初始化数据库实例

        // 初始化适配器
        recordAdapter = RecordAdapter(
            mutableListOf(),
            onItemClick = { record ->
                // 点击某项记录，跳转到详情页面
                val intent = Intent(this, RecordDetailActivity::class.java)
                intent.putExtra("RECORD_ID", record.id) // 传递记录的 ID
                startActivity(intent)
            },
            onDeleteClick = { record ->
                // 删除逻辑：从数据库删除记录
                deleteRecord(record)
            }
        )
        rvRecords.adapter = recordAdapter

        // 初次加载数据
        loadRecords()

        // 添加记录按钮
        val btnAddRecord = findViewById<Button>(R.id.btn_add_record)
        btnAddRecord.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        // 清空所有数据按钮
        val btnClearData = findViewById<Button>(R.id.btn_clear_data)
        btnClearData.setOnClickListener {
            clearAllData() // 点击后清空所有数据
        }
    }

    override fun onResume() {
        super.onResume()
        // 在返回 MainActivity 时重新加载数据
        loadRecords()
    }

    private fun loadRecords() {
        lifecycleScope.launch(Dispatchers.IO) {
            val records = db.travelDao().getAllRecordsWithFirstImage()

            withContext(Dispatchers.Main) {
                recordAdapter.updateRecords(records) // 更新数据
            }
        }
    }

    private fun deleteRecord(record: RecordWithFirstImage) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.travelDao().deleteRecordById(record.id) // 从数据库删除记录

            withContext(Dispatchers.Main) {
                // 使用适配器的删除方法更新数据
                recordAdapter.removeRecord(record)
                Toast.makeText(this@MainActivity, "记录已删除", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun add1000Records() {
        lifecycleScope.launch(Dispatchers.IO) {
            val newRecords = List(500) {
                val locationIndex = 501 + it // 递增的地点编号，从 Location500 到 Location999
                val location = "Location$locationIndex" // Location500, Location501, ...

                // 生成随机日期，年份在 2000 到 2024 之间
                val randomYear = Random.nextInt(2000, 2025) // 生成 2000 到 2024 年的随机年份
                val randomMonth = Random.nextInt(1, 13).toString().padStart(2, '0') // 生成 01 到 12 的月份
                val randomDay = Random.nextInt(1, 29).toString().padStart(2, '0') // 生成 01 到 28 的日期
                val randomDate = "$randomYear-$randomMonth-$randomDay"

                TravelRecord(
                    location = location,
                    date = randomDate
                )
            }

            // 插入记录并返回 ID 列表
            val recordIds = db.travelDao().insertRecords(newRecords)

            // 为每条记录生成随机图片内容，并为每个ContentItem指定一个orderIndex
            val contentItems = recordIds.flatMap { recordId ->
                List(1) { // 每条记录只插入一张图片
                    ContentItem(
                        recordId = recordId,
                        type = 1, // 假设 1 表示图片
                        content = getRandomImageUri(), // 随机图库图片路径
                        orderIndex = 0 // 设置 orderIndex（可以根据需求调整）
                    )
                }
            }

            db.travelDao().insertContentItems(contentItems)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "已添加1000条记录", Toast.LENGTH_SHORT).show()
                loadRecords() // 重新加载记录
            }
        }
    }


    private fun getRandomImageUri(): String {
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        val imagePaths = mutableListOf<String>()
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                imagePaths.add(it.getString(columnIndex))
            }
        }
        return imagePaths.randomOrNull() ?: ""
    }

    private fun clearAllData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 删除所有记录和内容项
            db.travelDao().clearAllRecords()
            db.travelDao().clearAllContentItems()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "所有数据已清空", Toast.LENGTH_SHORT).show()
                loadRecords() // 清空后重新加载数据
            }
        }
    }
}
