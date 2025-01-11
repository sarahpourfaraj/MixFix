package com.example.mixfix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter to bind levels to RecyclerView
class LevelAdapter(
    private val levels: List<Level>,
    private val onLevelClick: (Level) -> Unit
) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    // Create the view holder for each level item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level, parent, false)
        return LevelViewHolder(view)
    }

    // Bind data to the view holder
    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val level = levels[position]
        holder.bind(level, onLevelClick)
    }

    // Number of items in the list
    override fun getItemCount(): Int = levels.size

    // ViewHolder class for level items
    class LevelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLevelNumber: TextView = itemView.findViewById(R.id.tvLevelNumber)

        fun bind(level: Level, onLevelClick: (Level) -> Unit) {
            // Set the level number
            tvLevelNumber.text = "Level ${level.number}"
            // Dim the locked levels
            itemView.alpha = if (level.isLocked) 0.5f else 1.0f
            // Handle click events only if the level is unlocked
            itemView.setOnClickListener {
                if (!level.isLocked) onLevelClick(level)
            }
        }
    }
}
