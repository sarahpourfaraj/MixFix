package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class LevelSelectionActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvLevels: RecyclerView
    private lateinit var levelAdapter: LevelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_selection)

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance().reference

        // Initialize RecyclerView
        rvLevels = findViewById(R.id.rvLevels)
        rvLevels.layoutManager = GridLayoutManager(this, 3)  // 3 columns

        // Fetch levels from Firebase
        fetchLevelsFromFirebase()
    }

    private fun fetchLevelsFromFirebase() {
        // Reference to "levels" in Firebase
        val levelsRef = database.child("levels")

        // Fetch data from Firebase
        levelsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val levels = mutableListOf<Level>()

                // Log the data to ensure it's fetched
                Log.d("LevelSelectionActivity", "Data fetched from Firebase: $snapshot")

                // Iterate through the data and populate the levels list
                for (levelSnapshot in snapshot.children) {
                    val word = levelSnapshot.child("word").getValue(String::class.java)
                    val lettersSnapshot = levelSnapshot.child("letters")
                    val letters = mutableListOf<String>()

                    // Fetch the letters for the level (as an array)
                    for (letterSnapshot in lettersSnapshot.children) {
                        val letter = letterSnapshot.getValue(String::class.java)
                        if (letter != null) {
                            letters.add(letter)
                        }
                    }

                    // Create Level object with word and letters
                    if (word != null) {
                        val level = Level(word, letters)
                        levels.add(level)
                    }
                }

                // Log the levels to check if they are being populated
                Log.d("LevelSelectionActivity", "Levels populated: ${levels.size}")

                // Now that the data is fetched, initialize the adapter
                levelAdapter = LevelAdapter(levels) { level ->
                    // Handle level click, navigate to GameActivity
                    val intent = Intent(this@LevelSelectionActivity, GameActivity::class.java)
                    intent.putExtra("LEVEL_WORD", level.word)  // Send the word to GameActivity
                    intent.putStringArrayListExtra("LEVEL_LETTERS", ArrayList(level.letters)) // Send letters as well
                    startActivity(intent)
                }

                // Set the adapter to RecyclerView only after data is ready
                rvLevels.adapter = levelAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database read error
                Log.e("LevelSelectionActivity", "Error fetching levels: ${error.message}")
            }

            private fun fetchLevelsFromFirebase() {
                // Reference to "levels" in Firebase
                val levelsRef = database.child("levels")

                // Fetch data from Firebase
                levelsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val levels = mutableListOf<Level>()

                        // Log the entire snapshot to see if you're fetching the correct data
                        Log.d("LevelSelectionActivity", "Data fetched from Firebase: $snapshot")

                        // Iterate through the data and populate the levels list
                        for (levelSnapshot in snapshot.children) {
                            val word = levelSnapshot.child("word").getValue(String::class.java)
                            val lettersSnapshot = levelSnapshot.child("letters")
                            val letters = mutableListOf<String>()

                            // Fetch the letters for the level (as an array)
                            for (letterSnapshot in lettersSnapshot.children) {
                                val letter = letterSnapshot.getValue(String::class.java)
                                if (letter != null) {
                                    letters.add(letter)
                                }
                            }

                            // Log individual level data
                            Log.d("LevelSelectionActivity", "Fetched level: word = $word, letters = $letters")

                            // Create Level object with word and letters
                            if (word != null) {
                                val level = Level(word, letters)
                                levels.add(level)
                            }
                        }

                        // Log the levels list size to confirm it was populated
                        Log.d("LevelSelectionActivity", "Levels populated: ${levels.size}")

                        // Now that the data is fetched, initialize the adapter
                        levelAdapter = LevelAdapter(levels) { level ->
                            // Handle level click, navigate to GameActivity
                            val intent = Intent(this@LevelSelectionActivity, GameActivity::class.java)
                            intent.putExtra("LEVEL_WORD", level.word)  // Send the word to GameActivity
                            intent.putStringArrayListExtra("LEVEL_LETTERS", ArrayList(level.letters)) // Send letters as well
                            startActivity(intent)
                        }

                        // Set the adapter to RecyclerView only after data is ready
                        rvLevels.adapter = levelAdapter
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database read error
                        Log.e("LevelSelectionActivity", "Error fetching levels: ${error.message}")
                    }
                })
            }

        })
    }
}


