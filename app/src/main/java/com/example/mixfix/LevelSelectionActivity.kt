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

        dbHelper = DatabaseHelper(this)

        if (!dbHelper.isDatabasePopulated()) {
            val jsonDataImporter = JsonDataImporter(this, dbHelper)
            jsonDataImporter.importData()
        }

        val chapters = dbHelper.getAllChapters()

        displayChaptersAndLevels(chapters)
    }

    private fun displayChaptersAndLevels(chapters: Map<String, List<Map<String, String>>>) {
        val container = findViewById<LinearLayout>(R.id.container)

        for ((chapterName, levels) in chapters) {
            val chapterTextView = Button(this).apply {
                text = chapterName
                isAllCaps = false
                textSize = 18f
                setBackgroundResource(R.drawable.chapter_background)
            }
            container.addView(chapterTextView)

            for (level in levels) {
                val levelButton = Button(this).apply {
                    text = level["name"]
                    textSize = 16f
                    setBackgroundResource(R.drawable.level_background)

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