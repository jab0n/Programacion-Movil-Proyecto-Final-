package com.example.programacion_movil_pruyecto_final.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(application: NotesAndTasksApplication, onNavigateBack: () -> Unit) {
    val viewModel: TasksViewModel = viewModel(factory = ViewModelFactory(application.notesRepository, application.tasksRepository))
    val uiState by viewModel.uiState.collectAsState()
    val taskDetails = uiState.taskDetails

    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val type = uri?.let { context.contentResolver.getType(it) }
        viewModel.onAttachmentSelected(uri, type)
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

    DisposableEffect(Unit) {
        onDispose { 
            viewModel.clearTaskDetails()
            audioRecorder.stop()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_task)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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

            if (uiState.newAttachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Attachments:")
                uiState.newAttachments.forEach { (uri, _) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                        Text(text = uri.path?.substringAfterLast('/') ?: "unknown file")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                viewModel.insert()
                onNavigateBack()
            }) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
