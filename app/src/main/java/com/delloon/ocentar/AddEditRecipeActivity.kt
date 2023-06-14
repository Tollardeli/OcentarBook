package com.delloon.ocentar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.databinding.ActivityAddEditRecipeBinding
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class AddEditRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditRecipeBinding
    private lateinit var name: String
    private var recipeIngredients: MutableList<String> = mutableListOf()
    private var recipeStep: MutableList<String> = mutableListOf()
    private lateinit var cookingTime: String
    private lateinit var difficulty: String
    private lateinit var kitchen: String
    private lateinit var selectedImage: Uri
    private var dbManager = DBManager()
    private val myAuthentication = FirebaseAuth.getInstance()
    private lateinit var recipe: Recipe
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recipe = intent.getSerializableExtra("Recipe") as? Recipe ?: Recipe("")
        if(recipe.key == null){
            initialization()
        }
        else{
            initializationEdit()
        }
        binding.buttonAddStepItem.setOnClickListener {
            addOneStep()
        }
        binding.buttonAddIngredientItem.setOnClickListener {
            addOneIngredient()
        }
        binding.buttonSave.setOnClickListener{
            initializeVariables()
            if (checkInput()) {
                val recipeTemp = fillRecipe()
                uploadImage(recipeTemp)
            }
        }
        binding.buttonAddImageRecipe.setOnClickListener {
            setImageRecipe()
        }
    }
    private fun checkInput(): Boolean {
        return when {
            name.isEmpty() ->{
                Toast.makeText(this, getString(R.string.empty_name), Toast.LENGTH_SHORT).show()
                false
            }
            selectedImage == Uri.EMPTY ->{
                Toast.makeText(this, getString(R.string.empty_image), Toast.LENGTH_SHORT).show()
                false
            }
            recipeIngredients.isEmpty() ->{
                Toast.makeText(this, getString(R.string.empty_ingredient), Toast.LENGTH_SHORT).show()
                false
            }
            recipeStep.isEmpty() ->{
                Toast.makeText(this, getString(R.string.empty_step), Toast.LENGTH_SHORT).show()
                false
            }
            cookingTime.isEmpty() ->{
                Toast.makeText(this, getString(R.string.empty_cooking_time), Toast.LENGTH_SHORT).show()
                false
            }
            difficulty.isEmpty() ->{
                Toast.makeText(this, getString(R.string.empty_difficulty), Toast.LENGTH_SHORT).show()
                false
            }
            kitchen.isEmpty() ->{
                Toast.makeText(this, getString(R.string.empty_kitchen), Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun initialization(){
        addOneStep()
        addOneIngredient()
    }
    private fun initializationEdit(){
        binding.apply {
            editTextAddRecipeName.setText(recipe.name)
            textViewRecipeHint.visibility = View.GONE
            if (recipe.image != null) {
                selectedImage = Uri.parse(recipe.image)
                binding.buttonAddImageRecipe.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // Remove the listener to avoid multiple calls
                        binding.buttonAddImageRecipe.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        // Load the image using Picasso with the available dimensions
                        Picasso.get().load(selectedImage)
                            .resize(binding.buttonAddImageRecipe.width, binding.buttonAddImageRecipe.height)
                            .into(binding.viewAddRecipeImage)
                    }
                })
                Picasso.get().load(selectedImage).into(binding.viewAddRecipeImage)

            }
            for (i in 0 until recipe.ingredient!!.size) {
                listIngredient.addView(createEditText())
                listIngredient.getChildAt(i).findViewById<EditText>(R.id.editTextStep).setText(recipe.ingredient!![i])
            }
            for (i in 0 until recipe.step!!.size) {
                listStep.addView(createEditText())
                listStep.getChildAt(i).findViewById<EditText>(R.id.editTextStep).setText(recipe.step!![i])
            }
            spinnerCookingTimeTag.setSpinnerText(recipe.cookingTime.toString())
            spinnerDifficultyTag.setSpinnerText(recipe.difficulty.toString())
            spinnerKitchenTag.setSpinnerText(recipe.kitchen.toString())
        }
    }
    private fun Spinner.setSpinnerText(text: String) {
        for (i in 0 until this.adapter.count) {
            if (this.adapter.getItem(i).toString().contains(text)) {
                this.setSelection(i)
            }
        }
    }

    private fun setImageRecipe() {
        ImagePicker.with(this)
            .crop(4f, 4f)
            .maxResultSize(800, 800)
            .provider(ImageProvider.BOTH)
            .createIntentFromDialog { launcher.launch(it) }
    }
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                binding.textViewRecipeHint.visibility = View.GONE
                selectedImage = it.data?.data!!
                Picasso.get().load(selectedImage).resize(binding.buttonAddImageRecipe.width,binding.buttonAddImageRecipe.height).into(binding.viewAddRecipeImage)
            }
        }


    private fun uploadImage(recipeTemp: Recipe) {
        val imageStorageReference = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        if(recipe.image == null) {
            val uploadTask = imageStorageReference.putFile(selectedImage)
            uploadTask.continueWithTask {
                imageStorageReference.downloadUrl
            }.addOnCompleteListener {
                recipeIngredients.clear()
                recipeStep.clear()
                initializeVariables()
                dbManager.addRecipe(
                    recipeTemp.copy(image = it.result.toString()),
                    onPublishFinish()
                )
            }
        } else{
            val uploadTask = imageStorageReference.putFile(selectedImage)
            uploadTask.continueWithTask {
                imageStorageReference.downloadUrl
            }.addOnCompleteListener {
                if(it.isSuccessful()){
                    recipeIngredients.clear()
                    recipeStep.clear()
                    initializeVariables()
                    dbManager.addRecipe(
                        recipeTemp.copy(image = it.result.toString()),
                        onPublishFinish()
                    )
                }
                else{
                    recipeIngredients.clear()
                    recipeStep.clear()
                    initializeVariables()
                    dbManager.addRecipe(recipeTemp.copy(image = selectedImage.toString()),
                        onPublishFinish())
                }

            }
        }

    }

    private fun addOneIngredient() {
        binding.listIngredient.addView(createEditText())

    }

    private fun addOneStep() {
        binding.listStep.addView(createEditText())
    }

    private fun createEditText(): View {
        return LayoutInflater.from(this).inflate(R.layout.step, binding.listStep, false)
    }
    private fun initializeVariables() {
        name = binding.editTextAddRecipeName.text.toString()
        for (i in 0 until binding.listStep.childCount) {
            val line = binding.listStep.getChildAt(i).findViewById<EditText>(R.id.editTextStep).text.toString()
            if (line.isNotEmpty()) {
                recipeStep.add(line)
            }
        }
        for (i in 0 until binding.listIngredient.childCount) {
            val line = binding.listIngredient.getChildAt(i).findViewById<EditText>(R.id.editTextStep).text.toString()
            if (line.isNotEmpty()) {
                recipeIngredients.add(line)
            }
        }
        cookingTime = binding.spinnerCookingTimeTag.selectedItem.toString()
        difficulty = binding.spinnerDifficultyTag.selectedItem.toString()
        kitchen = binding.spinnerKitchenTag.selectedItem.toString()

    }
    private fun fillRecipe(): Recipe {
        if(recipe.key==null){
            val recipe : Recipe
            binding.apply {
                recipe = Recipe(editTextAddRecipeName.text.toString(),
                    "empty",
                    recipeIngredients,
                    recipeStep,
                    binding.spinnerCookingTimeTag.selectedItem.toString(),
                    binding.spinnerDifficultyTag.selectedItem.toString(),
                    binding.spinnerKitchenTag.selectedItem.toString(),
                    myAuthentication.currentUser?.uid,
                    dbManager.db.collection("Users").document(myAuthentication.currentUser!!.uid).collection("UserRecipe").document().id,false,"0.0",false
                )
            }
            return recipe
        } else {
            val recipeKey = recipe.key
            val rating = recipe.rating
            val recipe : Recipe
            binding.apply {
                recipe = Recipe(editTextAddRecipeName.text.toString(),
                    "empty",
                    recipeIngredients,
                    recipeStep,
                    binding.spinnerCookingTimeTag.selectedItem.toString(),
                    binding.spinnerDifficultyTag.selectedItem.toString(),
                    binding.spinnerKitchenTag.selectedItem.toString(),
                    myAuthentication.currentUser?.uid,
                    recipeKey,false, rating,false
                )
            }
            return recipe
        }
    }
    fun onDelete(view: View) {
        binding.listStep.removeView(view.parent as View)
        binding.listIngredient.removeView(view.parent as View)
    }
    private fun onPublishFinish(): DBManager.FinishWorkListener{
        return object : DBManager.FinishWorkListener{
            override fun onFinish() {
                if(recipe.image != null) {
                    val recipeKey = recipe.key
                    binding.apply {
                        recipe = Recipe(
                            editTextAddRecipeName.text.toString(),
                            selectedImage.toString(),
                            recipeIngredients,
                            recipeStep,
                            spinnerCookingTimeTag.selectedItem.toString(),
                            spinnerDifficultyTag.selectedItem.toString(),
                            spinnerKitchenTag.selectedItem.toString(),
                            myAuthentication.currentUser?.uid,
                            recipeKey, false, "0.0", false
                        )
                    }
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra("UpdatedRecipe", recipe)
                    })
                }
                finish()
            }

        }
    }

}
