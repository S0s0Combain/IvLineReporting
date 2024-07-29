package com.example.ivlinereporting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MaterialAdapter(
    private val materials: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {
    private var filteredMaterials = materials

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredMaterials = materials.filter { it.contains(query, ignoreCase = true) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return MaterialViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(filteredMaterials[position])
    }

    override fun getItemCount(): Int = filteredMaterials.size

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(materials: String) {
            textView.text = materials
            itemView.setOnClickListener { onItemClick(materials) }
        }
    }

}