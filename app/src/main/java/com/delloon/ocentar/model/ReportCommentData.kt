package com.delloon.ocentar.model

import java.io.Serializable

data class ReportCommentData(
    val uidAuthor: String? = null,
    val uidRecipe: String? = null,
    val uid: String? = null,
    val photoProfile: String? = null,
    val comment: String? = null,
    val nickname: String? = null
) : Serializable
