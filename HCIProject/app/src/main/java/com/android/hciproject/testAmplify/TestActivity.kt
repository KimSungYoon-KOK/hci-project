package com.android.hciproject.testAmplify

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.amazonaws.amplify.generated.graphql.OnCreatePostSubscription
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.android.material.floatingactionbutton.FloatingActionButton


class TestActivity : AppCompatActivity() {

    private lateinit var mAdapter: MyAdapter
    private val clientFactory = ClientFactory()
    private var mPosts: ArrayList<ListPostsQuery.Item>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        clientFactory.init(applicationContext)

        val mRecyclerView = findViewById<RecyclerView>(R.id.recycler_view);
        mRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        mAdapter = MyAdapter(clientFactory)
        mRecyclerView.adapter = mAdapter

        val btnAddPost = findViewById<FloatingActionButton>(R.id.btn_addPost)
        btnAddPost.setOnClickListener {
            val addPostIntent = Intent(this@TestActivity, AddPostActivity::class.java)
            startActivity(addPostIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        query()
        subscribe()
    }

    override fun onStop() {
        super.onStop()
//        subscriptionWatcher.cancel()
    }

    private fun query() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission not granted! Requesting...")
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
        }

        clientFactory.appSyncClient()
            .query(ListPostsQuery.builder().build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(queryCallback)
    }

    private val queryCallback: GraphQLCall.Callback<ListPostsQuery.Data> =
        object : GraphQLCall.Callback<ListPostsQuery.Data>() {
            override fun onResponse(response: Response<ListPostsQuery.Data>) {
                mPosts = ArrayList(response.data()?.listPosts()?.items())
                Log.i("Retrieved list items", "$mPosts")
                runOnUiThread {
                    mAdapter.setItems(mPosts!!)
                    mAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(e: ApolloException) {
                Log.e(TAG, e.toString())
            }
        }


    // 구독 및 알람
    private lateinit var subscriptionWatcher: AppSyncSubscriptionCall<OnCreatePostSubscription.Data>

    private fun subscribe() {
        val subscription: OnCreatePostSubscription = OnCreatePostSubscription.builder().build()
        subscriptionWatcher = clientFactory.appSyncClient().subscribe(subscription)
        subscriptionWatcher.execute(subCallback)
    }

    private val subCallback: AppSyncSubscriptionCall.Callback<OnCreatePostSubscription.Data> =
        object : AppSyncSubscriptionCall.Callback<OnCreatePostSubscription.Data> {
            override fun onResponse(response: Response<OnCreatePostSubscription.Data>) {
                Log.i("Subscription", response.data().toString())

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
                    null,
                    data.createdAt(),
                    data.updatedAt()
                )
                runOnUiThread {
                    mPosts!!.add(addedItem)
                    mAdapter.notifyItemInserted(mPosts!!.size - 1)
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


