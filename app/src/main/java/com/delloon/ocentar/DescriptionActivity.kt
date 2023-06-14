package com.delloon.ocentar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.model.AdminData
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.model.ViewData
import com.delloon.ocentar.adapters.RecyclerViewPreviewCommentAdapter
import com.delloon.ocentar.adapters.RecyclerViewRecipeIngredientAdapter
import com.delloon.ocentar.adapters.RecyclerViewRecipeStepAdapter
import com.delloon.ocentar.constants.Constants
import com.delloon.ocentar.databinding.ActivityDescriptionBinding
import com.delloon.ocentar.dialogHelper.CustomDialogFragment
import com.delloon.ocentar.reviews.AddReviewsActivity
import com.delloon.ocentar.reviews.ReviewsActivity
import com.delloon.ocentar.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import com.squareup.picasso.Picasso


class DescriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDescriptionBinding
    private val adapterIngredient = RecyclerViewRecipeIngredientAdapter()
    private val adapterStep = RecyclerViewRecipeStepAdapter()
    private val adapter = RecyclerViewPreviewCommentAdapter()
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val myAuthentication = FirebaseAuth.getInstance()
    private val customDialog = CustomDialogFragment(this)
    private var dbManager = DBManager()
    private lateinit var recipe: Recipe
    private var role = Constants.USER
    private var profile = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recipe = intent.getSerializableExtra("Recipe") as Recipe
        role = intent.getStringExtra("CheckRole") ?: Constants.USER
        recipe = intent.getSerializableExtra("Recipe") as? Recipe ?: Recipe("")
        profile = intent.getStringExtra("Profile") as String
        Log.d("MyLog",profile)
        getIntentFromRecyclerView()
        binding.buttonReadAll.setOnClickListener {
            startActivity(Intent(this, ReviewsActivity::class.java).apply {
                putExtra("Recipe", recipe)
            })
        }

        binding.buttonAddFastComment.setOnClickListener {
            startActivity(Intent(this, AddReviewsActivity::class.java).apply {
                putExtra("Recipe", recipe)
            })
        }
        binding.buttonEditRecipe.setOnClickListener {
            val intent = Intent(this, AddEditRecipeActivity::class.java).apply {
                putExtra("Recipe", recipe)
            }
            editRecipeResultLauncher.launch(intent)
        }
        binding.buttonShareRecipe.setOnClickListener {
            customDialog.createShareDialog(recipe)
        }

        binding.buttonRejectRecipe.setOnClickListener {
            rejectRecipe()
        }
        binding.buttonApproveRecipe.setOnClickListener {
            approveRecipe()
        }

        binding.buttonRemoveRecipe.setOnClickListener {
            dbManager.removeShareRecipe(recipe,onPublishFinish())
            dbManager.removeUserRecipe(recipe,onPublishFinish())
        }
    }

    private fun approveRecipe() {
        recipe.share = true
        dbManager.addApprovedRecipe(recipe,onPublishFinish())
    }

    private fun rejectRecipe() {
        dbManager.removeShareRecipe(recipe,onPublishFinish())
    }

    private val editRecipeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedRecipe = result.data?.getSerializableExtra("UpdatedRecipe") as? Recipe
            if (updatedRecipe != null) {
                adapterStep.list.clear()
                adapterIngredient.clearData()
                updateUI(updatedRecipe)
            }
        }
    }

    private fun getIntentFromRecyclerView(){
        updateUI(recipe)
        initViewModel()
        initRecyclerView()
        firebaseViewModel.loadPreviewComment(recipe)
    }
    private fun updateUI(recipe: Recipe){
        fillTextView(recipe)
    }

    private fun initViewModel(){
        firebaseViewModel.liveCommentData.observe(this) {
            adapter.updateAdapter(it)
        }
    }
    private fun initRecyclerView(){
        binding.apply {
            recyclerViewFastComment.layoutManager = LinearLayoutManager(this@DescriptionActivity)
            recyclerViewFastComment.adapter = adapter
        }
    }

    private fun fillTextView(recipe: Recipe) = with(binding){

        if(recipe.author == myAuthentication.uid){
            buttonEditRecipe.visibility = View.VISIBLE
            buttonRemoveRecipe.visibility = View.VISIBLE
        } else {
            buttonEditRecipe.visibility = View.GONE
        }
        if(recipe.rating == "0.0"){
            buttonReadAll.visibility = View.GONE
        }
        else {
            buttonReadAll.visibility = View.VISIBLE
            ratingBar.rating = recipe.rating!!.toFloat()
        }
        dbManager.db.collection(Constants.USER_RECIPE).document(recipe.key!!)
            .get()
            .addOnSuccessListener {document ->
                val recipeData = document.toObject<AdminData>()
                if(recipeData!=null){
                    buttonShareRecipe.visibility = View.GONE
                }
                else if(recipe.author == myAuthentication.uid && !recipe.share){
                    buttonShareRecipe.visibility = View.VISIBLE
                }
            }.addOnFailureListener {
                Toast.makeText(this@DescriptionActivity, "Произошла внезапная ошибка. Перезагрузите страницу!", Toast.LENGTH_SHORT).show()
            }

        if(role == Constants.MODERATOR){
            linearLayoutAdminPanelRecipe.visibility = View.VISIBLE
            buttonEditRecipe.visibility = View.GONE
            buttonRemoveRecipe.visibility = View.GONE
        }
        if(!recipe.share){
            linearLayoutCheckReviews.visibility = View.GONE
        }
        else{
            buttonRemoveRecipe.visibility= View.GONE
        }


        Picasso.get().load(recipe.image).into(imageView)
        textViewRecipeName.text = recipe.name


        binding.apply {
            recyclerViewIngredient.layoutManager = LinearLayoutManager(this@DescriptionActivity)
            recyclerViewIngredient.adapter = adapterIngredient
            adapterIngredient.clearData()
            if(myAuthentication.currentUser !=null){
                dbManager.readIngredientRecipeFromDataBase(recipe, object : DBManager.ReadDataIngredientCallBack {
                    override fun readData(list: Recipe, locate:Boolean) {
                        for (i in 0 until recipe.ingredient!!.size) {
                            val line = recipe.ingredient!![i]
                            if(locate){
                                val ingredient = ViewData(line, list.ingredient!!.contains(line),recipe,profile.toBoolean())
                                adapterIngredient.addData(ingredient)
                            }else{
                                val ingredient = ViewData(line, false,recipe,profile.toBoolean())
                                adapterIngredient.addData(ingredient)
                            }
                        }
                    }
                })
            } else{
                for (i in 0 until recipe.ingredient!!.size) {
                    val line = recipe.ingredient!![i]
                    val ingredient = ViewData(line, false,recipe)
                    adapterIngredient.addData(ingredient)
                }
            }

        }
        binding.apply {
            recyclerViewStep.layoutManager = LinearLayoutManager(this@DescriptionActivity)
            recyclerViewStep.adapter = adapterStep
            for (i in 0 until recipe.step!!.size) {
                val line = recipe.step[i]
                if (line.isNotEmpty()) {
                    val step = ViewData("Шаг ${i+1}: $line",false, recipe)
                    adapterStep.addData(step)
                }
            }
        }
    }
    private fun onPublishFinish(): DBManager.FinishWorkListener{
        return object : DBManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }

        }
    }

}