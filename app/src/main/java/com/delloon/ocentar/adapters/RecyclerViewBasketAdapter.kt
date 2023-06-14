package com.delloon.ocentar.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.DescriptionActivity
import com.delloon.ocentar.databinding.ListBasketIngredientBinding
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.ViewData
import com.squareup.picasso.Picasso

class RecyclerViewBasketAdapter : RecyclerView.Adapter<RecyclerViewBasketAdapter.RecipeBasketViewHolder>() {
    private val recipeArray = ArrayList<Recipe>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeBasketViewHolder {
        val binding = ListBasketIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeBasketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeBasketViewHolder, position: Int) {
        holder.setData(recipeArray[position])
    }

    override fun getItemCount(): Int {
        return recipeArray.size
    }
    fun updateAdapter(newList: List<Recipe>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelperRecipe(recipeArray, newList))
        diffResult.dispatchUpdatesTo(this)
        recipeArray.clear()
        recipeArray.addAll(newList)
    }
    class RecipeBasketViewHolder(val binding: ListBasketIngredientBinding) : RecyclerView.ViewHolder(binding.root) {
        val dbManager = DBManager()
        private val adapterIngredient = RecyclerViewRecipeIngredientAdapter()

        init {
            binding.recyclerViewBasketIngredient.layoutManager = LinearLayoutManager(binding.root.context)
            binding.recyclerViewBasketIngredient.adapter = adapterIngredient
        }

        fun setData(recipe: Recipe) = with(binding) {
            textViewAllRecipeName.text = recipe.name
            Picasso.get().load(recipe.image).into(imageViewAllRecipe)
            buttonViewRecipe.setOnClickListener {
                val intent = Intent(binding.root.context, DescriptionActivity::class.java)
                intent.putExtra("Recipe", recipe)
                intent.putExtra("Profile", "false")
                binding.root.context.startActivity(intent)
            }

            dbManager.readIngredientRecipeFromDataBase(recipe, object: DBManager.ReadDataIngredientCallBack {
                override fun readData(list: Recipe, locate:Boolean) {
                    adapterIngredient.clearData()
                    for (i in 0 until list.ingredient!!.size) {
                        val line = list.ingredient!![i]
                        if (line.isNotEmpty()) {
                            val ingredient = ViewData(line, locate,recipe)
                            Log.d("MyLog","1. $ingredient")
                            adapterIngredient.addData(ingredient)
                        }
                    }
                }
            })
        }
    }
}
