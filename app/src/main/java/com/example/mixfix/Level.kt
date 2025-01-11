package com.example.mixfix

// Represents a level in the game
data class Level(
    val number: Int,       // The level number (e.g., 1, 2, 3, ...)
    val isLocked: Boolean  // Whether the level is locked or not
)
