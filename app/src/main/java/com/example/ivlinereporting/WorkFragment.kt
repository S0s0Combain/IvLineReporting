package com.example.ivlinereporting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.view.isInvisible
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WorkFragment : Fragment(), OnAddItemClickListener {
    lateinit var titleLinearLayout: LinearLayout
    lateinit var workContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleLinearLayout = requireView().findViewById(R.id.titleLinearLayout)
    }

    override fun onAddItemClick() {
        addWork()
    }

    private fun addWork() {
        if (titleLinearLayout.isInvisible) {
            titleLinearLayout.visibility = View.VISIBLE
        }

        val workLayout = layoutInflater.inflate(R.layout.work_item, null)

        val deleteWorkButton = workLayout.findViewById<ImageView>(R.id.deleteWorkButton)
        val workSpinner = workLayout.findViewById<Spinner>(R.id.workSpinner)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            getWorks()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        workSpinner.adapter = adapter

        deleteWorkButton.setOnClickListener {
            (workLayout.parent as ViewGroup).removeView(workLayout)
            if (workContainer.childCount == 0) {
                titleLinearLayout.visibility = View.INVISIBLE
            }
        }

        workContainer = requireView().findViewById(R.id.workContainer)
        workContainer.addView(workLayout)
    }

    private fun getWorks(): List<String> {
        return listOf("Работа 1", "Работа 2", "Работа 3")
    }
}