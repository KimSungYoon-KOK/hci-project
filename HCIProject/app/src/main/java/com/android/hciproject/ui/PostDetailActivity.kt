package com.android.hciproject.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
        setLayout()

        binding = DataBindingUtil.setContentView<ActivityPostDetailBinding>(
            this,
            R.layout.activity_post_detail
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        fetchData()
    }

    private fun setLayout() {
        val lpWindow: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        lpWindow.dimAmount = 0.6f
        window.attributes = lpWindow

        val dm = applicationContext.resources.displayMetrics
        window.attributes.width = (dm.widthPixels * 0.9).toInt()
        window.attributes.height = (dm.heightPixels * 0.9).toInt()
    }

    private fun fetchData() {
        val p = intent.getSerializableExtra("post") as Post
        viewModel.fetchPost(p)
        setCommentAdapter()
        setOnClickListener()
        Log.d("PostDetailFragment", viewModel.post.toString())
        Log.d("PostDetailFragment::text", binding.content.text.toString())


    }

    private fun observePost() {
        viewModel.post.observe(this, Observer { posts ->

        })

    }

    private fun setCommentAdapter() {
        val recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = CommentAdapter()
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
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

    private fun observeComments() {
//        viewModel.post.observe(viewLifecycleOwner) {
//            if (it.comments == null)
//                return@observe
//
//            val comments = it.comments
//
//            val recyclerView = binding.recyclerview
//            val adapter = recyclerView.adapter as CommentAdapter
//            adapter.submitList(comments)
//        }
    }


}