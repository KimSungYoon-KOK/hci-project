package com.android.hciproject.adapters

import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.hciproject.data.Post

@BindingAdapter("bind_visibility")
fun ProgressBar.bindVisibility(loading: Boolean) {
    visibility = when (loading) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}

@BindingAdapter("bind_visibility")
fun RecyclerView.bindPostsVisibility(posts:List<Post>) {
    if (posts.isNullOrEmpty()) View.GONE else View.VISIBLE
}

