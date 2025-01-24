package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView // Add this import


class LevelSelectionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var chapterId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_selection)

        dbHelper = DatabaseHelper(this)
        chapterId = intent.getLongExtra("CHAPTER_ID", 1) // Read as Long

        if (!dbHelper.isDatabasePopulated()) {
            val jsonDataImporter = JsonDataImporter(this, dbHelper)
            jsonDataImporter.importData()
        }

        val levels = dbHelper.getLevelsForChapter(chapterId)
        displayLevels(levels)
    }


    private fun displayLevels(levels: List<Map<String, Any>>) {
        val container = findViewById<LinearLayout>(R.id.container)
        container.removeAllViews() // Clear existing views

        if (levels.isEmpty()) {
            // Show a message or handle empty levels
            val noLevelsText = TextView(this).apply {
                text = "No levels available" // Set the text
                textSize = 16f // Set the text size
            }
            container.addView(noLevelsText)
            return
        }

        for (level in levels) {
            val levelButton = Button(this).apply {
                text = level["name"].toString()
                textSize = 16f
                setBackgroundResource(
                    if (level["is_locked"] == true) R.drawable.level_locked else R.drawable.level_unlocked
                )
                isEnabled = level["is_locked"] != true

                setOnClickListener {
                    val intent = Intent(this@LevelSelectionActivity, GameActivity::class.java)
                    intent.putExtra("LEVEL_WORD", level["word"].toString())
                    intent.putStringArrayListExtra(
                        "LEVEL_LETTERS",
                        ArrayList(level["letters"].toString().split(","))
                    )
                    intent.putExtra("LEVEL_ID", level["id"] as Long)
                    startActivity(intent)
                }
            }
            container.addView(levelButton)
        }
    }
}