package com.delloon.ocentar.chatGPT

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.databinding.ItemChatGptBinding

class RecyclerViewChatGPTAdapter : RecyclerView.Adapter<RecyclerViewChatGPTAdapter.MessageGPTViewHolder>() {
    private val messageArray = ArrayList<MessageGPTData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageGPTViewHolder {
        val binding = ItemChatGptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageGPTViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return messageArray.size
    }

    override fun onBindViewHolder(holder: MessageGPTViewHolder, position: Int) {
        holder.setData(messageArray[position])
    }

    fun addMessage(message: MessageGPTData) {
        messageArray.add(message)
        notifyItemInserted(messageArray.size - 1)
    }
    fun clearMessages() {
        messageArray.clear()
        notifyDataSetChanged()
    }
    class MessageGPTViewHolder(private val binding: ItemChatGptBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(message: MessageGPTData) {
            if (!message.isUserMessage) {
                binding.leftChatView.visibility = View.VISIBLE
                binding.rightChatView.visibility = View.GONE

                binding.leftChatTextView.text = message.message
            } else {
                binding.leftChatView.visibility = View.GONE
                binding.rightChatView.visibility = View.VISIBLE

                binding.rightChatTextView.text = message.message
            }
        }
    }
}
