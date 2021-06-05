package com.android.hciproject.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.hciproject.data.Comment
import com.android.hciproject.data.Post
import com.android.hciproject.utils.MyTimeUtils
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

    fun getTime(): String {
        val time = MyTimeUtils.getTimeDiff(MyTimeUtils.getTimeInMillis(post.value!!.uploadTime!!))
        return time.toString() + "시간 전"
    }
}