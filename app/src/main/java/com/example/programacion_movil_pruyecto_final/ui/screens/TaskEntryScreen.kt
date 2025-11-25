package com.example.programacion_movil_pruyecto_final.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.programacion_movil_pruyecto_final.NotesAndTasksApplication
import com.example.programacion_movil_pruyecto_final.R
import com.example.programacion_movil_pruyecto_final.ViewModelFactory
import com.example.programacion_movil_pruyecto_final.media.AudioRecorder
import com.example.programacion_movil_pruyecto_final.ui.viewmodels.TasksViewModel
import com.example.programacion_movil_pruyecto_final.utils.getFileName
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEntryScreen(
    application: NotesAndTasksApplication, 
    onNavigateBack: () -> Unit,
    taskId: Int? = null
) {
    val viewModel: TasksViewModel = viewModel(factory = ViewModelFactory(application, application.notesRepository, application.tasksRepository))
    val uiState by viewModel.uiState.collectAsState()
    val taskDetails = uiState.taskDetails

    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.loadTaskForEditing(taskId)
        } else {
            viewModel.clearTaskDetails()
        }
    }

    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    fun copyUriToInternalStorage(uri: Uri, type: String?): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context, uri)
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val type = context.contentResolver.getType(it)
            val newUri = copyUriToInternalStorage(it, type)
            viewModel.onAttachmentSelected(newUri, type)
        }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val type = tempUri?.let { context.contentResolver.getType(it) }
            viewModel.onAttachmentSelected(tempUri, type)
        }
    }

    val captureVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            val type = tempUri?.let { context.contentResolver.getType(it) }
            viewModel.onAttachmentSelected(tempUri, type)
        }
    }

    fun createFile(extension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${extension.uppercase()}_${timeStamp}_"
        return File.createTempFile(fileName, ".$extension", context.externalCacheDir)
    }

    fun createFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        ).also { tempUri = it }
    }

    var actionToLaunch by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            when (actionToLaunch) {
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
        }
        actionToLaunch = null
    }

    fun launchWithPermission(permission: String, action: String) {
        when (ContextCompat.checkSelfPermission(context, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
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
            }
            else -> {
                actionToLaunch = action
                permissionLauncher.launch(permission)
            }
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            viewModel.onDateChange("%d-%02d-%02d".format(year, month + 1, dayOfMonth))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            viewModel.onTimeChange("%02d:%02d".format(hourOfDay, minute))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    BackHandler {
        viewModel.clearTaskDetails()
        onNavigateBack()
    }

    DisposableEffect(Unit) {
        onDispose { audioRecorder.stop() }
    }

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
                .padding(16.dp)
        ) {
            // Scrollable content area
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = taskDetails.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskDetails.content,
                    onValueChange = { viewModel.onContentChange(it) },
                    label = { Text(stringResource(R.string.content)) },
                    modifier = Modifier.fillMaxWidth().height(150.dp) // Give it a substantial default size
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val displayDate = remember(taskDetails.date) {
                        val parts = taskDetails.date.split("-")
                        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else taskDetails.date
                    }
                    Button(onClick = { datePickerDialog.show() }) {
                        Text(text = displayDate.ifEmpty { stringResource(R.string.select_date) })
                    }
                    Button(onClick = { timePickerDialog.show() }) {
                        Text(text = taskDetails.time.ifEmpty { stringResource(R.string.select_time) })
                    }
                }
                 Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = taskDetails.isCompleted, onCheckedChange = { viewModel.onCompletedChange(it) })
                    Text(text = stringResource(R.string.completed))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { getContent.launch("*/*") }) {
                        Text(text = stringResource(R.string.attach_file))
                    }
                    Button(onClick = { launchWithPermission(Manifest.permission.CAMERA, "photo") }) {
                        Text(text = stringResource(R.string.take_photo))
                    }
                    Button(onClick = { launchWithPermission(Manifest.permission.CAMERA, "video") }) {
                        Text(text = stringResource(R.string.record_video))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                     if (!isRecording) {
                        Button(onClick = { launchWithPermission(Manifest.permission.RECORD_AUDIO, "audio") }) {
                            Text(text = stringResource(R.string.start_recording))
                        }
                    } else {
                        Button(onClick = { 
                            audioRecorder.stop()
                            isRecording = false
                            val uri = audioFile?.let { FileProvider.getUriForFile(context, "${context.packageName}.provider", it) }
                            viewModel.onAttachmentSelected(uri, "audio/mp3")
                        }) {
                            Text(text = stringResource(R.string.stop_recording))
                        }
                    }
                }

                // Display attachments
                if (taskDetails.attachments.isNotEmpty() || uiState.newAttachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Attachments:")
                    taskDetails.attachments.forEach { attachment ->
                         Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(text = getFileName(context, Uri.parse(attachment.uri)), modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.removeExistingAttachment(attachment) }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove_attachment))
                            }
                        }
                    }
                    uiState.newAttachments.forEach { (uri, _) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(text = getFileName(context, uri), modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.removeAttachment(uri) }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove_attachment))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                viewModel.save()
                onNavigateBack()
            }) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
