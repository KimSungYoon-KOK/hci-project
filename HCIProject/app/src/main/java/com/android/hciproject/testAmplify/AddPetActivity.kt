package com.android.hciproject.testAmplify

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.amplify.generated.graphql.CreatePetMutation
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import type.CreatePetInput
import java.util.*
import javax.annotation.Nonnull


class AddPetActivity : AppCompatActivity() {

    private val clientFactory = ClientFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        clientFactory.init(applicationContext)

        val btnAddItem: Button = findViewById(R.id.btn_save)
        btnAddItem.setOnClickListener { save() }
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

        clientFactory.appSyncClient()
            .mutate(addPetMutation)
            ?.enqueue(mutateCallback)
    }

    // Mutation callback code
    private val mutateCallback: GraphQLCall.Callback<CreatePetMutation.Data> =
        object : GraphQLCall.Callback<CreatePetMutation.Data>() {
            override fun onResponse(response: Response<CreatePetMutation.Data>) {
                runOnUiThread {
                    Toast.makeText(this@AddPetActivity, "Added pet", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                runOnUiThread {
                    Log.e("", "Failed to perform AddPetMutation", e)
                    Toast.makeText(this@AddPetActivity, "Failed to add pet", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
        }
}
