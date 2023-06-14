package com.delloon.ocentar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.adapters.RecyclerViewAdapter
import com.delloon.ocentar.databinding.ActivitySearchRecipeBinding
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.viewmodel.FirebaseViewModel

class SearchRecipeActivity : AppCompatActivity(), RecyclerViewAdapter.Listener{
    private lateinit var binding : ActivitySearchRecipeBinding
    private val adapter = RecyclerViewAdapter(null,null,this,null,null)
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editTextSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText: String = binding.editTextSearch.text.toString()
                initRecyclerView()
                initViewModel()
                firebaseViewModel.loadSearchRecipe(searchText)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }
    private fun initViewModel(){
        firebaseViewModel.liveRecipeData.observe(this) {
            adapter.updateAdapter(it)
        }
    }
    private fun initRecyclerView(){
        binding.apply {
            recyclerViewSearchName.layoutManager = LinearLayoutManager(this@SearchRecipeActivity)
            recyclerViewSearchName.adapter = adapter
        }
    }
    override fun onFavouriteClicked(recipe: Recipe) {
        firebaseViewModel.onFavouriteClick(recipe)
        Log.d("Fav","model2 : $recipe")
    }

}