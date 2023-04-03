package com.idance.hocnhayonline2023.bindingAdapter

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.idance.hocnhayonline2023.R

@BindingAdapter("thumb")
fun setThumb(shapeableImageView: ShapeableImageView, thumb: String?) {
    if (thumb != null) {
        Glide.with(shapeableImageView.context).load(thumb).error(R.drawable.bg_round_8dp)
            .into(shapeableImageView)
    }
}