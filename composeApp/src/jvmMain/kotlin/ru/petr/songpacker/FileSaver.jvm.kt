package ru.petr.songpacker

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual fun saveXmlFile(fileName: String, content: String): String {
    return try {
        val dialog = FileDialog(null as Frame?, "Сохранить XML-файл", FileDialog.SAVE).apply {
            directory = System.getProperty("user.home") + File.separator + "Desktop"
            file = fileName
            // On Windows, filter by extension using the native mechanism
            setFilenameFilter { _, name -> name.endsWith(".xml", ignoreCase = true) }
        }
        dialog.isVisible = true  // blocks until user confirms or cancels

        val dir = dialog.directory ?: return "Cancelled"
        val file = dialog.file ?: return "Cancelled"
        val outputFile = File(dir, file).let {
            if (it.extension.lowercase() == "xml") it else File("${it.absolutePath}.xml")
        }
        outputFile.writeText(content, Charsets.UTF_8)
        outputFile.absolutePath
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
