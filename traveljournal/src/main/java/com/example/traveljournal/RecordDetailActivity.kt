package com.example.traveljournal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.example.traveljournal.database.TravelDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class RecordDetailActivity : AppCompatActivity() {

    private lateinit var etLocation: EditText
    private lateinit var etDate: EditText
    private lateinit var rvContents: RecyclerView
    private lateinit var contentAdapter: ContentAdapter

    private val contentItems = mutableListOf<Item>() // 动态内容列表
    private val deletedItems = mutableListOf<Item>() // 删除的内容

    private val imageStorageDir: File by lazy {
        File(filesDir, "images").apply {
            if (!exists()) mkdirs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_detail)

        val recordId = intent.getLongExtra("RECORD_ID", -1)
        if (recordId == -1L) {
            finish()
            return
        }

        etLocation = findViewById(R.id.et_location)
        etDate = findViewById(R.id.et_date)
        rvContents = findViewById(R.id.rv_contents)

        contentAdapter = ContentAdapter(contentItems, onDelete = { position ->
            deleteContentItem(position)
        })

        rvContents.layoutManager = LinearLayoutManager(this)
        rvContents.adapter = contentAdapter
        rvContents.setHasFixedSize(true)

        loadRecordDetails(recordId)

        findViewById<Button>(R.id.btn_add_text).setOnClickListener { addTextItem() }
        findViewById<Button>(R.id.btn_add_image).setOnClickListener { addImageItem() }
        findViewById<Button>(R.id.btn_save).setOnClickListener { saveChanges(recordId) }
        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun loadRecordDetails(recordId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = TravelDatabase.getDatabase(this@RecordDetailActivity)
            val record = db.travelDao().getRecordWithContents(recordId)

            withContext(Dispatchers.Main) {
                etLocation.setText(record.record.location)
                etDate.setText(record.record.date)

                val oldList = ArrayList(contentItems)
                contentItems.clear()

                contentItems.addAll(record.contents.map {
                    when (it.type) {
                        0 -> Item.TextItem(it.id, it.content)
                        1 -> Item.ImageItem(it.id, it.content)
                        else -> throw IllegalArgumentException("Unknown type")
                    }
                })

                val diffCallback = ContentDiffCallback(oldList, contentItems)
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                diffResult.dispatchUpdatesTo(contentAdapter)
            }
        }
    }

    private fun addTextItem() {
        val inputText = EditText(this)
        inputText.hint = "请输入文字"
        inputText.setLines(3)

        AlertDialog.Builder(this)
            .setTitle("添加文字")
            .setView(inputText)
            .setPositiveButton("确认") { dialog, _ ->
                val text = inputText.text.toString()
                if (text.isNotEmpty()) {
                    contentItems.add(Item.TextItem(0, text))
                    contentAdapter.notifyItemInserted(contentItems.size - 1)
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addImageItem() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val imagePath = saveImageToAppDirectory(it)
                    withContext(Dispatchers.Main) {
                        contentItems.add(Item.ImageItem(0, imagePath))
                        contentAdapter.notifyItemInserted(contentItems.size - 1)
                    }
                }
            }
        }
    }

    private fun saveImageToAppDirectory(imageUri: Uri): String {
        val inputStream = contentResolver.openInputStream(imageUri)
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val outputFile = File(imageStorageDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }

        return outputFile.absolutePath
    }

    private fun deleteContentItem(position: Int) {
        val oldList = ArrayList(contentItems)
        val removedItem = contentItems.removeAt(position)
        if (removedItem.id > 0) deletedItems.add(removedItem)

        val diffCallback = ContentDiffCallback(oldList, contentItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(contentAdapter)
    }

    private fun saveChanges(recordId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = TravelDatabase.getDatabase(this@RecordDetailActivity)

            val updatedRecord = db.travelDao().getRecordById(recordId).apply {
                location = etLocation.text.toString()
                date = etDate.text.toString()
            }

            db.travelDao().updateRecord(updatedRecord)

            val newContents = contentItems.filter { it.id == 0L }
            val existingContents = contentItems.filter { it.id > 0L }

            db.travelDao().updateContentItems(existingContents.mapIndexed { index, item ->
                item.toContentItem(recordId, index)
            })

            db.travelDao().insertContentItems(newContents.mapIndexed { index, item ->
                item.toContentItem(recordId, index)
            })

            deletedItems.forEach {
                db.travelDao().deleteContentItemById(it.id)
            }
            deletedItems.clear()

            withContext(Dispatchers.Main) {
                contentAdapter.notifyDataSetChanged()
                Toast.makeText(this@RecordDetailActivity, "All changes saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

class ContentDiffCallback(
    private val oldList: List<Item>,
    private val newList: List<Item>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
