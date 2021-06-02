package com.android.hciproject.data

import java.io.Serializable

data class Post(
    val pid: Int,
    val title: String,
    val img: String,
    val uname: String,
    val content: String,
    val uploadTime: String,
    val uploadLat: Double,
    val uploadLng: Double,
    var comments: ArrayList<Comment>?,
    var like: Int
) : Serializable {
    constructor() : this(1, "title", "img", "uname", "content", "time", 0.0, 0.0, null, 0)

    fun getLikeNumToString(): String {
        return like.toString()
    }
}