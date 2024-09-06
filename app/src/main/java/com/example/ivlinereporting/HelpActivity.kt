package com.example.ivlinereporting

import HelpAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HelpActivity : AppCompatActivity() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: HelpAdapter
    private lateinit var groupList: List<String>
    private lateinit var childList: HashMap<String, List<Any>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        expandableListView = findViewById(R.id.expandableListView)
        val btnBack = findViewById<Button>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        createGroupList()
        createChildList()

        adapter = HelpAdapter(this, groupList, childList)
        expandableListView.setAdapter(adapter)

        expandableListView.setOnGroupExpandListener { groupPosition ->
            for (i in 0 until adapter.groupCount) {
                if (i != groupPosition) {
                    expandableListView.collapseGroup(i)
                }
            }
        }

        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val selectedGroup = groupList[groupPosition]
            val selectedChild = childList[selectedGroup]!![childPosition]

            if (selectedChild is LayoutItem) {
                // Обновляем адаптер, чтобы показать layout
                adapter.notifyDataSetChanged()
            } else {
                when (selectedGroup) {
                    "О приложении" -> {
                        val layout = layoutInflater.inflate(R.layout.layout_about_app, null)
                        showLayout(layout, groupPosition, childPosition)
                    }
                    "Обратная связь" -> {
                        val layout = layoutInflater.inflate(R.layout.layout_feedback, null)
                        val tvFeedback = layout.findViewById<TextView>(R.id.tv_feedback)
                        val helpText = "Если у вас возникли проблемы при использовании приложения, пожалуйста, обратитесь по адресу "
                        val emailAddress = "dev.assist@.yandex.ru"
                        val fullText = "$helpText$emailAddress"

                        val spannableString = SpannableString(fullText)
                        val clickableSpan = object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                                    putExtra(Intent.EXTRA_SUBJECT, "Помощь по приложению")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Здравствуйте, \n\nУ меня возникли проблемы с использованием приложения..."
                                    )
                                }
                                if (intent.resolveActivity(packageManager) != null) {
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        this@HelpActivity,
                                        "Нет приложения для отправки почты",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                super.updateDrawState(ds)
                                ds.isUnderlineText = false
                                ds.color = ContextCompat.getColor(this@HelpActivity, android.R.color.holo_blue_light)
                            }
                        }

                        spannableString.setSpan(
                            clickableSpan,
                            helpText.length,
                            fullText.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        tvFeedback.text = spannableString
                        tvFeedback.movementMethod = LinkMovementMethod.getInstance()
                        showLayout(layout, groupPosition, childPosition)
                    }
                    "Главная страница" -> {
                        val layout = layoutInflater.inflate(R.layout.layout_main_page, null)
                        showLayout(layout, groupPosition, childPosition)
                    }
                    "Ввод данных" -> {
                        when (selectedChild) {
                            "Как ввести данные о выполненной работе" -> {
                                val layout = layoutInflater.inflate(R.layout.layout_data_entry_work, null)
                                showLayout(layout, groupPosition, childPosition)
                            }
                            "Как ввести данные об отработанных часах" -> {
                                val layout = layoutInflater.inflate(R.layout.layout_data_entry_hours, null)
                                showLayout(layout, groupPosition, childPosition)
                            }
                            "Как ввести данные об использованной технике" -> {
                                val layout = layoutInflater.inflate(R.layout.layout_data_entry_equipment, null)
                                showLayout(layout, groupPosition, childPosition)
                            }
                        }
                    }
                    "Отправка данных" -> {
                        val layout = layoutInflater.inflate(R.layout.layout_data_send, null)
                        showLayout(layout, groupPosition, childPosition)
                    }
                }
            }
            true
        }
    }

    private fun createGroupList() {
        groupList = listOf(
            "О приложении",
            "Обратная связь",
            "Главная страница",
            "Ввод данных",
            "Отправка данных"
        )
    }

    private fun createChildList() {
        childList = HashMap()

        childList["О приложении"] = listOf("Это приложение помогает вам...")
        childList["Обратная связь"] = listOf("Если у вас возникли проблемы при использовании приложения...")
        childList["Главная страница"] = listOf(
            "Как выбрать раздел",
            LayoutItem(R.layout.layout_main_page),
            "Описание меню",
            LayoutItem(R.layout.layout_main_page)
        )
        childList["Ввод данных"] = listOf(
            "Как ввести данные о выполненной работе",
            LayoutItem(R.layout.layout_data_entry_work),
            "Как ввести данные об отработанных часах",
            LayoutItem(R.layout.layout_data_entry_hours),
            "Как ввести данные об использованной технике",
            LayoutItem(R.layout.layout_data_entry_equipment)
        )
        childList["Отправка данных"] = listOf("Как отправить данные")
    }

    private fun showLayout(layout: View, groupPosition: Int, childPosition: Int) {
        val container = findViewById<ViewGroup>(R.id.container)
        container.removeAllViews()
        container.addView(layout)
        container.visibility = View.VISIBLE
    }
}