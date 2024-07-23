package com.example.ivlinereporting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.isInvisible
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TechniqueReportFragment : Fragment() {
    private lateinit var techniqueViews: MutableMap<String, EditText>
    private var savedTechniqueViews: MutableMap<String, EditText>?=null
    private lateinit var titleLinearLayout: LinearLayout
    private lateinit var techniqueContainer: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_technique_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleLinearLayout = requireView().findViewById(R.id.titleLinearLayout)
        techniqueViews = mutableMapOf()

        val addItemsButton =
            requireActivity().findViewById<FloatingActionButton>(R.id.addItemsButton)
        addItemsButton.setOnClickListener { addTechnique() }
    }

    private fun addTechnique() {
        if (titleLinearLayout.isInvisible) {
            titleLinearLayout.visibility = View.VISIBLE
        }
        val techniqueLayout = layoutInflater.inflate(R.layout.technique_layout, null)

        val deleteTechniqueButton =
            techniqueLayout.findViewById<ImageView>(R.id.deleteTechniqueButton)
        val techniqueSpinner = techniqueLayout.findViewById<Spinner>(R.id.techniqueSpinner)
        val hoursEditText = techniqueLayout.findViewById<EditText>(R.id.hoursEditText)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            getTechniqueNames()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        techniqueSpinner.adapter = adapter

        techniqueViews[techniqueSpinner.selectedItem.toString()] = hoursEditText

        deleteTechniqueButton.setOnClickListener {
            (techniqueLayout.parent as ViewGroup).removeView(techniqueLayout)
            if (techniqueContainer.childCount == 0) {
                titleLinearLayout.visibility = View.INVISIBLE
            }
        }

        techniqueContainer = requireView().findViewById<LinearLayout>(R.id.techniqueContainer)
        techniqueContainer.addView(techniqueLayout)

        savedTechniqueViews?.put(techniqueSpinner.selectedItem.toString(), hoursEditText)
    }

    private fun getTechniqueNames(): List<String> {
        return listOf(
            "самосвал самопал"
        )
    }
}