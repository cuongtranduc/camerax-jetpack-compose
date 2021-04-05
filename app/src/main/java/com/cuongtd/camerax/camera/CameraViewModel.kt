package com.cuongtd.camerax.camera

import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
    private val _cameraSelector = MutableLiveData(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector: LiveData<CameraSelector> = _cameraSelector

    fun onChangeCameraSelector(newCameraSelector: CameraSelector) {
        _cameraSelector.value = newCameraSelector
    }
}