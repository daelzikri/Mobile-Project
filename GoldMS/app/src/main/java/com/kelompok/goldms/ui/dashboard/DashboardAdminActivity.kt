package com.kelompok.goldms.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityDashboardAdminBinding
import com.kelompok.goldms.ui.auth.LoginActivity
import com.kelompok.goldms.ui.inventory.InventoryActivity
import com.kelompok.goldms.ui.user.UserManagementActivity
import com.kelompok.goldms.utils.FormatHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDashboardAdminBinding
    private val repository       = GoldRepository()
    private val auth             = FirebaseAuth.getInstance()
    private val scope            = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupButtons()
        loadDashboardData()
    }

    private fun setupButtons() {
        binding.btnInventaris.setOnClickListener    { startActivity(Intent(this, InventoryActivity::class.java)) }
        binding.btnKelolaPengguna.setOnClickListener { startActivity(Intent(this, UserManagementActivity::class.java)) }
        binding.btnInventaris.setOnClickListener    { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnPenjualan.setOnClickListener     { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnBuyback.setOnClickListener       { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnPengeluaran.setOnClickListener   { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnLaporan.setOnClickListener       { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnKelolaPengguna.setOnClickListener { Toast.makeText(this, "Fitur dalam pengembangan", Toast.LENGTH_SHORT).show() }
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Logout").setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ -> auth.signOut(); startActivity(Intent(this, LoginActivity::class.java)); finish() }
                .setNegativeButton("Batal", null).show()
        }
    }

    private fun loadDashboardData() {
        scope.launch {
            val goldPrice = withContext(Dispatchers.IO) { repository.fetchGoldPrice() }
            if (goldPrice.pricePerGramIdr > 0) {
                binding.tvHargaEmas.text  = FormatHelper.formatRupiah(goldPrice.pricePerGramIdr) + "/gram"
                binding.tvLastUpdate.text = "Update: " + FormatHelper.formatDate(Date(goldPrice.lastUpdated))
            } else {
                binding.tvHargaEmas.text = "Gagal memuat (cek internet)"
            }

            val todayStart = getTodayStartMs(); val todayEnd = getTodayEndMs()
            val transactions = withContext(Dispatchers.IO) { repository.getTransactionsByDate(todayStart, todayEnd) }
            val expenses     = withContext(Dispatchers.IO) { repository.getExpensesByDate(todayStart, todayEnd) }
            val inventory    = withContext(Dispatchers.IO) { repository.getAllInventory() }

            val omzet       = transactions.filter { it.tipe == "PENJUALAN" }.sumOf { it.totalHarga }
            val buyback     = transactions.filter { it.tipe == "BUYBACK" }.sumOf { it.totalHarga }
            val pengeluaran = expenses.sumOf { it.jumlah }
            val nilaiAset   = inventory.sumOf { it.beratGram * it.stok * goldPrice.pricePerGramIdr * FormatHelper.karatFactor(it.karat) }

            binding.tvOmzet.text       = FormatHelper.formatRupiah(omzet)
            binding.tvBuyback.text     = FormatHelper.formatRupiah(buyback)
            binding.tvPengeluaran.text = FormatHelper.formatRupiah(pengeluaran)
            binding.tvNilaiAset.text   = FormatHelper.formatRupiah(nilaiAset)

            loadWeeklyChart(goldPrice.pricePerGramIdr)
        }
    }

    private suspend fun loadWeeklyChart(goldPriceIdr: Double) {
        val sdf    = SimpleDateFormat("EEE", Locale("id"))
        val labels = mutableListOf<String>()
        val entries= mutableListOf<BarEntry>()

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val start = cal.apply { set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0) }.timeInMillis
            val end   = cal.apply { set(Calendar.HOUR_OF_DAY,23);set(Calendar.MINUTE,59);set(Calendar.SECOND,59) }.timeInMillis
            val txs   = withContext(Dispatchers.IO) { repository.getTransactionsByDate(start, end) }
            val omzet = txs.filter { it.tipe == "PENJUALAN" }.sumOf { it.totalHarga }.toFloat()
            entries.add(BarEntry((6 - i).toFloat(), omzet))
            labels.add(sdf.format(cal.time))
        }

        val dataSet = BarDataSet(entries, "Omzet (Rp)").apply {
            color = android.graphics.Color.parseColor("#B8860B")
            valueTextSize = 8f
        }
        binding.barChart.apply {
            data = BarData(dataSet)
            xAxis.apply {
                valueFormatter  = IndexAxisValueFormatter(labels)
                position        = XAxis.XAxisPosition.BOTTOM
                granularity     = 1f
                setDrawGridLines(false)
            }
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled    = false
            animateY(800)
            invalidate()
        }
    }

    override fun onResume() { super.onResume(); loadDashboardData() }
    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    private fun getTodayStartMs(): Long { val c = Calendar.getInstance(); c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0); return c.timeInMillis }
    private fun getTodayEndMs():   Long { val c = Calendar.getInstance(); c.set(Calendar.HOUR_OF_DAY,23);c.set(Calendar.MINUTE,59);c.set(Calendar.SECOND,59);c.set(Calendar.MILLISECOND,999); return c.timeInMillis }
}