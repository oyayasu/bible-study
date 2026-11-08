package com.oyayasu.biblestudy.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

data class JournalEntry(
    val date: String,
    val studyId: String,
    val session: String,
    val text: String
)

class LocalStudyStore(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("bible_study_private_v1", Context.MODE_PRIVATE)

    var revision by mutableIntStateOf(0)
        private set

    private fun put(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
        revision++
    }

    private fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
        revision++
    }

    fun morningEnabled(): Boolean = preferences.getBoolean("reminder_morning", true)
    fun eveningEnabled(): Boolean = preferences.getBoolean("reminder_evening", true)
    fun setMorningEnabled(value: Boolean) = putBoolean("reminder_morning", value)
    fun setEveningEnabled(value: Boolean) = putBoolean("reminder_evening", value)

    fun passage(id: String): String = preferences.getString("passage|" + id, "") ?: ""
    fun savePassage(id: String, text: String) = put("passage|" + id, text)

    fun note(date: LocalDate, id: String, session: String): String =
        preferences.getString(noteKey(date, id, session), "") ?: ""

    fun saveNote(date: LocalDate, id: String, session: String, text: String) =
        put(noteKey(date, id, session), text)

    fun isComplete(date: LocalDate, session: String): Boolean =
        preferences.getBoolean("complete|" + date.toString() + "|" + session, false)

    fun setComplete(date: LocalDate, session: String, complete: Boolean) =
        putBoolean("complete|" + date.toString() + "|" + session, complete)

    fun entries(): List<JournalEntry> = preferences.all.mapNotNull { (key, value) ->
        if (!key.startsWith("journal|") || value !is String || value.isBlank()) {
            null
        } else {
            val parts = key.split("|")
            if (parts.size != 4) null else JournalEntry(parts[1], parts[2], parts[3], value)
        }
    }.sortedByDescending { it.date }

    private fun noteKey(date: LocalDate, id: String, session: String) =
        "journal|" + date.toString() + "|" + id + "|" + session
}
