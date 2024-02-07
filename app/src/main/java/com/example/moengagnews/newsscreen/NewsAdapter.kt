package com.example.moengagnews.newsScreen

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moengagnews.utils.GlideUtils.loadImage
import com.example.moengagnews.data.dto.ArticlesResponse
import com.example.moengagnews.databinding.LayoutNewsItemBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NewsAdapter(
    private var items: List<ArticlesResponse>,
    private val callback: (url: String) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {


    inner class NewsViewHolder(private val binding: LayoutNewsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ArticlesResponse) {
            with(binding) {
                headlineTv.text = item.description
                headerImv.loadImage(item.urlToImage)
                authorTv.text = item.author
                timePostedTv.text = item.publishedAt?.let { convertISOToDDMMYYYY(it) }
                root.setOnClickListener { item.url?.let { it1 -> callback(it1) } }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NewsViewHolder(LayoutNewsItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<ArticlesResponse>) {
        if (newItems.isNotEmpty()) {
            items = newItems
            notifyDataSetChanged()
        }
    }

    fun convertISOToDDMMYYYY(isoDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoDate)
        val calendar = Calendar.getInstance()
        date?.let {
            calendar.time = it
        }
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return String.format("%02d-%02d-%04d", dayOfMonth, month, year)
    }

}