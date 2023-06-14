package com.delloon.ocentar.adminPanel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.adapters.RecyclerViewUserRecipeCheckAdapter
import com.delloon.ocentar.databinding.ActivityUserRecipeBinding
import com.delloon.ocentar.viewmodel.FirebaseViewModel

class UserRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserRecipeBinding
    private val adapter = RecyclerViewUserRecipeCheckAdapter()
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadUserRecipe()
        binding.swipeRefreshLayoutUserRecipe.setOnRefreshListener {
            firebaseViewModel.loadUserRecipe()
            binding.swipeRefreshLayoutUserRecipe.isRefreshing = false
        }
    }
    private fun initViewModel(){
        firebaseViewModel.liveRecipeData.observe(this) {
            adapter.updateAdapter(it)
        }
    }
    private fun initRecyclerView(){
        binding.apply {
            recyclerViewUserRecipe.layoutManager = LinearLayoutManager(this@UserRecipeActivity)
            recyclerViewUserRecipe.adapter = adapter
        }
    }
}