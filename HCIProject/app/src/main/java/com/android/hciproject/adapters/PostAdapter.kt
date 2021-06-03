package com.android.hciproject.adapters

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.android.hciproject.ClientFactory
import com.android.hciproject.R
import com.android.hciproject.data.Comment
import com.android.hciproject.data.Post
import java.io.File

class PostAdapter(val clientFactory: ClientFactory) :
    ListAdapter<ListPostsQuery.Item, PostAdapter.PostViewHolder>(DiffCallback) {

    lateinit var context: Context

    object DiffCallback : DiffUtil.ItemCallback<ListPostsQuery.Item>() {
        override fun areItemsTheSame(
            oldItem: ListPostsQuery.Item,
            newItem: ListPostsQuery.Item
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ListPostsQuery.Item,
            newItem: ListPostsQuery.Item
        ): Boolean {
            return oldItem.title() == newItem.title()
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val timeTextView: TextView = itemView.findViewById(R.id.time)
        val contentTextView: TextView = itemView.findViewById(R.id.content)
        val likeNumTextView: TextView = itemView.findViewById(R.id.likeNum)
        val commentNumTextView: TextView = itemView.findViewById(R.id.commentNum)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(currentList[adapterPosition])
            }
        }

        fun downloadWithTransferUtility(photo: String) {
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
//                        localUrl = localPath
                        imageView.setImageBitmap(BitmapFactory.decodeFile(localPath))
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                    val percentDone = percentDonef.toInt()
                    Log.d(
                        ContentValues.TAG,
                        "   ID:$id   bytesCurrent: $bytesCurrent   bytesTotal: $bytesTotal $percentDone%"
                    )
                }

                override fun onError(id: Int, ex: Exception) {
                    // Handle errors
                    Log.e(ContentValues.TAG, "Unable to download the file.", ex)
                }
            })
        }
    }

    lateinit var itemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(post: ListPostsQuery.Item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        context = parent.context
        val holder = PostViewHolder(
            LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        )

        holder.likeNumTextView.isSelected = true
        holder.commentNumTextView.isSelected = true
        holder.titleTextView.isSelected = true
        holder.timeTextView.isSelected = true
        holder.contentTextView.isSelected = true

        return holder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = currentList[position]
        holder.apply {
            titleTextView.text = post.title()
            timeTextView.text = post.updatedAt()
            contentTextView.text = post.content()
            likeNumTextView.text = post.likes().toString()
            if (!post.photo().isNullOrEmpty())
                downloadWithTransferUtility(post.photo()!!)
            if (post.comments() == null)
                commentNumTextView.text = "0"
            else
                commentNumTextView.text = "test"
        }
    }

    override fun submitList(list: MutableList<ListPostsQuery.Item>?) {
        super.submitList(list?.let { ArrayList(it) })
    }


}