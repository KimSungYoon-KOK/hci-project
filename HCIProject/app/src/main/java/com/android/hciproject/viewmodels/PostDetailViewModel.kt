package com.android.hciproject.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.amplify.generated.graphql.ListCommentsQuery
import com.amazonaws.amplify.generated.graphql.OnCreateCommentSubscription
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.android.hciproject.ClientFactory
import com.android.hciproject.data.Comment
import com.android.hciproject.data.Post
import com.android.hciproject.utils.MyTimeUtils
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.launch

class PostDetailViewModel : ViewModel() {
    val post = MutableLiveData<Post>()

    val comments = MutableLiveData<ArrayList<ListCommentsQuery.Item>?>()

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

    fun deleteComment(){
        viewModelScope.launch {
            writingComment.postValue("")
        }
    }

    fun fetchComments(clientFactory: ClientFactory){
        viewModelScope.launch {
            updateCommentList(clientFactory)
            subscribe(clientFactory)
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

    // Comment
    fun updateCommentList(clientFactory: ClientFactory){
        clientFactory.appSyncClient()
            .query(ListCommentsQuery.builder().build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(queryCallback)
    }

    private val queryCallback: GraphQLCall.Callback<ListCommentsQuery.Data> =
        object : GraphQLCall.Callback<ListCommentsQuery.Data>() {
            override fun onResponse(response: Response<ListCommentsQuery.Data>) {
                // 전체 댓글 다 불러와서 포스트 아이디 맞춰서 추가
                if (response.data() != null) {
                    if (response.data() != null) {
                        val commentList = ArrayList<ListCommentsQuery.Item>()
                        for (item in ArrayList(response.data()!!.listComments()!!.items())) {
                            if (getPid() == item.postID()) {
                                commentList.add(item)
                            }
                        }
                        comments.postValue(commentList)
                    }
                }
            }

            override fun onFailure(e: ApolloException) {
                Log.e(ContentValues.TAG, e.toString())
            }
        }

    private lateinit var subscriptionWatcher: AppSyncSubscriptionCall<OnCreateCommentSubscription.Data>

    private fun subscribe(clientFactory: ClientFactory) {
        val subscription: OnCreateCommentSubscription = OnCreateCommentSubscription.builder().build()
        subscriptionWatcher = clientFactory.appSyncClient().subscribe(subscription)
        subscriptionWatcher.execute(subCallback)
    }

    private val subCallback: AppSyncSubscriptionCall.Callback<OnCreateCommentSubscription.Data> =
        object : AppSyncSubscriptionCall.Callback<OnCreateCommentSubscription.Data> {
            override fun onResponse(response: Response<OnCreateCommentSubscription.Data>) {
                Log.d("Subscription_Comments", response.data().toString())
                if (response.data() != null) {
                    // Update UI with the newly added item
                    val data = response.data()!!.onCreateComment()
                    val addedItem = ListCommentsQuery.Item(
                        data!!.__typename(),
                        data.id(),
                        data.postID(),
                        data.content(),
                        data.uname()!!,
                        data.createdAt(),
                        data.updatedAt()
                    )
                    viewModelScope.launch {
                        comments.value?.add(addedItem)
                    }
                }
            }

            override fun onFailure(e: ApolloException) {
                Log.e("Subscription_Comments", e.toString())
            }

            override fun onCompleted() {
                Log.i("Subscription_Comments", "Subscription completed")
            }
        }
}