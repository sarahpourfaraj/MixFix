package com.example.mixfix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val levelNumber = intent.getIntExtra("LEVEL_NUMBER", 1)
        title = "Level $levelNumber"
    }
}
