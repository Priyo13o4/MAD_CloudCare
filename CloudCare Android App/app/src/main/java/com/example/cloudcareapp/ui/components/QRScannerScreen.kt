package com.example.cloudcareapp.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import java.util.concurrent.Executors

/**
 * QR Scanner overlay component with camera preview
 * Shows a camera feed with a scanning frame overlay
 */
@Composable
fun QRScannerOverlay(
    onDismiss: () -> Unit,
    onQRScanned: (decodedValue: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.CAMERA
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            permissionLauncher.launch(permission)
        }
    }

    if (hasCameraPermission) {
        LaunchedEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    // Unbind all usecases before binding new ones
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (exc: Exception) {
                    Log.e("QRScanner", "Camera setup failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Scanner overlay with frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val frameSize = 250.dp.toPx()
                    val frameLeft = (size.width - frameSize) / 2
                    val frameTop = (size.height - frameSize) / 2

                    // Draw semi-transparent overlay
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        size = Size(frameLeft, size.height)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(frameLeft + frameSize, 0f),
                        size = Size(size.width - frameLeft - frameSize, size.height)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(frameLeft, 0f),
                        size = Size(frameSize, frameTop)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(frameLeft, frameTop + frameSize),
                        size = Size(frameSize, size.height - frameTop - frameSize)
                    )

                    // Draw frame border
                    drawRect(
                        color = Color.Green,
                        topLeft = Offset(frameLeft, frameTop),
                        size = Size(frameSize, frameSize),
                        style = Stroke(width = 4f)
                    )
                }
        )

        // Close button with enhanced visibility
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.CircleShape)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Exit Scanner",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Instruction text with back indicator
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Point camera at QR code",
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Tap X to exit",
                color = Color.White.copy(alpha = 0.7f),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }
}
