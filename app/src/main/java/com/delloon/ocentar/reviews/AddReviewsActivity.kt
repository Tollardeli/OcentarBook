package com.delloon.ocentar.reviews

import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.delloon.ocentar.R
import com.delloon.ocentar.constants.Constants
import com.delloon.ocentar.databinding.ActivityAddReviewsBinding
import com.delloon.ocentar.model.CommentData
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import java.util.Locale

class AddReviewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddReviewsBinding
    private var starRating = "5"
    private var dbManager = DBManager()
    private val myAuthentication = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }
        val recipe = intent.getSerializableExtra("Recipe") as Recipe
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            starRating = rating.toInt().toString()
        }

        binding.editTextComment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textViewHint.visibility = View.VISIBLE
                binding.editTextComment.hint = ""
            } else {
                binding.textViewHint.visibility = View.INVISIBLE
                binding.editTextComment.hint = getString(R.string.add_comment_hint)
            }
        }
        binding.editTextComment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.textViewCharCount.text = "${s?.length ?: 0}/100"
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.buttonDoneReviews.setOnClickListener {
            binding.apply {
                dbManager.db.collection(Constants.USERS).document(myAuthentication.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val data = documentSnapshot.toObject(User::class.java)
                            val currentTime = Timestamp.now().toDate().time
                            Log.d("MyLog",currentTime.toString())
                            if(data!!.dateBlock != ""){
                                if ( currentTime > data.dateBlock!!.toLong()) {
                                    val comment = CommentData(editTextComment.text.toString(), starRating, myAuthentication.uid)
                                    dbManager.addComment(comment, onPublishFinish(), recipe.key!!, recipe)
                                }
                                else{
                                    Toast.makeText(this@AddReviewsActivity, "Вы были заблокированы до: ${parseResponseAndExtractTime(data.dateBlock)}", Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(this@AddReviewsActivity, "Отзыв отправлен", Toast.LENGTH_SHORT).show()
                                val comment = CommentData(editTextComment.text.toString(), starRating, myAuthentication.uid)
                                dbManager.addComment(comment, onPublishFinish(), recipe.key!!, recipe)
                            }
                        } else {
                            Toast.makeText(this@AddReviewsActivity, "Повторите попытку позже", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("MyLog", "Error getting documents: ", exception)
                    }

            }
        }
    }
    private fun parseResponseAndExtractTime(responseBody: String?): String {
        val serverTime = Date(responseBody!!.toLong())
        val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return displayFormat.format(serverTime)
    }

    private fun onPublishFinish(): DBManager.FinishWorkListener{
        return object : DBManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }

        }
    }
}