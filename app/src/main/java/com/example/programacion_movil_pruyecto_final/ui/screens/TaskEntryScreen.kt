package com.example.programacion_movil_pruyecto_final.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.data.Reminder
import com.example.programacion_movil_pruyecto_final.media.AudioRecorder
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel
import com.example.programacion_movil_pruyecto_final.utils.rememberPermissionHandler
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Composable que representa la pantalla para crear o editar una tarea.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEntryScreen(
    application: NotesAndTasksApplication,
    onNavigateBack: () -> Unit, // Lógica para navegar hacia atrás.
    onAttachmentClick: (String, String) -> Unit, // Lógica para manejar el clic en un adjunto.
    taskId: Int? = null // ID de la tarea a editar (nulo si se crea una nueva).
) {
    // Obtiene una instancia del ViewModel para esta pantalla.
    val viewModel: TasksViewModel = viewModel(factory = ViewModelFactory(application, application.notesRepository, application.tasksRepository))
    // Obtiene el estado de la UI desde el ViewModel.
    val uiState by viewModel.uiState.collectAsState()
    val taskDetails = uiState.taskDetails

    // Efecto que se ejecuta cuando el ID de la tarea cambia, para preparar los detalles de la tarea.
    LaunchedEffect(taskId) {
        viewModel.prepareForEntry(taskId)
    }

    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    // Función para copiar un archivo desde una URI a almacenamiento interno.
    fun copyUriToInternalStorage(uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = com.example.programacion_movil_pruyecto_final.utils.getFileName(context, uri)
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
            "notifications" -> {
                viewModel.save()
                onNavigateBack()
            }
        }
    })

    // --- Lógica de los diálogos de recordatorio ---
    val calendar = Calendar.getInstance()
    var tempDate by remember { mutableStateOf<String?>(null) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }

    // Diálogo para seleccionar la hora del recordatorio.
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            tempDate?.let {
                val newTime = "%02d:%02d".format(hourOfDay, minute)
                reminderToEdit?.let {
                    viewModel.onReminderEdited(it, tempDate!!, newTime)
                } ?: viewModel.addReminder(it, newTime)
            }
            tempDate = null
            reminderToEdit = null
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    // Diálogo para seleccionar la fecha del recordatorio.
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            tempDate = "%d-%02d-%02d".format(year, month + 1, dayOfMonth)
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    // Función para mostrar los diálogos de fecha y hora.
    fun showReminderDialog(reminder: Reminder? = null) {
        reminderToEdit = reminder
        datePickerDialog.show()
    }

    // Maneja el botón de retroceso, limpiando los detalles de la tarea antes de navegar hacia atrás.
    BackHandler {
        viewModel.clearTaskDetails()
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
                title = { Text(stringResource(if (taskId == null) R.string.add_task else R.string.edit_task)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearTaskDetails()
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
                // Campos de texto para el título y el contenido de la tarea.
                OutlinedTextField(
                    value = taskDetails.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = taskDetails.content,
                    onValueChange = { viewModel.onContentChange(it) },
                    label = { Text(stringResource(R.string.content)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox para marcar la tarea como completada.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = taskDetails.isCompleted, onCheckedChange = { viewModel.onCompletedChange(it) })
                    Text(text = stringResource(R.string.completed))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Sección de Recordatorios ---
                Text("Reminders", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Muestra la lista de recordatorios.
                taskDetails.reminders.forEach { reminder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReminderDialog(reminder) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${reminder.date} at ${reminder.time}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { viewModel.removeReminder(reminder) }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.remove_attachment))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Botón para añadir un nuevo recordatorio.
                OutlinedButton(onClick = { showReminderDialog() }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_reminder))
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                val allAttachments = taskDetails.attachments.map { it.uri to it.type } + uiState.newAttachments.map { it.first.toString() to it.second }
                if (allAttachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Attachments:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                taskDetails.attachments.forEach { attachment ->
                    AttachmentItem(
                        uri = Uri.parse(attachment.uri),
                        type = attachment.type,
                        onAttachmentClick = { 
                            if (attachment.type.startsWith("image/") || attachment.type.startsWith("video/") || attachment.type.startsWith("audio/")) {
                                onAttachmentClick(attachment.uri, attachment.type)
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
            // Botón para guardar la tarea.
            Button(
                onClick = {
                    // Si hay recordatorios, solicita permiso para notificaciones antes de guardar.
                    if (taskDetails.reminders.isNotEmpty()) {
                        permissionHandler(Manifest.permission.POST_NOTIFICATIONS, "notifications")
                    } else {
                        viewModel.save()
                        onNavigateBack()
                    }
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
