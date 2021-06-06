package com.android.hciproject.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.hciproject.R
import com.android.hciproject.data.Post
import com.android.hciproject.databinding.WriteContentFragmentBinding
import com.android.hciproject.viewmodels.SharedViewModel
import com.android.hciproject.viewmodels.WriteContentViewModel
import com.google.android.material.snackbar.Snackbar

class WriteContentFragment : Fragment() {

    private val viewModel: WriteContentViewModel by viewModels()
    private var _binding: WriteContentFragmentBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = WriteContentFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.fetchUserName(sharedViewModel.loginUserName.value!!)
        viewModel.fetchLatLng(sharedViewModel.latLng.value!!)
        //viewModel.fetchImageUri(sharedViewModel.writingPostImageUri.value!!)
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.beforeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writeContentFragment_to_writePostFragment)
        }

        binding.uploadBtn.setOnClickListener {
            uploadPost()
            findNavController().navigate(R.id.action_writeContentFragment_to_mainFragment)
        }
    }

    // 포스트 추가하기
    private fun uploadPost() {
        // 성윤

        // uri
        // val pictureUri = sharedViewModel.writingPostImageUri.value!!

        val username = viewModel.username.value!!
        val title = viewModel.title.value!!
        val content = viewModel.content.value!!
        val uploadLatLng = viewModel.uploadLatLng.value!!

        Snackbar.make(
            binding.container,
            getString(R.string.prompt_upload_post) + username,
            Snackbar.LENGTH_SHORT
        ).show()
    }

}