package com.example.ivlinereporting

import android.app.AlertDialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TechniqueReportFragment : Fragment() {
    private lateinit var techniqueViews: MutableMap<String, EditText>
    private lateinit var titleLinearLayout: LinearLayout
    private lateinit var techniqueContainer: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_technique_report, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        techniqueContainer = requireView().findViewById<LinearLayout>(R.id.techniqueContainer)
        titleLinearLayout = requireView().findViewById(R.id.titleLinearLayout)
        techniqueViews = mutableMapOf()

        val addItemsButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addTechnique() }

        val sendDataButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendTechniqueReport() }
    }

    private fun addTechnique() {
        if (titleLinearLayout.isInvisible) {
            titleLinearLayout.visibility = View.VISIBLE
        }
        val techniqueLayout = layoutInflater.inflate(R.layout.technique_layout, null)

        val deleteTechniqueButton =
            techniqueLayout.findViewById<ImageView>(R.id.deleteTechniqueButton)

        val techniqueEditText = techniqueLayout.findViewById<EditText>(R.id.techniqueEditText)

        techniqueEditText.setOnClickListener { showTechniqueDialog(techniqueEditText) }

        deleteTechniqueButton.setOnClickListener {
            (techniqueLayout.parent as ViewGroup).removeView(techniqueLayout)
            if (techniqueContainer.childCount == 0) {
                titleLinearLayout.visibility = View.INVISIBLE
            }
        }

        techniqueContainer.addView(techniqueLayout)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendTechniqueReport() {
        if (!validateForm()) {
            return
        }
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow)
        dialog.setTitle("Отправка данных")
        dialog.setMessage("Вы уверены, что хотите отправить отчет о технике?")
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
        if (techniqueContainer.childCount == 0) {
            Toast.makeText(
                requireContext(),
                "Необходимо добавить хотя бы один вид техники",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        val techniqueNames = mutableSetOf<String>()
        for (i in 0 until techniqueContainer.childCount) {
            val techniqueLayout = techniqueContainer.getChildAt(i) as LinearLayout
            val techniqueEditText = techniqueLayout.findViewById<EditText>(R.id.techniqueEditText)
            val quantityEditText = techniqueLayout.findViewById<EditText>(R.id.quantityEditText)

            if (techniqueEditText.text.isEmpty() || quantityEditText.text.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Необходимо заполнить все поля",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            if (!techniqueNames.add(techniqueEditText.text.toString())) {
                Toast.makeText(
                    requireContext(),
                    "Техника не должна повторяться",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
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
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow).setTitle("Ошибка")
                .setMessage("Дата должна быть не более, чем на два дня раньше текущей")
                .setPositiveButton("Ок") { dialog, _ -> { dialog.dismiss() } }.show()
            return false
        }
        if (!selectedDate.before(oneDayAfter)) {
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow).setTitle("Ошибка")
                .setMessage("Дата должна быть не более, чем на один день позже текущей")
                .setPositiveButton("Ок") { dialog, _ -> { dialog.dismiss() } }.show()
            return false
        }
        return true
    }

    private fun showTechniqueDialog(techniqueEditText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_technique, null)
        val searchTechniqueEditText =
            dialogView.findViewById<EditText>(R.id.searchTechniqueEditText)
        val techniqueRecyclerView =
            dialogView.findViewById<RecyclerView>(R.id.techniqueRecyclerView)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val adapter = TechniqueAdapter(getTechniqueNames()) { selectedTechnique ->
            techniqueEditText.setText(selectedTechnique)
            dialog.dismiss()
        }
        techniqueRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        techniqueRecyclerView.adapter = adapter

        searchTechniqueEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        dialog.show()
    }

    private fun getTechniqueNames(): List<String> {
        return listOf(
            "самосвал",
            "илососная машина",
            "Техника"
        )
    }
}