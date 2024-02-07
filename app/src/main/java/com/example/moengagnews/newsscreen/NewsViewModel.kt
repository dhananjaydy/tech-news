package com.example.moengagnews.newsScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.moengagnews.data.dto.ArticlesResponse
import com.example.moengagnews.data.dto.Response
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class NewsViewModel : ViewModel() {

    init {
        fetchData()
    }

    private val _showError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean>
        get() = _showError

    private val _response = MutableLiveData<List<ArticlesResponse>>()
    val response: LiveData<List<ArticlesResponse>>
        get() = _response

    private var isAscending = true

    fun sortArticlesByTimestamp() {
        viewModelScope.launch(Dispatchers.IO) {
            val newData = if (isAscending) {
                _response.value?.sortedBy { response ->
                    // Parsing timestamp to get the milliseconds for sorting
                    response.publishedAt?.let {
                        SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault()).parse(
                            it
                        )?.time
                    }
                }
            } else {
                _response.value?.sortedByDescending { response ->
                    // Parsing timestamp to get the milliseconds for sorting
                    response.publishedAt?.let {
                        SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault()).parse(
                            it
                        )?.time
                    }
                }
            }
            newData?.let {
                _response.postValue(it)
            }
            isAscending = !isAscending
        }
    }

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            val url = URL(ENDPOINT)
            val connection = url.openConnection() as HttpURLConnection
            try {

                val code = connection.responseCode

                if (code != 200) {
                    _showError.postValue(true)
                    throw IOException("The error from the server is $code")
                }

                val bufferedReader = BufferedReader(
                    InputStreamReader(connection.inputStream)
                )

                val jsonStringHolder: StringBuilder = StringBuilder()

                while (true) {
                    val readLine = bufferedReader.readLine() ?: break
                    jsonStringHolder.append(readLine)
                }
                Log.d(TAG, "fetchData: $jsonStringHolder")
                val finalResponse: Response = Gson().fromJson(jsonStringHolder.toString(), Response::class.java)
                Log.d(TAG, "fetchData: list size is ${finalResponse.articles.size}")
                _response.postValue(finalResponse.articles.toList())
            } finally {
                connection.disconnect()
            }
        }
    }

    companion object {
        const val ENDPOINT = "https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json"
        const val TAG = "NewsViewModel"
        const val TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    }
}