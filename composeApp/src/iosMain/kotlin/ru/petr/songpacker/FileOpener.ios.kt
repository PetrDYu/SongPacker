package ru.petr.songpacker

/**
 * File opening is not yet implemented on iOS (requires UIDocumentPickerViewController).
 * Returns null to signal cancellation.
 */
actual fun openXmlFile(): Pair<String, String>? = null
