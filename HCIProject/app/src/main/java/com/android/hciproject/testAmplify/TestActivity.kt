package com.android.hciproject.testAmplify

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Operation.Variables
import com.apollographql.apollo.exception.ApolloException
import javax.annotation.Nonnull


class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        mRecyclerView = findViewById(R.id.recycler_view)

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // specify an adapter (see also next example)
        var mAdapter = MyAdapter(this)
        mRecyclerView.setAdapter(mAdapter);

        ClientFactory.init(this);
    }

    private fun save() {
        val name = (findViewById<View>(R.id.editTxt_name) as EditText).text.toString()
        val description =
            (findViewById<View>(R.id.editText_description) as EditText).text.toString()
        val input: CreatePetInput = CreatePetInput.builder()
            .name(name)
            .description(description)
            .build()
        val addPetMutation: CreatePetMutation = CreatePetMutation.builder()
            .input(input)
            .build()
        ClientFactory.appSyncClient().mutate<Operation.Data, Any, Variables>(addPetMutation)
            .enqueue(mutateCallback)
    }

    // Mutation callback code
    private val mutateCallback: GraphQLCall.Callback<CreatePetMutation.Data> =
        object : GraphQLCall.Callback<CreatePetMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<CreatePetMutation.Data?>?) {
                runOnUiThread {
                    Toast.makeText(this@AddPetActivity, "Added pet", Toast.LENGTH_SHORT).show()
                    this@AddPetActivity.finish()
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                runOnUiThread {
                    Log.e("", "Failed to perform AddPetMutation", e)
                    Toast.makeText(this@AddPetActivity, "Failed to add pet", Toast.LENGTH_SHORT)
                        .show()
                    this@AddPetActivity.finish()
                }
            }
        }
}