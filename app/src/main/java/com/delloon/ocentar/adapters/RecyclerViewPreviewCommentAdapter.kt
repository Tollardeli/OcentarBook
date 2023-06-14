package com.delloon.ocentar.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.databinding.ListFastCommentBinding
import com.delloon.ocentar.model.CommentData
import com.delloon.ocentar.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class RecyclerViewPreviewCommentAdapter :
    RecyclerView.Adapter<RecyclerViewPreviewCommentAdapter.CommentViewHolder>() {
    private val previewCommentArray = ArrayList<CommentData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ListFastCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return minOf(previewCommentArray.size, 2)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.setData(previewCommentArray[position])
    }

    fun updateAdapter(newList: List<CommentData>){
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelperComment(previewCommentArray, newList))
        diffResult.dispatchUpdatesTo(this)
        previewCommentArray.clear()
        previewCommentArray.addAll(newList)

    }

    class CommentViewHolder(val binding: ListFastCommentBinding): RecyclerView.ViewHolder(binding.root) {
        private val db = Firebase.firestore

        fun setData(comment: CommentData){
            val docRef = db.collection("Users").document(comment.user!!)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                val userProfile = documentSnapshot.toObject<User>()
                binding.apply{
                    textViewPreviewComment.text = comment.comment
                    textViewNickNamePreviewComment.text = userProfile!!.nickname
                    textViewCountPreview.text = comment.rating
                    Picasso.get().load(userProfile.photoprofile).into(imageViewAvatarPreviewComment)
                }
            }
        }
    }
}