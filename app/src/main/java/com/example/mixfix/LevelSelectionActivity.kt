package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class LevelSelectionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_selection)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Import JSON data if the database is empty
        if (!dbHelper.isDatabasePopulated()) {
            val jsonDataImporter = JsonDataImporter(this, dbHelper)
            jsonDataImporter.importData()
        }

        // Fetch chapters from the database
        val chapters = dbHelper.getAllChapters()

        // Display chapters and levels dynamically
        displayChaptersAndLevels(chapters)
    }

    private fun displayChaptersAndLevels(chapters: Map<String, List<Map<String, String>>>) {
        val container = findViewById<LinearLayout>(R.id.container)

        for ((chapterName, levels) in chapters) {
            // Add a TextView for the chapter name
            val chapterTextView = Button(this).apply {
                text = chapterName
                isAllCaps = false
                textSize = 18f
                setBackgroundResource(R.drawable.chapter_background) // Add a background drawable if needed
            }
            container.addView(chapterTextView)

            // Add buttons for each level in the chapter
            for (level in levels) {
                val levelButton = Button(this).apply {
                    text = level["name"]
                    textSize = 16f
                    setBackgroundResource(R.drawable.level_background) // Add a background drawable if needed

                    // Handle level click
                    setOnClickListener {
                        val intent = Intent(this@LevelSelectionActivity, GameActivity::class.java)
                        intent.putExtra("LEVEL_WORD", level["word"])
                        intent.putStringArrayListExtra("LEVEL_LETTERS", ArrayList(level["letters"]?.split(",")))
                        startActivity(intent)
                    }
                }
                container.addView(levelButton)
            }
        }
    }
}