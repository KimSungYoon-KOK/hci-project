package com.android.hciproject.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.hciproject.data.Post
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.launch

class WriteContentViewModel : ViewModel() {
    val imageUri = MutableLiveData<Uri>()

    val username: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val content: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val title: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val uploadLatLng = MutableLiveData<LatLng>()

    fun fetchImageUri(u: Uri) {
        viewModelScope.launch {
            imageUri.postValue(u)
        }
    }

    fun fetchLatLng(latLng: LatLng) {
        viewModelScope.launch {
            uploadLatLng.postValue(latLng)
        }
    }

    fun fetchUserName(un: String) {
        viewModelScope.launch {
            username.postValue(un)
        }
    }

    fun insertPost() {


    }
}