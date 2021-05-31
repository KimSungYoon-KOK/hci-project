package com.android.hciproject

import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient


class ClientFactory {
    @Volatile
    private var client: AWSAppSyncClient? = null

    @Synchronized
    fun init(context: Context) {
        if (client == null) {
            val awsConfiguration = AWSConfiguration(context)
            client = AWSAppSyncClient.builder()
                    .context(context)
                    .awsConfiguration(awsConfiguration)
                    .cognitoUserPoolsAuthProvider {
                        try {
                            AWSMobileClient.getInstance().tokens.idToken.tokenString
                        } catch (e: Exception) {
                            e.localizedMessage
                        }
                    }.build()
        }
    }

    @Synchronized
    fun appSyncClient(): AWSAppSyncClient {
        return client!!
    }
}

