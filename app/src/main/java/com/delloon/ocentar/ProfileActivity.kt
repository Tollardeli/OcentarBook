package com.delloon.ocentar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.adapters.RecyclerViewAdapter
import com.delloon.ocentar.model.Recipe
import com.delloon.ocentar.databinding.ActivityProfileBinding
import com.delloon.ocentar.dialogHelper.DialogConst
import com.delloon.ocentar.dialogHelper.DialogHelper
import com.delloon.ocentar.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity(), RecyclerViewAdapter.Listener{
    private lateinit var avatar : ImageView
    private lateinit var textViewAccountName:TextView
    private lateinit var  binding: ActivityProfileBinding
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val dialogHelper = DialogHelper(this)
    val myAuthentication = FirebaseAuth.getInstance()
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val adapter = RecyclerViewAdapter(null,this,null,null,null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onActivityResult()

        binding.ButtonAuthorization.setOnClickListener {
            dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
        }
        binding.ButtonRegistration.setOnClickListener {
            dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
        }
        binding.floatingActionButtonAddRecipe.setOnClickListener{
            startActivity( Intent(this, AddEditRecipeActivity::class.java))
        }
        binding.buttonEdieProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.buttonExitProfile.setOnClickListener {
            exitAccount()
        }
        avatar = binding.Avatars.findViewById(R.id.Avatars)
        textViewAccountName = binding.textViewName.findViewById(R.id.textViewName)
    }
    private fun exitAccount(){
        uiUpdate(null)
        textViewAccountName.text.isEmpty()
        myAuthentication.signOut()
        dialogHelper.accountHelper.signOutGoogle()
    }
    private fun onActivityResult() {
        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        dialogHelper.accountHelper.signInFirebaseWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                    Log.d("ErrorLogs", "Api error : ${e.message}")
                }
            }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(myAuthentication.currentUser)
    }

    fun uiUpdate(user: FirebaseUser?){

        if (user != null) {
            Log.d("ErrorLogs", "Api error : ${user.photoUrl}")
            Picasso.get().load(user.photoUrl).resize(100,100).into(avatar)
        }
        textViewAccountName.text = if(user == null){
            resources.getString(R.string.not_registration)
        }else{
            user.displayName
        }
        if(user != null){
            binding.MainProfileView.visibility = View.VISIBLE
            binding.SecondProfileView.visibility = View.GONE
        } else {
            binding.SecondProfileView.visibility = View.VISIBLE
            binding.MainProfileView.visibility = View.GONE
        }
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadMyRecipe(user)
    }
    private fun initRecyclerView(){
        binding.apply {
            recyclerViewMyRecipe.layoutManager = LinearLayoutManager(this@ProfileActivity)
            recyclerViewMyRecipe.adapter = adapter
        }
    }
    private fun initViewModel(){
        firebaseViewModel.liveRecipeData.observe(this) {
            adapter.updateAdapter(it)
        }
    }
    override fun onFavouriteClicked(recipe: Recipe) {
        firebaseViewModel.onFavouriteClick(recipe)
    }
}