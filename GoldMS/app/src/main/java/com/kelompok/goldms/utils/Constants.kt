package com.kelompok.goldms.utils

object Constants {
    // Firestore Collection Names
    const val COLLECTION_USERS        = "users"
    const val COLLECTION_INVENTORY    = "inventory"
    const val COLLECTION_TRANSACTIONS = "transactions"
    const val COLLECTION_EXPENSES     = "expenses"

    // User Roles
    const val ROLE_ADMIN  = "ADMIN"
    const val ROLE_KASIR  = "KASIR"

    // Transaction Types
    const val TYPE_PENJUALAN = "PENJUALAN"
    const val TYPE_BUYBACK   = "BUYBACK"

    // SharedPreferences
    const val PREF_NAME      = "goldms_pref"
    const val PREF_USER_ROLE = "user_role"
    const val PREF_USER_NAME = "user_name"
    const val PREF_USER_ID   = "user_id"
}
