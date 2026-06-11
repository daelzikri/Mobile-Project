package com.kelompok.goldms.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Expense(
    @Exclude val id       : String     = "",
    val keterangan : String     = "",
    val jumlah     : Double     = 0.0,
    val kategori   : String     = "",   // "Listrik", "Gaji", "Perlengkapan", dll
    val kasirUid   : String     = "",
    val namaKasir  : String     = "",
    val tanggal    : Timestamp? = null
)
