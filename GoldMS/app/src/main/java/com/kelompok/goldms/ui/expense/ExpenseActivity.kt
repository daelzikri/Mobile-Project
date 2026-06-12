package com.kelompok.goldms.ui.expense

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.kelompok.goldms.data.model.Expense
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityExpenseBinding
import kotlinx.coroutines.*
import java.util.*

class ExpenseActivity : AppCompatActivity() {

    private lateinit var binding  : ActivityExpenseBinding
    private val repository        = GoldRepository()
    private val auth              = FirebaseAuth.getInstance()
    private lateinit var adapter  : ExpenseAdapter
    private val scope             = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ExpenseAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter       = adapter

        binding.btnTambah.setOnClickListener { tambahPengeluaran() }
        loadTodayExpenses()
    }

    private fun tambahPengeluaran() {
        val keterangan = binding.etKeterangan.text.toString().trim()
        val jumlahStr  = binding.etJumlah.text.toString().trim()
        val kategori   = binding.etKategori.text.toString().trim()

        if (keterangan.isEmpty() || jumlahStr.isEmpty()) {
            Toast.makeText(this, "Keterangan dan jumlah harus diisi", Toast.LENGTH_SHORT).show(); return
        }
        val jumlah = jumlahStr.toDoubleOrNull() ?: 0.0
        if (jumlah <= 0) { Toast.makeText(this, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show(); return }

        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnTambah.isEnabled    = false

        scope.launch {
            val userObj = withContext(Dispatchers.IO) { repository.getUserByUid(uid) }
            val expense = Expense(
                keterangan = keterangan,
                jumlah     = jumlah,
                kategori   = kategori,
                kasirUid   = uid,
                namaKasir  = userObj?.nama ?: "",
                tanggal    = Timestamp.now()
            )
            withContext(Dispatchers.IO) { repository.addExpense(expense) }

            binding.progressBar.visibility = View.GONE
            binding.btnTambah.isEnabled    = true
            binding.etKeterangan.text?.clear()
            binding.etJumlah.text?.clear()
            binding.etKategori.text?.clear()
            Toast.makeText(this@ExpenseActivity, "Pengeluaran dicatat", Toast.LENGTH_SHORT).show()
            loadTodayExpenses()
        }
    }

    private fun loadTodayExpenses() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val end   = cal.timeInMillis

        scope.launch {
            val list = withContext(Dispatchers.IO) { repository.getExpensesByDate(start, end) }
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
