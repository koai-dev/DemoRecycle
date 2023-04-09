package com.idance.hocnhayonline.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(val id: Int?, val thumb: String?, val linkMovie: String?, val name: String?) :
    Parcelable {
    constructor() : this(
        0,
        "https://i.vietgiaitri.com/2019/11/11/trang-phuc-muot-toi-tung-chi-tiet-cua-thi-sinh-kpop-dance-for-youth-a4ee6-4439586_default.jpg",
        null,
        "Lên là lên là lên"
    )

    constructor(thumb: String?, name: String?) : this(0, thumb, null, name)
}