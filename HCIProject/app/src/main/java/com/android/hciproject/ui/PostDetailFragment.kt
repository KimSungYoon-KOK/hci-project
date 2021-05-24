package com.android.hciproject.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.hciproject.R
import com.android.hciproject.databinding.PostDetailFragmentBinding
import com.android.hciproject.viewmodels.PostDetailViewModel

class PostDetailFragment : Fragment() {

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
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.closeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_postDetailFragmnet_to_mainFragment)
        }
    }

}