package com.delloon.ocentar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.adapters.RecyclerViewAdapter
import com.delloon.ocentar.databinding.ActivitySearchTagBinding
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.viewmodel.FirebaseViewModel

class SearchTagActivity : AppCompatActivity(), RecyclerViewAdapter.Listener {
    private lateinit var binding: ActivitySearchTagBinding
    private val adapter = RecyclerViewAdapter(null,null,null,null,this)
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchTagBinding.inflate(layoutInflater)
        setContentView(binding.root)
        click()
        initRecyclerView()
        initViewModel()
    }
    private fun click(){
        binding.buttonTagsIn15.setOnClickListener {
            val path = "cookingTime"
            val tag = "За 15 минут"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsIn30.setOnClickListener {
            val path = "cookingTime"
            val tag = "За 30 минут"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsIn1.setOnClickListener {
            val path = "cookingTime"
            val tag = "За 1 час"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsIn2.setOnClickListener {
            val path = "cookingTime"
            val tag = "За 2 часа"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsIn3.setOnClickListener {
            val path = "cookingTime"
            val tag = "За 3 часа"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsMore.setOnClickListener {
            val path = "cookingTime"
            val tag = "Больше"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsSimple.setOnClickListener {
            val path = "difficulty"
            val tag = "Простой"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsAdvanced.setOnClickListener {
            val path = "difficulty"
            val tag = "Продвинутый"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsComplex.setOnClickListener {
            val path = "difficulty"
            val tag = "Сложный"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsOtherOne.setOnClickListener {
            val path = "difficulty"
            val tag = "Другое"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsEuropean.setOnClickListener {
            val path = "kitchen"
            val tag = "Европейская"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsAsian.setOnClickListener {
            val path = "kitchen"
            val tag = "Азиатская"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsPanAsian.setOnClickListener {
            val path = "kitchen"
            val tag = "Паназиатская"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsEastern.setOnClickListener {
            val path = "kitchen"
            val tag = "Восточная"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsAmerican.setOnClickListener {
            val path = "kitchen"
            val tag = "Американская"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsRussian.setOnClickListener {
            val path = "kitchen"
            val tag = "Русская"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsMediterranean.setOnClickListener {
            val path = "kitchen"
            val tag = "Средиземноморская"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }
        binding.buttonTagsOtherTwo.setOnClickListener {
            val path = "kitchen"
            val tag = "Другая"
            firebaseViewModel.loadSearchTagRecipe(path,tag)
        }

    }



    private fun initViewModel(){
        firebaseViewModel.liveRecipeData.observe(this) {
            adapter.updateAdapter(it)
        }
    }

    private fun initRecyclerView(){
        binding.apply {
            RcViewTagsRecipe.layoutManager = LinearLayoutManager(this@SearchTagActivity)
            RcViewTagsRecipe.adapter = adapter
        }
    }

    override fun onFavouriteClicked(recipe: Recipe) {
        firebaseViewModel.onFavouriteClick(recipe)
    }
}