package com.android.hciproject.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.*
import com.android.hciproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AWSMobileClient.getInstance()
            .initialize(this, object : Callback<UserStateDetails?> {
                override fun onResult(userStateDetails: UserStateDetails?) {
                    Log.d("onResult", userStateDetails!!.userState.toString())
                    if (userStateDetails.userState == UserState.SIGNED_OUT) {
                        showSignIn()
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