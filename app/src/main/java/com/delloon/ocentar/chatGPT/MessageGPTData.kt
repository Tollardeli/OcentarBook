package com.delloon.ocentar.chatGPT

import java.io.Serializable

data class MessageGPTData(val message: String, val isUserMessage: Boolean)
 : Serializable
