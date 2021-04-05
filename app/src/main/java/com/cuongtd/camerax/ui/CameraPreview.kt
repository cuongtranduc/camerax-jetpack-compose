package com.cuongtd.camerax.ui

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.reflect.KFunction1
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LiveData
import com.cuongtd.camerax.camera.CameraViewModel

@Composable
fun CameraPreview(
    buildImageCapture: KFunction1<ImageCapture, Unit>,
    modifier: Modifier = Modifier,
    cameraViewModel: CameraViewModel,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraSelector: CameraSelector by cameraViewModel.cameraSelector.observeAsState(
        CameraSelector.DEFAULT_BACK_CAMERA
    )

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Preview is incorrectly scaled in Compose on some devices without this
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageCapture = ImageCapture.Builder()
                    .build()
                buildImageCapture(imageCapture)

                val _cameraSelector = CameraSelector.Builder().apply {
                    requireLensFacing(
                        when (cameraSelector) {
                            CameraSelector.DEFAULT_BACK_CAMERA -> CameraSelector.LENS_FACING_BACK
                            CameraSelector.DEFAULT_FRONT_CAMERA -> CameraSelector.LENS_FACING_FRONT
                            else -> throw IllegalStateException("Back and front camera are unavailable")
                        }
                    )
                }.build()

                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, _cameraSelector, preview, imageCapture
                    )
                } catch (exc: Exception) {
                    Log.e("TAG", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        })
}