package com.delloon.ocentar.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.R
import com.delloon.ocentar.databinding.ListViewStepItemBinding
import com.delloon.ocentar.model.ViewData

class RecyclerViewRecipeStepAdapter: RecyclerView.Adapter<RecyclerViewRecipeStepAdapter.RecipeDecHolder>(){
    val list = ArrayList<ViewData>()
    class RecipeDecHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListViewStepItemBinding.bind(itemView)
        fun bindData(viewData: ViewData)= with(binding){
            textViewListItem.text = viewData.listItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeDecHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_view_step_item,parent,false)
        return RecipeDecHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeDecHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(viewData: ViewData){
        list.add(viewData)
        notifyDataSetChanged()
    }
}