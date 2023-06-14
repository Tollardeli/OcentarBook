package com.delloon.ocentar.model

import java.io.Serializable

data class CommentData(val comment: String? = null,
                       val rating: String? = null,
                       val user: String? = null,
                       val approved:Boolean? = false
) : Serializable