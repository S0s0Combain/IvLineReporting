package com.example.ivlinereporting

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    lateinit var loginEditText: TextInputEditText
    lateinit var passwordEditText: TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_IvLineReporting)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        loginEditText = findViewById(R.id.loginEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun enterButton_onClick(v:View){
        if(loginEditText.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Введите логин", Toast.LENGTH_SHORT).show()
            return
        }
        if(passwordEditText.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Введите пароль", Toast.LENGTH_SHORT).show()
            return
        }
    }
}