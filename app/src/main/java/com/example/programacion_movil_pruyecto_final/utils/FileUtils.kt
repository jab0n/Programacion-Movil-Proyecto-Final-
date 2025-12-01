package com.example.programacion_movil_pruyecto_final.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

// Función para obtener el nombre de un archivo a partir de su URI.
fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    // Si el esquema de la URI es "content", se intenta obtener el nombre del archivo
    // a través del ContentResolver.
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    result = cursor.getString(displayNameIndex)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    // Si no se pudo obtener el nombre del archivo a través del ContentResolver,
    // se intenta obtener a partir de la ruta de la URI.
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    // Si no se pudo obtener el nombre del archivo, se devuelve "unknown_file".
    return result ?: "unknown_file"
}
