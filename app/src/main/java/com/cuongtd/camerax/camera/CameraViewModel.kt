package com.cuongtd.camerax.camera

import android.app.Activity
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel(val context: Activity) : ViewModel() {
    private val _cameraSelector = MutableLiveData(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector: LiveData<CameraSelector> = _cameraSelector

    private val _latestImageUri = MutableLiveData(fetchLatestImage())
    val latestImageUri: LiveData<String> = _latestImageUri


    fun onChangeCameraSelector(newCameraSelector: CameraSelector) {
        _cameraSelector.value = newCameraSelector
    }

    fun updateLastestImage() {
        _latestImageUri.value = fetchLatestImage();
    }

    fun fetchLatestImage(): String {
        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID
        ) //get all columns of type images
        val orderBy = MediaStore.Images.Media.DATE_TAKEN //order data by date
        val imageCursor = context.managedQuery(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
            null, "$orderBy ASC"
        ) //get all data in Cursor by sorting in DESC order

        if (imageCursor.count == 0) {
            return ""
        }

        imageCursor.moveToPosition(0)
        val dataColumnIndex =
            imageCursor.getColumnIndex(MediaStore.Images.Media.DATA) //get column index
        return imageCursor.getString(dataColumnIndex)
    }
}