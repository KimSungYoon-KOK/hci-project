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
import com.amazonaws.amplify.generated.graphql.*
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
import type.CreateCommentInput
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
        viewModel.fetchComments(clientFactory)
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
            addComment()
        }

        binding.likeBtn.setOnClickListener {
            updateLikes()
        }
    }

    private fun addComment() {
        val input = getCreateCommentInput()

        if (input != null) {
            val addCommentMutation = CreateCommentMutation.builder()
                .input(input)
                .build()

            clientFactory.appSyncClient()
                .mutate(addCommentMutation)
                .refetchQueries(ListCommentsQuery.builder().build())
                .enqueue(addComment_mutateCallback)

            Snackbar.make(binding.container, "댓글 작성", Snackbar.LENGTH_SHORT).show()

        } else {
            Snackbar.make(binding.container, "댓글 작성 실패", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun getCreateCommentInput(): CreateCommentInput? {
        val pid = viewModel.getPid()
        return if (pid != null) {
            val uname = viewModel.username.value!!
            val content = viewModel.writingComment.value!!

            CreateCommentInput.builder()
                .postID(pid)
                .content(content)
                .uname(uname)
                .build()
        } else {
            null
        }
    }

    private val addComment_mutateCallback: GraphQLCall.Callback<CreateCommentMutation.Data> =
        object : GraphQLCall.Callback<CreateCommentMutation.Data>() {
            override fun onResponse(response: Response<CreateCommentMutation.Data>) {
                if (response.data() != null) {
                    Log.d("Comment_Response", response.data()!!.toString())

                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.d("Comment_Response", "Fail")
            }
        }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.writeCommentEditText.windowToken, 0)

        //EditText 초기화
    }


    ///////////////////////// Post Likes Update /////////////////////////
    private fun updateLikes() {
        val pid = viewModel.getPid()
        if (pid != null) {
            val input = getUpdatePostInput(pid)
            val updatePostMutation = UpdatePostMutation.builder()
                .input(input)
                .build()

            clientFactory.appSyncClient()
                .mutate(updatePostMutation)
                .refetchQueries(ListPostsQuery.builder().build())
                .enqueue(updateLikes_mutateCallback)

            // 좋아요 버튼 클릭
            Snackbar.make(binding.container, "좋아요 클릭", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun getUpdatePostInput(pid: String): UpdatePostInput {
        val likes = viewModel.post.value!!.like.get() + 1
        return UpdatePostInput.builder()
            .id(pid)
            .likes(likes)
            .build()
    }
    private val updateLikes_mutateCallback: GraphQLCall.Callback<UpdatePostMutation.Data> =
        object : GraphQLCall.Callback<UpdatePostMutation.Data>() {
            override fun onResponse(response: Response<UpdatePostMutation.Data>) {
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


    ///////////////////////// Post Image Download /////////////////////////
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