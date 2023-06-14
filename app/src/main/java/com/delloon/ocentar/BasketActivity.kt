package com.delloon.ocentar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.adapters.RecyclerViewBasketAdapter
import com.delloon.ocentar.adapters.RecyclerViewRecipeIngredientAdapter
import com.delloon.ocentar.databinding.ActivityBasketBinding
import com.delloon.ocentar.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth

class BasketActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBasketBinding
    private val adapter = RecyclerViewBasketAdapter()
    private val adapterIngredient = RecyclerViewRecipeIngredientAdapter()
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    val auth  = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBasketBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(auth.currentUser !=null){
            initRecyclerView()
            initViewModel()
            firebaseViewModel.loadBasketRecipe()
            binding.swipeRefreshLayoutBasket.setOnRefreshListener {
                adapterIngredient.clearData()
                firebaseViewModel.loadBasketRecipe()
                binding.swipeRefreshLayoutBasket.isRefreshing = false
            }
        }
    }

    private fun initViewModel() {
        firebaseViewModel.liveRecipeData.observe(this) {
            adapter.updateAdapter(it)
        }
    }

    private fun initRecyclerView() {
        binding.recyclerViewBasket.layoutManager = LinearLayoutManager(this@BasketActivity)
        binding.recyclerViewBasket.adapter = adapter
    }
}
