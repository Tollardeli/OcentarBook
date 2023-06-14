package com.delloon.ocentar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.delloon.ocentar.chatGPT.MoreRecipeActivity
import com.delloon.ocentar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.buttonAllRecipe.setOnClickListener {
            startActivity(Intent(this, OutputAllRecipesActivity::class.java))
        }
        binding.buttonSearchRecipe.setOnClickListener {
            startActivity(Intent(this, SearchRecipeActivity::class.java))
        }
        binding.buttonFavourites.setOnClickListener {
            startActivity(Intent(this, FavouritesActivity::class.java))
        }
        binding.buttonSearchFromTag.setOnClickListener {
            startActivity(Intent(this, SearchTagActivity::class.java))
        }

        binding.buttonMoreRecipe.setOnClickListener {
            startActivity(Intent(this, MoreRecipeActivity::class.java))
        }

        binding.buttonBasket.setOnClickListener {
            startActivity(Intent(this, BasketActivity::class.java))
        }

    }

}