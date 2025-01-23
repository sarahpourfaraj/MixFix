package com.example.mixfix

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var levelWord: String
    private lateinit var levelLetters: List<String>
    private lateinit var scrambledLetters: List<String>
    private lateinit var dbHelper: DatabaseHelper
    private var levelId: Long = -1

    private lateinit var selectedLettersContainer: LinearLayout
    private lateinit var scrambledLettersContainer: GridLayout // Change to GridLayout
    private lateinit var btnSubmit: Button
    private lateinit var tvResult: TextView

    private val selectedLetters = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        dbHelper = DatabaseHelper(this)
        selectedLettersContainer = findViewById(R.id.selectedLettersContainer)
        scrambledLettersContainer = findViewById(R.id.scrambledLettersContainer) // This is now a GridLayout
        btnSubmit = findViewById(R.id.btnSubmit)
        tvResult = findViewById(R.id.tvResult)

        levelWord = intent.getStringExtra("LEVEL_WORD") ?: ""
        levelLetters = intent.getStringArrayListExtra("LEVEL_LETTERS")?.toList() ?: listOf()
        levelId = intent.getLongExtra("LEVEL_ID", -1)
        Log.d("GameActivity", "Received word: $levelWord")
        Log.d("GameActivity", "Received letters: $levelLetters")
        Log.d("GameActivity", "Received level ID: $levelId")

        scrambledLetters = levelLetters.shuffled()
        displayScrambledLetters()

        btnSubmit.setOnClickListener {
            checkGuess()
        }
    }

    private fun displayScrambledLetters() {
        scrambledLettersContainer.removeAllViews() // Clear the container

        for (letter in scrambledLetters) {
            val letterButton = Button(this).apply {
                text = letter
                textSize = 18f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // Use 0 to allow the GridLayout to manage the width
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Equal weight for columns
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Equal weight for rows
                    setMargins(4, 4, 4, 4) // Add margins for spacing
                }
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
            if (button.text == letter && button.isEnabled) {
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
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
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
            if (button.text == letter && !button.isEnabled) {
                button.isEnabled = true
                break
            }
        }
    }

    private fun checkGuess() {
        val guessedWord = selectedLetters.joinToString("")
        if (guessedWord.equals(levelWord, ignoreCase = true)) {
            dbHelper.markLevelAsCompleted(levelId)
            dbHelper.unlockNextLevel(levelId)

            if (!dbHelper.isScoreClaimed(levelId)) {
                val score = calculateScore(levelId)
                dbHelper.claimScore(levelId, score)
            }

            val score = calculateScore(levelId)
            showScorePopup(score, levelId)
        } else {
            showToast("Incorrect! Try again.")
        }
    }

    private fun calculateScore(levelId: Long): Int {
        val levelPosition = dbHelper.getLevelPositionInChapter(levelId)
        return if (levelPosition in 1..5) {
            40
        } else if (levelPosition in 6..10) {
            60
        } else {
            0
        }
    }

    private fun showScorePopup(score: Int, levelId: Long) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_score)

        val tvScore = dialog.findViewById<TextView>(R.id.tvScore)
        tvScore.text = "Your Score: $score"

        val btnNextLevel = dialog.findViewById<Button>(R.id.btnNextLevel)
        btnNextLevel.setOnClickListener {
            dialog.dismiss()
            loadNextLevel(levelId)
        }

        val btnChapters = dialog.findViewById<Button>(R.id.btnChapters)
        btnChapters.setOnClickListener {
            dialog.dismiss()
            goToChapters()
        }

        val btnMusicOptions = dialog.findViewById<Button>(R.id.btnMusicOptions)
        btnMusicOptions.setOnClickListener {
            dialog.dismiss()
            showMusicOptions()
        }

        dialog.show()
    }

    private fun loadNextLevel(currentLevelId: Long) {
        val nextLevelId = currentLevelId + 1
        val nextLevel = dbHelper.getLevelById(nextLevelId)
        if (nextLevel != null) {
            levelId = nextLevelId
            levelWord = nextLevel["word"] as String
            levelLetters = (nextLevel["letters"] as String).split(",")
            scrambledLetters = levelLetters.shuffled()
            resetUIForNextLevel()
        } else {
            showToast("No more levels available!")
        }
    }

    private fun resetUIForNextLevel() {
        selectedLetters.clear()
        updateSelectedLettersDisplay()
        displayScrambledLetters()
        tvResult.text = ""
    }

    private fun goToChapters() {
        val intent = Intent(this, ChapterSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showMusicOptions() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_music_options)

        val switchMusic = dialog.findViewById<Switch>(R.id.switchMusic)
        val switchSound = dialog.findViewById<Switch>(R.id.switchSound)

        switchMusic.isChecked = true
        switchSound.isChecked = true

        dialog.setOnDismissListener {
            val musicEnabled = switchMusic.isChecked
            val soundEnabled = switchSound.isChecked
            // Save preferences (e.g., using SharedPreferences)
        }

        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}