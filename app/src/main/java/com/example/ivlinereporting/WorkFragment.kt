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
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.sql.Connection
import java.sql.Date
import java.sql.Statement
import java.sql.Timestamp
import java.text.Format
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WorkFragment : Fragment(), OnAddItemClickListener, OnSendDataClickListener {
    lateinit var workContainer: LinearLayout
    private lateinit var workViews: MutableMap<String, EditText>
    private lateinit var workParametersViews: MutableMap<String, MutableMap<String, Spinner>>
    private lateinit var works: List<String>
    private lateinit var workParameters: Map<String, Map<String, List<String>>>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var objectUtils: ObjectUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        workContainer = requireView().findViewById(R.id.workContainer)
        workViews = mutableMapOf()
        workParametersViews = mutableMapOf()
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Пожалуйста, подождите...")
        progressDialog.setCancelable(false)

        val addItemsButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addWork() }
        val sendDataButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendWorkReport() }

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Нет доступа к интернету", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        progressDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val brigadeType =
                    requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        .getString("brigade_type", null) ?: ""
                val (workList, workParametersMap) = getWorksAndParametersFromDB(brigadeType)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    works = workList
                    workParameters = workParametersMap
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Ошибка сети", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            }
        }
        objectUtils = ObjectUtils(requireContext())
    }

    override fun onAddItemClick() {
        addWork()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSendDataClick() {
        sendWorkReport()
    }

    private fun addWork() {
        val workLayout = layoutInflater.inflate(R.layout.work_item, workContainer, false)

        val deleteWorkButton = workLayout.findViewById<ImageView>(R.id.deleteWorkButton)
        val workEditText = workLayout.findViewById<EditText>(R.id.workEditText)
        val workParametersContainer =
            workLayout.findViewById<LinearLayout>(R.id.parametersContainer)

        deleteWorkButton.setOnClickListener {
            (workLayout.parent as ViewGroup).removeView(workLayout)
        }

        workEditText.setOnClickListener { showWorkDialog(workEditText, workParametersContainer) }

        workContainer.addView(workLayout)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendWorkReport() {
        if (!validateForm()) {
            return
        }
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow)
        dialog.setTitle("Отправка данных")
        dialog.setMessage("Вы уверены, что хотите отправить отчет о выполненной работе?")
        dialog.setPositiveButton("Подтвердить") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(requireContext(), "Данные отправлены успешно", Toast.LENGTH_SHORT).show()
            val totalWorks = calculateTotalWorks()
            if (totalWorks > 10) {
                DialogUtils.showEncouragementDialog(
                    requireContext(),
                    "Отлично!",
                    "Ваша продуктивность и упорство просто поразительны!"
                )
            } else if (totalWorks > 5) {
                DialogUtils.showEncouragementDialog(
                    requireContext(),
                    "Поздравляем!",
                    "Вы выполнили много задач! Отличная работа!"
                )
            } else {
                DialogUtils.showEncouragementDialog(
                    requireContext(),
                    "Спасибо!",
                    "Ваш вклад в работу очень ценен!"
                )
            }
            sendXmlReportToDatabase()
            workContainer.removeAllViews()
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
            statement.setString(1, "work_report")
            statement.setDate(2, Date.valueOf(LocalDate.now().toString()))
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

    private fun createSpreadsheetMLFile(): String {
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
        stringBuilder.append("  <Worksheet ss:Name=\"Работа\">\n")
        stringBuilder.append("    <Table>\n")

        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidth(date)}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidth(obj)}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidthForWorks()}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidthForTypes()}\"/>\n")

        stringBuilder.append("      <Row>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Дата</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Объект</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Вид работы</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Тип</Data></Cell>\n")
        stringBuilder.append("      </Row>\n")

        val workCount = workContainer.childCount
        for (i in 0 until workCount) {
            val workLayout = workContainer.getChildAt(i) as LinearLayout
            val workEditText = workLayout.findViewById<EditText>(R.id.workEditText)
            val workParametersContainer =
                workLayout.findViewById<LinearLayout>(R.id.parametersContainer)

            stringBuilder.append("      <Row>\n")
            if (i == 0) {
                stringBuilder.append("        <Cell ss:MergeDown=\"${workCount - 1}\"><Data ss:Type=\"String\">$date</Data></Cell>\n")
                stringBuilder.append("        <Cell ss:MergeDown=\"${workCount - 1}\"><Data ss:Type=\"String\">$obj</Data></Cell>\n")
            }
            stringBuilder.append("        <Cell ss:Index=\"3\"><Data ss:Type=\"String\">${workEditText.text}</Data></Cell>\n")

            val parameterSpinner =
                workParametersContainer.findViewById<Spinner>(R.id.parameterValueSpinner)
            if (parameterSpinner != null) {
                val parameterValue = parameterSpinner.selectedItem
                stringBuilder.append("        <Cell><Data ss:Type=\"String\">$parameterValue</Data></Cell>\n")
            } else {
                stringBuilder.append("        <Cell><Data ss:Type=\"String\">-</Data></Cell>\n")
            }

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

    private fun calculateColumnWidthForWorks(): Int {
        var maxLength = 0
        for (i in 0 until workContainer.childCount) {
            val workLayout = workContainer.getChildAt(i) as LinearLayout
            val workEditText = workLayout.findViewById<EditText>(R.id.workEditText)
            val workName = workEditText.text.toString()
            if (workName.length > maxLength) {
                maxLength = workName.length
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
    }

    private fun calculateColumnWidthForTypes(): Int {
        var maxLength = 0
        for (i in 0 until workContainer.childCount) {
            val workLayout = workContainer.getChildAt(i) as LinearLayout
            val workEditText = workLayout.findViewById<EditText>(R.id.workEditText)
            val parameters = workParameters[workEditText.text.toString()] ?: mapOf()
            val parameterValues = parameters.values.flatten().joinToString(", ")
            if (parameterValues.length > maxLength) {
                maxLength = parameterValues.length
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
    }

    fun calculateTotalWorks(): Int {
        return workContainer.childCount
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun validateForm(): Boolean {
        if (workContainer.childCount == 0) {
            Toast.makeText(
                requireContext(),
                "Необходимо указать хотя бы один вид работы",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        val worksItems = mutableSetOf<WorkItem>()
        for (i in 0 until workContainer.childCount) {
            val workLayout = workContainer.getChildAt(i) as LinearLayout
            val workEditText = workLayout.findViewById<EditText>(R.id.workEditText)
            val workParametersContainer =
                workLayout.findViewById<LinearLayout>(R.id.parametersContainer)

            if (workEditText.text.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Необходимо указать вид работы",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            val workName = workEditText.text.toString()
            val parameterSpinner =
                workParametersContainer.findViewById<Spinner>(R.id.parameterValueSpinner)
            val parameterValue = parameterSpinner?.selectedItem?.toString() ?: ""

            val workItem = WorkItem(workName, parameterValue)

            if (!worksItems.add(workItem)) {
                Toast.makeText(
                    requireContext(),
                    "Виды работ не должны повторяться",
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

    private fun showWorkDialog(workEditText: EditText, workParametersContainer: LinearLayout) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_work, null)
        val searchWorksEditText = dialogView.findViewById<EditText>(R.id.searchWorksEditText)
        val worksRecyclerView = dialogView.findViewById<RecyclerView>(R.id.worksRecyclerView)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogWhite)
            .setView(dialogView)
            .create()

        val adapter = WorkAdapter(works) { selectedWork ->
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
        val parameters = workParameters[workName] ?: mapOf()
        val parameterViews = workParametersViews[workName]

        for ((parameter, values) in parameters) {
            val parameterLayout = layoutInflater.inflate(R.layout.parameter_item, null)
            val parameterTextView =
                parameterLayout.findViewById<TextView>(R.id.parameterNameTextView)
            val parameterValueSpinner =
                parameterLayout.findViewById<Spinner>(R.id.parameterValueSpinner)

            parameterTextView.text = parameter

            val parameterAdapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                values
            )
            parameterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            parameterValueSpinner.adapter = parameterAdapter

            parameterViews?.put(parameter, parameterValueSpinner)
            workParametersContainer.addView(parameterLayout)
        }
    }

    private suspend fun getWorksAndParametersFromDB(brigadeType: String): Pair<List<String>, Map<String, Map<String, List<String>>>> {
        return withContext(Dispatchers.IO) {
            val dbConnection = DatabaseConnection()
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = dbConnection.createConnection()

            val works = mutableListOf<String>()
            val workParameters = mutableMapOf<String, Map<String, List<String>>>()

            val worksStatement = connection.prepareStatement(
                "SELECT наименование FROM виды_работ WHERE вид_бригады = ?"
            )
            worksStatement.setString(1, brigadeType)
            val workResultSet = worksStatement.executeQuery()
            while (workResultSet.next()) {
                val workName = workResultSet.getString("наименование")
                works.add(workName)

                val parametersStatement = connection.prepareStatement(
                    "SELECT наименование_параметра, значение_параметра FROM параметры_работ WHERE код_вида_работы = (SELECT код FROM виды_работ WHERE наименование = ?)"
                )
                parametersStatement.setString(1, workName)
                val parametersResultSet = parametersStatement.executeQuery()
                val parameters = mutableMapOf<String, MutableList<String>>()
                while (parametersResultSet.next()) {
                    val parameterName = parametersResultSet.getString("наименование_параметра")
                    val parameterValue = parametersResultSet.getString("значение_параметра")
                    parameters.putIfAbsent(parameterName, mutableListOf())
                    parameters[parameterName]?.add(parameterValue)
                }
                workParameters[workName] = parameters
            }

            dbConnection.closeConnection(connection)
            Pair(works, workParameters)
        }
    }
}

data class WorkItem(val name: String, val type: String)
