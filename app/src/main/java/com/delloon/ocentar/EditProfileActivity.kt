package com.delloon.ocentar

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.delloon.ocentar.accountHelper.AccountLink
import com.delloon.ocentar.adminPanel.AdminPanelMenuActivity
import com.delloon.ocentar.constants.Constants
import com.delloon.ocentar.databinding.ActivityEditProfileBinding
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.User
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val myAuthentication = FirebaseAuth.getInstance()
    private lateinit var textViewAccountEmail: TextView
    private lateinit var editTextAccountName: EditText
    private lateinit var imageViewProfile: ImageView
    private lateinit var uri: Uri
    private val storageRef = FirebaseStorage.getInstance().reference
    private val dbManager = DBManager()
    private lateinit var role: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Редактирование"

        binding.imageChange.setOnClickListener {
           changePhotoUser()
        }
        initialization()
        binding.buttonSaveProfile.setOnClickListener {
            dataInsert(myAuthentication.currentUser)
        }
        binding.buttonAdminPanel.setOnClickListener {
            startActivity(Intent(this, AdminPanelMenuActivity::class.java))
        }
    }
    private fun changePhotoUser() {
        ImagePicker.with(this)
            .crop(4f, 4f)
            .maxResultSize(100, 100)
            .provider(ImageProvider.BOTH)
            .createIntentFromDialog { launcher.launch(it) }
    }
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                uri = it.data?.data!!
                Picasso.get().load(uri).resize(100,100).into(imageViewProfile)
            }
        }
    private fun initialization() {
        textViewAccountEmail = binding.textViewEmailProfile.findViewById(R.id.textViewEmailProfile)
        editTextAccountName = binding.editTextEditName.findViewById(R.id.editTextEditName)
        imageViewProfile = binding.imageViewAvatar.findViewById(R.id.imageViewAvatar)
        dbManager.db.collection(Constants.USERS).document(myAuthentication.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val doc = document.toObject<User>()
                    if (doc != null) {
                        role = doc.role!!
                        if (role == Constants.MODERATOR){
                            binding.textViewAdministratorText.visibility = View.VISIBLE
                            binding.buttonAdminPanel.visibility = View.VISIBLE
                            binding.buttonAdminPanel.isEnabled = true
                        }
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
    override fun onStart() {
        super.onStart()
        uiUpdate(myAuthentication.currentUser)
    }
    private fun uiUpdate(user: FirebaseUser?){
        if (user != null) {
            textViewAccountEmail.text = user.email
            editTextAccountName.setText(user.displayName)
            uri = user.photoUrl!!
            Picasso.get().load(user.photoUrl).resize(100,100).into(imageViewProfile)
        }
    }
    private fun dataInsert(user: FirebaseUser?){
        imageInsert(user)
    }
    private fun imageInsert(user: FirebaseUser?) {
        if(user!!.photoUrl != uri){
            val uploadTask = storageRef.child("ImageProfile/${user.uid}").putFile(uri)
            uploadTask.addOnFailureListener {
                user.photoUrl?.let { it1 -> dateProfile(user, it1) }
            }.addOnSuccessListener {
                storageRef.child("ImageProfile/${user.uid}").downloadUrl.addOnSuccessListener {
                    val downloadUri = it
                    dateProfile(user, downloadUri)
                }.addOnFailureListener {
                    Toast.makeText(this, R.string.edit_error, Toast.LENGTH_SHORT).show()
                    Log.d("MyLog","1.$it")
                }
            }
        }else{
            storageRef.child("ImageProfile/${user.uid}").downloadUrl
                .addOnSuccessListener {
                    val downloadUri = it
                    dateProfile(user, downloadUri)
                }.addOnFailureListener {
//                    Toast.makeText(this, R.string.edit_error, Toast.LENGTH_SHORT).show()
                    dateProfile(user, user.photoUrl!!)
                }
        }
    }
    private fun dateProfile(user: FirebaseUser?,downloadUri: Uri){
        val profileUpdates = userProfileChangeRequest {
            displayName = editTextAccountName.text.toString()
            photoUri = downloadUri
        }
        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val db = Firebase.firestore
                    val photoUri = if(user.photoUrl != null) user.photoUrl else Uri.parse(AccountLink.LINK_DEFAULT_AVATAR)
                    val userProfile = User(
                        user.displayName, user.email, user.uid, photoUri.toString(),role
                    )
                    db.collection("Users").document(user.uid).set(userProfile)
                    Log.d("MyLog", "User profile updated.")
                }
            }
        Toast.makeText(this, R.string.edit_completed, Toast.LENGTH_SHORT).show()
    }
}