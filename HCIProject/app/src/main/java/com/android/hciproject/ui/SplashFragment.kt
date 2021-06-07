package com.android.hciproject.ui

import com.android.hciproject.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.amazonaws.mobile.client.AWSMobileClient
import com.android.hciproject.databinding.SplashFragmentBinding
import com.android.hciproject.utils.NetworkUtils
import com.android.hciproject.viewmodels.SharedViewModel
import com.android.hciproject.viewmodels.SplashViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class SplashFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel: SplashViewModel by viewModel()
    private lateinit var binding: SplashFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SplashFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSplashAnimation()
    }

    private fun setSplashAnimation() {
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.splash_anim)
        val anim2 = AnimationUtils.loadAnimation(requireContext(), R.anim.splash_anim)
        binding.logoImage.startAnimation(anim)
        binding.logoTextView.startAnimation(anim2)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
//                TODO("Not yet implemented")
            }

            override fun onAnimationEnd(animation: Animation?) {
                observeLogin()
                handleNetwork()
            }

            override fun onAnimationRepeat(animation: Animation?) {
//                TODO("Not yet implemented")
            }

        })
    }

    private fun observeLogin() {
        sharedViewModel.isLogin.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
            }
        })
    }

    private fun handleNetwork() {
        val isConnected = checkNetworkConnected()
        if (isConnected) {
            (activity as MainActivity).checkAWS()
        }
    }

    private fun checkNetworkConnected(): Boolean =
        NetworkUtils.getConnectivityStatus(requireContext())
}