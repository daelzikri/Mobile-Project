package com.kelompok.goldms.ui.transaction

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.kelompok.goldms.data.model.InventoryItem
import com.kelompok.goldms.data.model.Transaction
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivitySaleFormBinding
import com.kelompok.goldms.utils.Constants
import com.kelompok.goldms.utils.FormatHelper
import kotlinx.coroutines.*

class SaleFormActivity : AppCompatActivity() {

    private lateinit var binding     : ActivitySaleFormBinding
    private val repository           = GoldRepository()
    private val auth                 = FirebaseAuth.getInstance()
    private val scope                = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var inventoryList        = listOf<InventoryItem>()
    private var selectedItem         : InventoryItem? = null
    private var currentGoldPriceIdr  = 0.0
    private var calculatedTotal      = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadData()

        binding.spinnerBarang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos > 0) {
                    selectedItem = inventoryList[pos - 1]
                    showItemInfo()
                } else {
                    selectedItem = null
                    binding.cardInfoBarang.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnHitung.setOnClickListener { hitungHarga() }
        binding.btnSimpan.setOnClickListener { simpanTransaksi() }
    }

    private fun loadData() {
        scope.launch {
            val goldPrice = withContext(Dispatchers.IO) { repository.fetchGoldPrice() }
            currentGoldPriceIdr = goldPrice.pricePerGramIdr
            if (currentGoldPriceIdr > 0) {
                binding.tvHargaEmasInfo.text = FormatHelper.formatRupiah(currentGoldPriceIdr) + "/gram"
            } else {
                binding.tvHargaEmasInfo.text = "Gagal memuat"
            }

            inventoryList = withContext(Dispatchers.IO) { repository.getAllInventory() }
                .filter { it.stok > 0 }

            val labels = mutableListOf("-- Pilih Barang --")
            labels.addAll(inventoryList.map { "${it.namaItem} (${it.karat}K, ${it.beratGram}gr) - Stok: ${it.stok}" })
            val adapter = ArrayAdapter(this@SaleFormActivity, android.R.layout.simple_spinner_item, labels)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerBarang.adapter = adapter
        }
    }

    private fun showItemInfo() {
        val item = selectedItem ?: return
        binding.cardInfoBarang.visibility = View.VISIBLE
        binding.tvInfoBarang.text = "Nama  : ${item.namaItem}\n" +
                "Jenis  : ${item.jenis}\n" +
                "Karat  : ${item.karat}K\n" +
                "Berat  : ${item.beratGram} gram\n" +
                "Stok   : ${item.stok} pcs\n" +
                "Biaya Buat: ${FormatHelper.formatRupiah(item.biayaBuat)}"
    }

    private fun hitungHarga() {
        val item   = selectedItem ?: run { Toast.makeText(this, "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show(); return }
        val jumlah = binding.etJumlah.text.toString().toIntOrNull() ?: 1

        if (jumlah <= 0) { Toast.makeText(this, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show(); return }
        if (jumlah > item.stok) { Toast.makeText(this, "Stok tidak mencukupi! Stok tersedia: ${item.stok}", Toast.LENGTH_SHORT).show(); return }

        val faktorKarat   = FormatHelper.karatFactor(item.karat)
        val hargaEmas     = item.beratGram * currentGoldPriceIdr * faktorKarat
        val biayaBuat     = item.biayaBuat
        val hargaPerItem  = hargaEmas + biayaBuat
        calculatedTotal   = hargaPerItem * jumlah

        binding.tvRincian.text =
            "Berat       : ${item.beratGram} gram\n" +
            "Karat       : ${item.karat}K (faktor: ${String.format("%.4f", faktorKarat)})\n" +
            "Harga emas  : ${FormatHelper.formatRupiah(hargaEmas)}\n" +
            "Biaya buat  : ${FormatHelper.formatRupiah(biayaBuat)}\n" +
            "Harga/item  : ${FormatHelper.formatRupiah(hargaPerItem)}\n" +
            "Jumlah      : $jumlah pcs"
        binding.tvTotal.text = FormatHelper.formatRupiah(calculatedTotal)
    }

    private fun simpanTransaksi() {
        val item = selectedItem ?: run { Toast.makeText(this, "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show(); return }
        if (calculatedTotal == 0.0) { Toast.makeText(this, "Tekan HITUNG terlebih dahulu", Toast.LENGTH_SHORT).show(); return }

        val jumlah = binding.etJumlah.text.toString().toIntOrNull() ?: 1
        val uid    = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled    = false

        scope.launch {
            val userObj = withContext(Dispatchers.IO) { repository.getUserByUid(uid) }
            val transaction = Transaction(
                tipe         = Constants.TYPE_PENJUALAN,
                namaItem     = item.namaItem,
                beratGram    = item.beratGram * jumlah,
                karat        = item.karat,
                hargaEmasApi = currentGoldPriceIdr,
                totalHarga   = calculatedTotal,
                namaKasir    = userObj?.nama ?: "",
                kasirUid     = uid,
                catatan      = binding.etCatatan.text.toString().trim(),
                tanggal      = Timestamp.now()
            )

            withContext(Dispatchers.IO) {
                repository.addTransaction(transaction)
                // Kurangi stok
                val updatedItem = item.copy(stok = item.stok - jumlah)
                repository.updateInventoryItem(updatedItem)
            }

            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@SaleFormActivity, "Transaksi penjualan disimpan!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
