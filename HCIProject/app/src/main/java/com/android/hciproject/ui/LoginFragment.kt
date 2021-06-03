package com.android.hciproject.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amazonaws.mobile.client.*
import com.android.hciproject.databinding.LoginFragmentBinding
import com.android.hciproject.testAmplify.TestActivity
import com.android.hciproject.viewmodels.LoginViewModel

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = LoginViewModel()
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setLoginClickListener()
        AWSMobileClient.getInstance()
            .initialize(this, object : Callback<UserStateDetails?> {
                override fun onResult(userStateDetails: UserStateDetails?) {
                    if (userStateDetails != null) {
                        Log.i(ContentValues.TAG, userStateDetails.userState.toString())

                        when (userStateDetails.userState) {
                            UserState.SIGNED_IN -> {
                                // 메인으로 이동
//                                val i = Intent(require(context), TestActivity::class.java)
//                                startActivity(i)
                            }
                            UserState.SIGNED_OUT -> showSignIn()
                            else -> {
                                AWSMobileClient.getInstance().signOut()
                                showSignIn()
                            }
                        }
                    }
                }

                override fun onError(e: Exception) {
                    Log.e(ContentValues.TAG, e.toString())
                }
            })
    }

    private fun showSignIn() {
        try {
            AWSMobileClient.getInstance()
                .showSignIn(
                    this,
                    SignInUIOptions.builder().nextActivity(TestActivity::class.java).build()
                )
        } catch (e: java.lang.Exception) {
            Log.e(ContentValues.TAG, e.toString())
        }
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