package com.android.hciproject.testAmplify

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.*
import com.android.hciproject.R


class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        AWSMobileClient.getInstance()
            .initialize(this, object : Callback<UserStateDetails?> {
                override fun onResult(userStateDetails: UserStateDetails?) {
                    if (userStateDetails != null) {
                        Log.i(TAG, userStateDetails.userState.toString())

                        when (userStateDetails.userState) {
                            UserState.SIGNED_IN -> {
                                val i = Intent(this@AuthenticationActivity, TestActivity::class.java)
                                startActivity(i)
                                finish()
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
                    Log.e(TAG, e.toString())
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
            finish()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.toString())
        }
    }
}