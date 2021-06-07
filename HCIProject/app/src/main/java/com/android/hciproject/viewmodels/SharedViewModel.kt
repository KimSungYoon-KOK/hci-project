package com.android.hciproject.viewmodels

import android.content.ContentValues
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.amazonaws.amplify.generated.graphql.OnCreatePostSubscription
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.android.hciproject.ClientFactory
import com.android.hciproject.data.Post
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.launch
import java.io.IOException

class SharedViewModel : ViewModel() {

    var writingPostImageID = MutableLiveData<String>()

    var loginUserName = MutableLiveData<String>()

    var searchWord = MutableLiveData<String>()

    val postList = MutableLiveData<ArrayList<ListPostsQuery.Item>>()

    val latLng = MutableLiveData<LatLng>()

    var selectedPost = MutableLiveData<ListPostsQuery.Item>()

    val selectedOverlaySize = MutableLiveData<Double>().apply {
        value = 1000.0
    }

    val address: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    init {
        viewModelScope.launch {
            val lat = LatLng(
                37.54225941463205,
                127.07629578159484
            )
            latLng.postValue(lat)
            searchWord.value = ""
        }

    }

    fun setWritingPostImageID(id: String) {
        viewModelScope.launch {
            writingPostImageID.postValue(id)
        }
    }

    fun fetchSelectedOverlaySize(overlaySize: Double) {
        viewModelScope.launch {
            selectedOverlaySize.postValue(overlaySize)
        }
    }

    fun fetchSelectedLatLng(selectLatLng: LatLng) {
        viewModelScope.launch {
            latLng.postValue(selectLatLng)
        }
    }

    fun selectPost(post: Post) {
        viewModelScope.launch {
//            selectedPost.postValue(post)
        }
    }

    fun fetchLoginUserName(name: String) {
        viewModelScope.launch {
            loginUserName.postValue(name)
        }
    }

    fun fetchSharedData() {
        viewModelScope.launch {
            latLng.postValue(
                LatLng(
                    37.54225941463205,
                    127.07629578159484
                )
            )
        }
    }

    fun fetchTotalPostFromDb() {
        viewModelScope.launch {
            // post list 가져오기!
        }
    }

    fun fetchAddressFromLocation(context: Context) {
        var address: List<Address>? = null
        val g = Geocoder(context)
        try {
            address = g.getFromLocation(latLng.value!!.latitude, latLng.value!!.longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("SharedViewModel", "입출력 오류")
        }
        if (address != null) {
            var addr = address[0].getAddressLine(0)
            if (addr.startsWith("대한민국 ")) {
                addr = addr.replace("대한민국 ", "")
            }
            this.address.postValue(addr)
        } else
            this.address.postValue("위치를 입력하세요")
    }

    fun fetchDB(clientFactory: ClientFactory) {
        viewModelScope.launch {
            updatePostList(clientFactory)
            subscribe(clientFactory)

            if (postList.value.isNullOrEmpty())
                Log.d("postListSize", "null")
        }
    }

    fun updatePostList(clientFactory: ClientFactory){
        clientFactory.appSyncClient()
            .query(ListPostsQuery.builder().build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(queryCallback)
    }

    private val queryCallback: GraphQLCall.Callback<ListPostsQuery.Data> =
        object : GraphQLCall.Callback<ListPostsQuery.Data>() {
            override fun onResponse(response: Response<ListPostsQuery.Data>) {
                postList.postValue(ArrayList(response.data()?.listPosts()?.items()))
            }

            override fun onFailure(e: ApolloException) {
                Log.e(ContentValues.TAG, e.toString())
            }
        }

    private lateinit var subscriptionWatcher: AppSyncSubscriptionCall<OnCreatePostSubscription.Data>

    private fun subscribe(clientFactory: ClientFactory) {
        val subscription: OnCreatePostSubscription = OnCreatePostSubscription.builder().build()
        subscriptionWatcher = clientFactory.appSyncClient().subscribe(subscription)
        subscriptionWatcher.execute(subCallback)
    }

    private val subCallback: AppSyncSubscriptionCall.Callback<OnCreatePostSubscription.Data> =
        object : AppSyncSubscriptionCall.Callback<OnCreatePostSubscription.Data> {
            override fun onResponse(response: Response<OnCreatePostSubscription.Data>) {
                Log.i("Subscription", response.data().toString())
                if (response.data() != null) {
                    // Update UI with the newly added item
                    val data = response.data()!!.onCreatePost()
                    val addedItem = ListPostsQuery.Item(
                        data!!.__typename(),
                        data.id(),
                        data.title(),
                        data.uname(),
                        data.content(),
                        data.uploadLat(),
                        data.uploadLng(),
                        data.photo(),
                        data.likes(),
                        null,
                        data.createdAt(),
                        data.updatedAt()
                    )
                    viewModelScope.launch {
                        postList.value?.add(addedItem)
                    }
                }
            }

            override fun onFailure(e: ApolloException) {
                Log.e("Subscription", e.toString())
            }

            override fun onCompleted() {
                Log.i("Subscription", "Subscription completed")
            }
        }
}