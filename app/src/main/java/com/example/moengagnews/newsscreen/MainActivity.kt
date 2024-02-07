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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerview()
        setupSwitch()
        initObservers()
    }

    private fun setupRecyclerview() {
        binding.newsRv.apply {
            newsAdapter = NewsAdapter(emptyList()) { url ->
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
            viewModel.sortArticlesByTimestamp()
            if (isChecked) {
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

        viewModel.response.observe(this) { newItems ->
            binding.layoutNonSuccess.root.visibility = View.GONE
            newsAdapter.updateItems(newItems)
        }

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