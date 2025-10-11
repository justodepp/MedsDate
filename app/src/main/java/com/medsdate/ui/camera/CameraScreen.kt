package com.medsdate.ui.camera

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Camera screen for taking medicine photos using CameraX.
 *
 * @param onNavigateBack Callback to navigate back
 * @param onImageCaptured Callback when image is captured with file URI
 */
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onImageCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var isCameraInitialized by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    // Initialize camera when screen loads
    LaunchedEffect(lensFacing) {
        try {
            val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
                val listenableFuture = ProcessCameraProvider.getInstance(context)
                listenableFuture.addListener(
                    { continuation.resume(listenableFuture.get()) },
                    ContextCompat.getMainExecutor(context)
                )
            }

            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()

            // Build preview use case
            val preview = Preview.Builder().build()

            // Build image capture use case
            val imageCaptureBuilder = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            imageCapture = imageCaptureBuilder.build()

            // Select camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            // Bind use cases to camera
            previewView?.let { pv ->
                preview.setSurfaceProvider(pv.surfaceProvider)
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                isCameraInitialized = true
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize camera")
        }
    }

    Scaffold(
        topBar = {
            CameraAppBar(
                onNavigateBack = onNavigateBack,
                onFlipCamera = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Camera preview
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        previewView = this
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Capture button at bottom
            if (isCameraInitialized) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                ) {
                    CaptureButton(
                        isCapturing = isCapturing,
                        onClick = {
                            imageCapture?.let { capture ->
                                isCapturing = true
                                takePicture(
                                    context = context,
                                    imageCapture = capture,
                                    onImageCaptured = { uri ->
                                        isCapturing = false
                                        onImageCaptured(uri)
                                    },
                                    onError = { error ->
                                        isCapturing = false
                                        Timber.e(error, "Image capture failed")
                                    }
                                )
                            }
                        }
                    )
                }
            } else {
                // Loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Top app bar for camera screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraAppBar(
    onNavigateBack: () -> Unit,
    onFlipCamera: () -> Unit
) {
    TopAppBar(
        title = { Text("Take Photo") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onFlipCamera) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip Camera",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black,
            titleContentColor = Color.White
        )
    )
}

/**
 * Capture button component.
 */
@Composable
private fun CaptureButton(
    isCapturing: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = !isCapturing,
        modifier = Modifier
            .size(72.dp)
            .border(4.dp, Color.White, CircleShape)
            .background(Color.White.copy(alpha = 0.3f), CircleShape)
    ) {
        if (isCapturing) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Takes a picture and saves it to app's private storage.
 */
private fun takePicture(
    context: android.content.Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // Create output file
    val photoFile = createImageFile(context)

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                Timber.d("Image saved successfully: $savedUri")
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                Timber.e(exception, "Image capture failed")
                onError(exception)
            }
        }
    )
}

/**
 * Creates a file for storing the captured image.
 */
private fun createImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "MEDICINE_$timeStamp"
    val storageDir = File(context.filesDir, "images")

    // Create directory if it doesn't exist
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }

    return File(storageDir, "$imageFileName.jpg")
}
