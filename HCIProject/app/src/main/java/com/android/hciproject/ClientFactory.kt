package com.android.hciproject

import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client


class ClientFactory {
    @Volatile
    private var client: AWSAppSyncClient? = null

    @Volatile
    private var transferUtility: TransferUtility? = null

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

        if (transferUtility == null) {
            val s3Client = AmazonS3Client(AWSMobileClient.getInstance(), Region.getRegion(Regions.AP_NORTHEAST_2))
            transferUtility = TransferUtility.builder()
                .context(context)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(s3Client)
                .build()
            TransferNetworkLossHandler.getInstance(context)
        }
    }

    @Synchronized
    fun appSyncClient(): AWSAppSyncClient {
        return client!!
    }


    @Synchronized
    fun transferUtility(): TransferUtility {
        return transferUtility!!
    }

}

