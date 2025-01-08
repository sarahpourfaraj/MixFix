package com.example.mixfix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mixfix.ui.theme.MixFixTheme
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startGameButton: Button = findViewById(R.id.startGameButton)

        startGameButton.setOnClickListener {
            navigateToSecondActivity()
        }
//        enableEdgeToEdge()
//        setContent {
//            MixFixTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    StartScreen(modifier = Modifier.padding(innerPadding))
//                }
//            }
//        }
    }
    private fun navigateToSecondActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
    }
}



@Composable
fun StartScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { /* Navigate to the next screen */ }) {
            Text(text = "شروع بازی")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    MixFixTheme {
        StartScreen()
    }
}
