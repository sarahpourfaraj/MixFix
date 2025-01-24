package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mixfix.databinding.ActivitySplashBinding
import android.graphics.Color

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.setBackgroundColor(Color.BLACK)

        Log.d("SplashActivity", "SplashActivity created")

        Glide.with(this)
            .asGif()
            .load(R.drawable.splash)
            .into(binding.splashGif)

        Handler().postDelayed({
            Log.d("SplashActivity", "Starting StartActivity")
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}