package com.example.ivlinereporting

import android.os.Bundle
import android.text.InputType
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

class MaterialsFragment : Fragment(), OnAddItemClickListener {
    private lateinit var materialViews: MutableMap<String, EditText>
    private lateinit var materialParametersViews: MutableMap<String, MutableMap<String, Spinner>>

    lateinit var materialsContainer: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_materials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        materialViews = mutableMapOf()
        materialParametersViews = mutableMapOf()
    }

    override fun onAddItemClick() {
        addMaterial()
    }

    fun addMaterial() {
        materialsContainer = requireView().findViewById(R.id.materialsContainer)
        val materialLayout =
            layoutInflater.inflate(R.layout.material_item, materialsContainer, false)

        val deleteMaterialButton = materialLayout.findViewById<ImageView>(R.id.deleteMaterialButton)
        val materialSpinner = materialLayout.findViewById<Spinner>(R.id.materialSpinner)
        val quantityEditText = materialLayout.findViewById<EditText>(R.id.quantityEditText)
        val materialParametersContainer =
            materialLayout.findViewById<LinearLayout>(R.id.parametersContainer)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            getMaterials()
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        materialSpinner.adapter = adapter

        materialViews[materialSpinner.selectedItem.toString()] = quantityEditText
        materialParametersViews[materialSpinner.selectedItem.toString()] = mutableMapOf()

        deleteMaterialButton.setOnClickListener {
            (materialLayout.parent as ViewGroup).removeView(materialLayout)
        }

        materialSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateMaterialParameters(
                    materialParametersContainer,
                    materialSpinner.selectedItem.toString()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        materialsContainer.addView(materialLayout)
    }

    private fun updateMaterialParameters(
        materialParametersContainer: LinearLayout,
        materialName: String
    ) {
        materialParametersContainer.removeAllViews()
        val parameters = getMaterialParameters(materialName)
        val parameterViews = materialParametersViews[materialName]

        for (parameter in parameters) {
            val parameterLayout = layoutInflater.inflate(R.layout.material_parameter_item, null)
            val parameterTextView =
                parameterLayout.findViewById<TextView>(R.id.parameterNameTextView)
            val parameterValueSpinner =
                parameterLayout.findViewById<Spinner>(R.id.parameterValueSpinner)

            parameterTextView.text = parameter

            val parameterAdapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                getMaterialParameters(parameter)
            )
            parameterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            parameterValueSpinner.adapter = parameterAdapter

            parameterViews?.put(parameter, parameterValueSpinner)
            materialParametersContainer.addView(parameterLayout)
        }
    }

    private fun getMaterials(): List<String> {
        return listOf("Труба ПЭ", "Муфта", "Телескопический удлинитель", "Грунт-эмаль по металлу")
    }

    private fun getMaterialParameters(materialName: String): List<String> {
        return when (materialName) {
            "Труба ПЭ" -> listOf("Диаметр")
            "Телескопический удлинитель" -> listOf("Длина", "Диаметр")
            else -> listOf()
        }
    }
}