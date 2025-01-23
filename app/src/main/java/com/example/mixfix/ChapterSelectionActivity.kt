package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class ChapterSelectionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_selection)

        dbHelper = DatabaseHelper(this)

        // Load chapter status from the database
        loadChapterStatus()
    }

    private fun loadChapterStatus() {
        val chapters = dbHelper.getAllChapters()
        Log.d("ChapterSelectionActivity", "Chapters: $chapters") // Log the chapters

        for ((chapterId, isActive) in chapters) {
            val imageView = findViewById<ImageView>(resources.getIdentifier("ivChapter$chapterId", "id", packageName))
            if (isActive) {
                imageView.setImageResource(R.drawable.stone_active) // Active stone
                imageView.isEnabled = true
            } else {
                imageView.setImageResource(R.drawable.stone_inactive) // Inactive stone
                imageView.isEnabled = false
            }
        }
    }

    fun onChapterClicked(view: View) {
        val chapterId = view.tag.toString().toInt()
        Log.d("ChapterSelectionActivity", "Chapter clicked: $chapterId")
        val intent = Intent(this, LevelSelectionActivity::class.java)
        intent.putExtra("CHAPTER_ID", chapterId)
        startActivity(intent)
    }
}