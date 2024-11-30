package com.example.traveljournal


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljournal.database.ContentItem
import com.example.traveljournal.database.TravelDatabase
import com.example.traveljournal.database.TravelRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AddRecordActivity : AppCompatActivity() {

    private val contentList = mutableListOf<Item>() // 数据列表
    private lateinit var contentAdapter: ContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)

        val etLocation = findViewById<EditText>(R.id.et_location)
        val tvDate = findViewById<TextView>(R.id.tv_date)
        val btnPickDate = findViewById<Button>(R.id.btn_pick_date)
        val btnAddText = findViewById<Button>(R.id.btn_add_text)
        val btnAddImage = findViewById<Button>(R.id.btn_add_image)
        val rvContent = findViewById<RecyclerView>(R.id.rv_content)
        val btnSave = findViewById<Button>(R.id.btn_save)

        // 初始化 RecyclerView
        contentAdapter = ContentAdapter(contentList) { position ->
            deleteContentItem(position)
        }
        rvContent.layoutManager = LinearLayoutManager(this)
        rvContent.adapter = contentAdapter

        // 日期选择
        val calendar = Calendar.getInstance()
        btnPickDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    tvDate.text = "$year-${month + 1}-$dayOfMonth"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // 添加文字
        btnAddText.setOnClickListener {
            val inputText = EditText(this)
            inputText.hint = "请输入文字"
            inputText.setSingleLine(false)
            inputText.setLines(3)

            AlertDialog.Builder(this)
                .setTitle("添加文字")
                .setView(inputText)
                .setPositiveButton("确认") { _, _ ->
                    val text = inputText.text.toString()
                    if (text.isNotEmpty()) {
                        contentList.add(Item.TextItem(id = System.currentTimeMillis(), text = text))
                        contentAdapter.notifyItemInserted(contentList.size - 1)
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        }

        // 添加图片
        btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        // 保存
        btnSave.setOnClickListener {
            val location = etLocation.text.toString()
            val date = tvDate.text.toString()

            if (location.isNotEmpty() && contentList.isNotEmpty() && date != "请选择日期") {
                val travelRecord = TravelRecord(location = location, date = date)

                // 为 contentList 添加顺序索引
                val contentItems = contentList.mapIndexed { index, item ->
                    when (item) {
                        is Item.TextItem -> ContentItem(
                            type = 0,
                            content = item.text,
                            recordId = 0,
                            orderIndex = index // 设置顺序值
                        )
                        is Item.ImageItem -> ContentItem(
                            type = 1,
                            content = item.imageUri,
                            recordId = 0,
                            orderIndex = index // 设置顺序值
                        )
                    }
                }

                // 在协程中执行数据库操作
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val db = TravelDatabase.getDatabase(this@AddRecordActivity)
                        // 插入记录
                        val recordId = db.travelDao().insertRecord(travelRecord)
                        // 插入内容，更新 recordId
                        db.travelDao().insertContentItems(contentItems.map { it.copy(recordId = recordId) })

                        // 切回主线程显示 Toast 提醒
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddRecordActivity, "记录保存成功！", Toast.LENGTH_SHORT).show()
                            finish() // 关闭当前界面
                        }
                    } catch (e: Exception) {
                        // 处理插入失败的情况
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddRecordActivity, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // 表单验证失败
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }

    }

    private fun deleteContentItem(position: Int) {
        contentList.removeAt(position)
        contentAdapter.notifyItemRemoved(position)
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { it.copyTo(outputStream) }
            outputStream.close()
            file.absolutePath // 返回保存的本地路径
        } catch (e: Exception) {
            e.printStackTrace()
            null // 返回 null 表示保存失败
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                // 将图片保存到本地文件系统
                lifecycleScope.launch(Dispatchers.IO) {
                    val localPath = saveImageToInternalStorage(imageUri)
                    if (localPath != null) {
                        // 添加到内容列表并切换回主线程更新 UI
                        withContext(Dispatchers.Main) {
                            contentList.add(Item.ImageItem(id = 0, imageUri = localPath))
                            contentAdapter.notifyItemInserted(contentList.size - 1)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddRecordActivity, "图片保存失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }
}

