package com.android.hciproject.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.hciproject.data.Comment
import com.android.hciproject.data.Post
import kotlinx.coroutines.launch

class PostDetailViewModel : ViewModel() {
    val post = MutableLiveData<Post>()

    fun fetchPost(p: Post) {
        viewModelScope.launch {
            post.value = p
        }
    }

    fun getPid(): String? {
        return post.value?.pid
    }

    fun insertComment(comment: Comment) {
        // insert 구현
    }
}