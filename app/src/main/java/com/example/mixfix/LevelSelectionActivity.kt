package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LevelSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_selection)

        // Find RecyclerView by ID
        val rvLevels: RecyclerView = findViewById(R.id.rvLevels)

        // Set LayoutManager for grid display
        rvLevels.layoutManager = GridLayoutManager(this, 3) // 3 columns

        // Create level data
        val levels = List(10) { index -> Level(index + 1, isLocked = index > 2) }

        // Set Adapter
        rvLevels.adapter = LevelAdapter(levels) { level ->
            if (!level.isLocked) {
                // Navigate to GameActivity (replace with your game activity)
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("LEVEL_NUMBER", level.number)
                startActivity(intent)
            }
        }
    }
}
