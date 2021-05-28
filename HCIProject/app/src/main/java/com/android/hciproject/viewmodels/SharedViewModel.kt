package com.android.hciproject.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.hciproject.data.Comment
import com.android.hciproject.data.Post
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {

    var searchWord = MutableLiveData<String>()

    val postList = MutableLiveData<ArrayList<Post>>()

    val mLocationSource = MutableLiveData<FusedLocationSource>()

    val latLng = MutableLiveData<LatLng>()

    var selectedPost = MutableLiveData<Post>()

    val selectedOverlaySize = MutableLiveData<Double>().apply {
        value = 1000.0
    }

    init {
        viewModelScope.launch {
            val comments = ArrayList<Comment>()
            comments.add(Comment("uname","content","uplaodtime"))
            comments.add(Comment("uname2","content","uplaodtime"))
            comments.add(Comment("uname3","content","uplaodtime"))
            comments.add(Comment("uname4","content","uplaodtime"))
            val list = ArrayList<Post>()
            val lat = LatLng(
                37.54225941463205,
                127.07629578159484
            )
            latLng.postValue(lat)
            list.add(
                Post(
                    1,
                    "청심대",
                    "img",
                    "김성윤",
                    "건국대학교의 청심대에요",
                    "time",
                    37.54225941463205,
                    127.07629578159484,
                    comments,
                    5
                )
            )
            list.add(
                Post(
                    2,
                    "일감호",
                    "img",
                    "김인애",
                    "건국대학교의 일감호에요.",
                    "2021.05.24.",
                    37.54125941463205,
                    127.07629578159484,
                    comments,
                    0
                )
            )
            list.add(
                Post(
                    4,
                    "타이틀",
                    "img",
                    "박현우",
                    "내용입니다",
                    "time",
                    37.54725941463205,
                    127.07629578159484,
                    comments,
                    0
                )
            )
            postList.postValue(list)

            searchWord.value = ""
        }

    }

    fun fetchTotalPostFromDb(){
        viewModelScope.launch {
            // post list 가져오기!
        }
    }
}