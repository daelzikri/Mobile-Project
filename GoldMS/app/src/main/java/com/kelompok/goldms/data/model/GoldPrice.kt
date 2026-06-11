package com.kelompok.goldms.data.model

import com.google.gson.annotations.SerializedName

// Response dari GoldAPI.io
data class GoldPriceResponse(
    @SerializedName("metal")     val metal     : String = "",
    @SerializedName("currency") val currency  : String = "",
    @SerializedName("price")    val price     : Double = 0.0,   // harga per troy ounce (USD)
    @SerializedName("price_gram_24k") val priceGram24k : Double = 0.0  // harga per gram 24k (USD)
)

// Model lokal untuk disimpan di cache
data class GoldPrice(
    val pricePerGramUsd : Double = 0.0,
    val pricePerGramIdr : Double = 0.0,
    val lastUpdated     : Long   = 0L   // timestamp
)
