package com.android.hciproject.testAmplify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.amplify.generated.graphql.CreatePostMutation
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import type.CreatePostInput
import java.io.*
import java.util.*
import javax.annotation.Nonnull


class AddPostActivity : AppCompatActivity() {

    private val clientFactory = ClientFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        clientFactory.init(applicationContext)

        val btnAddItem: Button = findViewById(R.id.btn_save)
        btnAddItem.setOnClickListener { uploadAndSave() }


        val btnAddPhoto = findViewById<Button>(R.id.btn_add_photo)
        btnAddPhoto.setOnClickListener {
            choosePhoto()
        }
    }

    override fun onStop() {
        super.onStop()

        // 캐시 파일 삭제
        try {
            val cacheFile = cacheDir // 내부저장소 캐시 경로를 받아오기
            val flist = cacheFile.listFiles()
            for (i in flist.indices) {    // 배열의 크기만큼 반복
                flist[i].delete() // 파일 삭제
            }
            Log.d("Uploading", "Delete Cache Success")
        } catch (e: java.lang.Exception) {
            Log.e("Uploading", "Delete Cache Fail")
        }
    }

    private fun save() {
        val input = getCreatePostInput()

        val addPostMutation = CreatePostMutation.builder()
            .input(input!!)
            .build()

        clientFactory.appSyncClient()
            .mutate(addPostMutation)
            .refetchQueries(ListPostsQuery.builder().build())
            .enqueue(mutateCallback)

    }

    // Mutation callback code
    private val mutateCallback: GraphQLCall.Callback<CreatePostMutation.Data> =
        object : GraphQLCall.Callback<CreatePostMutation.Data>() {
            override fun onResponse(response: Response<CreatePostMutation.Data>) {
                runOnUiThread {
                    Toast.makeText(this@AddPostActivity, "Added pet", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                runOnUiThread {
                    Log.e("", "Failed to perform AddPetMutation", e)
                    Toast.makeText(this@AddPostActivity, "Failed to add pet", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
        }


    ///////////// PhotoPath + S3 /////////////
    private var photoPath: String? = null
    private var photoUri: Uri? = null
    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        requestActivity.launch(intent)
    }

    private val requestActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { activityResult ->

        if (activityResult.resultCode == RESULT_OK && activityResult.data != null) {
            //Log.d("Uploading_data", activityResult.data!!.data.toString())
            val selectedImage = activityResult.data!!.data
            val listOfAllImages: MutableList<String> = mutableListOf()
            val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN
                )
            } else {
                arrayOf(MediaStore.Images.Media._ID)
            }
            contentResolver.query(
                selectedImage!!, projection, null, null, null
            )?.use { cursor ->
                val columnIndexId = cursor.getColumnIndexOrThrow(projection[0])
                val columnIndexName = cursor.getColumnIndexOrThrow(projection[1])
                val columnIndexDate = cursor.getColumnIndexOrThrow(projection[2])
                while (cursor.moveToNext()) {
                    val imageId = cursor.getLong(columnIndexId)
                    val imageName = cursor.getString(columnIndexName)
                    val imageDate = Date(cursor.getLong(columnIndexDate))
                    listOfAllImages.add(imageId.toString())
                    photoUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        imageId.toString()
                    )
                    Log.d("Uploading_URI", "id: $imageId, Name: $imageName, Date: $imageDate")
                }
                cursor.close()
            }
            photoPath = listOfAllImages[0]

            val imageView: ImageView = findViewById(R.id.selectedPhoto)
            imageView.setImageURI(photoUri)
            //Log.d("Uploading_photoUri", photoUri!!.path!!)
            //Log.d("Uploading_photoPath", photoPath!!)
        }
    }

    private fun saveCacheFile(localPath: String) {
        //캐시 파일 생성
        val resolver = applicationContext.contentResolver
        try {
            val inputStream = resolver.openInputStream(photoUri!!)
            val options = BitmapFactory.Options()
            options.inSampleSize = 4
            val imgBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            saveBitmapToJpeg(imgBitmap!!, localPath)
            Log.d("Uploading", "Save Cache Success")
        } catch (e: Exception) {
            Log.e("Uploading", "Save Cache Fail")
        }

    }

    private fun saveBitmapToJpeg(bitmap: Bitmap, imgName: String) {   // 선택한 이미지 내부 저장소에 저장
        val tempFile = File(cacheDir, imgName) // 파일 경로와 이름 넣기
        try {
            tempFile.createNewFile() // 자동으로 빈 파일을 생성하기
            val out = FileOutputStream(tempFile) // 파일을 쓸 수 있는 스트림을 준비하기
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, out) // compress 함수를 사용해 스트림에 비트맵을 저장하기
            out.close() // 스트림 닫아주기
            Log.d("Uploading", "Save Bitmap Success")
        } catch (e: java.lang.Exception) {
            Log.e("Uploading", "Save Bitmap Fail")
        }
    }


    private fun getS3Key(localPath: String): String {
        //We have read and write ability under the public folder
        return "public/${File(localPath).name}.jpg"
    }

    private fun uploadWithTransferUtility(localPath: String) {
        saveCacheFile(localPath)
        val key = getS3Key(localPath)
        Log.d("Uploading", "Uploading file from $localPath to $key")

        val uploadObserver: TransferObserver = clientFactory.transferUtility().upload(
            key, File(cacheDir, localPath)
        )

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d("Uploading", "Upload is completed.")
                    // Upload is successful. Save the rest and send the mutation to server.
                    save()
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()
                Log.d(
                    "Uploading", "ID:" + id + " bytesCurrent: " + bytesCurrent
                            + " bytesTotal: " + bytesTotal + " " + percentDone + "%"
                )
            }

            override fun onError(id: Int, ex: Exception) {
                // Handle errors
                Log.e("Uploading", "Failed to upload photo. ", ex)
                runOnUiThread {
                    Toast.makeText(
                        this@AddPostActivity,
                        "Failed to upload photo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun getCreatePostInput(): CreatePostInput? {
        val title = (findViewById<View>(R.id.editTxt_name) as EditText).text.toString()
        val content = (findViewById<View>(R.id.editText_description) as EditText).text.toString()
        val uname = AWSMobileClient.getInstance().username.toString()
        val uploadLat = "Lat"
        val uploadLng = "Lng"
        return if (photoPath != null) {
            CreatePostInput.builder()
                .title(title)
                .content(content)
                .uname(uname)
                .uploadLat(uploadLat)
                .uploadLng(uploadLng)
                .photo(getS3Key(photoPath!!)).build()
        } else {
            CreatePostInput.builder()
                .title(title)
                .content(content)
                .uname(uname)
                .uploadLat(uploadLat)
                .uploadLng(uploadLng)
                .build()
        }
    }

    private fun uploadAndSave() {
        if (photoPath != null) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
            // Upload a photo first. We will only call save on its successful callback.
            uploadWithTransferUtility(photoPath!!)
        } else {
            save()
        }
    }
}
