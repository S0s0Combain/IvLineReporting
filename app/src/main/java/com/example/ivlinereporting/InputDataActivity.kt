package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Scroller
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText


class InputDataActivity : AppCompatActivity(), DatePickerFragment.DatePickerDialogListener {
    lateinit var dateEditText: TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_input_data)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val fragmentType = intent.getStringExtra("fragment_type")
        val fragment = when(fragmentType){
            "workReport" -> WorkReportFragment()
            "workingHoursReport" -> WorkingHoursReportFragment()
            "techniqueReport" -> TechniqueReportFragment()
            else -> throw IllegalArgumentException("Неизвестный тип фрагмента")
        }
        showFragment(fragment)

        dateEditText = findViewById(R.id.dateEditText)
    }

    @SuppressLint("CommitTransaction")
    fun showFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }

    fun showDatePickerDialog(v: View){
        val datePickerFragment = DatePickerFragment()
        datePickerFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onDateSelected(date: String){
        dateEditText.setText(date)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null){
            val v: View? = currentFocus
            if(v is EditText){
                val scroller = Scroller(this)
                val scrollBounds = Rect()
                v.getHitRect(scrollBounds)
                scrollBounds.offset(-scroller.currX, -scroller.currY)
                if(!scrollBounds.contains(ev.x.toInt(), ev.y.toInt())){
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}