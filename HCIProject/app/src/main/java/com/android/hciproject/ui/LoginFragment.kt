package com.android.hciproject.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.hciproject.R
import com.android.hciproject.databinding.LoginFragmentBinding
import com.android.hciproject.viewmodels.LoginViewModel
import com.android.hciproject.viewmodels.SharedViewModel
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()
    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.loginButton.setOnClickListener {
            login()
        }

        binding.usernameEditText.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                login()
            }
            true
        }
    }

    private fun login() {
        hideKeyboard()
        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        Snackbar.make(
            binding.container,
            "안녕하세요, " + sharedViewModel.loginUserName.value + "님!",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.usernameEditText.windowToken, 0)
    }
}