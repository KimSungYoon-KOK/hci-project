package com.android.hciproject.testAmplify

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import java.io.File


class MyAdapter(val clientFactory: ClientFactory)
    :RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    lateinit var context: Context
    private var mData: List<ListPostsQuery.Item> = ArrayList()
    var localUrl: String? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var txt_title: TextView = itemView.findViewById(R.id.txt_name)
        private var txt_content: TextView = itemView.findViewById(R.id.txt_description)
        private var image_view: ImageView = itemView.findViewById(R.id.image_view)

        fun bindData(item: ListPostsQuery.Item) {
            txt_title.text = "${item.title()}, uname: ${item.uname()}"
            txt_content.text = item.content()
            if (item.photo() != null) {
                if (localUrl == null) {
                    downloadWithTransferUtility(item.photo()!!)
                } else {
                    image_view.setImageBitmap(BitmapFactory.decodeFile(localUrl))
                }
            } else image_view.setImageBitmap(null)
        }

        private fun downloadWithTransferUtility(photo: String) {
            val localPath = context.externalCacheDir!!.absolutePath + "/${photo}"
//            val localPath: String = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS
//            ).absolutePath.toString() + "/" + photo
            val downloadObserver: TransferObserver = clientFactory.transferUtility().download(
                photo,
                File(localPath)
            )

            // Attach a listener to the observer to get state update and progress notifications
            downloadObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.
                        localUrl = localPath
                        image_view.setImageBitmap(BitmapFactory.decodeFile(localPath))
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                    val percentDone = percentDonef.toInt()
                    Log.d(TAG,"   ID:$id   bytesCurrent: $bytesCurrent   bytesTotal: $bytesTotal $percentDone%")
                }

                override fun onError(id: Int, ex: Exception) {
                    // Handle errors
                    Log.e(TAG, "Unable to download the file.", ex)
                }
            })
        }
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun setItems(items: List<ListPostsQuery.Item>) {
        mData = items
    }

}