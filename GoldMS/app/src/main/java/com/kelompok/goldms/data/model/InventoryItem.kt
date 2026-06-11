package com.kelompok.goldms.data.model

import com.google.firebase.firestore.Exclude

data class InventoryItem(
    @Exclude val id     : String = "",
    val kodeSku  : String = "",
    val namaItem : String = "",
    val jenis    : String = "",  // "Kalung", "Gelang", "Cincin", dll
    val karat    : Int    = 24,
    val beratGram: Double = 0.0,
    val stok     : Int    = 0,
    val biayaBuat: Double = 0.0  // ongkos pembuatan per item (Rp)
)
