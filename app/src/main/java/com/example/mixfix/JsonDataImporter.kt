package com.example.mixfix

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

class JsonDataImporter(private val context: Context, private val dbHelper: DatabaseHelper) {

    fun importData() {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.levels)
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(jsonString)
        val chaptersArray = jsonObject.getJSONArray("chapters")

        for (i in 0 until chaptersArray.length()) {
            val chapterObject = chaptersArray.getJSONObject(i)
            val chapterName = chapterObject.getString("name")
            val isActive = i == 0 // Only the first chapter is active initially
            val chapterId = dbHelper.addChapter(chapterName, isActive)

            val levelsArray = chapterObject.getJSONArray("levels")
            for (j in 0 until levelsArray.length()) {
                val levelObject = levelsArray.getJSONObject(j)
                val levelName = levelObject.getString("name")
                val word = levelObject.getString("word")
                val letters = levelObject.getString("letters")
                val isCompleted = levelObject.getBoolean("is_completed")
                val isLocked = levelObject.getBoolean("is_locked")

                dbHelper.addLevel(levelName, word, letters, chapterId, isCompleted, isLocked)
            }
        }
    }
}