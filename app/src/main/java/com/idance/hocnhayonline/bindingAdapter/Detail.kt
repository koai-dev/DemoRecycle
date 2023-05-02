package com.idance.hocnhayonline.bindingAdapter

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.idance.hocnhayonline.R

@SuppressLint("UseCompatLoadingForDrawables")
@BindingAdapter("hasFavorite")
fun setCheck(imageView: ImageView, hasFavorite: Boolean){
    imageView.apply {
        if (hasFavorite){
            setImageDrawable(imageView.resources.getDrawable(R.drawable.ic_detail_heart_fill, imageView.resources.newTheme()))
        }else{
            setImageDrawable(imageView.resources.getDrawable(R.drawable.ic_detail_heart, imageView.resources.newTheme()))
        }
    }
}