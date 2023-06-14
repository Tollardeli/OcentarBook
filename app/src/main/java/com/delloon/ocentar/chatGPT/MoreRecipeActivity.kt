package com.delloon.ocentar.chatGPT

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.delloon.ocentar.constants.Constants
import com.delloon.ocentar.databinding.ActivityMoreRecipeBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.Locale
import java.util.concurrent.TimeUnit

class MoreRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMoreRecipeBinding
    private val chatAdapter = RecyclerViewChatGPTAdapter()
    private val messageList = ArrayList<MessageGPTData>()
    private val url = Constants.URL_API
    private var userMessageCount = 0
    private var mergedQuery: String = ""
    private var isBotResponded = false
    private var responseMessageRecipe: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = chatAdapter

        displayUserMessage()

        binding.editTextWriteHere.setOnEditorActionListener { textView, actionId, event ->
            if (!isBotResponded) {
                Toast.makeText(this, "Пожалуйста, дождитесь ответа от бота.", Toast.LENGTH_SHORT).show()
                return@setOnEditorActionListener true
            }

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (binding.editTextWriteHere.text.toString().isNotEmpty()) {
                    val userMessage = MessageGPTData(binding.editTextWriteHere.text.toString(), true)
                    messageList.add(userMessage)
                    chatAdapter.addMessage(userMessage)
                    handleUserResponse(binding.editTextWriteHere.text.toString())
                } else {
                    Toast.makeText(this, "Пожалуйста, введите ваш запрос.", Toast.LENGTH_SHORT).show()
                }
                binding.editTextWriteHere.text = null
                return@setOnEditorActionListener true
            }
            false
        }

        binding.imageButtonSendMessage.setOnClickListener {
            if (!isBotResponded) {
                Toast.makeText(this, "Пожалуйста, дождитесь ответа от бота.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inputText = binding.editTextWriteHere.text.toString()
            if (inputText.isNotEmpty()) {
                val userMessage = MessageGPTData(inputText, true)
                messageList.add(userMessage)
                chatAdapter.addMessage(userMessage)
                handleUserResponse(inputText)
                binding.editTextWriteHere.text = null
            } else {
                Toast.makeText(this, "Пожалуйста, введите ваш запрос.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserMessage() {
        when (userMessageCount) {
            0 -> sendMessageToChat("Что бы вы хотели съесть?")
            1 -> sendMessageToChat("Какие ингредиенты у вас есть?")
            2 -> sendMessageToChat("Какие-нибудь дополнительные замечания или предпочтения?")
        }
    }

    private fun sendMessageToChat(message: String) {
        isBotResponded = true
        val chatMessage = MessageGPTData(message, false)
        messageList.add(chatMessage)
        chatAdapter.addMessage(chatMessage)
    }

    private fun handleUserResponse(response: String) {
        if (userMessageCount == 0) {
            mergedQuery += "Покажи мне реально существующий рецепт: Я не могу найти рецепт по вашим требованиям. Требования: я хочу съесть: [$response], "
            userMessageCount++
            displayUserMessage()
        } else if (userMessageCount == 1) {
            mergedQuery += "у меня есть следующие ингредиенты: [$response], "
            userMessageCount++
            displayUserMessage()
        } else if (userMessageCount == 2) {
            mergedQuery += "мои замечания или предпочтения: [$response]."
            userMessageCount++
            displayUserMessage()
            getResponse(mergedQuery)
        } else if (userMessageCount > 2) {
            handleUserActionOption(response,mergedQuery)
        }
    }
    private fun handleUserActionOption(response: String,mergedQuery:String ) {
        val userInput = response.trim().lowercase(Locale.getDefault())

        when (userInput) {
            "сохранить" -> {
                responseMessageRecipe = response
            }
            "повторить" -> {
                resetChat()
            }
            "показать ещё" -> {
                val additionalOptionsQuery = "Покажи ещё варианты рецепта с требованиями: ${mergedQuery}"
                getResponse(additionalOptionsQuery)
            }
            else -> {
                val errorMessage = "Нераспознанный ввод. Пожалуйста, выберите подходящий вариант."
                val errorResponse = MessageGPTData(errorMessage, false)
                messageList.add(errorResponse)
                chatAdapter.addMessage(errorResponse)
            }
        }
    }

    private fun resetChat() {
        userMessageCount = 0
        mergedQuery = ""
        responseMessageRecipe = null
        messageList.clear()
        chatAdapter.clearMessages()
        displayUserMessage()
    }

    private fun getResponse(query: String) {
        isBotResponded = false

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("model", "text-davinci-003")
        json.put("prompt", query)
        json.put("temperature", 0)
        json.put("max_tokens", 1000)
        Log.d("JSON Response", json.toString())

        val requestBody: RequestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${Constants.API_KEY}")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.d("JSON Response", e.toString())
                    Toast.makeText(applicationContext, "Не удалось получить ответ.", Toast.LENGTH_SHORT).show()
                    isBotResponded = true
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    Log.d("JSON Response", responseData)
                }
                try{
                    val jsonResponse = JSONObject(responseData!!)
                    val responseMsg = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getString("text")
                    val chatResponse = MessageGPTData(responseMsg, false)
                    messageList.add(chatResponse)
                    runOnUiThread {
                        chatAdapter.addMessage(chatResponse)
                        isBotResponded = true
                        sendMessageToChat("Выберите действие: Сохранить, Повторить, Показать ещё")
                        if (responseMessageRecipe != null) {
                            handleUserActionOption(responseMessageRecipe!!,mergedQuery)
                        }
                    }
                }catch (e: Exception) {
                    Toast.makeText(applicationContext, "Бот временно не работает!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
