package com.example.mixfix

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

class JsonDataImporter(private val context: Context, private val dbHelper: DatabaseHelper) {

    fun importData() {
        // Read the JSON file from res/raw
        val inputStream: InputStream = context.resources.openRawResource(R.raw.levels)
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        // Parse the JSON
        val jsonObject = JSONObject(jsonString)
        val chaptersArray = jsonObject.getJSONArray("chapters")

        // Insert chapters and levels into the database
        for (i in 0 until chaptersArray.length()) {
            val chapterObject = chaptersArray.getJSONObject(i)
            val chapterName = chapterObject.getString("name")
            val chapterId = dbHelper.addChapter(chapterName)

            val levelsArray = chapterObject.getJSONArray("levels")
            for (j in 0 until levelsArray.length()) {
                val levelObject = levelsArray.getJSONObject(j)
                val levelName = levelObject.getString("name")
                val word = levelObject.getString("word")
                val letters = levelObject.getString("letters")

                dbHelper.addLevel(levelName, word, letters, chapterId)
            }
        }
    }
}