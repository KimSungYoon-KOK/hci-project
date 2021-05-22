package com.android.hciproject.ui

import com.android.hciproject.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.hciproject.databinding.SplashFragmentBinding
import com.android.hciproject.utils.NetworkUtils
import com.android.hciproject.viewmodels.SplashViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class SplashFragment : Fragment() {

    private val viewModel: SplashViewModel by viewModel()
    private var _binding: SplashFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SplashFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleNetwork()
    }

    private fun handleNetwork() {
        val isConnected = checkNetworkConnected()
        if(isConnected){
            navigateToLoginFragment()
        }
    }

    private fun navigateToLoginFragment(){
        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
    }

    private fun navigateToMainFragment() {
        /*Snackbar.make(
            binding.container,
            getString(R.string.prompt_no_network),
            Snackbar.LENGTH_SHORT
        ).show()*/
        findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
    }

    private fun checkNetworkConnected(): Boolean =
        NetworkUtils.getConnectivityStatus(requireContext())

}