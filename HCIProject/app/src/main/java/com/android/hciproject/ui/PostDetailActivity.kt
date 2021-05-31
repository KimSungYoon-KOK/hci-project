package com.android.hciproject.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.hciproject.R
import com.android.hciproject.adapters.CommentAdapter
import com.android.hciproject.data.Comment
import com.android.hciproject.data.Post
import com.android.hciproject.databinding.ActivityMainBinding
import com.android.hciproject.databinding.ActivityPostDetailBinding
import com.android.hciproject.viewmodels.PostDetailViewModel

class PostDetailActivity : AppCompatActivity() {

    val viewModel: PostDetailViewModel by viewModels()
    lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_post_detail
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setLayout()
        fetchData()
        observeComments()
    }

    private fun setLayout() {
        val lpWindow: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        lpWindow.dimAmount = 0.6f
        window.attributes = lpWindow

        val dm = applicationContext.resources.displayMetrics
        window.attributes.width = (dm.widthPixels * 0.9).toInt()
        window.attributes.height = (dm.heightPixels * 0.9).toInt()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    private fun fetchData() {
        val p = intent.getSerializableExtra("post") as Post
        viewModel.fetchPost(p)
        setCommentAdapter()
        setOnClickListener()
        Log.d("PostDetailFragment", viewModel.post.toString())
        Log.d("PostDetailFragment::text", binding.content.text.toString())
        Log.d("PostDetailFragment::comment", viewModel.post.value?.comments.toString())
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
            //구현
            hideKeyboard()
            val c = Comment()
            viewModel.insertComment(c)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.writeCommentEditText.windowToken, 0)
    }


}