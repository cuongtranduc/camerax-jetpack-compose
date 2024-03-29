package com.cuongtd.camerax

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.core.content.ContextCompat
import com.cuongtd.camerax.camera.CameraViewModel
import com.cuongtd.camerax.ui.CameraPreview
import com.cuongtd.camerax.ui.CaptureButton
import com.cuongtd.camerax.ui.theme.CameraXTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {
    var imageCapture: ImageCapture? = null

    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Permission.checkPermission(this.applicationContext, this);
        cameraViewModel = CameraViewModel(this)
        val x = cameraViewModel.fetchLatestImage();

        setContent {
            CameraXTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Scaffold(
                        floatingActionButton = {
                            CaptureButton(
                                cameraViewModel = cameraViewModel,
                                ::takePhoto,
                                ::flipCamera,
                            )
                        },
                        floatingActionButtonPosition = FabPosition.Center,
                    ) {
                        CameraPreview(
                            buildImageCapture = ::buildImageCapture,
                            cameraViewModel = cameraViewModel
                        )
                    }
                }
            }
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun buildImageCapture(_imageCapture: ImageCapture) {
        imageCapture = _imageCapture
    }

    fun flipCamera() {
        if (cameraViewModel.cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA) {
            cameraViewModel.onChangeCameraSelector(CameraSelector.DEFAULT_FRONT_CAMERA)
        } else {
            cameraViewModel.onChangeCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA)

        }
    }

    fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, savedUri))
                    cameraViewModel.updateLastestImage()
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Permission.REQUEST_CODE_PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                // Take the user to the success fragment when permission is granted
                Toast.makeText(
                    this.applicationContext,
                    "Permission request granted",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this.applicationContext,
                    "Permission request denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
