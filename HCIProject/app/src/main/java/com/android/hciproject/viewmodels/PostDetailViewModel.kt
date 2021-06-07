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

    val comments = MutableLiveData<ArrayList<Comment>>()

    val username: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val writingComment: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    fun fetchComments(){
        // 성윤
        // 댓글 불러오기
        // Post 객체 -> ListPostsQuery.Item 처럼 Comment도 바꾸면 됨!
        // comments에 넣기만하면 자동 업데이트 됨(ui)
        viewModelScope.launch {
            //comments.postValue("comment 리스트")
        }
    }

    fun fetchUsername(un: String) {
        viewModelScope.launch {
            username.postValue(un)
        }
    }

    fun updateLikes(like :Int){
        viewModelScope.launch {
            post.value!!.like.set(like)
        }
    }

    fun fetchPost(p: Post) {
        viewModelScope.launch {
//            post.postValue(p)
            post.value = p
        }
    }

    fun getPid(): String? {
        return post.value?.pid
    }

    fun insertComment(comment: Comment) {
        // insert 구현
        // writingComment가 내가 작성한 댓글임
        // string 접근 : writingComment.value!!
    }

    fun getTime(): String {
        val time = MyTimeUtils.getTimeDiff(MyTimeUtils.getTimeInMillis(post.value!!.uploadTime!!))
        return time.toString() + "시간 전"
    }
}