package com.example.mixfix

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "mixfix.db"
        const val DATABASE_VERSION = 3

        const val TABLE_CHAPTERS = "chapters"
        const val TABLE_LEVELS = "levels"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_IS_ACTIVE = "is_active"
        const val COLUMN_WORD = "word"
        const val COLUMN_LETTERS = "letters"
        const val COLUMN_CHAPTER_ID = "chapter_id"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_IS_LOCKED = "is_locked"
        const val COLUMN_IS_SCORE_CLAIMED = "is_score_claimed"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createChaptersTable = """
            CREATE TABLE $TABLE_CHAPTERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_IS_ACTIVE INTEGER DEFAULT 0
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
                $COLUMN_IS_SCORE_CLAIMED INTEGER DEFAULT 0,
                FOREIGN KEY ($COLUMN_CHAPTER_ID) REFERENCES $TABLE_CHAPTERS($COLUMN_ID)
            );
        """

        db?.execSQL(createChaptersTable)
        db?.execSQL(createLevelsTable)
        Log.d("DatabaseHelper", "Database created with tables: $TABLE_CHAPTERS and $TABLE_LEVELS")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_LEVELS ADD COLUMN $COLUMN_IS_SCORE_CLAIMED INTEGER DEFAULT 0")
            Log.d("DatabaseHelper", "Database upgraded to version 3: Added is_score_claimed column")
        }
    }

    fun addChapter(name: String, isActive: Boolean = false): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_IS_ACTIVE, if (isActive) 1 else 0)
        }
        return db.insert(TABLE_CHAPTERS, null, values)
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
        return db.insert(TABLE_LEVELS, null, values)
    }

    fun getAllChapters(): Map<Int, Boolean> {
        val chapters = mutableMapOf<Int, Boolean>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CHAPTERS", null)

        if (cursor.moveToFirst()) {
            do {
                val chapterId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)).toInt()
                val isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1
                chapters[chapterId] = isActive
            } while (cursor.moveToNext())
        }
        cursor.close()
        return chapters
    }

    fun getLevelsForChapter(chapterId: Long): List<Map<String, Any>> {
        val levels = mutableListOf<Map<String, Any>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_LEVELS WHERE $COLUMN_CHAPTER_ID = ?",
            arrayOf(chapterId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val level = mapOf(
                    "id" to cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    "name" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    "word" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD)),
                    "letters" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LETTERS)),
                    "is_completed" to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1),
                    "is_locked" to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED)) == 1)
                )
                levels.add(level)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return levels
    }

    fun isDatabasePopulated(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CHAPTERS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    fun markLevelAsCompleted(levelId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_COMPLETED, 1)
        }
        db.update(TABLE_LEVELS, values, "$COLUMN_ID = ?", arrayOf(levelId.toString()))
    }

    fun unlockNextLevel(currentLevelId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_LOCKED, 0)
        }
        db.update(TABLE_LEVELS, values, "$COLUMN_ID = ?", arrayOf((currentLevelId + 1).toString()))
    }

    fun getLevelById(levelId: Long): Map<String, Any>? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_LEVELS WHERE $COLUMN_ID = ?",
            arrayOf(levelId.toString())
        )

        return if (cursor.moveToFirst()) {
            val level = mapOf(
                "id" to cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                "name" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                "word" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD)),
                "letters" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LETTERS)),
                "is_completed" to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1),
                "is_locked" to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED)) == 1)
            )
            cursor.close()
            level
        } else {
            cursor.close()
            null
        }
    }

    fun isScoreClaimed(levelId: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_IS_SCORE_CLAIMED FROM $TABLE_LEVELS WHERE $COLUMN_ID = ?",
            arrayOf(levelId.toString())
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(0) == 1
        } else {
            false
        }
    }

    fun claimScore(levelId: Long, score: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_SCORE_CLAIMED, 1)
        }
        db.update(TABLE_LEVELS, values, "$COLUMN_ID = ?", arrayOf(levelId.toString()))
    }

    fun getLevelPositionInChapter(levelId: Long): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_ID FROM $TABLE_LEVELS WHERE $COLUMN_CHAPTER_ID = (SELECT $COLUMN_CHAPTER_ID FROM $TABLE_LEVELS WHERE $COLUMN_ID = ?) ORDER BY $COLUMN_ID",
            arrayOf(levelId.toString())
        )
        var position = 0
        while (cursor.moveToNext()) {
            position++
            if (cursor.getLong(0) == levelId) {
                break
            }
        }
        cursor.close()
        return position
    }

    fun getTotalScore(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(CASE WHEN $COLUMN_ID % 10 IN (1,2,3,4,5) THEN 40 WHEN $COLUMN_ID % 10 IN (6,7,8,9,0) THEN 60 ELSE 0 END) FROM $TABLE_LEVELS WHERE $COLUMN_IS_SCORE_CLAIMED = 1",
            null
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }
}