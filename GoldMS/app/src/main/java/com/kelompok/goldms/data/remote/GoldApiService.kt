package com.kelompok.goldms.data.remote

import com.kelompok.goldms.data.model.GoldPriceResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GoldApiService {
    @GET("api/{metal}/{currency}")
    suspend fun getGoldPrice(
        @Header("x-access-token") apiKey: String,
        @Path("metal")    metal    : String = "XAU",
        @Path("currency") currency : String = "USD"
    ): GoldPriceResponse
}
