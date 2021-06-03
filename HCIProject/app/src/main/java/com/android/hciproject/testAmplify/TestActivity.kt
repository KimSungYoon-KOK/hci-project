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
//        subscribe()
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





//    fun remove() {
//        val input = getDeletePostInput()
//        val removePostMutation = DeletePostMutation.builder()
//            .input(input)
//            .build()
//
//        clientFactory.appSyncClient()
//            .mutate(removePostMutation)
//            .refetchQueries(ListPostsQuery.builder().build())
//            .enqueue(mutateCallback)
//    }
//
//    private fun getDeletePostInput(): DeletePostInput {
//
//    }
//
//    // Mutation callback code
//    private val mutateCallback: GraphQLCall.Callback<DeletePostMutation.Data> =
//        object : GraphQLCall.Callback<DeletePostMutation.Data>() {
//            override fun onResponse(response: Response<DeletePostMutation.Data>) {
//                runOnUiThread {
//                    Toast.makeText(applicationContext, "Deleted post", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//
//            override fun onFailure(@Nonnull e: ApolloException) {
//                runOnUiThread {
//                    Log.e("", "Failed to perform AddPetMutation", e)
//                    Toast.makeText(applicationContext, "Failed to deleted post", Toast.LENGTH_SHORT)
//                        .show()
//                    finish()
//                }
//            }
//        }
}


