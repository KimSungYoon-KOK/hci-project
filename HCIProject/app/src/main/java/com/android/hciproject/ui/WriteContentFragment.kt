package com.android.hciproject.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.amazonaws.amplify.generated.graphql.CreatePostMutation
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.android.hciproject.databinding.WriteContentFragmentBinding
import com.android.hciproject.viewmodels.SharedViewModel
import com.android.hciproject.viewmodels.WriteContentViewModel
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.android.material.snackbar.Snackbar
import type.CreatePostInput
import java.io.File
import javax.annotation.Nonnull

class WriteContentFragment : Fragment() {

    private val viewModel: WriteContentViewModel by viewModels()
    private var _binding: WriteContentFragmentBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val clientFactory = ClientFactory()
    private var photoID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WriteContentFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        viewModel.fetchUserName(sharedViewModel.loginUserName.value!!)
        viewModel.fetchLatLng(sharedViewModel.latLng.value!!)
        //viewModel.fetchImageUri(sharedViewModel.writingPostImageUri.value!!)
        setOnClickListener()
    }

    private fun init() {
        clientFactory.init(requireContext())
    }

    private fun setOnClickListener() {
        binding.beforeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writeContentFragment_to_writePostFragment)
        }

        binding.uploadBtn.setOnClickListener {
            uploadPost()
            findNavController().navigate(R.id.action_writeContentFragment_to_mainFragment)
        }
    }

    // 포스트 추가하기
    private fun uploadPost() {
        // 성윤
//        photoID = sharedViewModel.writingPostImageID.value
//        if (photoID != null) {
//            uploadWithTransferUtility(photoID!!)
//        } else {
//            Snackbar.make(
//                binding.container,
//                getString(R.string.prompt_upload_post),
//                Snackbar.LENGTH_SHORT
//            ).show()
//        }
        photoID = "1011"
        save()
        Snackbar.make(
            binding.container,
            getString(R.string.prompt_upload_post),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    ////////////////////////////// Upload Post to S3 //////////////////////////////
    private fun save() {
        Log.d("CreatePost_save", "Save function Call")
        val input = getCreatePostInput()

        val addPostMutation = CreatePostMutation.builder()
            .input(input)
            .build()

        clientFactory.appSyncClient()
            .mutate(addPostMutation)
            .refetchQueries(ListPostsQuery.builder().build())
            .enqueue(mutateCallback)
    }

    private fun getCreatePostInput(): CreatePostInput {
        //val username = AWSMobileClient.getInstance().username.toString()
        val username = viewModel.username.value!!
        val title = viewModel.title.value!!
        val content = viewModel.content.value!!
        val uploadLatLng = viewModel.uploadLatLng.value!!

        return CreatePostInput.builder()
            .title(title)
            .content(content)
            .uname(username)
            .uploadLat(uploadLatLng.latitude.toString())
            .uploadLng(uploadLatLng.longitude.toString())
            .likes(0)
            .photo(getS3Key(photoID!!))
            .build()
    }

    private val mutateCallback: GraphQLCall.Callback<CreatePostMutation.Data> =
        object : GraphQLCall.Callback<CreatePostMutation.Data>() {
            override fun onResponse(response: Response<CreatePostMutation.Data>) {
                Log.d("CreatePost_callback", response.data().toString())
                Snackbar.make(
                    binding.container,
                    getString(R.string.prompt_upload_post) + viewModel.username.value,
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Snackbar.make(
                    binding.container,
                    getString(R.string.prompt_fail_upload_photo),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    private fun getS3Key(localPath: String): String {
        //We have read and write ability under the public folder
        return "public/${File(localPath).name}.jpg"
    }

    private fun uploadWithTransferUtility(localPath: String) {
        val key = getS3Key(localPath)
        Log.d("Uploading", "Uploading file from $localPath to $key")

        val path = requireContext().getDir("tmp", Context.MODE_PRIVATE).path
        val uploadObserver: TransferObserver = clientFactory.transferUtility().upload(
            key, File(path, localPath)
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
                Snackbar.make(
                    binding.container,
                    getString(R.string.prompt_fail_upload_photo),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }

//    private fun uploadAndSave() {
//        if (photoPath != null) {
//            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
//            }
//            // Upload a photo first. We will only call save on its successful callback.
//            uploadWithTransferUtility(photoPath!!)
//        } else {
//            save()
//        }
//    }

}