package ru.petr.songpacker

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual fun openXmlFile(): Pair<String, String>? {
    return try {
        val dialog = FileDialog(null as Frame?, "Открыть XML-файл", FileDialog.LOAD).apply {
            directory = System.getProperty("user.home") + File.separator + "Desktop"
            setFilenameFilter { _, name -> name.endsWith(".xml", ignoreCase = true) }
        }
        dialog.isVisible = true  // blocks until the user confirms or cancels

        val dir = dialog.directory ?: return null
        val fileName = dialog.file ?: return null
        val file = File(dir, fileName)
        if (!file.exists()) return null
        Pair(fileName, file.readText(Charsets.UTF_8))
    } catch (e: Exception) {
        null
    }
}
