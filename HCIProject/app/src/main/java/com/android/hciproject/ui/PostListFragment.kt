package com.android.hciproject.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.android.hciproject.adapters.CommentAdapter
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
    private val clientFactory = ClientFactory()

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
        init()
        setOnClickListener()
        setPostAdapter()
        observePostList()
    }

    private fun init() {
        clientFactory.init(requireContext())
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
        val adapter = PostAdapter(clientFactory)
        adapter.itemClickListener = object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: ListPostsQuery.Item) {
                sharedViewModel.selectedPost.value = post
                val intent = Intent(requireContext(), PostDetailActivity::class.java)
                Log.d("PostListFragment", post.toString())
                val tempPost = Post(post)
                intent.putExtra("post", tempPost)
                startActivity(intent)
            }
        }
        binding.recyclerview.adapter = adapter
    }

    private fun observePostList() {
        sharedViewModel.postList.observe(viewLifecycleOwner, Observer {
            if (it == null)
                return@Observer

            // Update comments recyclerview.
            val recyclerView = binding.recyclerview
            val adapter = recyclerView.adapter as PostAdapter
            if (!it.isNullOrEmpty())
                adapter.submitList(it.toMutableList())
        })
    }

}