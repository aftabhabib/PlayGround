package com.desimeals.app.util

import android.app.backup.BackupAgentHelper
import android.content.Context
import androidx.datastore.preferences.core.edit
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DesiBackupAgent : BackupAgentHelper()

object BackupHelper {
    private fun dbFile(ctx: Context): File = ctx.getDatabasePath("desi_meals.db")

    fun backup(ctx: Context): File {
        val src = dbFile(ctx)
        require(src.exists()) { "No database found to back up yet." }
        val dir = File(ctx.filesDir, "backups").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val dst = File(dir, "desi-meals-backup-$stamp.db")
        src.copyTo(dst, overwrite = true)
        return dst
    }

    suspend fun markBackup(ctx: Context, label: String) {
        ctx.settingsStore.edit { it[PrefsKeys.LAST_BACKUP] = label }
    }

    fun listBackups(ctx: Context): List<File> {
        val dir = File(ctx.filesDir, "backups")
        return if (dir.exists()) dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList() else emptyList()
    }

    /** Validate a backup file: must be non-empty SQLite file (header check). */
    fun validate(file: File): Boolean {
        if (!file.exists() || file.length() < 100) return false
        return try {
            file.inputStream().use { ins ->
                val header = ByteArray(16)
                ins.read(header)
                String(header).startsWith("SQLite format 3")
            }
        } catch (_: Exception) { false }
    }

    fun restore(ctx: Context, backup: File) {
        require(validate(backup)) { "Invalid backup file." }
        val dst = dbFile(ctx)
        // Close-friendly copy: Room will re-open on next access; best-effort.
        backup.copyTo(dst, overwrite = true)
    }
}
