package com.android.hciproject.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.*
import com.android.hciproject.R
import com.android.hciproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LoginInterface{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun test() {
        AWSMobileClient.getInstance()
            .initialize(this, object : Callback<UserStateDetails?> {
                override fun onResult(userStateDetails: UserStateDetails?) {
                    if (userStateDetails != null) {
                        Log.i("UserStateDetail", userStateDetails.userState.toString())
                        when (userStateDetails.userState) {
                            UserState.SIGNED_IN -> {
                                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
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
                    SignInUIOptions.builder().build()
                )
        } catch (e: java.lang.Exception) {
            Log.e(ContentValues.TAG, e.toString())
        }
    }


}