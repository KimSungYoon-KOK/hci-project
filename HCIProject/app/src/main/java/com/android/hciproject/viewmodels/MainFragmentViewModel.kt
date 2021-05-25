package com.android.hciproject.viewmodels

import androidx.appcompat.widget.SearchView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainFragmentViewModel : ViewModel() {
    private val searchWord: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    fun getSearchWord(): LiveData<String> {
        return searchWord
    }

//    @BindingAdapter("queryTextListener")
//    fun SearchView.OnQueryTextListener(
//        searchView: SearchView,
//        listener: SearchView.OnQueryTextListener
//    ) {
//        searchView.setOnQueryTextListener()
//    }


}