package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText

class DatePickerFragment : DialogFragment() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val contextThemeWrapper = ContextThemeWrapper(requireActivity(), R.style.DatePickerDialogTheme)

        return DatePickerDialog(contextThemeWrapper, dateSetListener, year, month, day)
    }

    @SuppressLint("DefaultLocale")
    private val dateSetListener = DatePickerDialog.OnDateSetListener{ _, year, month, day ->
        val selectedDate = String.format("%02d.%02d.%04d", day, month, year)
        (activity as DatePickerDialogListener).onDateSelected(selectedDate)
    }

    interface DatePickerDialogListener{
        fun onDateSelected(date: String)
    }
}