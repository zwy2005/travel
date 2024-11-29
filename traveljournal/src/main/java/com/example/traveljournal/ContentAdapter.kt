package com.example.traveljournal

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView

class ContentAdapter(
    private val items: MutableList<Item>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_IMAGE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Item.TextItem -> TYPE_TEXT
            is Item.ImageItem -> TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TEXT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_text, parent, false)
            TextViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image, parent, false)
            ImageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setOnLongClickListener {
            onDelete(position)
            true
        }

        when (val item = items[position]) {
            is Item.TextItem -> (holder as TextViewHolder).bind(item)
            is Item.ImageItem -> (holder as ImageViewHolder).bind(item)
        }
    }

    override fun getItemCount() = items.size

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val editText: EditText = itemView.findViewById(R.id.tv_text)

        fun bind(item: Item.TextItem) {
            editText.setText(item.text)
            editText.setTag(R.id.tag_text_item, item)
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val taggedItem = editText.getTag(R.id.tag_text_item) as? Item.TextItem
                    taggedItem?.let {
                        it.text = s.toString()
                        //ok--Toast.makeText(editText.context, "Updated item: ${it.text}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_image)

        fun bind(item: Item.ImageItem) {
            imageView.setImageURI(Uri.parse(item.imageUri))
        }
    }
}
