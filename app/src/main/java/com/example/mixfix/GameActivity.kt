package com.example.mixfix

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var levelWord: String
    private lateinit var levelLetters: List<String>
    private lateinit var scrambledLetters: List<String>
    private lateinit var dbHelper: DatabaseHelper
    private var levelId: Long = -1
    private var chapterId: Long = -1

    private lateinit var wordContainer: LinearLayout
    private lateinit var keyboardContainer: GridLayout
    private lateinit var btnSubmit: Button
    private lateinit var btnHint: Button
    private lateinit var btnIdea: Button
    private lateinit var btnShowScore: Button
    private lateinit var tvResult: TextView
    private lateinit var tvIdea: TextView
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView

    private val selectedLetters = mutableListOf<String>()
    private var hearts = 3
    private var hintsUsed = 0
    private var ideasUsed = 0

    private val hintRevealedIndices = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        dbHelper = DatabaseHelper(this)
        wordContainer = findViewById(R.id.wordContainer)
        keyboardContainer = findViewById(R.id.keyboardContainer)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnHint = findViewById(R.id.btnHint)
        btnIdea = findViewById(R.id.btnIdea)
        btnShowScore = findViewById(R.id.btnShowScore)
        tvResult = findViewById(R.id.tvResult)
        tvIdea = findViewById(R.id.tvIdea)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)

        levelWord = intent.getStringExtra("LEVEL_WORD") ?: ""
        levelLetters = intent.getStringArrayListExtra("LEVEL_LETTERS")?.toList() ?: listOf()
        levelId = intent.getLongExtra("LEVEL_ID", -1)
        chapterId = dbHelper.getChapterIdForLevel(levelId)

        Log.d("GameActivity", "Received word: $levelWord")
        Log.d("GameActivity", "Received letters: $levelLetters")
        Log.d("GameActivity", "Received level ID: $levelId")
        Log.d("GameActivity", "Chapter ID: $chapterId")

        // Load the total hints and ideas used across all chapters
        hintsUsed = dbHelper.getTotalHintsUsed()
        ideasUsed = dbHelper.getTotalIdeasUsed()

        scrambledLetters = levelLetters.distinct().shuffled()
        displayWordContainer()
        displayKeyboard()
        updateHeartsDisplay()
        updateHintAndIdeaButtons()

        btnSubmit.setOnClickListener {
            checkGuess()
        }

        btnHint.setOnClickListener {
            useHint()
        }

        btnIdea.setOnClickListener {
            useIdea()
        }

        btnShowScore.setOnClickListener {
            val totalScore = dbHelper.getTotalScore()
            Toast.makeText(this, "Total Score: $totalScore", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayWordContainer() {
        wordContainer.removeAllViews()

        for (i in levelWord.indices) {
            val textView = TextView(this).apply {
                text = ""
                textSize = 24f
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.square_size),
                    resources.getDimensionPixelSize(R.dimen.square_size)
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
                background = resources.getDrawable(R.drawable.square_background, null)
                gravity = android.view.Gravity.CENTER
                tag = "empty"

                setOnClickListener {
                    onWordSquareClicked(this, i)
                }
            }
            wordContainer.addView(textView)
        }
    }

    private fun onWordSquareClicked(textView: TextView, index: Int) {
        if (hintRevealedIndices.contains(index)) {
            return
        }

        if (textView.text.isNotEmpty()) {
            val letter = textView.text.toString()
            selectedLetters.remove(letter)

            textView.text = ""
            textView.tag = "empty"
        }
    }

    private fun displayKeyboard() {
        keyboardContainer.removeAllViews()

        for (letter in scrambledLetters) {
            val letterButton = Button(this).apply {
                text = letter
                textSize = 18f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                setOnClickListener {
                    onLetterClicked(letter)
                }
            }
            keyboardContainer.addView(letterButton)
        }
    }

    private fun onLetterClicked(letter: String) {
        for (i in 0 until wordContainer.childCount) {
            val textView = wordContainer.getChildAt(i) as TextView
            if (textView.text.isEmpty()) {
                textView.text = letter
                textView.tag = "filled"
                selectedLetters.add(letter)
                break
            }
        }
    }

    private fun updateHeartsDisplay() {
        heart1.setImageResource(if (hearts >= 1) R.drawable.heart_full else R.drawable.heart_empty)
        heart2.setImageResource(if (hearts >= 2) R.drawable.heart_full else R.drawable.heart_empty)
        heart3.setImageResource(if (hearts >= 3) R.drawable.heart_full else R.drawable.heart_empty)
    }

    private fun updateHintAndIdeaButtons() {
        btnHint.text = "Hint (${2 - hintsUsed})"
        btnIdea.text = "Idea (${1 - ideasUsed})"
    }

    private fun useHint() {
        val totalScore = dbHelper.getTotalScore()
        val remainingHints = 2 - hintsUsed

        if (remainingHints > 0) {
            hintsUsed++
            dbHelper.updateTotalHintsUsed(hintsUsed)
            updateHintAndIdeaButtons()

            revealRandomLetter()
        } else if (totalScore >= 100) {
            dbHelper.claimScore(levelId, -100)
            showToast("100 points deducted for hint!")

            revealRandomLetter()
        } else {
            showToast("Not enough points to use hint!")
        }
    }

    private fun revealRandomLetter() {
        val emptySquares = mutableListOf<Pair<TextView, Int>>()
        for (i in levelWord.indices) {
            val textView = wordContainer.getChildAt(i) as TextView
            if (textView.text.isEmpty()) {
                emptySquares.add(Pair(textView, i))
            }
        }

        if (emptySquares.isNotEmpty()) {
            val (textView, index) = emptySquares.random()
            val correctLetter = levelWord[index].toString()
            textView.text = correctLetter
            textView.tag = "filled"
            selectedLetters.add(correctLetter)

            hintRevealedIndices.add(index)
        }
    }

    private fun useIdea() {
        val totalScore = dbHelper.getTotalScore()
        val remainingIdeas = 1 - ideasUsed

        if (remainingIdeas > 0) {
            ideasUsed++
            dbHelper.updateTotalIdeasUsed(ideasUsed)
            updateHintAndIdeaButtons()

            showIdea()
        } else if (totalScore >= 500) {
            dbHelper.claimScore(levelId, -500)
            showToast("500 points deducted for idea!")

            showIdea()
        } else {
            showToast("Not enough points to use idea!")
        }
    }

    private fun showIdea() {
        val level = dbHelper.getLevelById(levelId)
        val idea = level?.get("idea") as? String ?: "Think of a common word."
        tvIdea.text = idea
    }

    private fun checkGuess() {
        val guessedWord = buildGuessedWordFromContainer()

        if (guessedWord.isEmpty()) {
            showToast("Please select some letters!")
            return
        }

        if (guessedWord.equals(levelWord, ignoreCase = true)) {
            dbHelper.markLevelAsCompleted(levelId)
            dbHelper.unlockNextLevel(levelId)

            if (dbHelper.isLastLevelInChapter(levelId)) {
                showChapterCompleteDialog()
            } else {
                showWinPopup()
            }
        } else {
            hearts--
            updateHeartsDisplay()
            if (hearts <= 0) {
                showLosePopup()
            } else {
                showToast("Incorrect! You have $hearts hearts left.")
            }
        }
    }

    private fun buildGuessedWordFromContainer(): String {
        val guessedWord = StringBuilder()
        for (i in 0 until wordContainer.childCount) {
            val textView = wordContainer.getChildAt(i) as TextView
            guessedWord.append(textView.text)
        }
        return guessedWord.toString()
    }

    private fun showChapterCompleteDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_chapter_completion)

        val totalScore = dbHelper.getTotalScoreForChapter(chapterId)

        val tvTotalScore: TextView = dialog.findViewById(R.id.tvTotalScore)
        tvTotalScore.text = "Total Score: $totalScore"

        val btnOk: Button = dialog.findViewById(R.id.btnOk)
        btnOk.setOnClickListener {
            dialog.dismiss()

            // Unlock the next chapter
            dbHelper.unlockChapter(chapterId)

            val intent = Intent(this, ChapterSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Close the GameActivity
        }

        dialog.show()
    }
    private fun showWinPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_win)

        val btnNextLevel = dialog.findViewById<Button>(R.id.btnNextLevel)
        val btnChapters = dialog.findViewById<Button>(R.id.btnChapters)

        btnNextLevel.setOnClickListener {
            dialog.dismiss()
            loadNextLevel(levelId)
        }

        btnChapters.setOnClickListener {
            dialog.dismiss()
            goToChapters()
        }

        dialog.show()
    }

    private fun showLosePopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_lose)

        val btnReplay = dialog.findViewById<Button>(R.id.btnReplay)
        val btnHome = dialog.findViewById<Button>(R.id.btnHome)

        btnReplay.setOnClickListener {
            dialog.dismiss()
            resetGame()
        }

        btnHome.setOnClickListener {
            dialog.dismiss()
            goToChapters()
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
            scrambledLetters = levelLetters.distinct().shuffled()
            resetGame()
        } else {
            showToast("No more levels available!")
        }
    }

    private fun goToChapters() {
        val intent = Intent(this, ChapterSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun resetGame() {
        hearts = 3
        selectedLetters.clear()
        updateHeartsDisplay()
        displayWordContainer()
        displayKeyboard()
        tvResult.text = ""
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}