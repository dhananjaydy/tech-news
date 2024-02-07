package com.example.moengagnews.newsScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    // initiating the API call from the init black itself rather than making a call from the view
    // to save up time and effectively lesser load time for user
    init {
        fetchData()
    }

    // keeping track of failure state of API through status code and making necessary layout changes accordingly
    private val _showError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean>
        get() = _showError

    private val _response = MutableLiveData<List<ArticlesResponse>>()
    val response: LiveData<List<ArticlesResponse>>
        get() = _response

    private var isAscending = true

    // by default storing the data in order of latest news received, function runs each time toggle button is pressed
    fun sortArticlesByTimestamp() {
        // doing the work in the background thread so that main thread does not suffer lag and
        // viewmodelscope for any expected events through which state shall be maintained
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
            // posting the data to variable using postValue rather than setValue as we are on background thread
            newData?.let {
                _response.postValue(it)
            }
            isAscending = !isAscending
        }
    }

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {

            // intialising the connection and making reqyest
            val url = URL(ENDPOINT)
            val connection = url.openConnection() as HttpURLConnection
            try {

                val code = connection.responseCode

                // checking if the code is valid for our case or not
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
                // in above steps, the string has been customised to be taken as JSON and then further actions
                // can be taken

                val finalResponse: Response = Gson().fromJson(jsonStringHolder.toString(), Response::class.java)
                _response.postValue(finalResponse.articles.toList())

                // JSON converted to data class using GSON
            } finally {
                connection.disconnect()
                // closing the initiated connection
            }
        }
    }

    companion object {
        const val ENDPOINT = "https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json"
        const val TAG = "NewsViewModel"
        const val TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    }
}

/*
    Future TODOs
        - Better state management using sealed classes inside ViewModel
        - filter functionality expansion basis several paramters using when condition
 */