package com.android.hciproject.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.hciproject.R
import com.android.hciproject.databinding.WriteContentFragmentBinding
import com.android.hciproject.viewmodels.WriteContentViewModel

class WriteContentFragment : Fragment() {

    private var _binding: WriteContentFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WriteContentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = WriteContentViewModel()
        _binding = WriteContentFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setOnClickListener()

    }

    private fun setOnClickListener() {
        binding.beforeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writeContentFragment_to_writePostFragment)
        }

        binding.uploadBtn.setOnClickListener {
            findNavController().navigate(R.id.action_writeContentFragment_to_mainFragment)
        }
    }

}