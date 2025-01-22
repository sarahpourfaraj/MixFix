package com.example.mixfix

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "mixfix.db"
        const val DATABASE_VERSION = 2

        const val TABLE_CHAPTERS = "chapters"
        const val TABLE_LEVELS = "levels"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_WORD = "word"
        const val COLUMN_LETTERS = "letters"
        const val COLUMN_CHAPTER_ID = "chapter_id"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_IS_LOCKED = "is_locked"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createChaptersTable = """
            CREATE TABLE $TABLE_CHAPTERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL
            );
        """

        val createLevelsTable = """
            CREATE TABLE $TABLE_LEVELS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_WORD TEXT NOT NULL,
                $COLUMN_LETTERS TEXT NOT NULL,
                $COLUMN_CHAPTER_ID INTEGER NOT NULL,
                $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_IS_LOCKED INTEGER DEFAULT 1,
                FOREIGN KEY ($COLUMN_CHAPTER_ID) REFERENCES $TABLE_CHAPTERS($COLUMN_ID)
            );
        """

        db?.execSQL(createChaptersTable)
        db?.execSQL(createLevelsTable)

        Log.d("DatabaseHelper", "Database created with tables: $TABLE_CHAPTERS and $TABLE_LEVELS")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add new columns to the levels table
            db?.execSQL("ALTER TABLE $TABLE_LEVELS ADD COLUMN $COLUMN_IS_COMPLETED INTEGER DEFAULT 0")
            db?.execSQL("ALTER TABLE $TABLE_LEVELS ADD COLUMN $COLUMN_IS_LOCKED INTEGER DEFAULT 1")
            Log.d("DatabaseHelper", "Database upgraded to version 2: Added is_completed and is_locked columns")
        }
    }

    fun addChapter(name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
        }
        val chapterId = db.insert(TABLE_CHAPTERS, null, values)
        Log.d("DatabaseHelper", "Added chapter: $name with ID: $chapterId")
        return chapterId
    }

    fun addLevel(name: String, word: String, letters: String, chapterId: Long, isCompleted: Boolean = false, isLocked: Boolean = true): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_WORD, word)
            put(COLUMN_LETTERS, letters)
            put(COLUMN_CHAPTER_ID, chapterId)
            put(COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
            put(COLUMN_IS_LOCKED, if (isLocked) 1 else 0)
        }
        val levelId = db.insert(TABLE_LEVELS, null, values)
        Log.d("DatabaseHelper", "Added level: $name to chapter ID: $chapterId")
        return levelId
    }

    fun getAllChapters(): Map<String, List<Map<String, Any>>> {
        val chapters = mutableMapOf<String, List<Map<String, Any>>>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_CHAPTERS", null)

        if (cursor.moveToFirst()) {
            do {
                val chapterId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val chapterName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val levels = getLevelsForChapter(chapterId)

                chapters[chapterName] = levels
            } while (cursor.moveToNext())
        }
        cursor.close()

        Log.d("DatabaseHelper", "Fetched ${chapters.size} chapters from the database")
        return chapters
    }

    private fun getLevelsForChapter(chapterId: Long): List<Map<String, Any>> {
        val levels = mutableListOf<Map<String, Any>>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_LEVELS WHERE $COLUMN_CHAPTER_ID = ?",
            arrayOf(chapterId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val levelId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val levelName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val word = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD))
                val letters = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LETTERS))
                val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1
                val isLocked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED)) == 1

                val level = mapOf(
                    "id" to levelId,
                    "name" to levelName,
                    "word" to word,
                    "letters" to letters,
                    "is_completed" to isCompleted,
                    "is_locked" to isLocked
                )
                levels.add(level)
            } while (cursor.moveToNext())
        }
        cursor.close()

        Log.d("DatabaseHelper", "Fetched ${levels.size} levels for chapter ID: $chapterId")
        return levels
    }

    fun isDatabasePopulated(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CHAPTERS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        Log.d("DatabaseHelper", "Database is populated: ${count > 0}")
        return count > 0
    }

    fun markLevelAsCompleted(levelId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_COMPLETED, 1)
        }
        db.update(TABLE_LEVELS, values, "$COLUMN_ID = ?", arrayOf(levelId.toString()))
        Log.d("DatabaseHelper", "Marked level ID: $levelId as completed")
    }

    fun unlockNextLevel(currentLevelId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_LOCKED, 0)
        }
        db.update(TABLE_LEVELS, values, "$COLUMN_ID = ?", arrayOf((currentLevelId + 1).toString()))
        Log.d("DatabaseHelper", "Unlocked level ID: ${currentLevelId + 1}")
    }
}