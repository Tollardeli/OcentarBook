package com.delloon.ocentar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.adapters.RecyclerViewAdapter
import com.delloon.ocentar.databinding.ActivityOutputAllRecipesBinding
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.viewmodel.FirebaseViewModel

class OutputAllRecipesActivity : AppCompatActivity(), RecyclerViewAdapter.Listener {
    private lateinit var binding: ActivityOutputAllRecipesBinding
    private val adapter = RecyclerViewAdapter(this,null,null,null,null)
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutputAllRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadAllRecipe()
    }
    private fun initViewModel(){
        firebaseViewModel.liveRecipeData.observe(this) {
            adapter.updateAdapter(it)
        }
    }
    private fun initRecyclerView(){
        binding.apply {
            recyclerViewAllRecipe.layoutManager = LinearLayoutManager(this@OutputAllRecipesActivity)
            recyclerViewAllRecipe.adapter = adapter
        }
    }
    override fun onFavouriteClicked(recipe: Recipe) {
        firebaseViewModel.onFavouriteClick(recipe)
    }


}