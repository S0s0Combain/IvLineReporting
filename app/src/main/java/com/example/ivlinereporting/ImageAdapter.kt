package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class ImageAdapter(private val items: MutableList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val deleteImageButton = itemView.findViewById<ImageButton>(R.id.deleteImageButton)
    }

    class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView: ImageView = itemView.findViewById(R.id.pdfView)
        val deletePdfButton = itemView.findViewById<ImageButton>(R.id.deletePdfButton)
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder ->{
                val image = items[position] as Bitmap
                holder.imageView.setImageBitmap(image)
                holder.deleteImageButton.setOnClickListener{
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, items.size)
                }
                holder.imageView.setOnClickListener{
                    showImageDialog(holder.imageView.context, image)
                }

            }
            is PdfViewHolder ->{
                val pdfUri = items[position] as Uri
                holder.pdfView.setImageResource(R.drawable.baseline_picture_as_pdf_24)
                holder.deletePdfButton.setOnClickListener{
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, items.size)
                }
                holder.pdfView.setOnClickListener{
                    openPdf(holder.itemView.context, pdfUri)
                }
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

    fun clearImages(){
        items.clear()
        notifyDataSetChanged()
    }

    private fun showImageDialog(context: Context, image: Bitmap){
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar)
        dialog.setContentView(R.layout.dialog_image_view)
        val imageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
        val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)
        imageView.setImageBitmap(image)

        closeButton.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openPdf(context: Context, uri: Uri){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.startActivity(intent)
    }
}