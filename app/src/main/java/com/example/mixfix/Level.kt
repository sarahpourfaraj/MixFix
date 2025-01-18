package com.example.mixfix
data class Level(
    val word: String = "",               // Word for the level (e.g., "book", "tree")
    val letters: List<String> = listOf()  // List of letters (e.g., ["B", "O", "O", "K"])
)
