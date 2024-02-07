package com.example.moengagnews.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.moengagnews.R

object GlideUtils {

    fun ImageView.loadImage(url: String?, placeholderResId: Int? = null, errorResId: Int? = null) {
        val requestOptions = RequestOptions()
            .placeholder(placeholderResId ?: R.drawable.ic_loading)
            .error(errorResId ?: R.drawable.ic_failed)

        Glide.with(this.context)
            .load(url)
            .apply(requestOptions)
            .into(this)
    }
}