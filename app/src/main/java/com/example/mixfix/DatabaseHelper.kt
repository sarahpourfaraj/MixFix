package com.example.mixfix

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "mixfix.db"
        const val DATABASE_VERSION = 6 // Incremented version for schema changes

        // Table names
        const val TABLE_CHAPTERS = "chapters"
        const val TABLE_LEVELS = "levels"
        const val TABLE_GAME_STATS = "game_stats"

        // Common column names
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"

        // Chapters table columns
        const val COLUMN_IS_ACTIVE = "is_active"

        // Levels table columns
        const val COLUMN_WORD = "word"
        const val COLUMN_LETTERS = "letters"
        const val COLUMN_CHAPTER_ID = "chapter_id"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_IS_LOCKED = "is_locked"
        const val COLUMN_IS_SCORE_CLAIMED = "is_score_claimed"
        const val COLUMN_SCORE = "score"
        const val COLUMN_IDEA = "idea" // New column for idea

        // Game stats table columns
        const val COLUMN_TOTAL_HINTS_USED = "total_hints_used"
        const val COLUMN_TOTAL_IDEAS_USED = "total_ideas_used"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create chapters table
        val createChaptersTable = """
            CREATE TABLE $TABLE_CHAPTERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_IS_ACTIVE INTEGER DEFAULT 0
            );
        """

        // Create levels table
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
                $COLUMN_SCORE INTEGER DEFAULT 0,
                $COLUMN_IDEA TEXT, -- New column for idea
                FOREIGN KEY ($COLUMN_CHAPTER_ID) REFERENCES $TABLE_CHAPTERS($COLUMN_ID)
            );
        """

        // Create game_stats table
        val createGameStatsTable = """
            CREATE TABLE $TABLE_GAME_STATS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TOTAL_HINTS_USED INTEGER DEFAULT 0,
                $COLUMN_TOTAL_IDEAS_USED INTEGER DEFAULT 0
            );
        """

        // Execute SQL statements
        db?.execSQL(createChaptersTable)
        db?.execSQL(createLevelsTable)
        db?.execSQL(createGameStatsTable)

        // Insert a default row into game_stats
        val insertGameStats = """
            INSERT INTO $TABLE_GAME_STATS ($COLUMN_ID, $COLUMN_TOTAL_HINTS_USED, $COLUMN_TOTAL_IDEAS_USED)
            VALUES (1, 0, 0);
        """
        db?.execSQL(insertGameStats)

        Log.d("DatabaseHelper", "Database created with tables: $TABLE_CHAPTERS, $TABLE_LEVELS, and $TABLE_GAME_STATS")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            // Add the idea column to the levels table
            db?.execSQL("ALTER TABLE $TABLE_LEVELS ADD COLUMN $COLUMN_IDEA TEXT")
            Log.d("DatabaseHelper", "Database upgraded to version 6: Added idea column")
        }
    }

    // Add a new chapter
    fun addChapter(name: String, isActive: Boolean = false): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_IS_ACTIVE, if (isActive) 1 else 0)
        }
        return db.insert(TABLE_CHAPTERS, null, values)
    }

    // Add a new level
    fun addLevel(
        name: String,
        word: String,
        letters: String,
        chapterId: Long,
        isCompleted: Boolean = false,
        isLocked: Boolean = true,
        idea: String? = null // Add this parameter for the idea field
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_WORD, word)
            put(COLUMN_LETTERS, letters)
            put(COLUMN_CHAPTER_ID, chapterId)
            put(COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
            put(COLUMN_IS_LOCKED, if (isLocked) 1 else 0)
            put(COLUMN_IDEA, idea) // Add this line to include the idea field

            // Assign score based on level position in the chapter
            val levelPosition = getLevelPositionInChapter(chapterId, name)
            val score = if (levelPosition in 1..5) 40 else 60
            put(COLUMN_SCORE, score)
        }
        return db.insert(TABLE_LEVELS, null, values)
    }

    // Get the position of a level within its chapter
    private fun getLevelPositionInChapter(chapterId: Long, levelName: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LEVELS WHERE $COLUMN_CHAPTER_ID = ? AND $COLUMN_NAME <= ?",
            arrayOf(chapterId.toString(), levelName)
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }

    // Get a level by its ID
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
                "is_locked" to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED)) == 1),
                "score" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE)),
                "idea" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IDEA)) // Add this line
            )
            cursor.close()
            level
        } else {
            cursor.close()
            null
        }
    }

    // Check if the database is populated
    fun isDatabasePopulated(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CHAPTERS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    // Get all levels for a specific chapter
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
                    "is_locked" to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED)) == 1),
                    "score" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE)),
                    "idea" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IDEA)) // Include idea
                )
                levels.add(level)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return levels
    }

    fun getAllChapters(): List<Map<String, Any>> {
        val chapters = mutableListOf<Map<String, Any>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CHAPTERS", null)

        if (cursor.moveToFirst()) {
            do {
                val chapter = mapOf(
                    "id" to cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    "name" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    "is_active" to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1)
                )
                chapters.add(chapter)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return chapters
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

    fun claimScore(levelId: Long, scoreDelta: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SCORE, scoreDelta)
            put(COLUMN_IS_SCORE_CLAIMED, 1)
        }
        val rowsUpdated = db.update(TABLE_LEVELS, values, "$COLUMN_ID = ?", arrayOf(levelId.toString()))
        Log.d("DatabaseHelper", "Rows updated for level $levelId: $rowsUpdated")
    }

    fun getTotalScore(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_SCORE) FROM $TABLE_LEVELS WHERE $COLUMN_IS_SCORE_CLAIMED = 1",
            null
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }

    fun getChapterIdForLevel(levelId: Long): Long {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_CHAPTER_ID FROM $TABLE_LEVELS WHERE $COLUMN_ID = ?",
            arrayOf(levelId.toString())
        )
        return if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else {
            -1
        }
    }

    fun isLastLevelInChapter(levelId: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LEVELS WHERE $COLUMN_CHAPTER_ID = " +
                    "(SELECT $COLUMN_CHAPTER_ID FROM $TABLE_LEVELS WHERE $COLUMN_ID = ?)",
            arrayOf(levelId.toString())
        )
        return if (cursor.moveToFirst()) {
            val totalLevels = cursor.getInt(0)
            cursor.close()
            totalLevels == getLevelPositionInChapter(levelId)
        } else {
            cursor.close()
            false
        }
    }

    fun getTotalScoreForChapter(chapterId: Long): Int {
        val db = readableDatabase
        val query = """
        SELECT SUM($COLUMN_SCORE) 
        FROM $TABLE_LEVELS 
        WHERE $COLUMN_CHAPTER_ID = ? AND $COLUMN_IS_SCORE_CLAIMED = 1
    """.trimIndent()

        Log.d("DatabaseHelper", "Executing query: $query with chapterId: $chapterId")

        val cursor = db.rawQuery(query, arrayOf(chapterId.toString()))
        return if (cursor.moveToFirst()) {
            val totalScore = cursor.getInt(0)
            Log.d("DatabaseHelper", "Total score for chapter $chapterId: $totalScore")
            totalScore
        } else {
            Log.d("DatabaseHelper", "No scores found for chapter $chapterId")
            0
        }.also {
            cursor.close()
        }
    }

    fun getLevelPositionInChapter(levelId: Long): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_LEVELS WHERE $COLUMN_CHAPTER_ID = " +
                    "(SELECT $COLUMN_CHAPTER_ID FROM $TABLE_LEVELS WHERE $COLUMN_ID = ?) AND $COLUMN_ID <= ?",
            arrayOf(levelId.toString(), levelId.toString())
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }

    fun unlockChapter(chapterId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_ACTIVE, 1)
        }
        val rowsUpdated = db.update(TABLE_CHAPTERS, values, "$COLUMN_ID = ?", arrayOf(chapterId.toString()))
        Log.d("DatabaseHelper", "Rows updated for chapter $chapterId: $rowsUpdated")
    }

    fun getTotalHintsUsed(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_TOTAL_HINTS_USED FROM $TABLE_GAME_STATS WHERE $COLUMN_ID = 1",
            null
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }

    fun getTotalIdeasUsed(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_TOTAL_IDEAS_USED FROM $TABLE_GAME_STATS WHERE $COLUMN_ID = 1",
            null
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }

    fun updateTotalHintsUsed(hintsUsed: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TOTAL_HINTS_USED, hintsUsed)
        }
        db.update(TABLE_GAME_STATS, values, "$COLUMN_ID = 1", null)
    }

    fun updateTotalIdeasUsed(ideasUsed: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TOTAL_IDEAS_USED, ideasUsed)
        }
        db.update(TABLE_GAME_STATS, values, "$COLUMN_ID = 1", null)
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
}