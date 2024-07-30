package com.example.ivlinereporting

import android.app.AlertDialog
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
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WorkingHoursReportFragment : Fragment() {
    private lateinit var workersViews: MutableMap<String, EditText>

    lateinit var titleLinearLayout: LinearLayout
    lateinit var workersContainer: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_working_hours_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleLinearLayout = requireView().findViewById(R.id.titleLinearLayout)
        workersViews = mutableMapOf()

        val addItemsButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addWorker() }
        val sendDataButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendWorkingHoursReport() }
    }

    private fun sendWorkingHoursReport() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Отправка данных")
        dialog.setMessage("Вы уверены, что хотите отправить отчет об отработанных часах?")
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
    }

    private fun addWorker() {
        if (titleLinearLayout.isInvisible) {
            titleLinearLayout.visibility = View.VISIBLE
        }
        val workerLayout = layoutInflater.inflate(R.layout.worker_layout, null)

        val deleteWorkerButton = workerLayout.findViewById<ImageView>(R.id.deleteWorkerButton)
        val workerEditText = workerLayout.findViewById<EditText>(R.id.workerEditText)

        workerEditText.setOnClickListener { showWorkerDialog(workerEditText) }

        deleteWorkerButton.setOnClickListener {
            (workerLayout.parent as ViewGroup).removeView(workerLayout)
            if (workersContainer.childCount == 0) {
                titleLinearLayout.visibility = View.INVISIBLE
            }
        }

        workersContainer = requireView().findViewById<LinearLayout>(R.id.workersContainer)
        workersContainer.addView(workerLayout)
    }

    private fun showWorkerDialog(workerEditText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_worker, null)
        val searchWorkerEditText = dialogView.findViewById<EditText>(R.id.searchWorkerEditText)
        val workersRecyclerView = dialogView.findViewById<RecyclerView>(R.id.workersRecyclerView)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val adapter = WorkerAdapter(getWorkersNames()) { selectedWorker ->
            workerEditText.setText(selectedWorker)
            dialog.dismiss()
        }
        workersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        workersRecyclerView.adapter = adapter

        searchWorkerEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        dialog.show()
        adjustDialogSize(dialog, adapter.itemCount)
    }

    fun adjustDialogSize(dialog: AlertDialog, itemCount: Int) {
        val window = dialog.window
        val layoutParams = window?.attributes
        val maxHeight = (resources.displayMetrics.heightPixels * 0.8).toInt()
        val itemHeight = 50
        val desiredHeight = itemHeight * itemCount + 100

        layoutParams?.height = if (desiredHeight > maxHeight) maxHeight else desiredHeight
        window?.attributes = layoutParams
    }

    private fun getWorkersNames(): List<String> {
        return listOf(
            "Иванов Иван Иванович",
            "Петров Петр Петрович",
            "Васильев Василий Васильевич",
            "Николай Коля",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
            "Сотрудник",
        )
    }
}