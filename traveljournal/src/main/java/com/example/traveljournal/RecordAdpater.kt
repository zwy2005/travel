package com.example.traveljournal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.traveljournal.database.RecordWithFirstImage

class RecordAdapter(
    private val records: MutableList<RecordWithFirstImage>, // 数据源
    private val onItemClick: (RecordWithFirstImage) -> Unit,
    private val onDeleteClick: (RecordWithFirstImage) -> Unit
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    private var currentDeleteVisiblePosition: Int? = null

    // 提供更新数据的方法
    fun updateRecords(newRecords: List<RecordWithFirstImage>) {
        records.clear() // 清空当前数据
        records.addAll(newRecords) // 添加新数据
        notifyDataSetChanged() // 刷新视图
    }

    fun removeRecord(record: RecordWithFirstImage) {
        val position = records.indexOf(record)
        if (position != -1) {
            records.removeAt(position)
            notifyItemRemoved(position)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.bind(record)

        holder.itemView.setOnClickListener {
            currentDeleteVisiblePosition?.let {
                notifyItemChanged(it)
                currentDeleteVisiblePosition = null
            }
            onItemClick(record)
        }

        holder.itemView.setOnLongClickListener {
            currentDeleteVisiblePosition?.let {
                notifyItemChanged(it)
            }
            currentDeleteVisiblePosition = holder.adapterPosition
            holder.showDeleteButton(true)
            true
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(record)
            records.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)

            if (currentDeleteVisiblePosition == holder.adapterPosition) {
                currentDeleteVisiblePosition = null
            }
        }

        holder.showDeleteButton(currentDeleteVisiblePosition == position)
    }

    override fun getItemCount() = records.size

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val ivImage: ImageView = itemView.findViewById(R.id.iv_image)
        val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete)

        fun bind(record: RecordWithFirstImage) {
            tvLocation.text = record.location
            tvDate.text = record.date
            val imagePath = record.firstImageUri

            if (!imagePath.isNullOrEmpty()) {
                Glide.with(ivImage.context)
                    .load(imagePath)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(ivImage)
            } else {
                ivImage.setImageResource(R.drawable.placeholder_image)
            }

            showDeleteButton(false)
        }

        fun showDeleteButton(show: Boolean) {
            btnDelete.visibility = if (show) View.VISIBLE else View.GONE
        }
    }
}


