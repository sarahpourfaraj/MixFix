package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class LevelSelectionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var chapterId: Long = 0 // Use Long for chapterId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_selection)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Get chapterId from the intent (default to 1 if not provided)
        chapterId = intent.getLongExtra("CHAPTER_ID", 1)

        // Check if the database is populated; if not, import data
        if (!dbHelper.isDatabasePopulated()) {
            val jsonDataImporter = JsonDataImporter(this, dbHelper)
            jsonDataImporter.importData()
        }

        // Fetch levels for the selected chapter
        val levels = dbHelper.getLevelsForChapter(chapterId)

        // Display levels
        displayLevels(levels)
    }

    private fun displayLevels(levels: List<Map<String, Any>>) {
        val container = findViewById<LinearLayout>(R.id.container)

        for (level in levels) {
            // Create a button for each level
            val levelButton = Button(this).apply {
                text = level["name"].toString() // Level name
                textSize = 16f
                // Set background based on lock status
                setBackgroundResource(
                    if (level["is_locked"] == true) R.drawable.level_locked else R.drawable.level_unlocked
                )
                // Enable/disable button based on lock status
                isEnabled = level["is_locked"] != true

                // Handle level selection
                setOnClickListener {
                    val intent = Intent(this@LevelSelectionActivity, GameActivity::class.java)
                    intent.putExtra("LEVEL_WORD", level["word"].toString()) // Pass word
                    intent.putStringArrayListExtra(
                        "LEVEL_LETTERS",
                        ArrayList(level["letters"].toString().split(",")) // Pass letters
                    )
                    intent.putExtra("LEVEL_ID", level["id"] as Long) // Pass level ID as Long
                    startActivity(intent)
                }
            }
            container.addView(levelButton) // Add button to the container
        }
    }
}