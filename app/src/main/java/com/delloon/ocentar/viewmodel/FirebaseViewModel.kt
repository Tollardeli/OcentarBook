package com.delloon.ocentar.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delloon.ocentar.model.CommentData
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.model.ReportCommentData
import com.google.firebase.auth.FirebaseUser

class FirebaseViewModel : ViewModel(){
    private val dbManager = DBManager()
    val liveRecipeData = MutableLiveData<ArrayList<Recipe>>()
    val liveCommentData = MutableLiveData<ArrayList<CommentData>>()
    val liveReportUserData = MutableLiveData<ArrayList<ReportCommentData>>()


    fun loadAllRecipe(){
        dbManager.readDataFromDataBaseApprovedRecipe(object: DBManager.ReadDataCallBack{
            override fun readData(list: ArrayList<Recipe>) {
                liveRecipeData.value = list
            }

        })
    }
    fun loadUserRecipe(){
        dbManager.readDataFromDataBaseUserRecipe(object : DBManager.ReadDataCallBack{
            override fun readData(list: ArrayList<Recipe>) {
                liveRecipeData.value = list
            }

        })
    }
    fun loadMyRecipe(user: FirebaseUser?){
        dbManager.readDataFromDataBaseTestableRecipes(object: DBManager.ReadDataCallBack{
            override fun readData(list: ArrayList<Recipe>) {
                liveRecipeData.value = list
            }

        },user)
    }
    fun loadSearchRecipe(searchText: String){
        dbManager.searchDataFromDataBase(object: DBManager.ReadDataCallBack{
            override fun readData(list: ArrayList<Recipe>) {
                liveRecipeData.value = list
            }

        },searchText)
    }
    fun loadPreviewComment(recipe: Recipe) {
        dbManager.readCommentFromDataBase(object: DBManager.ReadDataCommentCallBack{
            override fun readData(list: ArrayList<CommentData>) {
                liveCommentData.value = list
            }

        }, recipe.key!!)
    }

    fun loadComment(recipe: Recipe){
        dbManager.readCommentFromDataBase(object: DBManager.ReadDataCommentCallBack{
            override fun readData(list: ArrayList<CommentData>) {
                liveCommentData.value = list
            }

        },recipe.key!!)
    }
    fun loadSearchTagRecipe(path: String,tag: String){
        dbManager.searchTagDataFromDataBase(object: DBManager.ReadDataCallBack{
            override fun readData(list: ArrayList<Recipe>) {
                liveRecipeData.value = list
            }

        },path,tag)
    }
    fun loadFavouriteRecipe(user: FirebaseUser?){
        dbManager.favoriteDataFromDataBase(object: DBManager.ReadDataCallBack{
            override fun readData(list: ArrayList<Recipe>) {
                liveRecipeData.value = list
            }
        },user)
    }
    fun loadBasketRecipe(){
        dbManager.readDataFromDataBaseBasketRecipe(object: DBManager.ReadDataCallBack{
            override fun readData(list: ArrayList<Recipe>) {
                liveRecipeData.value = list
            }
        })
    }

    fun loadReportComment(){
        dbManager.readDataReportComment(object : DBManager.ReadDataReportCommentCallBack{
            override fun readData(list: ArrayList<ReportCommentData>) {
                liveReportUserData.value = list
            }

        })
    }


    fun onFavouriteClick(recipe: Recipe){
        dbManager.onFavouriteClick(recipe,object: DBManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveRecipeData.value
                val position = updatedList?.indexOf(recipe)
                if(position != -1){
                    position?.let {
                        updatedList[position] = updatedList[position].copy(isFavourite = !recipe.isFavourite)
                    }
                }
                liveRecipeData.postValue(updatedList!!)
            }
        })
    }
}