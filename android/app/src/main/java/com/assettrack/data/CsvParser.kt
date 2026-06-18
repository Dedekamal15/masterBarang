package com.assettrack.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Parses a CSV file from a content URI.
     * Expected header columns (case-insensitive):
     *   name, category, serial_number (or imei), description, location
     */
    suspend fun parse(uri: Uri): List<Map<String, String>> = withContext(Dispatchers.IO) {
        val rows = mutableListOf<Map<String, String>>()
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            val headerLine = reader.readLine() ?: return@withContext rows
            val headers = headerLine.split(",").map { it.trim().lowercase().removeSurrounding("\"") }
            reader.forEachLine { line ->
                if (line.isBlank()) return@forEachLine
                val values = parseCsvLine(line)
                if (values.size >= headers.size) {
                    rows.add(headers.zip(values).toMap())
                }
            }
        }
        rows
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> { result.add(current.toString().trim()); current = StringBuilder() }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}
