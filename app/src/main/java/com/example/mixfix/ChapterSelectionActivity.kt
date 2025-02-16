package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChapterSelectionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_selection)

        dbHelper = DatabaseHelper(this)

        loadChapterStatus()

        val btnTotalScore: Button = findViewById(R.id.btnTotalScore)
        btnTotalScore.setOnClickListener {
            val totalScore = dbHelper.getTotalScore()
            Toast.makeText(this, "Total Score: $totalScore", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadChapterStatus()
    }

    private fun loadChapterStatus() {
        val chapters = dbHelper.getAllChapters()
        Log.d("ChapterSelectionActivity", "Chapters: $chapters")

        for (chapter in chapters) {
            val chapterId = chapter["id"] as Long
            val isActive = chapter["is_active"] as Boolean

            val imageView = findViewById<ImageView?>(resources.getIdentifier("ivChapter$chapterId", "id", packageName))
            imageView?.let {
                if (isActive) {
                    it.setImageResource(R.drawable.stone_active)
                    it.isEnabled = true
                } else {
                    it.setImageResource(R.drawable.stone_inactive)
                    it.isEnabled = false
                }
            }
        }
    }

    fun onChapterClicked(view: View) {
        val chapterId = view.tag.toString().toLong()
        Log.d("ChapterSelectionActivity", "Chapter clicked: $chapterId")
        val intent = Intent(this, LevelSelectionActivity::class.java)
        intent.putExtra("CHAPTER_ID", chapterId)
        startActivity(intent)
    }
}