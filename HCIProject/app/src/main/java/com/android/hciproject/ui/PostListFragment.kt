package com.android.hciproject.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.hciproject.R
import com.android.hciproject.adapters.PostAdapter
import com.android.hciproject.data.Post
import com.android.hciproject.databinding.PostListFragmentBinding
import com.android.hciproject.viewmodels.PostListViewModel
import com.android.hciproject.viewmodels.SharedViewModel

class PostListFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: PostListFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PostListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = PostListViewModel()
        _binding = PostListFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListener()
        setPostAdapter()
    }

    private fun setOnClickListener() {
        binding.showMapBtn.setOnClickListener {
            findNavController().navigate(R.id.action_postListFragment_to_mainFragment)
        }
    }

    private fun setPostAdapter() {
        binding.recyclerview.layoutManager = LinearLayoutManager(context)
        binding.recyclerview.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        val adapter = PostAdapter(sharedViewModel.postList.value!!)
        adapter.itemClickListener = object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: Post) {
                sharedViewModel.selectedPost.value = post
                findNavController().navigate(R.id.action_postListFragment_to_postDetailFragment)
            }
        }
        binding.recyclerview.adapter = adapter
    }


}