package com.example.mixfix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LevelAdapter(
    private val levels: List<Level>,
    private val onLevelClick: (Level) -> Unit
) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_level, parent, false)
        return LevelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val level = levels[position]
        holder.bind(level, onLevelClick)
    }

    override fun getItemCount(): Int = levels.size

    class LevelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLevelWord: TextView = itemView.findViewById(R.id.tvLevelWord)

        fun bind(level: Level, onLevelClick: (Level) -> Unit) {
            // Display the word for the level
            tvLevelWord.text = level.word

            // Handle click events
            itemView.setOnClickListener {
                onLevelClick(level)
            }
        }
    }
}
