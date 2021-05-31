package com.android.hciproject.testAmplify

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListPetsQuery
import com.amazonaws.amplify.generated.graphql.OnCreatePetSubscription
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
    private var mPets: ArrayList<ListPetsQuery.Item>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        clientFactory.init(applicationContext)

        val mRecyclerView = findViewById<RecyclerView>(R.id.recycler_view);
        mRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        mAdapter = MyAdapter()
        mRecyclerView.adapter = mAdapter

        val btnAddPet = findViewById<FloatingActionButton>(R.id.btn_addPet)
        btnAddPet.setOnClickListener {
            val addPetIntent = Intent(this@TestActivity, AddPetActivity::class.java)
            startActivity(addPetIntent)
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
        clientFactory.appSyncClient()
            .query(ListPetsQuery.builder().build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(queryCallback)
    }

    private val queryCallback: GraphQLCall.Callback<ListPetsQuery.Data> =
        object : GraphQLCall.Callback<ListPetsQuery.Data>() {
            override fun onResponse(response: Response<ListPetsQuery.Data>) {
                mPets = ArrayList(response.data()?.listPets()?.items())
                Log.i(TAG, "Retrieved list items: $mPets")
                runOnUiThread {
                    mAdapter.setItems(mPets!!)
                    mAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(e: ApolloException) {
                Log.e(TAG, e.toString())
            }
        }


    // 구독 및 알람
    private lateinit var subscriptionWatcher: AppSyncSubscriptionCall<OnCreatePetSubscription.Data>

    private fun subscribe() {
        val subscription: OnCreatePetSubscription = OnCreatePetSubscription.builder().build()
        subscriptionWatcher = clientFactory.appSyncClient().subscribe(subscription)
        subscriptionWatcher.execute(subCallback)
    }

    private val subCallback: AppSyncSubscriptionCall.Callback<OnCreatePetSubscription.Data> =
        object : AppSyncSubscriptionCall.Callback<OnCreatePetSubscription.Data> {
            override fun onResponse(response: Response<OnCreatePetSubscription.Data>) {
                Log.i("Subscription", response.data().toString())

                // Update UI with the newly added item
                val data = response.data()!!.onCreatePet()
                val addedItem = ListPetsQuery.Item(
                    data!!.__typename(),
                    data.id(),
                    data.name(),
                    data.description(),
                    data.photo(),
                    data.createdAt(),
                    data.updatedAt()
                )
                runOnUiThread {
                    mPets!!.add(addedItem)
                    mAdapter.notifyItemInserted(mPets!!.size - 1)
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


