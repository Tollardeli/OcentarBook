package com.delloon.ocentar.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.DescriptionActivity
import com.delloon.ocentar.constants.Constants
import com.delloon.ocentar.databinding.ListItemUserRecipeCheckBinding
import com.delloon.ocentar.model.Recipe
import com.squareup.picasso.Picasso

class RecyclerViewUserRecipeCheckAdapter: RecyclerView.Adapter<RecyclerViewUserRecipeCheckAdapter.UserRecipeCheckViewHolder>() {
    private val userRecipeArray = ArrayList<Recipe>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRecipeCheckViewHolder {
        val binding = ListItemUserRecipeCheckBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserRecipeCheckViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userRecipeArray.size
    }

    override fun onBindViewHolder(holder: UserRecipeCheckViewHolder, position: Int) {
        holder.setData(userRecipeArray[position])

    }
    fun updateAdapter(newList: List<Recipe>){
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelperRecipe(userRecipeArray,newList))
        diffResult.dispatchUpdatesTo(this)
        userRecipeArray.clear()
        userRecipeArray.addAll(newList)
    }
    class UserRecipeCheckViewHolder(val binding: ListItemUserRecipeCheckBinding): RecyclerView.ViewHolder(binding.root){
        fun setData(recipe: Recipe) {
            binding.apply {
                textViewUserRecipeName.text = recipe.name
                Picasso.get().load(recipe.image).into(imageViewUserRecipe)
                buttonUserRecipeTagKitchen.text = recipe.kitchen
                buttonUserRecipeTagDifficulty.text = recipe.difficulty
                buttonUserRecipeTagCookingTime.text = recipe.cookingTime
                buttonViewUserRecipe.setOnClickListener {
                    val intent = Intent(binding.root.context, DescriptionActivity::class.java)
                    intent.putExtra("CheckRole", Constants.MODERATOR)
                    intent.putExtra("Recipe", recipe)
                    intent.putExtra("Profile", "false")
                    binding.root.context.startActivity(intent)

                }
            }
        }

    }
}