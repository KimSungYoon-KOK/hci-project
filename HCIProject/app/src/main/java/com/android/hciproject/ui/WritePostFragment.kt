package com.android.hciproject.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import com.android.hciproject.R
import com.android.hciproject.databinding.WritePostFragmentBinding
import com.android.hciproject.viewmodels.WritePostViewModel
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.android.hciproject.viewmodels.SharedViewModel


class WritePostFragment : Fragment() {

    private var _binding: WritePostFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WritePostViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = WritePostViewModel()
        _binding = WritePostFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setBtnOnClickListener()
    }

    private fun setBtnOnClickListener() {
        binding.takePictureBtn.setOnClickListener {
            takePicture()
        }

        binding.closeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writePostFragment_to_mainFragment)
        }

        binding.nextBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writePostFragment_to_writeContentFragment)
        }
    }

    private fun takePicture() {
        // 성윤

        // uri만 가지고 오기
        val pictureUri = "uri" // uri 타입

        // sharedViewModel의 writingPostImageUri 변수에 저장하기 (아래 코드)
        // sharedViewModel.setWritingPostImageUri(pictureUri)
    }

}
