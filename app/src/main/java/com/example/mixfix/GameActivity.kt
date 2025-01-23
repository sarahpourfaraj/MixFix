package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var levelWord: String
    private lateinit var levelLetters: List<String>
    private lateinit var scrambledLetters: List<String>
    private lateinit var dbHelper: DatabaseHelper // Add this line
    private var levelId: Long = -1 // Add this line

    private lateinit var selectedLettersContainer: LinearLayout
    private lateinit var scrambledLettersContainer: LinearLayout
    private lateinit var btnSubmit: Button
    private lateinit var tvResult: TextView

    private val selectedLetters = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Initialize dbHelper
        dbHelper = DatabaseHelper(this) // Add this line

        selectedLettersContainer = findViewById(R.id.selectedLettersContainer)
        scrambledLettersContainer = findViewById(R.id.scrambledLettersContainer)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvResult = findViewById(R.id.tvResult)

        // Get the word, letters, and level ID from the intent
        levelWord = intent.getStringExtra("LEVEL_WORD") ?: ""
        levelLetters = intent.getStringArrayListExtra("LEVEL_LETTERS")?.toList() ?: listOf()
        levelId = intent.getLongExtra("LEVEL_ID", -1) // Add this line

        Log.d("GameActivity", "Received word: $levelWord")
        Log.d("GameActivity", "Received letters: $levelLetters")
        Log.d("GameActivity", "Received level ID: $levelId") // Add this line

        scrambledLetters = levelLetters.shuffled()

        displayScrambledLetters()

        btnSubmit.setOnClickListener {
            checkGuess()
        }
    }

    private fun displayScrambledLetters() {
        scrambledLettersContainer.removeAllViews()

        for (letter in scrambledLetters) {
            val letterButton = Button(this).apply {
                text = letter
                textSize = 18f
                setOnClickListener {
                    onLetterClicked(letter)
                }
            }
            scrambledLettersContainer.addView(letterButton)
        }
    }

    private fun onLetterClicked(letter: String) {
        selectedLetters.add(letter)

        updateSelectedLettersDisplay()

        for (i in 0 until scrambledLettersContainer.childCount) {
            val button = scrambledLettersContainer.getChildAt(i) as Button
            if (button.text == letter) {
                button.isEnabled = false
                break
            }
        }
    }

    private fun updateSelectedLettersDisplay() {
        selectedLettersContainer.removeAllViews()

        for (letter in selectedLetters) {
            val letterButton = Button(this).apply {
                text = letter
                textSize = 18f
                setOnClickListener {
                    onSelectedLetterClicked(letter)
                }
            }
            selectedLettersContainer.addView(letterButton)
        }
    }

    private fun onSelectedLetterClicked(letter: String) {
        selectedLetters.remove(letter)

        updateSelectedLettersDisplay()

        for (i in 0 until scrambledLettersContainer.childCount) {
            val button = scrambledLettersContainer.getChildAt(i) as Button
            if (button.text == letter) {
                button.isEnabled = true
                break
            }
        }
    }

    private fun checkGuess() {
        val guessedWord = selectedLetters.joinToString("")
        if (guessedWord.equals(levelWord, ignoreCase = true)) {
            tvResult.text = "Correct!"
            dbHelper.markLevelAsCompleted(levelId) // Mark level as completed
            dbHelper.unlockNextLevel(levelId) // Unlock the next level
            Handler().postDelayed({
                val intent = Intent(this, LevelSelectionActivity::class.java)
                startActivity(intent)
                finish()
            }, 1000)
        } else {
            tvResult.text = "Incorrect, try again!"
        }
    }
}