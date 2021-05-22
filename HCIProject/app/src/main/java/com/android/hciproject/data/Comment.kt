package com.android.hciproject.data

import java.io.Serializable

data class Comment(
    val uname: String,
    val content: String,
    val uploadTime: String
) : Serializable {
    constructor() : this("uname", "content", "uploadTime")
}