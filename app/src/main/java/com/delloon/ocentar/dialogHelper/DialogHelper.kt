package com.delloon.ocentar.dialogHelper

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.delloon.ocentar.ProfileActivity
import com.delloon.ocentar.R
import com.delloon.ocentar.accountHelper.AccountHelper
import com.delloon.ocentar.databinding.AuthorizationBinding


class DialogHelper(private val activity: ProfileActivity) {
    val accountHelper = AccountHelper(activity)

    fun createSignDialog(index:Int){
        val dialogBuilder = AlertDialog.Builder(activity)
        val binding = AuthorizationBinding.inflate(activity.layoutInflater)
        dialogBuilder.setView(binding.root)

        setDialogState(index,binding)

        val dialog = dialogBuilder.create()
        binding.buttonSignUpIn.setOnClickListener {
            if(index == DialogConst.SIGN_UP_STATE){
                if(binding.checkBoxUserAgreement.isChecked && binding.checkBoxPrivacyPolicy.isChecked){
                    setOnClickSignUpIn(index,binding,dialog)
                }
                else{
                    Toast.makeText(activity, "Примите соглашение и политику!", Toast.LENGTH_LONG).show()
                }
            }else{
                setOnClickSignUpIn(index,binding,dialog)
            }
        }

        binding.forgetPassword.setOnClickListener {
            setOnClickResetPassword(binding,dialog)
        }
        binding.buttonGoogleSignIn.setOnClickListener {
            accountHelper.signInWithGoogle()
            dialog.dismiss()
        }
        binding.buttonPrivacyPolicyFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1wsILded7334VlAV4fB5kJA1mtX4OIZnf/view"))
            activity.startActivity(intent)
        }
        binding.buttonUserAgreementFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1iBY6SESv4wSM6zjOm62Obzmf7YwB8CB2/view"))
            activity.startActivity(intent)
        }
        dialog.show()
    }




    private fun setOnClickResetPassword(binding: AuthorizationBinding, dialog: AlertDialog?) {
        if (binding.editTextSignEmail.text.isNotEmpty()){
            activity.myAuthentication.sendPasswordResetEmail(binding.editTextSignEmail.text.toString()).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    Toast.makeText(activity, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG).show()
                }
            }
            dialog?.dismiss()
        } else {
            binding.textViewDialogMessage.visibility = View.VISIBLE
        }
    }

    private fun setOnClickSignUpIn(index: Int, binding: AuthorizationBinding, dialog: AlertDialog?) {
        dialog?.dismiss()
        if(index == DialogConst.SIGN_UP_STATE){
            accountHelper.singUpWithEmail(binding.editTextSignEmail.text.toString(),binding.editTextSignPassword.text.toString(),binding.editTextSignName.text.toString())
        } else {
            accountHelper.singInWithEmail(binding.editTextSignEmail.text.toString(),binding.editTextSignPassword.text.toString())
            binding.forgetPassword.visibility = View.VISIBLE
        }
    }
    private fun setDialogState(index: Int, binding: AuthorizationBinding) {
        if(index == DialogConst.SIGN_UP_STATE){
            binding.textViewTitleAuthorization.text = activity.resources.getString(R.string.sign_up)
            binding.buttonSignUpIn.text = activity.resources.getString(R.string.sign_up_action)
        } else {
            binding.textViewTitleAuthorization.text = activity.resources.getString(R.string.sign_in)
            binding.buttonSignUpIn.text = activity.resources.getString(R.string.sign_in_action)
            binding.forgetPassword.visibility = View.VISIBLE
            binding.editTextSignName.visibility = View.GONE
            binding.checkBoxUserAgreement.visibility = View.GONE
            binding.checkBoxPrivacyPolicy.visibility = View.GONE
            binding.buttonPrivacyPolicyFile.visibility = View.GONE
            binding.buttonUserAgreementFile.visibility = View.GONE
        }
    }
}