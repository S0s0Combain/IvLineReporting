package com.example.ivlinereporting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class WorkingHoursReportFragment : Fragment() {
    private lateinit var workersViews: MutableMap<String, TextInputEditText>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_working_hours_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workersViews = mutableMapOf()

        val addWorkerButton = requireActivity().findViewById<FloatingActionButton>(R.id.addWorkerButton)
        addWorkerButton.setOnClickListener { addWorker() }
    }

    private fun addWorker() {
        val workerLayout = layoutInflater.inflate(R.layout.worker_layout, null)

        val workerSpinner = workerLayout.findViewById<Spinner>(R.id.workerSpinner)
        val hoursEditText = workerLayout.findViewById<TextInputEditText>(R.id.hoursEditText)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            getWorkersNames()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        workerSpinner.adapter = adapter

        workersViews[workerSpinner.selectedItem.toString()] = hoursEditText

        val container = requireView().findViewById<LinearLayout>(R.id.workersContainer)
        container.addView(workerLayout)
    }

    private fun getWorkersNames(): List<String> {
        return listOf("Сотрудник 1", "Сотрудник 2", "Сотрудник 3")
    }
}