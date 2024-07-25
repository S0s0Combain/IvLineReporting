package com.example.ivlinereporting

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImageAdapter(private val items: MutableList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView: ImageView = itemView.findViewById(R.id.pdfView)
    }

    companion object {
        private const val VIEW_TYPE_IMAGE = 0
        private const val VIEW_TYPE_PDF = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Bitmap -> VIEW_TYPE_IMAGE
            is Uri -> VIEW_TYPE_PDF
            else -> throw IllegalArgumentException("Неизвестный тип файла")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.images_item, parent, false)
                ImageViewHolder(view)
            }

            VIEW_TYPE_PDF -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.pdf_item, parent, false)
                PdfViewHolder(view)
            }

            else -> throw IllegalArgumentException("Неизвестный тип файла")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder ->{
                val image = items[position] as Bitmap
                holder.imageView.setImageBitmap(image)
            }
            is PdfViewHolder ->{
                val pdfUri = items[position] as Uri
                holder.pdfView.setImageResource(R.drawable.baseline_picture_as_pdf_24)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addImage(image: Bitmap) {
        items.add(image)
        notifyItemInserted(items.size - 1)
    }

    fun addPdf(uri: Uri) {
        items.add(uri)
        notifyItemInserted(items.size - 1)
    }
}