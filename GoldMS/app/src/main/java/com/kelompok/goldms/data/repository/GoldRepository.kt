package com.kelompok.goldms.data.repository

import com.kelompok.goldms.BuildConfig
import com.kelompok.goldms.data.model.GoldPrice
import com.kelompok.goldms.data.remote.RetrofitClient
import com.kelompok.goldms.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kelompok.goldms.data.model.Expense
import com.kelompok.goldms.data.model.InventoryItem
import com.kelompok.goldms.data.model.Transaction
import com.kelompok.goldms.data.model.User
import kotlinx.coroutines.tasks.await

class GoldRepository {

    private val db   = FirebaseFirestore.getInstance()
    // Kurs USD ke IDR — bisa diganti fetch dari API kurs jika ada
    private val usdToIdr = 16500.0

    // ───────────────── GOLD PRICE API ─────────────────
    suspend fun fetchGoldPrice(): GoldPrice {
        return try {
            val response = RetrofitClient.goldApiService.getGoldPrice(
                apiKey   = BuildConfig.GOLD_API_KEY,
                metal    = "XAU",
                currency = "USD"
            )
            val priceGramUsd = response.priceGram24k.takeIf { it > 0 }
                ?: (response.price / 31.1035)   // fallback: harga per troy oz ÷ 31.1035
            val priceGramIdr = priceGramUsd * usdToIdr
            GoldPrice(
                pricePerGramUsd = priceGramUsd,
                pricePerGramIdr = priceGramIdr,
                lastUpdated     = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            // Kembalikan 0 jika gagal — UI akan tampilkan cached price
            GoldPrice()
        }
    }

    // ───────────────── USER ─────────────────
    suspend fun getUserByUid(uid: String): User? {
        return try {
            val doc = db.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) { null }
    }

    suspend fun saveUser(user: User) {
        db.collection(Constants.COLLECTION_USERS)
            .document(user.uid)
            .set(user)
            .await()
    }

    suspend fun getAllKasir(): List<User> {
        return try {
            val snapshot = db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("role", Constants.ROLE_KASIR)
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun updateUserActive(uid: String, isActive: Boolean) {
        db.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .update("isActive", isActive)
            .await()
    }

    // ───────────────── INVENTORY ─────────────────
    suspend fun addInventoryItem(item: InventoryItem): String {
        val doc = db.collection(Constants.COLLECTION_INVENTORY).add(item).await()
        return doc.id
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        db.collection(Constants.COLLECTION_INVENTORY)
            .document(item.id)
            .set(item)
            .await()
    }

    suspend fun deleteInventoryItem(itemId: String) {
        db.collection(Constants.COLLECTION_INVENTORY)
            .document(itemId)
            .delete()
            .await()
    }

    suspend fun getAllInventory(): List<InventoryItem> {
        return try {
            val snapshot = db.collection(Constants.COLLECTION_INVENTORY).get().await()
            snapshot.documents.mapNotNull {
                it.toObject(InventoryItem::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) { emptyList() }
    }

    // ───────────────── TRANSACTIONS ─────────────────
    suspend fun addTransaction(transaction: Transaction): String {
        val doc = db.collection(Constants.COLLECTION_TRANSACTIONS).add(transaction).await()
        return doc.id
    }

    suspend fun getTransactionsByDate(startMs: Long, endMs: Long): List<Transaction> {
        return try {
            val start = com.google.firebase.Timestamp(startMs / 1000, 0)
            val end   = com.google.firebase.Timestamp(endMs / 1000, 0)
            val snapshot = db.collection(Constants.COLLECTION_TRANSACTIONS)
                .whereGreaterThanOrEqualTo("tanggal", start)
                .whereLessThanOrEqualTo("tanggal", end)
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull {
                it.toObject(Transaction::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) { emptyList() }
    }

    // ───────────────── EXPENSES ─────────────────
    suspend fun addExpense(expense: Expense): String {
        val doc = db.collection(Constants.COLLECTION_EXPENSES).add(expense).await()
        return doc.id
    }

    suspend fun getExpensesByDate(startMs: Long, endMs: Long): List<Expense> {
        return try {
            val start = com.google.firebase.Timestamp(startMs / 1000, 0)
            val end   = com.google.firebase.Timestamp(endMs / 1000, 0)
            val snapshot = db.collection(Constants.COLLECTION_EXPENSES)
                .whereGreaterThanOrEqualTo("tanggal", start)
                .whereLessThanOrEqualTo("tanggal", end)
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull {
                it.toObject(Expense::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) { emptyList() }
    }
}
