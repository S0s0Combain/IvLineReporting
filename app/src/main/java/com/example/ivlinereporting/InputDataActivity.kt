package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class InputDataActivity : AppCompatActivity() {
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
    }

    @SuppressLint("CommitTransaction")
    fun showFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}