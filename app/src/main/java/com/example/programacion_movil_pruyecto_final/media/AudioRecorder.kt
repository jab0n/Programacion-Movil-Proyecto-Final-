package com.example.programacion_movil_pruyecto_final.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

// Clase que gestiona la grabación de audio.
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null

    // Crea una instancia de MediaRecorder, teniendo en cuenta la versión de Android.
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    // Inicia la grabación de audio y la guarda en el archivo especificado.
    fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC) // Establece la fuente de audio (micrófono).
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // Establece el formato de salida.
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Establece el codificador de audio.
            setOutputFile(outputFile.absolutePath) // Establece la ruta del archivo de salida.

            prepare() // Prepara el grabador.
            start() // Inicia la grabación.

            recorder = this
        }
    }

    // Detiene la grabación de audio.
    fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}
