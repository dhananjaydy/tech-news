package com.example.moengagnews.newsScreen

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moengagnews.R
import com.example.moengagnews.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: NewsViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // executing screen via mini functions
        setupRecyclerview()
        setupSwitch()
        initObservers()
    }

    private fun setupRecyclerview() {
        binding.newsRv.apply {
            newsAdapter = NewsAdapter(emptyList()) { url ->

                // url received in callback, to open it in the chrome tab
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, Uri.parse(url))
            }
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupSwitch() {
        binding.dateSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            // toggle state being mainTained inside the ViewModel and data being sorted accordingly
            viewModel.sortArticlesByTimestamp()
            if (isChecked) {
                // use of scope function and trying to make the leaned side of the switch more darker by setting different colors
                with(binding) {
                    latestTv.setTextColor(Color.BLACK)
                    earliestTv.setTextColor(Color.GRAY)
                }
            } else {
                with(binding) {
                    latestTv.setTextColor(Color.GRAY)
                    earliestTv.setTextColor(Color.BLACK)
                }
            }
        }
    }

    private fun initObservers() {

        // whenever data change is there, items can be updated
        viewModel.response.observe(this) { newItems ->
            // the default loaidng layout is meant to made invisble in event of success of response
            binding.layoutNonSuccess.root.visibility = View.GONE
            newsAdapter.updateItems(newItems)
        }

        // commonn layout to show error to the user in case of any failure
        viewModel.showError.observe(this) { showError ->
            if (showError) {
                with (binding.layoutNonSuccess) {
                    root.visibility = View.VISIBLE
                    messageTv.text = getString(R.string.error_message)
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}

/*
    Future TODOs
        - adding mutiple tabs on the main app, having three to four tabs to increase user activity
        - the current layout is a normal scrolling one, we can replace it with a swipe upwards and sidewards
            to make it more habitual to the user
        - adding of gestures such as double tap, swipes in direction to perform actions like save, like functionalites
        - inclusion of search functionality to provide easier access to the trending news one might be looking for
 */