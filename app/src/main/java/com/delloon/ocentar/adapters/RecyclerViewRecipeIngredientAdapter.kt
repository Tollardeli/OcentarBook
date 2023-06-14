package com.delloon.ocentar.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.R
import com.delloon.ocentar.databinding.ListViewIngredientItemBinding
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.model.ViewData
import com.google.firebase.auth.FirebaseAuth

class RecyclerViewRecipeIngredientAdapter : RecyclerView.Adapter<RecyclerViewRecipeIngredientAdapter.RecipeDecHolder>() {
    private val list = ArrayList<ViewData>()
    private var recipeBasic = ArrayList<Recipe>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeDecHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_view_ingredient_item, parent, false)
        return RecipeDecHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeDecHolder, position: Int) {
        if (list.isEmpty()) {
            holder.clearData()
        } else {
            holder.bindData(list[position])
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun clearData() {
        val itemCount = list.size
        list.clear()
        notifyItemRangeRemoved(0, itemCount)
    }

    fun addData(viewData: ViewData) {
        val newList = ArrayList(list)
        newList.add(ViewData(viewData.listItem, viewData.isChecked, viewData.recipe,viewData.profileActivity))
        val diffResult = DiffUtil.calculateDiff(RecipeDiffCallback(list, newList))
        list.clear()
        list.addAll(newList)
        recipeBasic.add(viewData.recipe)
        diffResult.dispatchUpdatesTo(this)
    }

    class RecipeDecHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dbManager = DBManager()
        private val binding = ListViewIngredientItemBinding.bind(itemView)
        val auth  = FirebaseAuth.getInstance()
        fun bindData(viewData: ViewData) {
            with(binding) {
                checkBoxListItem.text = viewData.listItem
                if(auth.currentUser != null){
                    checkBoxListItem.isChecked = viewData.isChecked
                    checkBoxListItem.setOnClickListener {
                        val isChanged = binding.checkBoxListItem.isChecked
                        if (isChanged) {
                            if(!viewData.profileActivity){
                                Log.d("MyLog",viewData.profileActivity.toString())
                                dbManager.addBasketRecipe(binding.checkBoxListItem.text.toString(), viewData.recipe, onPublishFinish())
                            }
                            else{
                                checkBoxListItem.isChecked = true
                                checkBoxListItem.isEnabled = false
                            }
                        } else {
                            if(!viewData.profileActivity) {
                                dbManager.removeBasketRecipe(
                                    binding.checkBoxListItem.text.toString(),
                                    viewData.recipe,
                                    onPublishFinish()
                                )
                            }
                            else{
                                checkBoxListItem.isChecked = true
                                checkBoxListItem.isEnabled = false
                            }
                        }
                    }
                }
                else{
                    checkBoxListItem.setOnClickListener {
                        Toast.makeText(binding.root.context, "Зарегестрируйтесь или войдите в аккаунт!", Toast.LENGTH_SHORT).show()
                        checkBoxListItem.isEnabled = false
                    }

                }
            }
        }


        fun clearData() {
            with(binding) {
                checkBoxListItem.text = null
                checkBoxListItem.isChecked = false
            }
        }



        private fun onPublishFinish(): DBManager.FinishWorkListener {
            return object : DBManager.FinishWorkListener {
                override fun onFinish() {

                }
            }
        }
    }
}
