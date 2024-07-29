package com.example.ivlinereporting

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TechniqueReportFragment : Fragment() {
    private lateinit var techniqueViews: MutableMap<String, EditText>
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

        val techniqueEditText = techniqueLayout.findViewById<EditText>(R.id.techniqueEditText)

        techniqueEditText.setOnClickListener { showTechniqueDialog(techniqueEditText) }

        deleteTechniqueButton.setOnClickListener {
            (techniqueLayout.parent as ViewGroup).removeView(techniqueLayout)
            if (techniqueContainer.childCount == 0) {
                titleLinearLayout.visibility = View.INVISIBLE
            }
        }

        techniqueContainer = requireView().findViewById<LinearLayout>(R.id.techniqueContainer)
        techniqueContainer.addView(techniqueLayout)
    }

    private fun showTechniqueDialog(techniqueEditText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search_technique, null)
        val searchTechniqueEditText = dialogView.findViewById<EditText>(R.id.searchTechniqueEditText)
        val techniqueRecyclerView = dialogView.findViewById<RecyclerView>(R.id.techniqueRecyclerView)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val adapter = TechniqueAdapter(getTechniqueNames()) { selectedTechnique ->
            techniqueEditText.setText(selectedTechnique)
            dialog.dismiss()
        }
        techniqueRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        techniqueRecyclerView.adapter = adapter

        searchTechniqueEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        dialog.show()
        adjustDialogSize(dialog, adapter.itemCount)
    }

    fun adjustDialogSize(dialog: AlertDialog, itemCount: Int){
        val window = dialog.window
        val layoutParams = window?.attributes
        val maxHeight = (resources.displayMetrics.heightPixels*0.8).toInt()
        val itemHeight = 50
        val desiredHeight = itemHeight * itemCount+100

        layoutParams?.height= if(desiredHeight>maxHeight) maxHeight else desiredHeight
        window?.attributes = layoutParams
    }

    private fun getTechniqueNames(): List<String> {
        return listOf(
            "самосвал",
            "илососная машина",
            "Техника"
        )
    }
}