package com.android.hciproject.data

import java.io.Serializable

data class Comment(
    val pid : String,
    val uname: String,
    val content: String,
) : Serializable {
    constructor() : this("pid","uname", "content")
}