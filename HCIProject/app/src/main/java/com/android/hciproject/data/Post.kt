package com.android.hciproject.data

import androidx.databinding.ObservableInt
import com.amazonaws.amplify.generated.graphql.ListPostsQuery
import java.io.Serializable

data class Post(
    var pid: String,
    var title: String,
    var img: String?,
    var uname: String?,
    var content: String?,
    var uploadTime: String?,
    var uploadLat: Double?,
    var uploadLng: Double?,
    var comments: ArrayList<Comment>?,
    var like: ObservableInt
) : Serializable {
    constructor() : this("id", "title", "img", "uname", "content", "time", 0.0, 0.0, null, ObservableInt(0))

    fun getLikeNumToString(): String {
        return like.toString()
    }

    constructor(p: ListPostsQuery.Item) : this() {
        pid = p.id()
        title = p.title()
        img = p.photo()
        uname = p.uname()
        content = p.content()
        uploadTime = p.createdAt()
        uploadLat = p.uploadLat()!!.toDouble()
        uploadLng = p.uploadLng()!!.toDouble()
        like = ObservableInt(p.likes()!!)
        comments = null
    }
}