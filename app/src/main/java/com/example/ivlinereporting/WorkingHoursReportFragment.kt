package com.example.ivlinereporting

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.renderscript.ScriptGroup.Input
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WorkingHoursReportFragment : Fragment() {
    private lateinit var workersViews: MutableMap<String, EditText>
    private lateinit var workers: List<String>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var objectUtils: ObjectUtils

    lateinit var titleLinearLayout: LinearLayout
    lateinit var workersContainer: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_working_hours_report, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleLinearLayout = requireView().findViewById(R.id.titleLinearLayout)
        workersViews = mutableMapOf()
        workersContainer = requireView().findViewById<LinearLayout>(R.id.workersContainer)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Пожалуйста, подождите...")
        progressDialog.setCancelable(false)

        val addItemsButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addWorker() }
        val sendDataButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendWorkingHoursReport() }

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
                val workers = getWorkersNamesFromDB(brigadeType)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    this@WorkingHoursReportFragment.workers = workers
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

    private suspend fun getWorkersNamesFromDB(brigadeType: String): List<String> {
        return withContext(Dispatchers.IO) {
            val dbConnection = DatabaseConnection()
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = dbConnection.createConnection()

            val workers = mutableListOf<String>()
            val userBrigadeWorkers = mutableListOf<String>()
            val otherBrigadeWorkers = mutableListOf<String>()
            val noBrigadeWorkers = mutableListOf<String>()

            val userCode =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .getString("code", null) ?: ""

            val workersStatement =
                connection.prepareStatement("SELECT с.фамилия, с.имя, с.отчество, б.код, б.код_бригадира FROM сотрудники с LEFT JOIN бригады_сотрудники бс ON с.код = бс.код_сотрудника LEFT JOIN бригады б ON бс.код_бригады = б.код ORDER BY б.код, с.фамилия, с.имя, с.отчество")
            val workersResultSet = workersStatement.executeQuery()
            while (workersResultSet.next()) {
                val workerName =
                    "${workersResultSet.getString("фамилия")} ${workersResultSet.getString("имя")} ${
                        workersResultSet.getString("отчество") ?: ""
                    }"
                val workerBrigade = workersResultSet.getString("код")
                val brigadeLeaderCode = workersResultSet.getString("код_бригадира")

                if (brigadeLeaderCode == userCode) {
                    userBrigadeWorkers.add(workerName)
                } else if (workerBrigade != null) {
                    otherBrigadeWorkers.add(workerName)
                } else {
                    noBrigadeWorkers.add(workerName)
                }
            }

            workers.addAll(userBrigadeWorkers)
            workers.addAll(otherBrigadeWorkers)
            workers.addAll(noBrigadeWorkers)

            dbConnection.closeConnection(connection)
            workers
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendWorkingHoursReport() {
        if (!validateForm()) {
            return
        }
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogYellow)
        dialog.setTitle("Отправка данных")
        dialog.setMessage("Вы уверены, что хотите отправить отчет об отработанных часах?")
        dialog.setPositiveButton("Подтвердить") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(requireContext(), "Данные отправлены успешно", Toast.LENGTH_SHORT).show()

            val averageHours = calculateAverageHours()
            if (averageHours > 12) {
                DialogUtils.showEncouragementDialog(
                    requireContext(),
                    "Вот это да!",
                    "Ваш упорный труд и преданность делу просто поразительны!"
                )
            } else if (averageHours > 8) {
                DialogUtils.showEncouragementDialog(
                    requireContext(),
                    "Поздравляем!",
                    "Вы отработали много часов! Отличная работа!"
                )
            } else {
                DialogUtils.showEncouragementDialog(
                    requireContext(),
                    "Спасибо!",
                    "Ваш труд не остался незамеченным!"
                )
            }
            sendXmlReportToDatabase()
            titleLinearLayout.visibility = View.INVISIBLE
            workersContainer.removeAllViews()
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
            statement.setString(1, "working_hours_report")
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

    private fun createSpreadsheetMLFile() : String {
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
        stringBuilder.append("  <Worksheet ss:Name=\"Отработанные часы\">\n")
        stringBuilder.append("    <Table>\n")

        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidth(date)}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidth(obj)}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"${calculateColumnWidthForWorkers()}\"/>\n")
        stringBuilder.append("      <Column ss:Width=\"50\"/>\n")

        stringBuilder.append("      <Row>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Дата</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Объект</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Сотрудник</Data></Cell>\n")
        stringBuilder.append("        <Cell><Data ss:Type=\"String\">Часы</Data></Cell>\n")
        stringBuilder.append("      </Row>\n")

        val workerCount = workersContainer.childCount
        for (i in 0 until workerCount) {
            val workerLayout = workersContainer.getChildAt(i) as LinearLayout
            val workerEditText = workerLayout.findViewById<EditText>(R.id.workerEditText)
            val hoursEditText = workerLayout.findViewById<EditText>(R.id.hoursEditText)

            stringBuilder.append("      <Row>\n")
            if (i == 0) {
                stringBuilder.append("        <Cell ss:MergeDown=\"${workerCount - 1}\"><Data ss:Type=\"String\">$date</Data></Cell>\n")
                stringBuilder.append("        <Cell ss:MergeDown=\"${workerCount - 1}\"><Data ss:Type=\"String\">$obj</Data></Cell>\n")
            }
            stringBuilder.append("        <Cell ss:Index=\"3\"><Data ss:Type=\"String\">${workerEditText.text}</Data></Cell>\n")
            stringBuilder.append("        <Cell><Data ss:Type=\"Number\">${hoursEditText.text}</Data></Cell>\n")
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

    private fun calculateColumnWidthForWorkers(): Int {
        var maxLength = 0
        for (i in 0 until workersContainer.childCount) {
            val workerLayout = workersContainer.getChildAt(i) as LinearLayout
            val workerEditText = workerLayout.findViewById<EditText>(R.id.workerEditText)
            val workerName = workerEditText.text.toString()
            if (workerName.length > maxLength) {
                maxLength = workerName.length
            }
        }
        val averageCharWidth = 8
        return maxLength * averageCharWidth
    }

    private fun calculateAverageHours(): Double {
        var totalHours = 0
        val workerCount = workersContainer.childCount
        for (i in 0 until workerCount) {
            val workerLayout = workersContainer.getChildAt(i) as LinearLayout
            val hoursEditText = workerLayout.findViewById<EditText>(R.id.hoursEditText)
            totalHours += hoursEditText.text.toString().toIntOrNull() ?: 0
        }
        return if (workerCount > 0) totalHours.toDouble() / workerCount else 0.0
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun validateForm(): Boolean {
        if (workersContainer.childCount == 0) {
            Toast.makeText(
                requireContext(),
                "Необходимо добавить хотя бы одного сотрудника",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        val workerNames = mutableSetOf<String>()
        for (i in 0 until workersContainer.childCount) {
            val workerLayout = workersContainer.getChildAt(i) as LinearLayout
            val workerEditText = workerLayout.findViewById<EditText>(R.id.workerEditText)
            val hoursEditText = workerLayout.findViewById<EditText>(R.id.hoursEditText)

            if (workerEditText.text.isEmpty() || hoursEditText.text.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Необходимо заполнить все поля",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            if (!workerNames.add(workerEditText.text.toString())) {
                Toast.makeText(
                    requireContext(),
                    "Сотрудники не должны повторяться",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            if (hoursEditText.text.toString().toInt() < 1 || hoursEditText.text.toString()
                    .toInt() > 24
            ) {
                Toast.makeText(
                    requireContext(),
                    "Количество часов может быть от 1 до 24",
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

    private fun addWorker() {
        if (titleLinearLayout.isInvisible) {
            titleLinearLayout.visibility = View.VISIBLE
        }
        val workerLayout = layoutInflater.inflate(R.layout.worker_layout, null)

        val deleteWorkerButton = workerLayout.findViewById<ImageView>(R.id.deleteWorkerButton)
        val workerEditText = workerLayout.findViewById<EditText>(R.id.workerEditText)

        deleteWorkerButton.setOnClickListener {
            (workerLayout.parent as ViewGroup).removeView(workerLayout)
            if (workersContainer.childCount == 0) {
                titleLinearLayout.visibility = View.INVISIBLE
            }
        }

        workerEditText.setOnClickListener{showWorkerDialog(workerEditText)}

        workersContainer.addView(workerLayout)
    }

    private fun showWorkerDialog(workerEditText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_worker, null)
        val searchWorkerEditText = dialogView.findViewById<EditText>(R.id.searchWorkerEditText)
        val workersRecyclerView = dialogView.findViewById<RecyclerView>(R.id.workersRecyclerView)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogWhite)
            .setView(dialogView).create()

        val adapter = WorkerAdapter(workers) { selectedWorker ->
            workerEditText.setText(selectedWorker)
            dialog.dismiss()
        }
        workersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        workersRecyclerView.adapter = adapter

        searchWorkerEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        dialog.show()
    }
}
