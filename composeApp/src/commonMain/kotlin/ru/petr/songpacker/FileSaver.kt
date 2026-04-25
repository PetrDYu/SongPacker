package ru.petr.songpacker

/**
 * Saves [content] as a UTF-8 XML file named [fileName].
 * Returns the absolute path of the saved file, or an error message starting with "Error:".
 */
expect fun saveXmlFile(fileName: String, content: String): String
