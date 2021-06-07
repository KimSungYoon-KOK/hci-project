package com.android.hciproject.adapters

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListCommentsQuery
import com.amazonaws.amplify.generated.graphql.ListPostsQuery

@BindingAdapter("bind_visibility")
fun ProgressBar.bindVisibility(loading: Boolean) {
    visibility = when (loading) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}

@BindingAdapter("bind_visibility")
fun RecyclerView.bindPostsVisibility(posts: List<ListPostsQuery.Item>) {
    if (posts.isNullOrEmpty()) View.GONE else View.VISIBLE
}

@BindingAdapter("bind_visibility")
fun RecyclerView.bindCommentsVisibility(comments: List<ListCommentsQuery.Item>?) {
    if (comments.isNullOrEmpty()) View.GONE else View.VISIBLE
}

