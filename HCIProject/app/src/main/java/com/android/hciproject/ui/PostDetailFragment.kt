package com.android.hciproject.ui

import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.android.hciproject.adapters.CommentAdapter
import com.android.hciproject.databinding.PostDetailFragmentBinding
import com.android.hciproject.viewmodels.PostDetailViewModel
import com.android.hciproject.viewmodels.SharedViewModel
import java.io.File

class PostDetailFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var viewModel: PostDetailViewModel
    private var _binding: PostDetailFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = PostDetailViewModel()
        _binding = PostDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListener()
        setCommentAdapter()
        if(sharedViewModel.selectedPost.value != null)
            Log.d("selectedPost",sharedViewModel.selectedPost.value!!.photo()!!)
//        observeComments()
    }



    private fun setCommentAdapter() {
        val recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = CommentAdapter()
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
    }

    private fun setOnClickListener() {
        binding.closeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_postDetailFragmnet_to_mainFragment)
        }

        binding.writeCommentBtn.setOnClickListener {
            //구현
        }
    }

    private fun observeComments() {
//        sharedViewModel.selectedPost.observe(viewLifecycleOwner) {
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