package com.example.ivlinereporting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class WorkingHoursReportFragment : Fragment() {
    private lateinit var workersViews: MutableMap<String, EditText>

    lateinit var titleLinearLayout: LinearLayout
    lateinit var workersContainer: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_working_hours_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleLinearLayout = requireView().findViewById(R.id.titleLinearLayout)
        workersViews = mutableMapOf()

        val addWorkerButton = requireActivity().findViewById<FloatingActionButton>(R.id.addWorkerButton)
        addWorkerButton.setOnClickListener { addWorker() }
    }

    private fun addWorker() {
        if(titleLinearLayout.isInvisible){
            titleLinearLayout.visibility = View.VISIBLE
        }
        val workerLayout = layoutInflater.inflate(R.layout.worker_layout, null)

        val deleteWorkerButton = workerLayout.findViewById<ImageView>(R.id.deleteWorkerButton)
        val workerSpinner = workerLayout.findViewById<Spinner>(R.id.workerSpinner)
        val hoursEditText = workerLayout.findViewById<EditText>(R.id.hoursEditText)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            getWorkersNames()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        workerSpinner.adapter = adapter

        workersViews[workerSpinner.selectedItem.toString()] = hoursEditText

        deleteWorkerButton.setOnClickListener{
            (workerLayout.parent as ViewGroup).removeView(workerLayout)
            if(workersContainer.childCount==0){
                titleLinearLayout.visibility = View.INVISIBLE
            }
        }

        workersContainer = requireView().findViewById<LinearLayout>(R.id.workersContainer)
        workersContainer.addView(workerLayout)
    }

    private fun getWorkersNames(): List<String> {
        return listOf("Иванов Иван Иванович", "Петров Петр Петрович", "Васильев Василий Васильевич", "Николай Коля")
    }
}