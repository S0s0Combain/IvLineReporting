package com.example.ivlinereporting

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.sql.Connection

class MaterialsFragment : Fragment(), OnAddItemClickListener, OnSendDataClickListener {
    lateinit var materialsContainer: LinearLayout
    private lateinit var materialViews: MutableMap<String, EditText>
    private lateinit var materialTypesViews: MutableMap<String, Spinner>
    private lateinit var materials: List<String>
    private lateinit var materialTypes: Map<String, List<String>>
    private lateinit var materialUnits: Map<String, String>
    private lateinit var progressDialog: AlertDialog
    private lateinit var objectUtils: ObjectUtils

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
        materialTypesViews = mutableMapOf()
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Пожалуйста, подождите...")
        progressDialog.setCancelable(false)

        val addItemsButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addMaterial() }
        val sendDataButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendMaterialsReport() }

        progressDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            val (materialList, materialTypesMap, materialUnitsMap) = getMaterialsAndTypesFromDB()
            withContext(Dispatchers.Main){
                progressDialog.dismiss()
                materials = materialList
                materialTypes = materialTypesMap
                materialUnits = materialUnitsMap
            }
        }
        objectUtils = ObjectUtils(requireContext())
    }

    private suspend fun getMaterialsAndTypesFromDB(): Triple<List<String>, Map<String, List<String>>, Map<String, String>> {
        return withContext(Dispatchers.IO) {
            val dbConnection = DatabaseConnection()
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = dbConnection.createConnection()

            val materials = mutableListOf<String>()
            val materialTypes = mutableMapOf<String, List<String>>()
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

                val typesStatement = connection.prepareStatement(
                    "SELECT тм.значение FROM типы_материалов тм JOIN материалы_типы_материалов мтм ON тм.код = мтм.код_типа WHERE мтм.код_материала = (SELECT код FROM материалы WHERE наименование = ?)"
                )
                typesStatement.setString(1, materialName)
                val typesResultSet = typesStatement.executeQuery()
                val types = mutableListOf<String>()
                while (typesResultSet.next()) {
                    val typeValue = typesResultSet.getString("значение")
                    types.add(typeValue)
                }
                materialTypes[materialName] = types
            }

            dbConnection.closeConnection(connection)
            Triple(materials, materialTypes, materialUnits)
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
        val searchMaterialButton = materialLayout.findViewById<ImageView>(R.id.searchMaterialButton)
        val materialParametersContainer =
            materialLayout.findViewById<LinearLayout>(R.id.parametersContainer)
        val unitMeasurementTextView = materialLayout.findViewById<TextView>(R.id.unitMeasurementTextView)

        deleteMaterialButton.setOnClickListener {
            (materialLayout.parent as ViewGroup).removeView(materialLayout)
        }

        searchMaterialButton.setOnClickListener {
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
            createSpreadsheetMLFile()
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

        val materialsData = mutableSetOf<Pair<String, String?>>()

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
            val typeSpinner = materialParametersContainer.findViewById<Spinner>(R.id.parameterValueSpinner)
            val typeValue = if (typeSpinner != null) typeSpinner.selectedItem.toString() else null

            if (!materialsData.add(Pair(materialName, typeValue))) {
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

    private fun createSpreadsheetMLFile() {
        val activity = requireActivity() as InputDataActivity
        val dateEditText = activity.findViewById<EditText>(R.id.dateEditText)
        val objectEditText = activity.findViewById<EditText>(R.id.objectEditText)
        val date = dateEditText.text.toString()
        val obj = objectEditText.text.toString()

        objectUtils.saveObjectIfNotExists(objectEditText)

        val file = File(requireContext().filesDir, "material_report.xml")
        val outputStream = FileOutputStream(file)
        val writer = OutputStreamWriter(outputStream, "UTF-8")

        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        writer.write("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        writer.write("          xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
        writer.write("          xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
        writer.write("          xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        writer.write("          xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")
        writer.write("  <Worksheet ss:Name=\"Материалы\">\n")
        writer.write("    <Table>\n")

        writer.write("      <Column ss:Width=\"${calculateColumnWidth(date)}\"/>\n")
        writer.write("      <Column ss:Width=\"${calculateColumnWidth(obj)}\"/>\n")
        writer.write("      <Column ss:Width=\"${calculateColumnWidthForMaterials()}\"/>\n")
        writer.write("      <Column ss:Width=\"${calculateColumnWidthForTypes()}\"/>\n")
        writer.write("      <Column ss:Width=\"${calculateColumnWidthForQuantity()}\"/>\n")
        writer.write("      <Column ss:Width=\"${calculateColumnWidthForUnits()}\"/>\n")

        writer.write("      <Row>\n")
        writer.write("        <Cell><Data ss:Type=\"String\">Дата</Data></Cell>\n")
        writer.write("        <Cell><Data ss:Type=\"String\">Объект</Data></Cell>\n")
        writer.write("        <Cell><Data ss:Type=\"String\">Наименование</Data></Cell>\n")
        writer.write("        <Cell><Data ss:Type=\"String\">Тип</Data></Cell>\n")
        writer.write("        <Cell><Data ss:Type=\"String\">Количество</Data></Cell>\n")
        writer.write("        <Cell><Data ss:Type=\"String\">Единица измерения</Data></Cell>\n")
        writer.write("      </Row>\n")

        val materialCount = materialsContainer.childCount
        for (i in 0 until materialCount) {
            val materialLayout = materialsContainer.getChildAt(i) as LinearLayout
            val materialEditText = materialLayout.findViewById<EditText>(R.id.materialEditText)
            val materialParametersContainer = materialLayout.findViewById<LinearLayout>(R.id.parametersContainer)
            val quantityEditText = materialLayout.findViewById<EditText>(R.id.quantityEditText)
            val unitMeasurementTextView = materialLayout.findViewById<TextView>(R.id.unitMeasurementTextView)

            writer.write("      <Row>\n")
            if (i == 0) {
                writer.write("        <Cell ss:MergeDown=\"${materialCount - 1}\"><Data ss:Type=\"String\">$date</Data></Cell>\n")
                writer.write("        <Cell ss:MergeDown=\"${materialCount - 1}\"><Data ss:Type=\"String\">$obj</Data></Cell>\n")
            }
            writer.write("        <Cell ss:Index=\"3\"><Data ss:Type=\"String\">${materialEditText.text}</Data></Cell>\n")

            val typeSpinner = materialParametersContainer.findViewById<Spinner>(R.id.parameterValueSpinner)
            if (typeSpinner != null) {
                val typeValue = typeSpinner.selectedItem
                writer.write("        <Cell><Data ss:Type=\"String\">$typeValue</Data></Cell>\n")
            } else {
                writer.write("        <Cell><Data ss:Type=\"String\">-</Data></Cell>\n")
            }

            writer.write("        <Cell><Data ss:Type=\"Number\">${quantityEditText.text}</Data></Cell>\n")
            writer.write("        <Cell><Data ss:Type=\"String\">${unitMeasurementTextView.text}</Data></Cell>\n")

            writer.write("      </Row>\n")
        }

        writer.write("    </Table>\n")
        writer.write("  </Worksheet>\n")
        writer.write("</Workbook>\n")

        writer.close()
        outputStream.close()

        Log.i("fileTag", "Файл SpreadsheetML создан: ${file.absolutePath}")
    }

    private fun calculateColumnWidth(text: String): Int {
        val averageCharWidth = 8
        return text.length * averageCharWidth
    }

    private fun calculateColumnWidthForMaterials(): Int {
        var maxLength = 0
        for (i in 0 until materialsContainer.childCount) {
            val materialLayout = materialsContainer.getChildAt(i) as LinearLayout
            val materialEditText = materialLayout.findViewById<EditText>(R.id.materialEditText)
            val materialName = materialEditText.text.toString()
            if (materialName.length > maxLength) {
                maxLength = materialName.length
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
    }

    private fun calculateColumnWidthForTypes(): Int {
        var maxLength = 0
        for (i in 0 until materialsContainer.childCount) {
            val materialLayout = materialsContainer.getChildAt(i) as LinearLayout
            val materialEditText = materialLayout.findViewById<EditText>(R.id.materialEditText)
            val materialParametersContainer = materialLayout.findViewById<LinearLayout>(R.id.parametersContainer)
            val typeSpinner = materialParametersContainer.findViewById<Spinner>(R.id.parameterValueSpinner)
            if (typeSpinner != null) {
                val typeValue = typeSpinner.selectedItem.toString()
                if (typeValue.length > maxLength) {
                    maxLength = typeValue.length
                }
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
    }

    private fun calculateColumnWidthForQuantity(): Int {
        var maxLength = 0
        for (i in 0 until materialsContainer.childCount) {
            val materialLayout = materialsContainer.getChildAt(i) as LinearLayout
            val quantityEditText = materialLayout.findViewById<EditText>(R.id.quantityEditText)
            val quantity = quantityEditText.text.toString()
            if (quantity.length > maxLength) {
                maxLength = quantity.length
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
    }

    private fun calculateColumnWidthForUnits(): Int {
        var maxLength = 0
        for (i in 0 until materialsContainer.childCount) {
            val materialLayout = materialsContainer.getChildAt(i) as LinearLayout
            val unitMeasurementTextView = materialLayout.findViewById<TextView>(R.id.unitMeasurementTextView)
            val unit = unitMeasurementTextView.text.toString()
            if (unit.length > maxLength) {
                maxLength = unit.length
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
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
            updateMaterialParameters(materialParametersContainer, selectedMaterial, unitMeasurementTextView)
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
        val types = materialTypes[materialName] ?: listOf()

        if (types.isNotEmpty()) {
            val parameterLayout = layoutInflater.inflate(R.layout.parameter_item, null)
            val parameterValueSpinner = parameterLayout.findViewById<Spinner>(R.id.parameterValueSpinner)

            val parameterAdapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                types
            )
            parameterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            parameterValueSpinner.adapter = parameterAdapter

            materialTypesViews[materialName] = parameterValueSpinner
            materialParametersContainer.addView(parameterLayout)
        }

        unitMeasurementTextView.text = materialUnits[materialName]
    }
}
