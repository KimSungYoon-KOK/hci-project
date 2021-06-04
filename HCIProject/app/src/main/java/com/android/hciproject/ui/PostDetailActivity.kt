package com.android.hciproject.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.android.hciproject.adapters.CommentAdapter
import com.android.hciproject.data.Comment
import com.android.hciproject.data.Post
import com.android.hciproject.databinding.ActivityPostDetailBinding
import com.android.hciproject.viewmodels.PostDetailViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File

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
        val p = intent.getSerializableExtra("post") as Post
        viewModel.fetchPost(p)
        //setCommentAdapter()
        downloadWithTransferUtility(viewModel.post.value!!.img!!)
    }

    private fun observeComments() {
        viewModel.post.observe(this, Observer { posts ->
            if (posts.comments == null)
                return@Observer

            // Update comments recyclerview.
            val recyclerView = binding.recyclerview
            val adapter = recyclerView.adapter as CommentAdapter
            if (!posts.comments.isNullOrEmpty())
                adapter.submitList(posts.comments!!.toMutableList())
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
            val c = Comment()
            viewModel.insertComment(c)
            // 성윤
            // 댓글 추가
            Snackbar.make(binding.container, "댓글 작성", Snackbar.LENGTH_SHORT).show()
        }

        binding.likeBtn.setOnClickListener {
            val pid = viewModel.getPid()
            if (pid != null) {
                // 성윤
                // 좋아요 버튼 클릭
                Snackbar.make(binding.container, "좋아요 클릭", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.writeCommentEditText.windowToken, 0)
    }

    fun downloadWithTransferUtility(photo: String) {
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