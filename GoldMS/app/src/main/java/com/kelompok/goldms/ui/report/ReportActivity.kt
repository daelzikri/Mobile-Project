package com.kelompok.goldms.ui.report

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok.goldms.data.model.Transaction
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityReportBinding
import com.kelompok.goldms.utils.FormatHelper
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var binding   : ActivityReportBinding
    private val repository         = GoldRepository()
    private lateinit var adapter   : TransactionAdapter
    private val scope              = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var startCal           = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
    private var endCal             = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }
    private var currentFilter      = "ALL"
    private var allTransactions    = listOf<Transaction>()
    private val sdf                = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = TransactionAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter       = adapter

        updateButtonLabels()

        binding.btnPickStart.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                startCal.set(y, m, d, 0, 0, 0)
                updateButtonLabels()
                loadReport()
            }, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnPickEnd.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                endCal.set(y, m, d, 23, 59, 59)
                updateButtonLabels()
                loadReport()
            }, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnFilterAll.setOnClickListener      { currentFilter = "ALL"; applyFilter() }
        binding.btnFilterPenjualan.setOnClickListener { currentFilter = "PENJUALAN"; applyFilter() }
        binding.btnFilterBuyback.setOnClickListener   { currentFilter = "BUYBACK"; applyFilter() }

        loadReport()
    }

    private fun updateButtonLabels() {
        binding.btnPickStart.text = "Dari: ${sdf.format(startCal.time)}"
        binding.btnPickEnd.text   = "Sampai: ${sdf.format(endCal.time)}"
    }

    private fun loadReport() {
        scope.launch {
            allTransactions = withContext(Dispatchers.IO) {
                repository.getTransactionsByDate(startCal.timeInMillis, endCal.timeInMillis)
            }
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            "PENJUALAN" -> allTransactions.filter { it.tipe == "PENJUALAN" }
            "BUYBACK"   -> allTransactions.filter { it.tipe == "BUYBACK" }
            else        -> allTransactions
        }
        adapter.submitList(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE

        val totalJual   = allTransactions.filter { it.tipe == "PENJUALAN" }.sumOf { it.totalHarga }
        val totalBuyback= allTransactions.filter { it.tipe == "BUYBACK" }.sumOf { it.totalHarga }
        binding.tvTotalPenjualan.text = FormatHelper.formatRupiah(totalJual)
        binding.tvTotalBuyback.text   = FormatHelper.formatRupiah(totalBuyback)
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
