package com.example.ivlinereporting

import android.app.AlertDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection

class MaterialsFragment : Fragment(), OnAddItemClickListener, OnSendDataClickListener {
    lateinit var materialsContainer: LinearLayout
    private lateinit var materialViews: MutableMap<String, EditText>
    private lateinit var materialParametersViews: MutableMap<String, MutableMap<String, Spinner>>
    private lateinit var materials: List<String>
    private lateinit var materialParameters: Map<String, Map<String, List<String>>>
    private lateinit var materialUnits: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_materials, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
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

        CoroutineScope(Dispatchers.IO).launch {
            val (materialList, materialParametersMap, materialUnitsMap) = getMaterialsAndParametersFromDB()
            withContext(Dispatchers.Main){
                materials = materialList
                materialParameters = materialParametersMap
                materialUnits = materialUnitsMap
            }
        }
    }

    private suspend fun getMaterialsAndParametersFromDB(): Triple<List<String>, Map<String, Map<String, List<String>>>, Map<String, String>> {
        return withContext(Dispatchers.IO) {
            val dbConnection = DatabaseConnection()
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = dbConnection.createConnection()

            val materials = mutableListOf<String>()
            val materialParameters = mutableMapOf<String, Map<String, List<String>>>()
            val materialUnits = mutableMapOf<String, String>()

            val userBrigadeType = getUserBrigadeType(connection)

            val materialsStatement = connection.prepareStatement("SELECT м.наименование, м.единица_измерения FROM материалы м JOIN материалы_виды_бригад мвб ON м.код = мвб.код_материала WHERE мвб.вид_бригады = ?")
            materialsStatement.setString(1, userBrigadeType)
            val materialResultSet = materialsStatement.executeQuery()
            while (materialResultSet.next()) {
                val materialName = materialResultSet.getString("наименование")
                val unitMeasurement = materialResultSet.getString("единица_измерения")
                materials.add(materialName)
                materialUnits[materialName] = unitMeasurement

                val parametersStatement = connection.prepareStatement(
                    "SELECT наименование_параметра, значение_параметра FROM параметры_материалов WHERE код_материала = (SELECT код FROM материалы WHERE наименование = ?)"
                )
                parametersStatement.setString(1, materialName)
                val parametersResultSet = parametersStatement.executeQuery()
                val parameters = mutableMapOf<String, MutableList<String>>()
                while (parametersResultSet.next()) {
                    val parameterName = parametersResultSet.getString("наименование_параметра")
                    val parameterValue = parametersResultSet.getString("значение_параметра")
                    parameters.putIfAbsent(parameterName, mutableListOf())
                    parameters[parameterName]?.add(parameterValue)
                }
                materialParameters[materialName] = parameters
            }

            dbConnection.closeConnection(connection)
            Triple(materials, materialParameters, materialUnits)
        }
    }



    private suspend fun getUserBrigadeType(connection: Connection): String {
        val userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("code", null)
        val brigadeStatement = connection.prepareStatement(
            "SELECT вид_бригады FROM бригады WHERE код_бригадира = ?"
        )
        brigadeStatement.setInt(1, userId.toString().toInt())
        val brigadeResultSet = brigadeStatement.executeQuery()
        if (brigadeResultSet.next()) {
            return brigadeResultSet.getString("вид_бригады")
        }
        throw IllegalStateException("Brigade type not found for user")
    }

    override fun onAddItemClick() {
        addMaterial()
    }

    @RequiresApi(Build.VERSION_CODES.N)
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
        val unitMeasurementTextView = materialLayout.findViewById<TextView>(R.id.unitMeasurementTextView)

        deleteMaterialButton.setOnClickListener {
            (materialLayout.parent as ViewGroup).removeView(materialLayout)
        }

        materialEditText.setOnClickListener {
            showMaterialDialog(
                materialEditText,
                materialParametersContainer,
                unitMeasurementTextView
            )
        }

        materialsContainer.addView(materialLayout)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun sendMaterialsReport() {
        if (!validateForm()) {
            return
        }
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow)
        dialog.setTitle("Отправка данных")
        dialog.setMessage("Вы уверены, что хотите отправить отчет о выполненной работе?")
        dialog.setPositiveButton("Подтвердить") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(requireContext(), "Данные отправлены успешно", Toast.LENGTH_SHORT).show()
            DialogUtils.showEncouragementDialog(requireContext(), "Спасибо!", "Вы тщательно отследили использование материалов! Ваша внимательнность к деталям не осталась незамеченной!")
            materialsContainer.removeAllViews()
        }
        dialog.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun validateForm(): Boolean {
        if (materialsContainer.childCount == 0) {
            Toast.makeText(
                requireContext(),
                "Необходимо указать хотя бы один материал",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val materialsData = mutableSetOf<Pair<String, Map<String, String>>>()

        for (i in 0 until materialsContainer.childCount) {
            val materialsLayout = materialsContainer.getChildAt(i) as LinearLayout
            val materialsEditText = materialsLayout.findViewById<EditText>(R.id.materialEditText)
            val quantityEditText = materialsLayout.findViewById<EditText>(R.id.quantityEditText)
            val materialParametersContainer = materialsLayout.findViewById<LinearLayout>(R.id.parametersContainer)

            if (materialsEditText.text.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Необходимо указать название материала",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            if (quantityEditText.text.isEmpty()) {
                Toast.makeText(requireContext(), "Необходимо ввести количество", Toast.LENGTH_SHORT)
                    .show()
                return false
            }

            if (quantityEditText.text.toString().toInt() < 1) {
                Toast.makeText(requireContext(), "Количество должно быть больше 1", Toast.LENGTH_SHORT).show()
                return false
            }

            val materialName = materialsEditText.text.toString()
            val parameters = mutableMapOf<String, String>()

            for (j in 0 until materialParametersContainer.childCount) {
                val parameterLayout = materialParametersContainer.getChildAt(j) as LinearLayout
                val parameterNameTextView = parameterLayout.findViewById<TextView>(R.id.parameterNameTextView)
                val parameterValueSpinner = parameterLayout.findViewById<Spinner>(R.id.parameterValueSpinner)

                val parameterName = parameterNameTextView.text.toString()
                val parameterValue = parameterValueSpinner.selectedItem.toString()

                parameters[parameterName] = parameterValue
            }

            if (!materialsData.add(Pair(materialName, parameters))) {
                Toast.makeText(
                    requireContext(),
                    "Виды материалов не должны повторяться",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }

        return validateActivityFields()
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
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogRed)
                .setTitle("Ошибка")
                .setMessage("Дата должна быть не более, чем на два дня раньше текущей")
                .setPositiveButton("Ок") { dialog, _ -> { dialog.dismiss() } }.show()
            return false
        }
        if (!selectedDate.before(oneDayAfter)) {
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogRed)
                .setTitle("Ошибка")
                .setMessage("Дата должна быть не более, чем на один день позже текущей")
                .setPositiveButton("Ок") { dialog, _ -> { dialog.dismiss() } }.show()
            return false
        }
        return true
    }

    private fun showMaterialDialog(
        materialEditText: EditText,
        materialParametersContainer: LinearLayout,
        unitMeasurementTextView: TextView
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_material, null)
        val searchMaterialsEditText =
            dialogView.findViewById<EditText>(R.id.searchMaterialsEditText)
        val materialsRecyclerView =
            dialogView.findViewById<RecyclerView>(R.id.materialsRecyclerView)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogWhite)
            .setView(dialogView)
            .create()

        val adapter = MaterialAdapter(materials) { selectedMaterial ->
            materialEditText.setText(selectedMaterial)
            updateMaterialParameters(materialParametersContainer, selectedMaterial, unitMeasurementTextView) // Передайте unitMeasurementTextView
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
        materialName: String,
        unitMeasurementTextView: TextView
    ) {
        materialParametersContainer.removeAllViews()
        val parameters = materialParameters[materialName] ?: mapOf()
        val parameterViews = materialParametersViews[materialName]

        for ((parameter, values) in parameters) {
            val parameterLayout = layoutInflater.inflate(R.layout.parameter_item, null)
            val parameterTextView = parameterLayout.findViewById<TextView>(R.id.parameterNameTextView)
            val parameterValueSpinner = parameterLayout.findViewById<Spinner>(R.id.parameterValueSpinner)

            parameterTextView.text = parameter

            val parameterAdapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                values
            )
            parameterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            parameterValueSpinner.adapter = parameterAdapter

            parameterViews?.put(parameter, parameterValueSpinner)
            materialParametersContainer.addView(parameterLayout)
        }

        unitMeasurementTextView.text = materialUnits[materialName]
    }

}
