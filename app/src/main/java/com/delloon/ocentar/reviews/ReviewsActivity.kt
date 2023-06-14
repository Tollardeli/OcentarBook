package com.delloon.ocentar.reviews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.R
import com.delloon.ocentar.adapters.RecyclerViewCommentAdapter
import com.delloon.ocentar.databinding.ActivityReviewsBinding
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.viewmodel.FirebaseViewModel

class ReviewsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityReviewsBinding
    private val adapter = RecyclerViewCommentAdapter(this)
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var dbManager = DBManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val recipe = intent.getSerializableExtra("Recipe") as Recipe
        init(recipe)
        binding.buttonAddComment.setOnClickListener {
            startActivity(Intent(this, AddReviewsActivity::class.java).apply {
                putExtra("Recipe", recipe)
            })
        }
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun init(recipe: Recipe){
        initViewModel(recipe)
        initRecyclerView()
        firebaseViewModel.loadComment(recipe)
        binding.textViewRecipeNameReviews.text = recipe.name
        binding.textViewRatingRecipe.text = getString(R.string.average_rating_recipe_text, recipe.rating)
        countRating(recipe)
    }

    private fun countRating(recipe: Recipe) {
        val docRef = dbManager.db.collection("ApprovedRecipe").document(recipe.key!!).collection("comments")
        docRef.get()
            .addOnSuccessListener { querySnapshot ->
                val documentCount = querySnapshot.size()
                val commentCountString = resources.getQuantityString(R.plurals.comment_count, documentCount, documentCount)
                binding.textViewCommentCount.text = commentCountString
            }
            .addOnFailureListener { exception ->
                Log.d("MyLog", "countRating: $exception")
            }
    }


    private fun initViewModel(recipe: Recipe){
        firebaseViewModel.liveCommentData.observe(this) {
            adapter.updateAdapter(it,recipe)
        }
    }
    private fun initRecyclerView(){
        binding.apply {
            recyclerViewComment.layoutManager = LinearLayoutManager(this@ReviewsActivity)
            recyclerViewComment.adapter = adapter
        }
    }

}