package com.delloon.ocentar.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.DescriptionActivity
import com.delloon.ocentar.FavouritesActivity
import com.delloon.ocentar.OutputAllRecipesActivity
import com.delloon.ocentar.ProfileActivity
import com.delloon.ocentar.SearchRecipeActivity
import com.delloon.ocentar.SearchTagActivity
import com.delloon.ocentar.*
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class RecyclerViewAdapter(
    val activityAllRecipe: OutputAllRecipesActivity?,
    val activityProfile: ProfileActivity?,
    val activitySearch: SearchRecipeActivity?,
    val activityFavourites: FavouritesActivity?,
    val activitySearchTag: SearchTagActivity?
) : RecyclerView.Adapter<RecyclerViewAdapter.RecipeViewHolder>() {
    val recipeArray = ArrayList<Recipe>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(
            binding,
            activityAllRecipe,
            activityProfile,
            activitySearch,
            activityFavourites,
            activitySearchTag
        )
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
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

    class RecipeViewHolder(
        val binding: ListItemBinding,
        val activityAllRecipe: OutputAllRecipesActivity?,
        val activityProfile: ProfileActivity?,
        val activitySearch: SearchRecipeActivity?,
        val activityFavourites: FavouritesActivity?,
        val activitySearchTag: SearchTagActivity?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(recipe: Recipe) = with(binding) {
            textViewAllRecipeName.text = recipe.name
            Picasso.get().load(recipe.image).into(imageViewAllRecipe)
            buttonTagKitchen.text = recipe.kitchen
            buttonTagDifficulty.text = recipe.difficulty
            buttonTagCookingTime.text = recipe.cookingTime
            if (activityProfile != null) {
                imageButtonAddFavoriteRecipe.visibility = View.GONE
            } else {
                if (recipe.isFavourite) {
                    imageButtonAddFavoriteRecipe.setImageResource(R.drawable.ic_like)
                } else {
                    imageButtonAddFavoriteRecipe.setImageResource(R.drawable.ic_unlike)
                }
                imageButtonAddFavoriteRecipe.setOnClickListener {
                    if (activityAllRecipe != null) {
                        activityAllRecipe.onFavouriteClicked(recipe)
                    } else if (activitySearch != null) {
                        activitySearch.onFavouriteClicked(recipe)
                    } else if (activityFavourites != null) {
                        activityFavourites.onFavouriteClicked(recipe)
                    } else if (activitySearchTag != null) {
                        activitySearchTag.onFavouriteClicked(recipe)
                    }
                }
            }
            buttonViewRecipe.setOnClickListener {
                val intent = Intent(binding.root.context, DescriptionActivity::class.java)
                intent.putExtra("Recipe", recipe)
                if(activityProfile!=null){
                    intent.putExtra("Profile", "true")
                }
                else{
                    intent.putExtra("Profile", "false")
                }
                binding.root.context.startActivity(intent)
            }
        }
    }

    interface Listener {
        fun onFavouriteClicked(recipe: Recipe)
    }

}
