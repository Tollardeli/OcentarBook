package com.delloon.ocentar.model

import android.net.Uri
import java.io.Serializable
import java.sql.Timestamp
import java.util.Date

data class User(
    val nickname: String? = null, val email: String? = null,
    val uid: String? = null, val photoprofile: String? = null,
    val role: String? = null, val dateBlock: String? = ""
) : Serializable