package com.example.ivlinereporting

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class HelpActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var menuHandler: MenuHandler
    private lateinit var expandableListView: ExpandableListView
    private lateinit var helpAdapter: HelpAdapter
    private var currentSubSectionView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)
        window.statusBarColor = ContextCompat.getColor(this, R.color.yellow)
        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle("Помощь")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
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

        expandableListView = findViewById(R.id.expandableListView)

        val sections = listOf(
            Section("О приложении", listOf(Item.LayoutItem(R.layout.about_app_layout))),
            Section("Обратная связь", listOf(Item.LayoutItem(R.layout.feedback_layout))),
            Section("Меню приложения", listOf(Item.LayoutItem(R.layout.help_menu_layout))),
            Section("Главная страница", listOf(Item.LayoutItem(R.layout.help_main_layout))),
            Section(
                "Ввод данных",
                listOf(
                    Item.SubSection(
                        "Как ввести данные о выполненной работе",
                        listOf(Item.LayoutItem(R.layout.input_work_help))
                    ),
                    Item.SubSection(
                        "Как ввести данные об отработанных часах",
                        listOf(Item.LayoutItem(R.layout.input_working_hours_help))
                    ),
                    Item.SubSection(
                        "Как ввести данные о задействованной технике",
                        listOf(Item.LayoutItem(R.layout.input_technique_help))
                    )
                )
            )
        )

        helpAdapter = HelpAdapter(this, sections)
        expandableListView.setAdapter(helpAdapter)
        expandableListView.setOnGroupExpandListener { groupPosition ->
            for (i in 0 until helpAdapter.groupCount) {
                if (i != groupPosition) {
                    expandableListView.collapseGroup(i)
                }
            }
        }

        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val item = helpAdapter.getChild(groupPosition, childPosition)
            if (item is Item.SubSection) {
                if (currentSubSectionView != null && currentSubSectionView == v) {
                    hideSubSectionLayout(v)
                } else {
                    currentSubSectionView?.let { hideSubSectionLayout(it) }
                    showSubSectionLayout(v, item)
                    currentSubSectionView = v
                }
            }
            true
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuHandler.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    private fun showSubSectionLayout(view: View, subSection: Item.SubSection) {
        Log.d("MyLog", "showSubSectionLayout called")
        val layoutId = subSection.subItems[0].layoutId
        val frameLayout = view.findViewById<FrameLayout>(R.id.sub_section_content)
        frameLayout.removeAllViews()
        val inflatedView = layoutInflater.inflate(layoutId, frameLayout, false)
        frameLayout.addView(inflatedView)
        frameLayout.visibility = View.VISIBLE
    }

    private fun hideSubSectionLayout(view: View) {
        val frameLayout = view.findViewById<FrameLayout>(R.id.sub_section_content)
        frameLayout.removeAllViews()
        frameLayout.visibility = View.GONE
    }
}
