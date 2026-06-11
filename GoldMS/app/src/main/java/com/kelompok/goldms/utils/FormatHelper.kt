package com.kelompok.goldms.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatHelper {

    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }

    fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(date)
    }

    fun formatWeight(weight: Double): String {
        return String.format(Locale.getDefault(), "%.2f gram", weight)
    }

    // Faktor kemurnian karat (persentase emas murni)
    fun karatFactor(karat: Int): Double {
        return when (karat) {
            24 -> 1.0
            22 -> 22.0 / 24.0
            21 -> 21.0 / 24.0
            20 -> 20.0 / 24.0
            18 -> 18.0 / 24.0
            17 -> 17.0 / 24.0
            16 -> 16.0 / 24.0
            14 -> 14.0 / 24.0
            9  -> 9.0  / 24.0
            else -> 1.0
        }
    }
}
