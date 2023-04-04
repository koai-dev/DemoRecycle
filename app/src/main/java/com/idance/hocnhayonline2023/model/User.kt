package com.idance.hocnhayonline2023.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String?,
    val join: String?,
    val vip: Int?,
    val email: String?,
    val phone: String?,
    val status: String?,
    val id: Int?,
    val avatar: String?,
    val role: Int?
) : Parcelable {
    constructor(): this("Nguyen Kim Khanh", null, 0, "dtako.developer@gmail.com", "0394998716", "activated", 0, "", 0)
}