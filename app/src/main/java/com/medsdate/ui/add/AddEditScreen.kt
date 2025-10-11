package com.medsdate.ui.add

import android.Manifest
import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.medsdate.ui.components.MedsAppBar
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Add/Edit medicine screen with form validation.
 *
 * @param medicineId Medicine ID for edit mode, null for add mode
 * @param onNavigateBack Callback to navigate back
 * @param onNavigateToCamera Callback to navigate to camera screen
 * @param capturedImageUri URI of captured image from camera (from savedStateHandle)
 * @param onSaveSuccess Callback when save is successful
 * @param viewModel The AddEditViewModel
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddEditScreen(
    medicineId: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit = {},
    capturedImageUri: String? = null,
    onSaveSuccess: () -> Unit,
    viewModel: AddEditViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle captured image from camera
    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let { uri ->
            viewModel.onImagePathChange(uri)
        }
    }

    // Load medicine if editing
    LaunchedEffect(medicineId) {
        medicineId?.let { viewModel.loadMedicine(it) }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditEvent.SaveSuccess -> onSaveSuccess()
                is AddEditEvent.SaveError -> {
                    // Show error snackbar (handled by parent)
                }
            }
        }
    }

    // Permission state
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    )

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImagePathChange(it.toString()) }
    }

    Scaffold(
        topBar = {
            MedsAppBar(
                title = if (uiState.isEditMode) "Edit Medicine" else "Add Medicine",
                onNavigationClick = onNavigateBack,
                showNavigationIcon = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name field (required)
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Medicine Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )

            // Expiry date picker (required)
            ExpiryDatePicker(
                selectedDate = uiState.expiryDate,
                onDateSelected = viewModel::onExpiryDateChange,
                error = uiState.expiryDateError
            )

            // Photo section (optional)
            PhotoSection(
                imagePath = uiState.imagePath,
                onCameraClick = {
                    if (permissionsState.allPermissionsGranted) {
                        onNavigateToCamera()
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                onGalleryClick = {
                    galleryLauncher.launch("image/*")
                }
            )

            // Notes field (optional)
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = viewModel::saveMedicine,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (uiState.isEditMode) "Update Medicine" else "Save Medicine")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditScreenPreview() {
    AddEditScreen(
        medicineId = 1,
        onNavigateBack = {},
        onSaveSuccess = {}
    )
}

/**
 * Expiry date picker field.
 */
@Composable
private fun ExpiryDatePicker(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    error: String?
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val datePickerDialog = remember {
        val calendar = Calendar.getInstance().apply {
            time = selectedDate
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                onDateSelected(newDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Only allow future dates
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    OutlinedTextField(
        value = dateFormatter.format(selectedDate),
        onValueChange = {},
        label = { Text("Expiry Date *") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        readOnly = true,
        enabled = false,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * Photo section with camera and gallery options.
 */
@Composable
private fun PhotoSection(
    imagePath: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Photo (Optional)",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Image preview or placeholder
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Medicine photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Camera and Gallery buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCameraClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Camera")
            }

            OutlinedButton(
                onClick = onGalleryClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Image, contentDescription = "Gallery")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gallery")
            }
        }
    }
}
