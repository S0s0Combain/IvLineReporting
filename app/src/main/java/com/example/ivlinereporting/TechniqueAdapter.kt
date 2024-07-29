package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TechniqueAdapter(
    private val technique: List<String>,
    private val onItemClick: (String) -> Unit
) :
    RecyclerView.Adapter<TechniqueAdapter.TechniqueViewHolder>() {
    private var filteredTechnique = technique

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredTechnique = technique.filter { it.contains(query, ignoreCase = true) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechniqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return TechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechniqueViewHolder, position: Int) {
        holder.bind(filteredTechnique[position])
    }

    override fun getItemCount(): Int = filteredTechnique.size

    inner class TechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(technique: String) {
            textView.text = technique
            itemView.setOnClickListener { onItemClick(technique) }
        }
    }
}