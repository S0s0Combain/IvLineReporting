package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.graphics.Color
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
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText


class InputDataActivity : AppCompatActivity(), DatePickerFragment.DatePickerDialogListener {
    lateinit var dateEditText: TextInputEditText
    lateinit var addItemsButton: FloatingActionButton
    lateinit var  sendDataButton: FloatingActionButton
    lateinit var toolbarTitle: String
    lateinit var menuHandler: MenuHandler
    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_input_data)
        addItemsButton = findViewById(R.id.addItemsButton)
        sendDataButton = findViewById(R.id.sendDataButton)
        addItemsButton.drawable.setTint(Color.BLACK)
        sendDataButton.drawable.setTint(Color.BLACK)
        setAddItemClickListener(null)
        setSendDataClickListener(null)
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)
        val fragmentType = intent.getStringExtra("fragment_type")
        val fragment = when (fragmentType) {
            "workReport" -> WorkReportFragment()
            "workingHoursReport" -> WorkingHoursReportFragment()
            "techniqueReport" -> TechniqueReportFragment()
            else -> throw IllegalArgumentException("Неизвестный тип фрагмента")
        }
        toolbarTitle = when (fragmentType) {
            "workReport" -> "Отчет о выполненной работе"
            "workingHoursReport" -> "Отчет об отработанных часах"
            "techniqueReport" -> "Отчет о технике"
            else -> ""
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle(toolbarTitle)
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

        showFragment(fragment)

        dateEditText = findViewById(R.id.dateEditText)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuHandler.onOptionsItemSelected(item) || return super.onOptionsItemSelected(item)
    }

    fun setAddItemClickListener(listener: OnAddItemClickListener?) {
        addItemsButton.setOnClickListener {
            listener?.onAddItemClick()
        }
    }

    fun setSendDataClickListener(listener: OnSendDataClickListener?) {
        sendDataButton.setOnClickListener{
            listener?.onSendDataClick()
        }
    }

    @SuppressLint("CommitTransaction")
    fun showFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }

    fun showDatePickerDialog(v: View) {
        val datePickerFragment = DatePickerFragment()
        datePickerFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onDateSelected(date: String) {
        dateEditText.setText(date)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            val v: View? = currentFocus
            if (v is EditText) {
                val scroller = Scroller(this)
                val scrollBounds = Rect()
                v.getHitRect(scrollBounds)
                scrollBounds.offset(-scroller.currX, -scroller.currY)
                if (!scrollBounds.contains(ev.x.toInt(), ev.y.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun backButton_onClick(v: View){
        onBackPressed()
    }
}