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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
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
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TechniqueReportFragment : Fragment() {
    private lateinit var techniqueViews: MutableMap<String, EditText>
    private lateinit var techniqueContainer: LinearLayout
    private lateinit var rentedTechniques: List<String>
    private lateinit var nonRentedTechniques: List<String>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var objectUtils: ObjectUtils
    private lateinit var titleLinearLayout: LinearLayout

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
        titleLinearLayout = requireView().findViewById(R.id.titleLinearLayout)
        techniqueContainer = requireView().findViewById(R.id.techniqueContainer)
        techniqueViews = mutableMapOf()
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Пожалуйста, подождите...")
        progressDialog.setCancelable(false)

        val addItemsButton = requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addTechnique() }

        val sendDataButton = requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendTechniqueReport() }

        if(!NetworkUtils.isNetworkAvailable(requireContext())){
            Toast.makeText(requireContext(), "Нет доступа к интернету", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        progressDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (rentedTechniques, nonRentedTechniques) = getTechniqueNamesFromDB()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    this@TechniqueReportFragment.rentedTechniques = rentedTechniques
                    this@TechniqueReportFragment.nonRentedTechniques = nonRentedTechniques
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Ошибка сети", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            }
        }
        objectUtils = ObjectUtils(requireContext())
    }

    private fun addTechnique() {
        if (titleLinearLayout.isInvisible) {
            titleLinearLayout.visibility = View.VISIBLE
        }
        val techniqueLayout = layoutInflater.inflate(R.layout.technique_layout, null)

        val deleteTechniqueButton = techniqueLayout.findViewById<ImageView>(R.id.deleteTechniqueButton)
        val techniqueEditText = techniqueLayout.findViewById<EditText>(R.id.techniqueEditText)

        deleteTechniqueButton.setOnClickListener {
            (techniqueLayout.parent as ViewGroup).removeView(techniqueLayout)

            if (techniqueContainer.childCount == 0) {
                titleLinearLayout.visibility = View.INVISIBLE
            }
        }

        techniqueEditText.setOnClickListener{showTechniqueDialog(techniqueEditText)}

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
            dialog.dismiss()
            Toast.makeText(requireContext(), "Данные отправлены успешно", Toast.LENGTH_SHORT).show()
            DialogUtils.showEncouragementDialog(
                requireContext(),
                "Спасибо!",
                "Вы тщательно отследили использование техники! Отличная работа!"
            )
            sendXmlReportToDatabase()
            titleLinearLayout.visibility = View.INVISIBLE
            techniqueContainer.removeAllViews()
        }
        dialog.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun sendXmlReportToDatabase() {
        val xmlContent = createSpreadsheetMLFile()
        CoroutineScope(Dispatchers.IO).launch {
            val connection = DatabaseConnection().createConnection()
            val statement =
                connection.prepareStatement("INSERT INTO отчеты(тип_отчета, дата, код_пользователя, файл, формат_файла, создан_в) VALUES(?, ?, ?, ?, ?, ?)")
            statement.setString(1, "technic_report")
            val dateEditText = activity?.findViewById<EditText>(R.id.dateEditText)
            val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val parsedDate = inputFormat.parse(dateEditText?.text?.toString())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = outputFormat.format(parsedDate)
            val sqlDate = java.sql.Date.valueOf(formattedDate)
            statement.setDate(2, sqlDate)
            context?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                ?.getString("code", null)
                ?.let { statement.setInt(3, it.toInt()) }
            statement.setBytes(4, (xmlContent.toByteArray()))
            statement.setString(5, "xml")
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSSSSS]")
            val formattedDateTime = LocalDateTime.now().format(formatter)
            val timestamp = Timestamp.valueOf(formattedDateTime)
            statement.setTimestamp(6, timestamp)
            statement.executeUpdate()
            connection.close()
        }
    }

    private fun createSpreadsheetMLFile():String {
        val activity = requireActivity() as InputDataActivity
        val dateEditText = activity.findViewById<EditText>(R.id.dateEditText)
        val objectEditText = activity.findViewById<EditText>(R.id.objectEditText)
        val date = dateEditText.text.toString()
        val obj = objectEditText.text.toString()

        objectUtils.saveObjectIfNotExists(objectEditText)

        val stringBuilder = StringBuilder()
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        stringBuilder.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        stringBuilder.append("          xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
        stringBuilder.append("          xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
        stringBuilder.append("          xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        stringBuilder.append("          xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")
        stringBuilder.append("  <Worksheet ss:Name=\"Техника\">\n")
        stringBuilder.append("    <Table>\n")

        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidth(date)}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidth(obj)}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidthForTechniques()}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidthForWorkTypes()}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"50\"/>\n")

        stringBuilder.append("      <Row>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Дата</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Объект</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Техника</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Тип работы</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Количество</Data></Cell>\n")
        stringBuilder.append("      </Row>\n")

        val techniqueCount = techniqueContainer.childCount
        for (i in 0 until techniqueCount) {
            val techniqueLayout = techniqueContainer.getChildAt(i) as LinearLayout
            val techniqueEditText = techniqueLayout.findViewById<EditText>(R.id.techniqueEditText)
            val timeTypeSpinner = techniqueLayout.findViewById<Spinner>(R.id.timeType)
            val quantityEditText = techniqueLayout.findViewById<EditText>(R.id.quantityEditText)

            stringBuilder.append("      <Row>\n")
            if (i == 0) {
                stringBuilder.append("        <Cell ss:MergeDown=\"${techniqueCount - 1}\"><Data ss:Type=\"String\">$date</Data></Cell>\n")
                stringBuilder.append("        <Cell ss:MergeDown=\"${techniqueCount - 1}\"><Data ss:Type=\"String\">$obj</Data></Cell>\n")
            }
            stringBuilder.append("        <Cell ss:Index=\"3\"><Data ss:Type=\"String\">${techniqueEditText.text}</Data></Cell>\n")
            stringBuilder.append("        <Cell><Data ss:Type=\"String\">${timeTypeSpinner.selectedItem}</Data></Cell>\n")
            stringBuilder.append("        <Cell><Data ss:Type=\"Number\">${quantityEditText.text}</Data></Cell>\n")
            stringBuilder.append("      </Row>\n")
        }

        stringBuilder.append("    </Table>\n")
        stringBuilder.append("  </Worksheet>\n")
        stringBuilder.append("</Workbook>\n")

        return stringBuilder.toString()
    }

    private fun calculateColumnWidth(text: String): Int {
        val averageCharWidth = 8
        return text.length * averageCharWidth
    }

    private fun calculateColumnWidthForTechniques(): Int {
        var maxLength = 0
        for (i in 0 until techniqueContainer.childCount) {
            val techniqueLayout = techniqueContainer.getChildAt(i) as LinearLayout
            val techniqueEditText = techniqueLayout.findViewById<EditText>(R.id.techniqueEditText)
            val techniqueName = techniqueEditText.text.toString()
            if (techniqueName.length > maxLength) {
                maxLength = techniqueName.length
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
    }

    private fun calculateColumnWidthForWorkTypes(): Int {
        var maxLength = 0
        for (i in 0 until techniqueContainer.childCount) {
            val techniqueLayout = techniqueContainer.getChildAt(i) as LinearLayout
            val timeTypeSpinner = techniqueLayout.findViewById<Spinner>(R.id.timeType)
            val workTypeName = timeTypeSpinner.selectedItem.toString()
            if (workTypeName.length > maxLength) {
                maxLength = workTypeName.length
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
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

            if (quantityEditText.text.toString().toInt() < 1) {
                Toast.makeText(requireContext(), "Количество часов / рейсов должно быть больше 1", Toast.LENGTH_SHORT).show()
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
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogRed).setTitle("Ошибка")
                .setMessage("Дата должна быть не более, чем на два дня раньше текущей")
                .setPositiveButton("Ок") { dialog, _ -> { dialog.dismiss() } }.show()
            return false
        }
        if (!selectedDate.before(oneDayAfter)) {
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogRed).setTitle("Ошибка")
                .setMessage("Дата должна быть не более, чем на один день позже текущей")
                .setPositiveButton("Ок") { dialog, _ -> { dialog.dismiss() } }.show()
            return false
        }
        return true
    }

    private fun showTechniqueDialog(techniqueEditText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_technique, null)
        val searchTechniqueEditText = dialogView.findViewById<EditText>(R.id.searchTechniqueEditText)
        val techniqueRecyclerView = dialogView.findViewById<RecyclerView>(R.id.techniqueRecyclerView)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogWhite)
            .setView(dialogView)
            .create()

        val allTechniques = rentedTechniques + nonRentedTechniques
        val adapter = TechniqueAdapter(allTechniques) { selectedTechnique ->
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

    private suspend fun getTechniqueNamesFromDB(): Pair<List<String>, List<String>> {
        return withContext(Dispatchers.IO) {
            val dbConnection = DatabaseConnection()
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = dbConnection.createConnection()

            val rentedTechniques = mutableListOf<String>()
            val nonRentedTechniques = mutableListOf<String>()

            val techniquesStatement = connection.prepareStatement(
                "SELECT название, наемная FROM техника"
            )
            val techniquesResultSet = techniquesStatement.executeQuery()
            while (techniquesResultSet.next()) {
                val techniqueName = techniquesResultSet.getString("название")
                val isRented = techniquesResultSet.getBoolean("наемная")
                if (isRented) {
                    rentedTechniques.add("$techniqueName (найм)")
                } else {
                    nonRentedTechniques.add(techniqueName)
                }
            }

            dbConnection.closeConnection(connection)
            Pair(rentedTechniques, nonRentedTechniques)
        }
    }
}
