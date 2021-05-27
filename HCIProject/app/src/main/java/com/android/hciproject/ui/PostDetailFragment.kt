package com.android.hciproject.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.hciproject.R
import com.android.hciproject.adapters.CommentAdapter
import com.android.hciproject.databinding.PostDetailFragmentBinding
import com.android.hciproject.viewmodels.PostDetailViewModel
import com.android.hciproject.viewmodels.SharedViewModel

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
        binding.viewModel = viewModel
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setOnClickListener()
        setCommentAdapter()
        observeComments()
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
        sharedViewModel.selectedPost.observe(viewLifecycleOwner) {
            if (it.comments == null)
                return@observe

            val comments = it.comments

            val recyclerView = binding.recyclerview
            val adapter = recyclerView.adapter as CommentAdapter
            adapter.submitList(comments)
        }
    }

}