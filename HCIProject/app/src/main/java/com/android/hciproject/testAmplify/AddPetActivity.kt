package com.android.hciproject.testAmplify

import android.content.ContentValues.TAG
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.amplify.generated.graphql.CreatePetMutation
import com.amazonaws.amplify.generated.graphql.ListPetsQuery
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import type.CreatePetInput
import java.io.File
import java.util.*
import javax.annotation.Nonnull


class AddPetActivity : AppCompatActivity() {

    private val clientFactory = ClientFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        clientFactory.init(applicationContext)

        val btnAddItem: Button = findViewById(R.id.btn_save)
        btnAddItem.setOnClickListener {
            if (photoPath != null) {
                uploadWithTransferUtility(photoPath!!)
            } else {
                save()
            }

        }


        val btnAddPhoto = findViewById<Button>(R.id.btn_add_photo)
        btnAddPhoto.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun save() {
        val input = getCreatePetInput()

        val addPetMutation = CreatePetMutation.builder()
            .input(input!!)
            .build()

        clientFactory.appSyncClient()
            .mutate(addPetMutation)
            .refetchQueries(ListPetsQuery.builder().build())
            .enqueue(mutateCallback)

        // Enables offline support via an optimistic update
        // Add to event list while offline or before request returns

        // Enables offline support via an optimistic update
        // Add to event list while offline or before request returns
//        addPetOffline(input)
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



    ///////////// PhotoPath + S3 /////////////
    private var photoPath: String? = null
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri ->
            val cursor: Cursor?
            val columnIndexId: Int
            val listOfAllImages: MutableList<Uri> = mutableListOf()
            val projection = arrayOf(MediaStore.Images.Media._ID)
            var imageId: Long
            cursor = applicationContext.contentResolver
                .query(uri, projection, null, null, null)

            if (cursor != null) {
                columnIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    imageId = cursor.getLong(columnIndexId)
                    val uriImage = Uri.withAppendedPath(uri, "" + imageId)
                    listOfAllImages.add(uriImage)
                }
                cursor.close()
            }
            photoPath = listOfAllImages[0].toString()
            Log.d("IMAGE_URI", photoPath!!)
        }

    private fun getS3Key(localPath: String): String {
        //We have read and write ability under the public folder
        return "public/" + File(localPath).name
    }

    fun uploadWithTransferUtility(localPath: String) {
        val key = getS3Key(localPath)
        Log.d(TAG, "Uploading file from $localPath to $key")
        val uploadObserver: TransferObserver = clientFactory.transferUtility().upload(
            key,
            File(localPath)
        )

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d(TAG, "Upload is completed. ")

                    // Upload is successful. Save the rest and send the mutation to server.
                    save()
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()
                Log.d(
                    TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                            + " bytesTotal: " + bytesTotal + " " + percentDone + "%"
                )
            }

            override fun onError(id: Int, ex: Exception) {
                // Handle errors
                Log.e(TAG, "Failed to upload photo. ", ex)
                runOnUiThread {
                    Toast.makeText(
                        this@AddPetActivity,
                        "Failed to upload photo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun getCreatePetInput(): CreatePetInput? {
        val name = (findViewById<View>(R.id.editTxt_name) as EditText).text.toString()
        val description =
            (findViewById<View>(R.id.editText_description) as EditText).text.toString()
        return if (photoPath != null && photoPath!!.isNotEmpty()) {
            CreatePetInput.builder()
                .name(name)
                .description(description)
                .photo(getS3Key(photoPath!!)).build()
        } else {
            CreatePetInput.builder()
                .name(name)
                .description(description)
                .build()
        }
    }

//    private fun uploadAndSave() {
//        if (photoPath != null) {
//            // For higher Android levels, we need to check permission at runtime
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                // Permission is not granted
//                Log.d(TAG, "READ_EXTERNAL_STORAGE permission not granted! Requesting...")
//                ActivityCompat.requestPermissions(
//                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                    1
//                )
//            }
//
//            // Upload a photo first. We will only call save on its successful callback.
//            uploadWithTransferUtility(photoPath!!)
//        } else {
//            save()
//        }
//    }

}
