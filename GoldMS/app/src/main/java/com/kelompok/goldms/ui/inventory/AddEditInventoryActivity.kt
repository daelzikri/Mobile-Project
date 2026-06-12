package com.kelompok.goldms.ui.inventory

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.goldms.data.model.InventoryItem
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityAddEditInventoryBinding
import kotlinx.coroutines.*

class AddEditInventoryActivity : AppCompatActivity() {

    private lateinit var binding  : ActivityAddEditInventoryBinding
    private val repository        = GoldRepository()
    private val scope             = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var existingItemId    = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        existingItemId = intent.getStringExtra("itemId") ?: ""
        if (existingItemId.isNotEmpty()) {
            binding.tvTitle.text = "Edit Barang"
            binding.etKodeSku.setText(intent.getStringExtra("kodeSku"))
            binding.etNamaItem.setText(intent.getStringExtra("namaItem"))
            binding.etJenis.setText(intent.getStringExtra("jenis"))
            binding.etKarat.setText(intent.getIntExtra("karat", 24).toString())
            binding.etBerat.setText(intent.getDoubleExtra("beratGram", 0.0).toString())
            binding.etStok.setText(intent.getIntExtra("stok", 0).toString())
            binding.etBiayaBuat.setText(intent.getDoubleExtra("biayaBuat", 0.0).toString())
        }

        binding.btnSimpan.setOnClickListener { saveItem() }
    }

    private fun saveItem() {
        val kodeSku   = binding.etKodeSku.text.toString().trim()
        val namaItem  = binding.etNamaItem.text.toString().trim()
        val jenis     = binding.etJenis.text.toString().trim()
        val karatStr  = binding.etKarat.text.toString().trim()
        val beratStr  = binding.etBerat.text.toString().trim()
        val stokStr   = binding.etStok.text.toString().trim()
        val biayaStr  = binding.etBiayaBuat.text.toString().trim()

        if (kodeSku.isEmpty() || namaItem.isEmpty() || karatStr.isEmpty() ||
            beratStr.isEmpty() || stokStr.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val karat     = karatStr.toIntOrNull() ?: 24
        val berat     = beratStr.toDoubleOrNull() ?: 0.0
        val stok      = stokStr.toIntOrNull() ?: 0
        val biayaBuat = biayaStr.toDoubleOrNull() ?: 0.0

        val item = InventoryItem(
            id        = existingItemId,
            kodeSku   = kodeSku,
            namaItem  = namaItem,
            jenis     = jenis,
            karat     = karat,
            beratGram = berat,
            stok      = stok,
            biayaBuat = biayaBuat
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled    = false

        scope.launch {
            withContext(Dispatchers.IO) {
                if (existingItemId.isEmpty()) repository.addInventoryItem(item)
                else repository.updateInventoryItem(item)
            }
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@AddEditInventoryActivity,
                if (existingItemId.isEmpty()) "Barang ditambahkan" else "Barang diperbarui",
                Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
