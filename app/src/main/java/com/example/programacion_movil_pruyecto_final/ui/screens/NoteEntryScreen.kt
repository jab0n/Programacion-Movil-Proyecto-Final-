package com.example.programacion_movil_pruyecto_final.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.media.AudioRecorder
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.NotesViewModel
import com.example.programacion_movil_pruyecto_final.utils.getFileName
import com.example.programacion_movil_pruyecto_final.utils.rememberPermissionHandler
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Composable que representa la pantalla para crear o editar una nota.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEntryScreen(
    application: NotesAndTasksApplication,
    onNavigateBack: () -> Unit, // Lógica para navegar hacia atrás.
    onAttachmentClick: (String, String) -> Unit, // Lógica para manejar el clic en un adjunto.
    noteId: Int? = null // ID de la nota a editar (nulo si se crea una nueva).
) {
    // Obtiene una instancia del ViewModel para esta pantalla.
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(application, application.notesRepository, application.tasksRepository))
    // Obtiene el estado de la UI desde el ViewModel.
    val uiState by viewModel.uiState.collectAsState()
    val noteDetails = uiState.noteDetails

    // Efecto que se ejecuta cuando el ID de la nota cambia, para preparar los detalles de la nota.
    LaunchedEffect(noteId) {
        viewModel.prepareForEntry(noteId)
    }

    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    // Función para copiar un archivo desde una URI a almacenamiento interno.
    fun copyUriToInternalStorage(uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context, uri)
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    // Lanza la actividad para obtener contenido (archivos) y los añade como adjuntos.
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        uris.forEach { uri ->
            val newUri = copyUriToInternalStorage(uri)
            val type = context.contentResolver.getType(uri)
            viewModel.onAttachmentSelected(newUri, type)
        }
    }

    // Lanza la cámara para tomar una foto y la añade como adjunto.
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempUri?.let {
                val type = context.contentResolver.getType(it)
                viewModel.onAttachmentSelected(it, type)
            }
        }
    }

    // Lanza la cámara para grabar un video y lo añade como adjunto.
    val captureVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            tempUri?.let {
                val type = context.contentResolver.getType(it)
                viewModel.onAttachmentSelected(it, type)
            }
        }
    }

    // Función para crear un archivo temporal con una extensión dada.
    fun createFile(extension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${extension.uppercase()}_$timeStamp"
        return File.createTempFile(fileName, ".$extension", context.externalCacheDir)
    }

    // Función para crear una URI para un archivo.
    fun createFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        ).also { tempUri = it }
    }

    // Lanza el diálogo de permisos y ejecuta una acción si el permiso es concedido.
    val permissionHandler = rememberPermissionHandler(onGranted = { action ->
        when (action) {
            "photo" -> {
                val uri = createFileUri(createFile("jpg"))
                takePicture.launch(uri)
            }
            "video" -> {
                val uri = createFileUri(createFile("mp4"))
                captureVideo.launch(uri)
            }
            "audio" -> {
                isRecording = true
                audioFile = createFile("mp3")
                audioFile?.let { audioRecorder.start(it) }
            }
        }
    })

    // Maneja el botón de retroceso, limpiando los detalles de la nota antes de navegar hacia atrás.
    BackHandler {
        viewModel.clearNoteDetails()
        onNavigateBack()
    }

    // Efecto que se ejecuta al salir de la pantalla para detener la grabación de audio.
    DisposableEffect(Unit) {
        onDispose { audioRecorder.stop() }
    }

    // Estructura de la pantalla.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (noteId == null) R.string.add_note else R.string.edit_note)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearNoteDetails()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Área de contenido con scroll.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Campos de texto para el título y el contenido de la nota.
                OutlinedTextField(
                    value = noteDetails.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = noteDetails.content,
                    onValueChange = { viewModel.onContentChange(it) },
                    label = { Text(stringResource(R.string.content)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción para adjuntar archivos, tomar fotos, etc.
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { getContent.launch("*/*") }) {
                        Icon(Icons.Outlined.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.attach_file))
                    }
                    OutlinedButton(onClick = { permissionHandler(Manifest.permission.CAMERA, "photo") }) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.take_photo))
                    }
                    OutlinedButton(onClick = { permissionHandler(Manifest.permission.CAMERA, "video") }) {
                        Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.record_video))
                    }
                    if (!isRecording) {
                        OutlinedButton(onClick = { permissionHandler(Manifest.permission.RECORD_AUDIO, "audio") }) {
                            Icon(Icons.Outlined.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.start_recording))
                        }
                    } else {
                        FilledTonalButton(onClick = {
                            audioRecorder.stop()
                            isRecording = false
                            val uri = audioFile?.let { FileProvider.getUriForFile(context, "${context.packageName}.provider", it) }
                            viewModel.onAttachmentSelected(uri, "audio/mp3")
                        }) {
                            Icon(Icons.Outlined.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.stop_recording))
                        }
                    }
                }

                // Muestra la lista de adjuntos.
                val allAttachments = noteDetails.attachments.map { it.uri to it.type } + uiState.newAttachments.map { it.first.toString() to it.second }
                if (allAttachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Attachments:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                noteDetails.attachments.forEach { attachment ->
                    AttachmentItem(
                        uri = Uri.parse(attachment.uri),
                        type = attachment.type,
                        onAttachmentClick = { 
                            if (attachment.type?.startsWith("image/") == true || attachment.type?.startsWith("video/") == true || attachment.type?.startsWith("audio/") == true) {
                                onAttachmentClick(attachment.uri, attachment.type ?: "") 
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(attachment.uri), attachment.type)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No application can handle this file", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onRemoveClick = { viewModel.removeExistingAttachment(attachment) }
                    )
                }
                uiState.newAttachments.forEach { (uri, type) ->
                    AttachmentItem(
                        uri = uri,
                        type = type,
                        onAttachmentClick = { 
                            if (type?.startsWith("image/") == true || type?.startsWith("video/") == true || type?.startsWith("audio/") == true) {
                                onAttachmentClick(uri.toString(), type ?: "") 
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, type)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No application can handle this file", Toast.LENGTH_SHORT).show()
                                }
                            }
                         },
                        onRemoveClick = { viewModel.removeAttachment(uri) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Botón para guardar la nota.
            Button(
                onClick = {
                    viewModel.save()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

// Composable que representa un único elemento de adjunto en la lista.
@Composable
fun AttachmentItem(uri: Uri, type: String?, onAttachmentClick: () -> Unit, onRemoveClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onAttachmentClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (type?.startsWith("image/") == true || type?.startsWith("video/") == true) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Outlined.AttachFile, contentDescription = null, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = getFileName(context, uri), style = MaterialTheme.typography.bodyMedium)
        }
        // Botón para eliminar el adjunto.
        IconButton(onClick = onRemoveClick) {
            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.remove_attachment))
        }
    }
}
