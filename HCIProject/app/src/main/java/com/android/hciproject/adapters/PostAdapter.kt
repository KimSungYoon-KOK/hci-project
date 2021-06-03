package com.android.hciproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.hciproject.R
import com.android.hciproject.data.Comment
import com.android.hciproject.data.Post

class PostAdapter : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.title == newItem.title
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val timeTextView: TextView = itemView.findViewById(R.id.time)
        val contentTextView: TextView = itemView.findViewById(R.id.content)
        val likeNumTextView: TextView = itemView.findViewById(R.id.likeNum)
        val commentNumTextView: TextView = itemView.findViewById(R.id.commentNum)

        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(currentList[adapterPosition])
            }
        }
    }

    lateinit var itemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(post: Post)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val holder = PostViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
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
            titleTextView.text = post.title
            timeTextView.text = post.uploadTime
            contentTextView.text = post.content
            likeNumTextView.text = post.like.toString()
            if (post.comments == null)
                commentNumTextView.text = "0"
            else
                commentNumTextView.text = post.comments!!.size.toString()
        }
    }

    override fun submitList(list: MutableList<Post>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

}