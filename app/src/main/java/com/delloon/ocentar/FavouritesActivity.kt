package com.delloon.ocentar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.adapters.RecyclerViewAdapter
import com.delloon.ocentar.databinding.ActivityFavouritesBinding
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth

class FavouritesActivity : AppCompatActivity(), RecyclerViewAdapter.Listener {
    private lateinit var binding: ActivityFavouritesBinding
    private val adapter = RecyclerViewAdapter(null,null,null,this,null)
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val myAuthentication = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadFavouriteRecipe(myAuthentication.currentUser)
    }
    private fun initViewModel(){
        firebaseViewModel.liveRecipeData.observe(this) {
            adapter.updateAdapter(it)
        }
    }

    private fun initRecyclerView(){
        binding.apply {
            recyclerViewFavourites.layoutManager = LinearLayoutManager(this@FavouritesActivity)
            recyclerViewFavourites.adapter = adapter
        }
    }

    override fun onFavouriteClicked(recipe: Recipe) {
        firebaseViewModel.onFavouriteClick(recipe)
    }
}