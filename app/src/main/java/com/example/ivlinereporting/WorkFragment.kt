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

class WorkFragment : Fragment(), OnAddItemClickListener, OnSendDataClickListener {
    lateinit var workContainer: LinearLayout
    private lateinit var workViews: MutableMap<String, EditText>
    private lateinit var workParametersViews: MutableMap<String, MutableMap<String, Spinner>>
    private lateinit var works: List<String>
    private lateinit var workParameters: Map<String, Map<String, List<String>>>

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

        val addItemsButton = requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addWork() }
        val sendDataButton = requireActivity().findViewById<FloatingActionButton>(R.id.sendDataButton)
        sendDataButton.setOnClickListener { sendWorkReport() }

        CoroutineScope(Dispatchers.IO).launch {
            val brigadeType = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("brigade_type", null) ?: ""
            val (workList, workParametersMap) = getWorksAndParametersFromDB(brigadeType)
            withContext(Dispatchers.Main) {
                works = workList
                workParameters = workParametersMap
            }
        }
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
        val workParametersContainer = workLayout.findViewById<LinearLayout>(R.id.parametersContainer)

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
            workContainer.removeAllViews()
        }
        dialog.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }
        dialog.show()
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
        val worksNames = mutableSetOf<String>()
        for (i in 0 until workContainer.childCount) {
            val workLayout = workContainer.getChildAt(i) as LinearLayout
            val workEditText = workLayout.findViewById<EditText>(R.id.workEditText)

            if (workEditText.text.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Необходимо указать вид работы",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            if (!worksNames.add(workEditText.text.toString())) {
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
