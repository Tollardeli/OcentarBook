package com.delloon.ocentar.model

import com.delloon.ocentar.model.Recipe

data class ViewData(val listItem: String, var isChecked: Boolean = false, val recipe: Recipe, val profileActivity: Boolean = false)
