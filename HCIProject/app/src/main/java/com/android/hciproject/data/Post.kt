package com.android.hciproject.data

import java.io.Serializable

data class Post(
    val pid: String,
    val title: String,
    val img: String,
    val uname: String,
    val content: String,
    val uploadTime: String,
    var comments: ArrayList<Comment>?
) : Serializable {
    constructor() : this("pid", "title", "img", "uname", "content", "time", null)
}