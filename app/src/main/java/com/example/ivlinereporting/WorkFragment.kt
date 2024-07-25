package com.example.ivlinereporting

import android.os.Bundle
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.w3c.dom.Text

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
        workViews = mutableMapOf()
        workParametersViews = mutableMapOf()
    }

    override fun onAddItemClick() {
        addWork()
    }

    private fun addWork() {
        workContainer = requireView().findViewById(R.id.workContainer)

        val workLayout = layoutInflater.inflate(R.layout.work_item, workContainer, false)

        val deleteWorkButton = workLayout.findViewById<ImageView>(R.id.deleteWorkButton)
        val workSpinner = workLayout.findViewById<Spinner>(R.id.workSpinner)
        val workParametersContainer =
            workLayout.findViewById<LinearLayout>(R.id.parametersContainer)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            getWorks()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        workSpinner.adapter = adapter

        deleteWorkButton.setOnClickListener {
            (workLayout.parent as ViewGroup).removeView(workLayout)
        }

        workSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateWorkParameters(workParametersContainer, workSpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        workContainer.addView(workLayout)
    }

    private fun updateWorkParameters(workParametersContainer: LinearLayout, workName: String) {
        workParametersContainer.removeAllViews()
        val parameters = getWorkParameters(workName)
        val parameterViews = workParametersViews[workName]

        for (parameter in parameters) {
            val parameterLayout = layoutInflater.inflate(R.layout.parameter_item, null)
            val parameterTextView =
                parameterLayout.findViewById<TextView>(R.id.parameterNameTextView)
            val parameterValueSpinner =
                parameterLayout.findViewById<Spinner>(R.id.parameterValueSpinner)

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