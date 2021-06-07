package com.android.hciproject.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.amazonaws.amplify.generated.graphql.UpdatePostMutation
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.android.hciproject.adapters.CommentAdapter
import com.android.hciproject.data.Post
import com.android.hciproject.databinding.ActivityPostDetailBinding
import com.android.hciproject.viewmodels.PostDetailViewModel
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.android.material.snackbar.Snackbar
import type.UpdatePostInput
import java.io.File
import javax.annotation.Nonnull

class PostDetailActivity : AppCompatActivity() {

    val viewModel: PostDetailViewModel by viewModels()
    lateinit var binding: ActivityPostDetailBinding
    private val clientFactory = ClientFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_post_detail
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        init()
    }

    private fun init() {
        clientFactory.init(this)
        fetchData()
        setLayout()
        setOnClickListener()
        setCommentAdapter()
        observeComments()
    }

    private fun setLayout() {
        val lpWindow: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        lpWindow.dimAmount = 0.6f
        window.attributes = lpWindow

        val dm = applicationContext.resources.displayMetrics
        window.attributes.width = (dm.widthPixels * 0.9).toInt()
        window.attributes.height = (dm.heightPixels * 0.8).toInt()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    private fun fetchData() {
        viewModel.fetchPost(intent.getSerializableExtra("post") as Post)
        viewModel.fetchUsername(intent.getStringExtra("username")!!)
        downloadWithTransferUtility(viewModel.post.value!!.img!!)
    }

    private fun observeComments() {
        viewModel.comments.observe(this, Observer {
            if (it == null) {
                return@Observer
            }

            val recyclerView = binding.recyclerview
            val adapter = recyclerView.adapter as CommentAdapter
            if (!it.isNullOrEmpty())
                adapter.submitList(it.toMutableList())
        })
    }

    private fun setCommentAdapter() {
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        val adapter = CommentAdapter()
        binding.recyclerview.adapter = adapter

    }

    private fun setOnClickListener() {
        binding.closeBtn.setOnClickListener {
            finish()
        }

        binding.writeCommentBtn.setOnClickListener {
            hideKeyboard()

            // 성윤

            // 댓글 추가
            val pid = viewModel.getPid()
            val username = viewModel.username.value!!
            val comment = viewModel.writingComment.value!!

            Snackbar.make(binding.container, "댓글 작성", Snackbar.LENGTH_SHORT).show()
        }

        binding.likeBtn.setOnClickListener {
            updateLikes()
        }
    }


    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.writeCommentEditText.windowToken, 0)
    }

    private fun updateLikes() {
        val pid = viewModel.getPid()
        if (pid != null) {
            // 성윤
            val input = getUpdatePostInput(pid)
            val updatePostMutation = UpdatePostMutation.builder()
                .input(input)
                .build()

            clientFactory.appSyncClient()
                .mutate(updatePostMutation)
                .refetchQueries(ListPostsQuery.builder().build())
                .enqueue(mutateCallback)

            // 좋아요 버튼 클릭
            Snackbar.make(binding.container, "좋아요 클릭", Snackbar.LENGTH_SHORT).show()
        }
    }

    private val mutateCallback: GraphQLCall.Callback<UpdatePostMutation.Data> =
        object : GraphQLCall.Callback<UpdatePostMutation.Data>() {
            override fun onResponse(response: Response<UpdatePostMutation.Data>) {
                Log.d("Update_Response", response.data().toString())
                if (response.data() != null) {
                    val data = response.data()!!.updatePost()
                    viewModel.updateLikes(data!!.likes()!!)
//                    viewModel.post.value!!.like = data!!.likes()!!
                    Log.d("Update_Response", viewModel.post.value!!.like.toString())
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.d("Update_Response", "Update Fail")
            }
        }

    private fun getUpdatePostInput(pid: String): UpdatePostInput {
        val likes = viewModel.post.value!!.like.get() + 1
        return UpdatePostInput.builder()
            .id(pid)
            .likes(likes)
            .build()
    }

    private fun downloadWithTransferUtility(photo: String) {
        val localPath = externalCacheDir!!.absolutePath + "/${photo}"
//            val localPath: String = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS
//            ).absolutePath.toString() + "/" + photo
        Log.d("localPath", localPath)
        val downloadObserver: TransferObserver = clientFactory.transferUtility().download(
            photo,
            File(localPath)
        )

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
//                        localUrl = localPath
                    binding.imageView.setImageBitmap(BitmapFactory.decodeFile(localPath))
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()
                Log.d(
                    ContentValues.TAG,
                    "   ID:$id   bytesCurrent: $bytesCurrent   bytesTotal: $bytesTotal $percentDone%"
                )
            }

            override fun onError(id: Int, ex: Exception) {
                // Handle errors
                Log.e(ContentValues.TAG, "Unable to download the file.", ex)
            }
        })
    }

}