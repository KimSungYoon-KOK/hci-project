package com.android.hciproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListCommentsQuery
import com.android.hciproject.R

class CommentAdapter : ListAdapter<ListCommentsQuery.Item, CommentAdapter.CommentViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ListCommentsQuery.Item>() {
        override fun areItemsTheSame(oldItem: ListCommentsQuery.Item, newItem: ListCommentsQuery.Item): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ListCommentsQuery.Item, newItem: ListCommentsQuery.Item): Boolean {
            return oldItem.content() == newItem.content()
        }
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uname: TextView = itemView.findViewById(R.id.username)
        val content: TextView = itemView.findViewById(R.id.content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val holder = CommentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        )

        holder.uname.isSelected = true
        holder.content.isSelected = true

        return holder
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = currentList[position]
        holder.apply {
            uname.text = comment.uname()
            content.text = comment.content()
        }
    }

    override fun submitList(list: MutableList<ListCommentsQuery.Item>?) {
        super.submitList(list?.let { ArrayList(it) })
    }
}