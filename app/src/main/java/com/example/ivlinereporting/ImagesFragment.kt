package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
    lateinit var imageRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageAdapter = ImageAdapter(mutableListOf())

        imageRecyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerView)
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSendDataClick() {
        sendAttachment()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun sendAttachment() {
        if (!validateForm()) {
            return
        }
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow)
        dialog.setTitle("Отправка данных")
        dialog.setMessage("Вы уверены, что хотите отправить отчет о выполненной работе?")
        dialog.setPositiveButton("Подтвердить") { dialog, _ ->
            {
                dialog.dismiss()
            }
        }
        dialog.setNegativeButton("Отмена") { dialog, _ ->
            {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun validateForm(): Boolean {
        if (imageRecyclerView.childCount == 0) {
            Toast.makeText(
                requireContext(),
                "Необходимо добавить хотя бы одно вложение",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (!validateActivityFields()) {
            return false
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun validateActivityFields(): Boolean {
        val activity = requireActivity() as InputDataActivity
        val dateEditText = activity.findViewById<EditText>(R.id.dateEditText)
        val objectEditText = activity.findViewById<EditText>(R.id.objectEditText)

        if (dateEditText.text.isEmpty()) {
            Toast.makeText(requireContext(), "Необходимо ввести дату", Toast.LENGTH_SHORT).show()
            return false
        }

        if (objectEditText.text.isEmpty()) {
            Toast.makeText(requireContext(), "Необходимо ввести объект", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!validateDate(dateEditText.text.toString())) {
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun validateDate(date: String): Boolean {
        val selectedDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, date.substring(6).toInt())
            set(Calendar.MONTH, date.substring(3, 5).toInt() - 1)
            set(Calendar.DAY_OF_MONTH, date.substring(0, 2).toInt())
        }
        val twoDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -3)
        }
        val oneDayAfter = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        if (!selectedDate.after(twoDaysAgo)) {
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow)
                .setTitle("Ошибка")
                .setMessage("Дата должна быть не более, чем на два дня раньше текущей")
                .setPositiveButton("Ок") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Данные отправлены успешно",
                        Toast.LENGTH_SHORT
                    ).show()
                    DialogUtils.showEncouragementDialog(requireContext(), "Спасибо!", "Ваша организованность в работе с файлами впечатляет!")
                }
                .show()
            return false
        }
        if (!selectedDate.before(oneDayAfter)) {
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogRed)
                .setTitle("Ошибка")
                .setMessage("Дата должна быть не более, чем на один день позже текущей")
                .setPositiveButton("Ок") { dialog, _ -> dialog.dismiss() }.show()
            return false
        }
        return true
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