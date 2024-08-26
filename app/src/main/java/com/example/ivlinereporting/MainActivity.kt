package com.example.ivlinereporting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Scroller
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var menuHandler: MenuHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navView)
        menuHandler = MenuHandler(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)

        navigationView.setNavigationItemSelectedListener { item ->
            menuHandler.onNavigationItemSelected(
                item
            )
        }

        val user = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("login", null)
        val loginTextView = navigationView.getHeaderView(0).findViewById<TextView>(R.id.loginTextView)
        loginTextView.text = user ?: "Неизвестный пользователь"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuHandler.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    fun onSelectButton_onClick(v: View) {
        val intent = Intent(this, InputDataActivity::class.java)
        val fragmentType = when (v.id) {
            R.id.selectWorkReportButton -> "workReport"
            R.id.selectWorkingHoursReportButton -> "workingHoursReport"
            R.id.selectTechniqueReportButton -> "techniqueReport"
            else -> throw IllegalArgumentException("Неизвестный id элемента")
        }
        intent.putExtra("fragment_type", fragmentType)
        startActivity(intent)
    }
}