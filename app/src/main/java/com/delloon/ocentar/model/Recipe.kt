package com.delloon.ocentar.model

import java.io.Serializable

data class Recipe(
    val name: String? = null, val image: String? = null,
    var ingredient: List<String>? = null, val step: List<String>? = null,
    val cookingTime: String? = null, val difficulty: String? = null,
    val kitchen: String? = null, val author: String? = null,
    val key: String? = null, var isFavourite: Boolean = false,
    var rating: String? = null, var share: Boolean = false
) : Serializable