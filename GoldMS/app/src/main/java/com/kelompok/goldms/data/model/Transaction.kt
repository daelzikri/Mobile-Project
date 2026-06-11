package com.kelompok.goldms.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Transaction(
    @Exclude val id          : String     = "",
    val tipe          : String     = "",   // "PENJUALAN" atau "BUYBACK"
    val namaItem      : String     = "",
    val beratGram     : Double     = 0.0,
    val karat         : Int        = 24,
    val hargaEmasApi  : Double     = 0.0,  // harga emas/gram saat transaksi (IDR)
    val totalHarga    : Double     = 0.0,
    val namaKasir     : String     = "",
    val kasirUid      : String     = "",
    val catatan       : String     = "",
    val tanggal       : Timestamp? = null
)
