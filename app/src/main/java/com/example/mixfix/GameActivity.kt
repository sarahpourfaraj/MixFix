package com.example.mixfix

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class GameActivity : AppCompatActivity() {

    private lateinit var levelWord: String
    private lateinit var levelLetters: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Receive the word and letters from the previous activity
        levelWord = intent.getStringExtra("LEVEL_WORD") ?: ""
        levelLetters = intent.getStringArrayListExtra("LEVEL_LETTERS")?.toList() ?: listOf()

        // Log the received data for debugging
        Log.d("GameActivity", "Received word: $levelWord")
        Log.d("GameActivity", "Received letters: $levelLetters")

        // Set up the game UI with the word and letters (You can update this as per your UI design)
        title = "Level - $levelWord" // For example, set the title as the word
    }
}
