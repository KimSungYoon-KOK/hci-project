package com.android.hciproject.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.hciproject.data.Post
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    var searchWord = MutableLiveData<String>()
    private val _postList = MutableLiveData<ArrayList<Post>>()
    val postList:LiveData<ArrayList<Post>> get() = _postList
    var mLocationSource = MutableLiveData<FusedLocationSource>()
    var selectedPost = MutableLiveData<Post>()

    init {
        viewModelScope.launch {
            val list = ArrayList<Post>()
            list.add(Post(1, "청심대", "img", "김성윤", "건국대학교의 청심대에요", "time", 37.54225941463205, 127.07629578159484,null))
            list.add(Post(2, "일감호", "img", "김인애", "건국대학교의 일감호에요.", "2021.05.24.", 37.54125941463205, 127.07629578159484,null))
            list.add(Post(4, "타이틀", "img", "박현우", "내용입니다", "time", 37.54725941463205, 127.07629578159484,null))
            _postList.postValue(list)

            searchWord.value = ""
        }

    }
}