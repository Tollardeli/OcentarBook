package com.delloon.ocentar.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.databinding.ListCommentBinding
import com.delloon.ocentar.model.CommentData
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.model.User
import com.delloon.ocentar.model.ReportCommentData
import com.delloon.ocentar.reviews.ReviewsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class RecyclerViewCommentAdapter(val activity: ReviewsActivity) : RecyclerView.Adapter<RecyclerViewCommentAdapter.CommentViewHolder>() {
    private val commentArray = ArrayList<CommentData>()
    private var recipeList = Recipe()
    val auth  = FirebaseAuth.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ListCommentBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CommentViewHolder(binding,activity)
    }

    override fun getItemCount(): Int {
        return commentArray.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.setData(commentArray[position],recipeList)
    }

    fun updateAdapter(newList: List<CommentData>, recipe: Recipe){
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelperComment(commentArray,newList))
        diffResult.dispatchUpdatesTo(this)
        commentArray.clear()
        commentArray.addAll(newList)
        recipeList = recipe
    }

    class CommentViewHolder(val binding: ListCommentBinding, val activity: ReviewsActivity) : RecyclerView.ViewHolder(binding.root) {
        private val db = Firebase.firestore
        private var dbManager = DBManager()
        private var userProfile: User? = null
        private val auth = FirebaseAuth.getInstance()

        fun setData(comment: CommentData, recipe: Recipe) {
            val docRef = db.collection("Users").document(comment.user!!)
            docRef.get().addOnSuccessListener {
                userProfile = it.toObject<User>()
                binding.apply {
                    if (userProfile != null) {
                        Picasso.get().load(userProfile!!.photoprofile).into(imageViewAvatarComment)
                        textViewComment.text = comment.comment
                        textViewNickNameComment.text = userProfile!!.nickname
                        ratingBar.rating = comment.rating!!.toFloat()
                    }
                }
            }

            binding.buttonReportComment.setOnClickListener {
                if (comment.approved != true) {
                    if (comment.comment != "") {
                        val reportCommentData = ReportCommentData(
                            comment.user, recipe.key!!, auth.currentUser!!.uid,
                            userProfile!!.photoprofile, comment.comment, userProfile!!.nickname
                        )
                        if (userProfile != null) {
                            dbManager.reportComment(reportCommentData, activity)
                        }
                    } else {
                        Toast.makeText(activity, "Жалоба отклонена", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(activity, "Комментарий уже был проверен!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}