package com.delloon.ocentar.dialogHelper

import android.app.AlertDialog
import com.delloon.ocentar.DescriptionActivity
import com.delloon.ocentar.databinding.DialogShareBinding
import com.delloon.ocentar.model.AdminData
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.Recipe

class CustomDialogFragment(private val activity: DescriptionActivity) {
    private var dbManager = DBManager()
    fun createShareDialog(recipe: Recipe) {
        val dialogBuilder = AlertDialog.Builder(activity)
        val binding = DialogShareBinding.inflate(activity.layoutInflater)
        dialogBuilder.setView(binding.root)

        val dialog = dialogBuilder.create()
        binding.buttonPossitive.setOnClickListener {
            setOnClickShareRecipe(recipe, dialog)
        }
        binding.buttonNegative.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setOnClickShareRecipe(recipe: Recipe, dialog: AlertDialog?) {
        val userRecipe = AdminData(recipe.author, recipe.key)
        dbManager.publishUserRecipe(recipe,userRecipe,activity)
        dialog?.dismiss()
    }
}