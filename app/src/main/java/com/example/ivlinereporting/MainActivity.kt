package com.example.ivlinereporting

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Scroller
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun onSelectButton_onClick(v: View){
        val intent = Intent(this, InputDataActivity::class.java)
        val fragmentType = when(v.id){
            R.id.selectWorkReportButton -> "workReport"
            R.id.selectWorkingHoursReportButton -> "workingHoursReport"
            R.id.selectTechniqueReportButton -> "techniqueReport"
            else -> throw IllegalArgumentException("Неизвестный id элемента")
        }
        intent.putExtra("fragment_type", fragmentType)
        startActivity(intent)
    }
}