package com.android.hciproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.UserState
import com.android.hciproject.R

class LoginFragment : Fragment() {

//    private lateinit var viewModel: LoginViewModel
//    private var _binding: LoginFragmentBinding? = null
//    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        viewModel = LoginViewModel()
//        _binding = LoginFragmentBinding.inflate(inflater, container, false)
//        binding.lifecycleOwner = this
//        binding.viewModel = viewModel
//        return binding.root
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setLoginClickListener()

        //AWS Login


    }



//    private fun setLoginClickListener() {
//        binding.loginButton.setOnClickListener {
//            login()
//        }
//    }
//
//    private fun login() {
//        val imm =
//            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(binding.username.windowToken, 0)
//        viewModel.login()
//        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
//        Snackbar.make(
//            binding.container,
//            "안녕하세요, "+binding.username.text.toString()+"님!",
//            Snackbar.LENGTH_SHORT
//        ).show()
//    }

}