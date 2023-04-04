package com.idance.hocnhayonline2023.bindingAdapter

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.idance.hocnhayonline2023.R
import de.hdodenhof.circleimageview.CircleImageView

@BindingAdapter("img_avt")
fun setAvatar(imageView: CircleImageView, src: String?){
    if (src!=null){
        Glide.with(imageView.context).load(src).centerCrop().into(imageView)
    }
}