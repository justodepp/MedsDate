package com.medsdate.ui.add

import android.Manifest
import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.medsdate.ui.components.MedsAppBar
import com.medsdate.ui.components.MedsBottomNavigation
import com.medsdate.ui.theme.MedsDateTheme
import com.medsdate.utils.ImageUtils
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Add/Edit medicine screen with form validation.
 *
 * @param navController Navigation controller for bottom nav
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
    navController: NavController,
    medicineId: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit = {},
    capturedImageUri: String? = null,
    onSaveSuccess: () -> Unit,
    viewModel: AddEditViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Handle captured image from camera
    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let { uriString ->
            // Extract file path from URI (camera returns file:// URIs)
            val path = if (uriString.startsWith("file://")) {
                uriString.removePrefix("file://")
            } else {
                uriString
            }
            viewModel.onImagePathChange(path)
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

    // Gallery launcher - copies selected image to app storage
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            // Copy the image to app storage so it persists
            val copiedPath = ImageUtils.copyImageToAppStorage(context, selectedUri)
            copiedPath?.let { path ->
                viewModel.onImagePathChange(path)
            }
        }
    }

    Scaffold(
        topBar = {
            MedsAppBar(
                title = if (uiState.isEditMode) "Edit Medicine" else "Add Medicine",
                showAppIcon = !uiState.isEditMode,
                showNavigationIcon = uiState.isEditMode,
                onNavigationClick = if (uiState.isEditMode) onNavigateBack else null
            )
        },
        bottomBar = {
            MedsBottomNavigation(navController = navController)
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
                shape = RoundedCornerShape(16.dp),
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
                shape = RoundedCornerShape(16.dp),
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
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 0.dp
                ),
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

/**
 * Preview for ExpiryDatePicker.
 */
@Preview(showBackground = true)
@Composable
private fun ExpiryDatePickerPreview() {
    MaterialTheme {
        ExpiryDatePicker(
            selectedDate = Date(),
            onDateSelected = {},
            error = null
        )
    }
}

/**
 * Preview for PhotoSection.
 */
@Preview(showBackground = true)
@Composable
private fun PhotoSectionPreview() {
    MaterialTheme {
        PhotoSection(
            imagePath = null,
            onCameraClick = {},
            onGalleryClick = {}
        )
    }
}

/**
 * Preview for form content (Add mode).
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddEditFormPreview() {
    MedsDateTheme {
        Scaffold(
            topBar = {
                MedsAppBar(
                    title = "Add Medicine",
                    showAppIcon = true,
                    showNavigationIcon = false
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
                // Name field
                OutlinedTextField(
                    value = "Aspirin",
                    onValueChange = {},
                    label = { Text("Medicine Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Expiry date
                OutlinedTextField(
                    value = "Jan 15, 2026",
                    onValueChange = {},
                    label = { Text("Expiry Date *") },
                    trailingIcon = {
                        Icon(Icons.Outlined.Today, contentDescription = "Calendar")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )

                // Photo section
                Text(
                    text = "Photo (Optional)",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera")
                    }
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Gallery")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery")
                    }
                }

                // Notes field
                OutlinedTextField(
                    value = "Take with food",
                    onValueChange = {},
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                // Save button
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Medicine")
                }
            }
        }
    }
}

/**
 * Preview for form content (Edit mode with back button).
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditFormPreview() {
    MedsDateTheme {
        Scaffold(
            topBar = {
                MedsAppBar(
                    title = "Edit Medicine",
                    showAppIcon = false,
                    showNavigationIcon = true,
                    onNavigationClick = {}
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
                // Name field
                OutlinedTextField(
                    value = "Aspirin",
                    onValueChange = {},
                    label = { Text("Medicine Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Expiry date
                OutlinedTextField(
                    value = "Jan 15, 2026",
                    onValueChange = {},
                    label = { Text("Expiry Date *") },
                    trailingIcon = {
                        Icon(Icons.Outlined.Today, contentDescription = "Calendar")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )

                // Photo section
                Text(
                    text = "Photo (Optional)",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera")
                    }
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Gallery")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery")
                    }
                }

                // Notes field
                OutlinedTextField(
                    value = "Take with food",
                    onValueChange = {},
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                // Update button (in edit mode)
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Medicine")
                }
            }
        }
    }
}

/**
 * Preview for form with errors.
 */
@Preview(showBackground = true)
@Composable
private fun AddEditFormWithErrorsPreview() {
    MedsDateTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name field with error
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Medicine Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = true,
                supportingText = { Text("Name is required") }
            )

            // Expiry date with error
            OutlinedTextField(
                value = "Dec 31, 2023",
                onValueChange = {},
                label = { Text("Expiry Date *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = true,
                supportingText = { Text("Expiry date must be in the future") }
            )
        }
    }
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
        trailingIcon = {
            Icon(Icons.Outlined.Today, contentDescription = "Calendar")
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        shape = RoundedCornerShape(16.dp),
        readOnly = true,
        enabled = false,
        isError = error != null,
        supportingText = error?.let { { Text(it) } }
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
    var showChooserDialog by remember { mutableStateOf(false) }

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

        OutlinedButton(
            onClick = {
                showChooserDialog = showChooserDialog.not()
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.AddAPhoto,
                    contentDescription = "Camera",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Camera",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    "Scatta o carica una foto dalla libreria",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Camera and Gallery buttons
        if (showChooserDialog) {
            AlertDialog(
                onDismissRequest = {
                    showChooserDialog = false
                },
                title = {
                    Text("What's your choice?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        onCameraClick()
                        showChooserDialog = false
                    }) {
                        Text("Camera")
                    }
                },
                dismissButton =
                    {
                        TextButton(onClick = {
                            onGalleryClick()
                            showChooserDialog = false
                        }) {
                            Text("Gallery")
                        }
                    }
            )
        }
    }
}
