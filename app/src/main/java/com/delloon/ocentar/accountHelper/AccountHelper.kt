package com.delloon.ocentar.accountHelper

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.delloon.ocentar.ProfileActivity
import com.delloon.ocentar.R
import com.delloon.ocentar.constants.Constants
import com.delloon.ocentar.constants.FirebaseAutnConstants
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.toObject

class AccountHelper(private val activity: ProfileActivity) {
    private lateinit var signInClient: GoogleSignInClient
    private var dbManager = DBManager()
    fun singUpWithEmail(email:String, password:String, name:String){
        if(email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()){
            activity.myAuthentication
                .createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener{ task->
                if (task.isSuccessful) {
                    sendEmailVerification(task.result?.user!!)
                    activity.uiUpdate(task.result?.user)
                    uploadProfileDataBase(task.result?.user!!, name)
                } else {
                    if(task.exception is FirebaseAuthUserCollisionException){
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        if(exception.errorCode == FirebaseAutnConstants.ERROR_EMAIL_ALREADY_IN_USE){
                            linkEmailToGoogle(email,password)
                        }
                    } else if(task.exception is FirebaseAuthInvalidCredentialsException){
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if(exception.errorCode == FirebaseAutnConstants.ERROR_INVALID_EMAIL){
                            Toast.makeText(activity,
                                FirebaseAutnConstants.ERROR_INVALID_EMAIL,
                                Toast.LENGTH_LONG).show()
                        }
                    }
                    if(task.exception is FirebaseAuthWeakPasswordException){
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if(exception.errorCode == FirebaseAutnConstants.ERROR_WEAK_PASSWORD){
                            Toast.makeText(activity,
                                FirebaseAutnConstants.ERROR_WEAK_PASSWORD,
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    private fun uploadProfileDataBase(user: FirebaseUser, name: String) {
        val profileUpdates = userProfileChangeRequest {
            displayName = name
            photoUri = if(user.photoUrl != null) user.photoUrl else Uri.parse(AccountLink.LINK_DEFAULT_AVATAR)
        }
        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MyLog", "User profile upload.")
                }
            }
        dbManager.db.collection(Constants.USERS).document(user.uid).get()
            .addOnSuccessListener { document ->
                val doc = document.toObject<User>()
                if(doc != null){
                    val photoUri = if(user.photoUrl != null) user.photoUrl else Uri.parse(AccountLink.LINK_DEFAULT_AVATAR)
                    val userProfile = User(
                        name, user.email, user.uid, photoUri.toString(), doc.role
                    )
                    dbManager.db.collection("Users")
                        .document(user.uid)
                        .set(userProfile)
                }
                else {
                    val photoUri = if(user.photoUrl != null) user.photoUrl else Uri.parse(AccountLink.LINK_DEFAULT_AVATAR)
                    val userProfile = User(
                        name, user.email, user.uid, photoUri.toString(), Constants.USER
                    )
                    dbManager.db.collection("Users")
                        .document(user.uid)
                        .set(userProfile)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    private fun linkEmailToGoogle(email: String,password: String){
        val credential = EmailAuthProvider.getCredential(email, password)
        if(activity.myAuthentication.currentUser != null){
            activity.myAuthentication.currentUser?.linkWithCredential(credential)?.
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, activity.resources.getString(R.string.link_done),
                        Toast.LENGTH_LONG)
                        .show()
                }
            }
        } else{
            Toast.makeText(activity, activity.resources.getString(R.string.enter_to_google),
                Toast.LENGTH_LONG)
                .show()
        }
    }

    fun singInWithEmail(email:String, password:String){
        if(email.isNotEmpty() && password.isNotEmpty()){
            activity.myAuthentication.signInWithEmailAndPassword(email,password).addOnCompleteListener{ task->
                if (task.isSuccessful) {
                    activity.uiUpdate(task.result?.user)
                } else {
                    if(task.exception is FirebaseAuthInvalidCredentialsException){
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if(exception.errorCode == FirebaseAutnConstants.ERROR_INVALID_EMAIL){
                            Toast.makeText(activity,
                                FirebaseAutnConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
                        } else if(exception.errorCode == FirebaseAutnConstants.ERROR_WRONG_PASSWORD){
                            Toast.makeText(activity,
                                FirebaseAutnConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG).show()
                        }
                    } else if(task.exception is FirebaseAuthInvalidUserException){
                        val exception = task.exception as FirebaseAuthInvalidUserException
                        if(exception.errorCode == FirebaseAutnConstants.ERROR_USER_NOT_FOUND) {
                            Toast.makeText(activity, FirebaseAutnConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun sendEmailVerification(user:FirebaseUser){
        user.sendEmailVerification().addOnCompleteListener { task->
            if(task.isSuccessful){
                Toast.makeText(activity,activity.resources.getString(R.string.send_verification_done),
                    Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(activity,activity.resources.getString(R.string.send_verification_email_error),
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getSignInClient(): GoogleSignInClient{
        val googleSignOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail().requestProfile().build()
        return GoogleSignIn.getClient(activity,googleSignOptions)
    }
    fun  signInWithGoogle(){
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        activity.googleSignInLauncher.launch(intent)
    }
    fun signOutGoogle(){
        getSignInClient().signOut()
    }
    fun signInFirebaseWithGoogle(token:String){
        val credential = GoogleAuthProvider.getCredential(token,null)
        activity.myAuthentication.signInWithCredential(credential)
            .addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(activity,activity.resources.getString(R.string.sign_in_done),
                    Toast.LENGTH_LONG)
                    .show()
                activity.uiUpdate(task.result?.user)
                uploadProfileDataBase(task.result?.user!!, task.result?.user!!.displayName!!)
            } else {
                Log.d("MyLog","Google Sign In Exception : ${task.exception}")
            }

        }
    }
}