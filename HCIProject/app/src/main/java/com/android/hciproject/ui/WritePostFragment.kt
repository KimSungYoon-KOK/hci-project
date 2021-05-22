package com.android.hciproject.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import com.android.hciproject.R
import com.android.hciproject.databinding.WritePostFragmentBinding
import com.android.hciproject.viewmodels.WritePostViewModel
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.navigation.fragment.findNavController


class WritePostFragment : Fragment() {

    private var _binding: WritePostFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WritePostViewModel
    val GET_GALLERY_IMAGE = 200

    val getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        binding.selectedPhoto.setImageURI(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = WritePostViewModel()
        _binding = WritePostFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setBtnOnClickListener()
    }

    private fun setBtnOnClickListener() {
        binding.takePictureBtn.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.closeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writePostFragment_to_mainFragment)
        }

        binding.nextBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writePostFragment_to_mainFragment)
        }
    }

    private fun selectPicture() {
//        val requestActivity = registerForActivityResult(
//            StartActivityForResult()
//        ) { activityResult ->
//            val selectedImageUri = activityResult.
//            binding.selectedPhoto.setImageURI(selectedImageUri)
//        }
//
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.setDataAndType(
//            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            "image/*"
//        )
//        requestActivity.launch(intent)


    }
}