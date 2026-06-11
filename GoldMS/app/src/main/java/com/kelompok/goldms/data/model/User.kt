package com.kelompok.goldms.data.model

data class User(
    val uid      : String = "",
    val nama     : String = "",
    val email    : String = "",
    val role     : String = "KASIR",  // "ADMIN" atau "KASIR"
    val isActive : Boolean = true
)
