package com.android.hciproject.viewmodels

import androidx.appcompat.widget.SearchView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val searchView: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

//    @BindingAdapter("queryTextListener")
//    fun SearchView.OnQueryTextListener(
//        searchView: SearchView,
//        listener: SearchView.OnQueryTextListener
//    ) {
//        searchView.setOnQueryTextListener()
//    }


}