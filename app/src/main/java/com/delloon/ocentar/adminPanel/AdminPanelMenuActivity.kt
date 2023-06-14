package com.delloon.ocentar.adminPanel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.delloon.ocentar.databinding.ActivityAdminPanelMenuBinding

class AdminPanelMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPanelMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPanelMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonUserRecipe.setOnClickListener {
            startActivity(Intent(this, UserRecipeActivity::class.java))
        }
        binding.buttonReportUsersComment.setOnClickListener {
            startActivity(Intent(this, ReportCommentActivity::class.java))
        }
        binding.buttonUsers.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
        }

    }
}