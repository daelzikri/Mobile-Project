package com.kelompok.goldms.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityDashboardKasirBinding
import com.kelompok.goldms.ui.auth.LoginActivity
import com.kelompok.goldms.utils.FormatHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.util.*

class DashboardKasirActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDashboardKasirBinding
    private val repository       = GoldRepository()
    private val auth             = FirebaseAuth.getInstance()
    private val scope            = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardKasirBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupButtons()
        loadDashboardData()
    }

    private fun setupButtons() {
        binding.btnPenjualan.setOnClickListener   { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnBuyback.setOnClickListener     { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnInventaris.setOnClickListener  { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnPengeluaran.setOnClickListener { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun loadDashboardData() {
        scope.launch {
            // Load harga emas
            val goldPrice = withContext(Dispatchers.IO) { repository.fetchGoldPrice() }
            if (goldPrice.pricePerGramIdr > 0) {
                binding.tvHargaEmas.text  = FormatHelper.formatRupiah(goldPrice.pricePerGramIdr) + "/gram"
                binding.tvLastUpdate.text = "Update: " + FormatHelper.formatDate(Date(goldPrice.lastUpdated))
            } else {
                binding.tvHargaEmas.text = "Gagal memuat (cek internet)"
            }

            // Load statistik hari ini
            val todayStart = getTodayStartMs()
            val todayEnd   = getTodayEndMs()

            val transactions = withContext(Dispatchers.IO) {
                repository.getTransactionsByDate(todayStart, todayEnd)
            }
            val expenses = withContext(Dispatchers.IO) {
                repository.getExpensesByDate(todayStart, todayEnd)
            }
            val inventory = withContext(Dispatchers.IO) {
                repository.getAllInventory()
            }

            val omzet      = transactions.filter { it.tipe == "PENJUALAN" }.sumOf { it.totalHarga }
            val buyback    = transactions.filter { it.tipe == "BUYBACK" }.sumOf { it.totalHarga }
            val pengeluaran = expenses.sumOf { it.jumlah }
            val totalStok   = inventory.sumOf { it.stok }

            binding.tvOmzet.text       = FormatHelper.formatRupiah(omzet)
            binding.tvBuyback.text     = FormatHelper.formatRupiah(buyback)
            binding.tvPengeluaran.text = FormatHelper.formatRupiah(pengeluaran)
            binding.tvStok.text        = "$totalStok item"
        }
    }

    override fun onResume() { super.onResume(); loadDashboardData() }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }

    private fun getTodayStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    private fun getTodayEndMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59);       cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}