package com.kelompok.goldms.ui.transaction

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.kelompok.goldms.data.model.Transaction
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityBuybackFormBinding
import com.kelompok.goldms.utils.Constants
import com.kelompok.goldms.utils.FormatHelper
import kotlinx.coroutines.*

class BuybackFormActivity : AppCompatActivity() {

    private lateinit var binding    : ActivityBuybackFormBinding
    private val repository          = GoldRepository()
    private val auth                = FirebaseAuth.getInstance()
    private val scope               = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentGoldPriceIdr = 0.0
    private var calculatedTotal     = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuybackFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scope.launch {
            val goldPrice = withContext(Dispatchers.IO) { repository.fetchGoldPrice() }
            currentGoldPriceIdr = goldPrice.pricePerGramIdr
            binding.tvHargaBuybackInfo.text =
                if (currentGoldPriceIdr > 0) FormatHelper.formatRupiah(currentGoldPriceIdr) + "/gram"
                else "Gagal memuat"
        }

        binding.btnHitung.setOnClickListener { hitungBuyback() }
        binding.btnSimpan.setOnClickListener { simpanBuyback() }
    }

    private fun hitungBuyback() {
        val karatStr   = binding.etKarat.text.toString().trim()
        val beratStr   = binding.etBerat.text.toString().trim()
        val potonganStr= binding.etPotongan.text.toString().trim()

        if (karatStr.isEmpty() || beratStr.isEmpty()) {
            Toast.makeText(this, "Isi karat dan berat terlebih dahulu", Toast.LENGTH_SHORT).show(); return
        }

        val karat       = karatStr.toIntOrNull() ?: 24
        val berat       = beratStr.toDoubleOrNull() ?: 0.0
        val potongan    = potonganStr.toDoubleOrNull() ?: 0.0
        val faktorKarat = FormatHelper.karatFactor(karat)

        val hargaKotor  = berat * currentGoldPriceIdr * faktorKarat
        val nilaiPotongan = hargaKotor * (potongan / 100.0)
        calculatedTotal = hargaKotor - nilaiPotongan

        binding.tvRincian.text =
            "Berat          : $berat gram\n" +
            "Karat          : ${karat}K (faktor: ${String.format("%.4f", faktorKarat)})\n" +
            "Harga kotor    : ${FormatHelper.formatRupiah(hargaKotor)}\n" +
            "Potongan $potongan% : ${FormatHelper.formatRupiah(nilaiPotongan)}"
        binding.tvTotal.text = FormatHelper.formatRupiah(calculatedTotal)
    }

    private fun simpanBuyback() {
        val namaBarang = binding.etNamaBarang.text.toString().trim()
        if (namaBarang.isEmpty()) { Toast.makeText(this, "Isi nama/keterangan barang", Toast.LENGTH_SHORT).show(); return }
        if (calculatedTotal == 0.0) { Toast.makeText(this, "Tekan HITUNG terlebih dahulu", Toast.LENGTH_SHORT).show(); return }

        val karat  = binding.etKarat.text.toString().toIntOrNull() ?: 24
        val berat  = binding.etBerat.text.toString().toDoubleOrNull() ?: 0.0
        val uid    = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled    = false

        scope.launch {
            val userObj = withContext(Dispatchers.IO) { repository.getUserByUid(uid) }
            val transaction = Transaction(
                tipe         = Constants.TYPE_BUYBACK,
                namaItem     = namaBarang,
                beratGram    = berat,
                karat        = karat,
                hargaEmasApi = currentGoldPriceIdr,
                totalHarga   = calculatedTotal,
                namaKasir    = userObj?.nama ?: "",
                kasirUid     = uid,
                catatan      = binding.etCatatan.text.toString().trim(),
                tanggal      = Timestamp.now()
            )
            withContext(Dispatchers.IO) { repository.addTransaction(transaction) }

            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@BuybackFormActivity, "Buyback disimpan!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
