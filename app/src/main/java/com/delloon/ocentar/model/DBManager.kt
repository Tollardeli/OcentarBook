package com.delloon.ocentar.model

import android.util.Log
import android.widget.Toast
import com.delloon.ocentar.DescriptionActivity
import com.delloon.ocentar.adminPanel.ReportCommentActivity
import com.delloon.ocentar.constants.Constants
import com.delloon.ocentar.reviews.ReviewsActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Calendar
import java.util.Date
import kotlin.math.round

class DBManager {
    val db = FirebaseFirestore.getInstance()
    val dbStorage = Firebase.storage.getReference("ImageRecipe")
    val auth  = FirebaseAuth.getInstance()
    fun addRecipe(recipe: Recipe, finishListener: FinishWorkListener){
        db.collection("Users").document(auth.uid!!).collection("UserRecipe")
            .document(recipe.key.toString())
            .set(recipe)
            .addOnCompleteListener {
                if(it.isSuccessful)
                    finishListener.onFinish()
            }
    }

    private fun addToFavourite(recipe: Recipe, listener: FinishWorkListener){
        recipe.key?.let {
            auth.uid?.let { uid ->
                val data = hashMapOf("uid" to uid)
                db.collection("ApprovedRecipe")
                    .document(it)
                    .collection("favorite")
                    .document(uid)
                    .set(data)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            listener.onFinish()
                        }
                    }.addOnFailureListener {
                        Log.d("MyLog", "Error getting documents: ", it)
                    }
            }
        }
    }

    private fun removeFromFavourite(recipe: Recipe, listener: FinishWorkListener) {
        recipe.key?.let {
            auth.uid?.let { uid ->
                db.collection("ApprovedRecipe")
                    .document(it).collection("favorite").document(uid).delete()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            listener.onFinish()
                        }
                    }
            }
        }
    }
    fun onFavouriteClick(recipe: Recipe, listener: FinishWorkListener){
        if(recipe.isFavourite){
            removeFromFavourite(recipe,listener)
        }else{
            addToFavourite(recipe,listener)
        }
    }
    fun readDataFromDataBaseApprovedRecipe(readDataCallBack: ReadDataCallBack?) {
        db.collection("ApprovedRecipe")
            .get()
            .addOnSuccessListener { result ->
            val recipeArray = ArrayList<Recipe>()
            for (document in result) {
                val recipe = document.toObject(Recipe::class.java)
                if (auth.currentUser != null) {
                    auth.uid?.let { uid ->
                        document.reference.collection("favorite")
                            .document(uid)
                            .get()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val isFavourite = it.result.data
                                    recipe.isFavourite = isFavourite != null
                                    recipeArray.add(recipe)
                                }
                                readDataCallBack?.readData(recipeArray)
                            }.addOnFailureListener {
                                Log.d("MyLog", "Error getting documents: ", it)
                            }
                    }
                } else {
                    recipeArray.add(recipe)
                    readDataCallBack?.readData(recipeArray)
                }
            }
        }.addOnFailureListener { exception ->
            Log.d("MyLog", "Error getting documents: ", exception)
        }
    }
    fun readDataFromDataBaseTestableRecipes(readDataCallBack: ReadDataCallBack?, user: FirebaseUser?){
        if (user != null) {
            db.collection("Users").document(user.uid).collection("UserRecipe")
                .get()
                .addOnSuccessListener { result ->
                    val recipeArray = ArrayList<Recipe>()
                    for (document in result) {
                        val recipe = document.toObject(Recipe::class.java)
                        recipeArray.add(recipe)
                    }
                    readDataCallBack?.readData(recipeArray)
                }
                .addOnFailureListener { exception ->
                    Log.d("MyLog", "Error getting documents: ", exception)
                }
        }
    }
    fun searchTagDataFromDataBase(readDataCallBack: ReadDataCallBack?, path: String, tag: String) {
        db.collection("ApprovedRecipe").whereEqualTo(path, tag)
            .get()
            .addOnSuccessListener { result ->
                val recipeArray = ArrayList<Recipe>()
                for (document in result) {
                    val recipe = document.toObject(Recipe::class.java)
                    if(auth.currentUser != null){
                        auth.uid?.let { uid ->
                            document.reference.collection("favorite").document(uid).get()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val isFavourite = it.result.data
                                        recipe.isFavourite = isFavourite != null
                                        recipeArray.add(recipe)
                                    }
                                    readDataCallBack?.readData(recipeArray)
                                }
                        }
                    } else{
                        recipeArray.add(recipe)
                        readDataCallBack?.readData(recipeArray)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }
    fun searchDataFromDataBase(readDataCallBack: ReadDataCallBack?, searchText: String) {
        db.collection("ApprovedRecipe").orderBy("name")
            .startAt(searchText).endAt("$searchText\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                val recipeArray = ArrayList<Recipe>()
                for (document in result) {
                    val recipe = document.toObject(Recipe::class.java)
                    if(auth.currentUser != null){
                        auth.uid?.let { uid ->
                            document.reference.collection("favorite")
                                .document(uid)
                                .get()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val isFavourite = it.result.data
                                        recipe.isFavourite = isFavourite != null
                                        recipeArray.add(recipe)
                                    }
                                    readDataCallBack?.readData(recipeArray)
                                }
                        }
                    } else{
                        recipeArray.add(recipe)
                        readDataCallBack?.readData(recipeArray)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }
    fun favoriteDataFromDataBase(readDataCallBack: ReadDataCallBack?, user: FirebaseUser?) {
        if (user != null) {
            db.collection("ApprovedRecipe")
                .get()
                .addOnSuccessListener { result ->
                    val recipeArray = ArrayList<Recipe>()
                    for (document in result) {
                        val recipe = document.toObject(Recipe::class.java)
                        auth.uid?.let { uid ->
                            document.reference.collection("favorite").document(uid).get()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val isFavourite = it.result.data
                                        recipe.isFavourite = isFavourite != null
                                        if (recipe.isFavourite){
                                            recipeArray.add(recipe)
                                        }
                                    }
                                    readDataCallBack?.readData(recipeArray)
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("MyLog", "Error getting documents: ", exception)
                }
        }
    }

    fun addComment(
        commentData: CommentData,
        finishListener: FinishWorkListener,
        key: String, recipe: Recipe
    ) {
        commentData.rating
        db.collection("ApprovedRecipe")
            .document(key)
            .collection("comments")
            .document(auth.uid!!)
            .set(commentData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    readAverageRating(recipe)
                    finishListener.onFinish()
                }
            }
    }

    private fun readAverageRating(recipe: Recipe) {
        db.collection(Constants.APPROVED_RECIPE).document(recipe.key!!).collection(
            Constants.COMMENTS)
            .get().addOnSuccessListener {documents ->

                var sum = 0.0
                for (document in documents) {
                    val ratingString = document.getString("rating") ?: "0"
                    val rating = ratingString.toDoubleOrNull() ?: 0.0
                    if(rating in 1.0..5.0){
                        sum += rating
                    }
                }
                val average = sum / documents.size()
                val roundedAverage = round(average * 10) / 10
                addAverageRating(recipe,roundedAverage)
            }
    }

    private fun addAverageRating(recipe: Recipe, roundedAverage: Double,) {
        db.collection(Constants.APPROVED_RECIPE).document(recipe.key!!)
            .update("rating",roundedAverage.toString())
    }


    fun publishUserRecipe(recipe: Recipe, adminData: AdminData, activity: DescriptionActivity) {
        db.collection(Constants.USER_RECIPE).document(recipe.key!!).set(adminData).addOnCompleteListener {
            Toast.makeText(activity, "Рецепт успешно отправлен на рассмотрение", Toast.LENGTH_SHORT).show()
        }
    }
    fun readDataFromDataBaseUserRecipe(readDataCallBack: ReadDataCallBack?) {
        db.collection(Constants.USER_RECIPE)
            .get()
            .addOnSuccessListener { result ->
                val userRecipeArray = ArrayList<Recipe>()
                for (document in result) {
                    val userRecipe = document.toObject(AdminData::class.java)
                    db.collection(Constants.USERS).document(userRecipe.userID!!).collection(
                        Constants.USER_RECIPE).document(userRecipe.userRecipeID!!)
                        .get().addOnSuccessListener { recipe ->
                                val userRecipeData = recipe.toObject(Recipe::class.java)
                                userRecipeArray.add(userRecipeData!!)

                            readDataCallBack?.readData(userRecipeArray)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }
    fun addApprovedRecipe(recipe: Recipe, finishListener: FinishWorkListener){
        db.collection(Constants.APPROVED_RECIPE).document(recipe.key!!)
            .set(recipe)
            .addOnSuccessListener {
                removeShareRecipe(recipe,finishListener)
            }.addOnFailureListener {exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }
    fun removeShareRecipe(recipe: Recipe, finishListener: FinishWorkListener){
        db.collection(Constants.USER_RECIPE).document(recipe.key!!)
            .delete()
            .addOnSuccessListener {
                finishListener.onFinish()
            }.addOnFailureListener {exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }

    fun removeUserRecipe(recipe: Recipe, finishListener: FinishWorkListener){
        db.collection(Constants.USERS).document(auth.currentUser!!.uid).collection(Constants.USER_RECIPE).document(
            recipe.key!!
        )
            .delete()
            .addOnSuccessListener {
                finishListener.onFinish()
            }.addOnFailureListener {exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }

    fun readDataFromDataBaseBasketRecipe(readDataCallBack: ReadDataCallBack?) {
        db.collection(Constants.APPROVED_RECIPE)
            .get()
            .addOnSuccessListener { result ->
                val recipeArray = ArrayList<Recipe>()
                for (document in result) {
                    val basketRef = document.reference.collection(Constants.BASKET).document(auth.currentUser!!.uid)
                    basketRef.get()
                        .addOnSuccessListener { basketSnapshot ->
                            if (basketSnapshot.exists()) {
                                val recipe = document.toObject(Recipe::class.java)
                                recipeArray.add(recipe)
                            }
                            readDataCallBack?.readData(recipeArray)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }
    fun readIngredientRecipeFromDataBase(recipe: Recipe, readDataIngredientCallBack: ReadDataIngredientCallBack){
        if(auth.currentUser!!.uid != null){
            db.collection(Constants.APPROVED_RECIPE).document(recipe.key!!).collection(Constants.BASKET).document(
                auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { result ->
                    val recipeIngredient = result.toObject(Recipe::class.java)
                    if (recipeIngredient != null) {
                        readDataIngredientCallBack.readData(recipeIngredient,true)
                    }
                    else{
                        readDataIngredientCallBack.readData(recipe,false)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("MyLog", "Error getting documents: ", exception)
                }
        }
    }

    fun addBasketRecipe(ingredient: String, recipe: Recipe, finishListener: FinishWorkListener){
        val basketRef = db.collection(Constants.APPROVED_RECIPE)
            .document(recipe.key.toString())
            .collection(Constants.BASKET)
            .document(auth.uid!!)
        basketRef.set(HashMap<String, Any>(), SetOptions.merge())
            .addOnSuccessListener {
                basketRef.update(Constants.INGREDIENT, FieldValue.arrayUnion(ingredient))
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            finishListener.onFinish()
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }

    }
    fun removeBasketRecipe(ingredient: String, recipe: Recipe, finishListener: FinishWorkListener) {
        val basketRef = db.collection(Constants.APPROVED_RECIPE)
            .document(recipe.key.toString())
            .collection(Constants.BASKET)
            .document(auth.uid!!)

        basketRef.update(Constants.INGREDIENT, FieldValue.arrayRemove(ingredient))
            .addOnCompleteListener { updateTask ->
                if (updateTask.isSuccessful) {
                    basketRef.get().addOnSuccessListener { documentSnapshot ->
                        val ingredientArray = documentSnapshot.get(Constants.INGREDIENT) as? ArrayList<*>
                        if (ingredientArray == null || ingredientArray.isEmpty()) {
                            basketRef.delete()
                                .addOnSuccessListener {
                                    finishListener.onFinish()
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("MyLog", "Error getting documents: ", exception)
                                }
                        } else {
                            finishListener.onFinish()
                        }
                    }
                }
            }
    }
    fun reportComment(reportCommentData: ReportCommentData, activity: ReviewsActivity){
        val docRef = db.collection(Constants.APPROVED_RECIPE)
            .document(reportCommentData.uidRecipe!!)
            .collection(Constants.REPORT_COMMENT)
            .document(reportCommentData.uidAuthor!!)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    Toast.makeText(activity, "Жалоба уже на рассмотрении", Toast.LENGTH_SHORT).show()
                } else {
                    docRef.set(reportCommentData)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Жалоба успешно отправлена", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { exception ->
                            Log.d("MyLog", "Error getting documents: ", exception)
                        }
                }
            } else {
                Log.d("MyLog", "Error getting documents: ", task.exception)
            }
        }
    }
    fun readCommentFromDataBase(readDataCommentCallBack: ReadDataCommentCallBack?, recipeKey: String){
        db.collection("ApprovedRecipe").document(recipeKey).collection("comments")
            .get()
            .addOnSuccessListener { result ->
                val commentArray = ArrayList<CommentData>()
                for (document in result) {
                    val comment = document.toObject(CommentData::class.java)
                    commentArray.add(comment)
                }
                readDataCommentCallBack?.readData(commentArray)
            }
            .addOnFailureListener { exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }
    fun readDataReportComment(readDataReportCommentCallBack: ReadDataReportCommentCallBack?) {
        db.collection(Constants.APPROVED_RECIPE)
            .get()
            .addOnSuccessListener { result ->
                val reportCommentArray = ArrayList<ReportCommentData>()
                for (document in result) {
                    document.reference.collection("reportComment")
                        .get()
                        .addOnSuccessListener { commentResult ->
                            if (!commentResult.isEmpty) {
                                for (documentComment in commentResult) {
                                    val commentData = documentComment.toObject(ReportCommentData::class.java)
                                    reportCommentArray.add(commentData)
                                }
                            }
                            readDataReportCommentCallBack?.readData(reportCommentArray)

                        }
                        .addOnFailureListener { exception ->
                            Log.d("MyLog", "Error getting reportComment collection: ", exception)
                        }

                }
            }
            .addOnFailureListener { exception ->
                Log.d("MyLog", "Error getting documents: ", exception)
            }
    }
    fun skipReportComment(reportCommentData: ReportCommentData, activity: ReportCommentActivity, callback: FinishWorkListener) {
        db.collection(Constants.APPROVED_RECIPE)
            .document(reportCommentData.uidRecipe!!)
            .collection(Constants.REPORT_COMMENT)
            .document(reportCommentData.uidAuthor!!)
            .delete()
            .addOnSuccessListener {
                db.collection(Constants.APPROVED_RECIPE)
                    .document(reportCommentData.uidRecipe)
                    .collection(Constants.COMMENTS)
                    .document(reportCommentData.uidAuthor)
                    .update("approved", true)
                    .addOnCompleteListener {
                        Toast.makeText(activity, "Жалоба успешно рассмотрена", Toast.LENGTH_SHORT).show()
                        callback.onFinish()
                    }
                    .addOnFailureListener { exception ->
                        callback.onFinish()
                        Log.d("MyLog", "Error updating comment: ", exception)
                    }
            }
            .addOnFailureListener { exception ->
                callback.onFinish()
                Log.d("MyLog", "Error deleting report comment: ", exception)
            }
    }

    fun blockReportComment(reportCommentData: ReportCommentData, activity: ReportCommentActivity, callback: FinishWorkListener){
        val currentTime = Timestamp.now().toDate().time
        val calendar = Calendar.getInstance()
        calendar.time = Date(currentTime)
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        val newTime = calendar.time.time
        db.collection(Constants.USERS).document(reportCommentData.uidAuthor!!).update("dateBlock",newTime.toString()).addOnCompleteListener {
            db.collection(Constants.APPROVED_RECIPE).document(reportCommentData.uidRecipe!!).collection(
                Constants.COMMENTS).document(reportCommentData.uidAuthor).delete().addOnCompleteListener {
                db.collection(Constants.APPROVED_RECIPE)
                    .document(reportCommentData.uidRecipe)
                    .collection(Constants.REPORT_COMMENT)
                    .document(reportCommentData.uidAuthor)
                    .delete().addOnCompleteListener {
                        Toast.makeText(activity, "Пользователь заблокирован", Toast.LENGTH_SHORT).show()
                        callback.onFinish()
                    }
            }
        }


    }



    interface ReadDataCommentCallBack {
        fun readData(list: ArrayList<CommentData>)
    }
    interface ReadDataReportCommentCallBack {
        fun readData(list: ArrayList<ReportCommentData>)
    }

    interface ReadDataCallBack {
        fun readData(list: ArrayList<Recipe>)
    }
    interface ReadDataIngredientCallBack {
        fun readData(list: Recipe, locate:Boolean)
    }
    interface FinishWorkListener{
        fun onFinish()
    }

}