package com.android.hciproject.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.*
import com.android.hciproject.databinding.ActivityMainBinding
import com.android.hciproject.viewmodels.SharedViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun checkAWS() {
        AWSMobileClient.getInstance()
            .initialize(this, object : Callback<UserStateDetails?> {
                override fun onResult(userStateDetails: UserStateDetails?) {
                    Log.d("onResult", userStateDetails!!.userState.toString())
                    when (userStateDetails.userState) {
                        UserState.SIGNED_OUT -> {
                            showSignIn()
                        }
                        UserState.SIGNED_IN -> {
                            viewModel.setLoginStatus(true)
                        }
                        else -> {
                            showSignIn()
                        }
                    }
                }

                override fun onError(e: Exception) {
                    Log.e(ContentValues.TAG, e.toString())
                }
            })
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun showSignIn() {
        try {
            AWSMobileClient.getInstance()
                .showSignIn(
                    this,
                    SignInUIOptions.builder().build()
                )
        } catch (e: java.lang.Exception) {
            Log.e(ContentValues.TAG, e.toString())
        }
    }


}