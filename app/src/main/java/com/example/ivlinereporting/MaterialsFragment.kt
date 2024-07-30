package com.example.ivlinereporting

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MaterialsFragment : Fragment(), OnAddItemClickListener, OnSendDataClickListener {
    lateinit var materialsContainer: LinearLayout
    private lateinit var materialViews: MutableMap<String, EditText>
    private lateinit var materialParametersViews: MutableMap<String, MutableMap<String, Spinner>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_materials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        materialsContainer = requireView().findViewById(R.id.materialsContainer)
        materialViews = mutableMapOf()
        materialParametersViews = mutableMapOf()

        val addItemsButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addMaterial() }
        val sendDataButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendMaterialsReport() }
    }

    override fun onAddItemClick() {
        addMaterial()
    }

    override fun onSendDataClick() {
        sendMaterialsReport()
    }

    fun addMaterial() {
        val materialLayout =
            layoutInflater.inflate(R.layout.material_item, materialsContainer, false)

        val deleteMaterialButton = materialLayout.findViewById<ImageView>(R.id.deleteMaterialButton)
        val materialEditText = materialLayout.findViewById<EditText>(R.id.materialEditText)
        val materialParametersContainer =
            materialLayout.findViewById<LinearLayout>(R.id.parametersContainer)

        deleteMaterialButton.setOnClickListener {
            (materialLayout.parent as ViewGroup).removeView(materialLayout)
        }

        materialEditText.setOnClickListener {
            showMaterialDialog(
                materialEditText,
                materialParametersContainer
            )
        }

        materialsContainer.addView(materialLayout)
    }

    fun sendMaterialsReport(){
        Toast.makeText(context, "Отправка отчета о затраченных материалах", Toast.LENGTH_SHORT).show()
    }

    private fun showMaterialDialog(
        materialEditText: EditText,
        materialParametersContainer: LinearLayout
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_material, null)
        val searchMaterialsEditText =
            dialogView.findViewById<EditText>(R.id.searchMaterialsEditText)
        val materialsRecyclerView =
            dialogView.findViewById<RecyclerView>(R.id.materialsRecyclerView)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val adapter = MaterialAdapter(getMaterials()) { selectedMaterial ->
            materialEditText.setText(selectedMaterial)
            updateMaterialParameters(materialParametersContainer, selectedMaterial)
            dialog.dismiss()
        }
        materialsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        materialsRecyclerView.adapter = adapter

        searchMaterialsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        dialog.show()
    }

    private fun updateMaterialParameters(
        materialParametersContainer: LinearLayout,
        materialName: String
    ) {
        materialParametersContainer.removeAllViews()
        val parameters = getMaterialParameters(materialName)
        val parameterViews = materialParametersViews[materialName]

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

    private fun getParameterValues(parameterName: String): List<String> {
        return when (parameterName) {
            "Диаметр" -> listOf("32", "63", "110")
            "Длина" -> listOf("1,2-2,0")
            else -> listOf()
        }
    }
}