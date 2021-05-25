package com.android.hciproject.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val username: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }
    private val _loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            postValue(false)
        }
    }
    val loading get() = _loading

    val eventBit = MutableLiveData(0b00)

    fun updateEvent(position: Int) {
        val event = eventBit.value ?: 0
        eventBit.value = event or position
    }

    fun login() {
        val username = username.value
        _loading.value = true
    }

}