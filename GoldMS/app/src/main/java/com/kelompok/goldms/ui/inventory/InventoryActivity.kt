package com.kelompok.goldms.ui.inventory

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok.goldms.data.model.InventoryItem
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityInventoryBinding
import kotlinx.coroutines.*

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding  : ActivityInventoryBinding
    private val repository        = GoldRepository()
    private lateinit var adapter  : InventoryAdapter
    private val scope             = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = InventoryAdapter(
            onEdit   = { item ->
                val intent = Intent(this, AddEditInventoryActivity::class.java)
                intent.putExtra("itemId",      item.id)
                intent.putExtra("kodeSku",     item.kodeSku)
                intent.putExtra("namaItem",    item.namaItem)
                intent.putExtra("jenis",       item.jenis)
                intent.putExtra("karat",       item.karat)
                intent.putExtra("beratGram",   item.beratGram)
                intent.putExtra("stok",        item.stok)
                intent.putExtra("biayaBuat",   item.biayaBuat)
                startActivity(intent)
            },
            onDelete = { item -> confirmDelete(item) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter       = adapter
        binding.fab.setOnClickListener { startActivity(Intent(this, AddEditInventoryActivity::class.java)) }

        loadInventory()
    }

    private fun loadInventory() {
        scope.launch {
            val list = withContext(Dispatchers.IO) { repository.getAllInventory() }
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun confirmDelete(item: InventoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Barang")
            .setMessage("Hapus ${item.namaItem}?")
            .setPositiveButton("Hapus") { _, _ ->
                scope.launch {
                    withContext(Dispatchers.IO) { repository.deleteInventoryItem(item.id) }
                    Toast.makeText(this@InventoryActivity, "Barang dihapus", Toast.LENGTH_SHORT).show()
                    loadInventory()
                }
            }
            .setNegativeButton("Batal", null).show()
    }

    override fun onResume() {
        super.onResume()
        loadInventory()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
