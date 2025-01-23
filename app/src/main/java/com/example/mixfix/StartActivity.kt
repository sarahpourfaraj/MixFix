package com.example.mixfix

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        Log.d("StartActivity", "StartActivity created")

        val btnStart: Button = findViewById(R.id.btnStart)
        btnStart.setOnClickListener {
            Log.d("StartActivity", "Start button clicked")
            // Navigate to ChapterSelectionActivity instead of LevelSelectionActivity
            val intent = Intent(this, ChapterSelectionActivity::class.java)
            startActivity(intent)
        }

        val btnThemeToggle: Button = findViewById(R.id.btnThemeToggle)
        btnThemeToggle.setOnClickListener {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
            recreate()
        }

        val ivGitHub: ImageView = findViewById(R.id.ivGitHub)
        val ivGmail: ImageView = findViewById(R.id.ivGmail)
        val ivLinkedIn: ImageView = findViewById(R.id.ivLinkedIn)

        ivGitHub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yourusername"))
            startActivity(intent)
        }

        ivGmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:youremail@example.com")
            }
            startActivity(intent)
        }

        ivLinkedIn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/yourprofile"))
            startActivity(intent)
        }
    }
}