package com.example.ivlinereporting

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WorkFragment : Fragment(), OnAddItemClickListener {
    lateinit var workContainer: LinearLayout
    private lateinit var workViews: MutableMap<String, EditText>
    private lateinit var workParametersViews: MutableMap<String, MutableMap<String, Spinner>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        workContainer = requireView().findViewById(R.id.workContainer)
        workViews = mutableMapOf()
        workParametersViews = mutableMapOf()

        val addItemsButton = requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addWork() }
    }

    override fun onAddItemClick() {
        addWork()
    }

    private fun addWork() {
        val workLayout = layoutInflater.inflate(R.layout.work_item, workContainer, false)

        val deleteWorkButton = workLayout.findViewById<ImageView>(R.id.deleteWorkButton)
        val workEditText = workLayout.findViewById<EditText>(R.id.workEditText)
        val workParametersContainer = workLayout.findViewById<LinearLayout>(R.id.parametersContainer)

        deleteWorkButton.setOnClickListener {
            (workLayout.parent as ViewGroup).removeView(workLayout)
        }

        workEditText.setOnClickListener { showWorkDialog(workEditText, workParametersContainer) }

        workContainer.addView(workLayout)
    }

    private fun showWorkDialog(workEditText: EditText, workParametersContainer: LinearLayout) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_work, null)
        val searchWorksEditText = dialogView.findViewById<EditText>(R.id.searchWorksEditText)
        val worksRecyclerView = dialogView.findViewById<RecyclerView>(R.id.worksRecyclerView)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val adapter = WorkAdapter(getWorks()) { selectedWork ->
            workEditText.setText(selectedWork)
            updateWorkParameters(workParametersContainer, selectedWork)
            dialog.dismiss()
        }
        worksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        worksRecyclerView.adapter = adapter

        searchWorksEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        dialog.show()
    }

    private fun updateWorkParameters(workParametersContainer: LinearLayout, workName: String) {
        workParametersContainer.removeAllViews()
        val parameters = getWorkParameters(workName)
        val parameterViews = workParametersViews[workName]

        for (parameter in parameters) {
            val parameterLayout = layoutInflater.inflate(R.layout.parameter_item, null)
            val parameterTextView = parameterLayout.findViewById<TextView>(R.id.parameterNameTextView)
            val parameterValueSpinner = parameterLayout.findViewById<Spinner>(R.id.parameterValueSpinner)

            parameterTextView.text = parameter

            val parameterAdapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                getParameterValues(parameter)
            )
            parameterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            parameterValueSpinner.adapter = parameterAdapter

            parameterViews?.put(parameter, parameterValueSpinner)
            workParametersContainer.addView(parameterLayout)
        }
    }

    private fun getWorks(): List<String> {
        return listOf("Ознакомление с объектом работ", "Укладка трубы", "Копка траншеи")
    }

    private fun getWorkParameters(workName: String): List<String> {
        return when (workName) {
            "Укладка трубы" -> listOf("Тип укладки")
            "Копка траншеи" -> listOf("Тип копки")
            else -> listOf()
        }
    }

    private fun getParameterValues(parameterName: String): List<String> {
        return when (parameterName) {
            "Тип укладки" -> listOf("Открытая", "С помощью ГНБ")
            "Тип копки" -> listOf("Ручная", "С помощью техники")
            else -> listOf()
        }
    }
}
