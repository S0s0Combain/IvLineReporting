package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ImagesFragment : Fragment(), OnAddItemClickListener, OnSendDataClickListener {
    private lateinit var imageAdapter: ImageAdapter
    private val REQUEST_IMAGE_PICK = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private val REQUEST_PDF_PICK = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageAdapter = ImageAdapter(mutableListOf())

        val imageRecyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerView)
        imageRecyclerView.adapter = imageAdapter
        imageRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        val addItemButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemButton.setOnClickListener { showDialog() }
        val sendDataButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendAttachment() }
    }

    override fun onAddItemClick() {
        showDialog()
    }

    override fun onSendDataClick() {
        sendAttachment()
    }

    fun sendAttachment() {
        Toast.makeText(context, "Отправка вложений", Toast.LENGTH_SHORT).show()
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow)
        dialog.setTitle("Выберите вариант")

        val items = arrayOf("Открыть галерею", "Открыть камеру", "Прикрепить PDF")
        dialog.setItems(items) { _, which ->
            when (which) {
                0 -> pickImageFromGallery()
                1 -> captureImage()
                2 -> pickPdfFromStorage()
            }
        }
        dialog.setNegativeButton("Отмена", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        dialog.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun pickPdfFromStorage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, REQUEST_PDF_PICK)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let { uri ->
                        val image =
                            MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                uri
                            )
                        imageAdapter.addImage(image)
                    }
                }

                REQUEST_IMAGE_CAPTURE -> {
                    data?.extras?.get("data")?.let { bitmap ->
                        imageAdapter.addImage(bitmap as Bitmap)
                    }
                }

                REQUEST_PDF_PICK -> {
                    data?.data?.let { uri -> imageAdapter.addPdf(uri) }
                }
            }
        }
    }
}