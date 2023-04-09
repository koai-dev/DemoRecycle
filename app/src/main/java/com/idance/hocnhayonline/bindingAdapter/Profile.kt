package com.idance.hocnhayonline.bindingAdapter

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

@BindingAdapter("img_avt")
fun setAvatar(imageView: CircleImageView, src: String?){
    if (src!=null){
        Glide.with(imageView.context).load(src).centerCrop().into(imageView)
    }
}