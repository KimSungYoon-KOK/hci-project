package com.android.hciproject.data

import com.amazonaws.amplify.generated.graphql.ListCommentsQuery
import java.io.Serializable

data class Comment(
    var pid : String,
    var uname: String?,
    var content: String,
) : Serializable {
    constructor() : this("pid","uname", "content")

    constructor(p: ListCommentsQuery.Item) : this() {
        pid = p.postID()
        uname = p.uname()
        content = p.content()
    }
}