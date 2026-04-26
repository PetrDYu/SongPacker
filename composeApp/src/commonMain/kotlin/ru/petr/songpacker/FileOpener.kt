package ru.petr.songpacker

/**
 * Opens a native file-picker dialog allowing the user to choose an XML file.
 * Returns a [Pair] of (fileName, fileContent) on success, or null if the user
 * cancelled or an error occurred.
 */
expect fun openXmlFile(): Pair<String, String>?
