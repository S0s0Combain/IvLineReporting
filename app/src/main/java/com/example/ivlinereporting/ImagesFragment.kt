package com.example.ivlinereporting

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ImagesFragment : Fragment(), OnAddItemClickListener {
    private lateinit var imageAdapter: ImageAdapter
    private val REQUEST_IMAGE_PICK = 1
    private val REQUEST_IMAGE_CAPTURE = 2

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
        imageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onAddItemClick() {
        showDialog()
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Выберите вариант")
        val items = arrayOf("Открыть галерею", "Открыть камеру")
        dialog.setItems(items){_, which ->
            when(which){
                0 -> pickImageFromGallery()
                1->captureImage()
            }
        }
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
                    data?.extras?.get("data")?.let {bitmap ->
                        imageAdapter.addImage(bitmap as Bitmap)
                    }
                }
            }
        }
    }
}