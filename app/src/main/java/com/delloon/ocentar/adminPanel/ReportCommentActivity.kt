package com.delloon.ocentar.adminPanel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.adapters.RecyclerViewReportCommentAdapter
import com.delloon.ocentar.databinding.ActivityReportCommentBinding
import com.delloon.ocentar.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth

class ReportCommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportCommentBinding
    private val adapterReport = RecyclerViewReportCommentAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    val auth  = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initViewModel()
        updateAdapter()
    }
    fun updateAdapter(){
        firebaseViewModel.loadReportComment()
    }

    private fun initViewModel() {
        firebaseViewModel.liveReportUserData.observe(this) {
            adapterReport.updateAdapter(it)
        }
    }

    private fun initRecyclerView() {
        binding.recyclerViewReportComment.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReportComment.adapter = adapterReport
    }

}