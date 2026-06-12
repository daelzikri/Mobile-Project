package com.kelompok.goldms.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelompok.goldms.data.model.InventoryItem
import com.kelompok.goldms.databinding.ItemInventoryBinding
import com.kelompok.goldms.utils.FormatHelper

class InventoryAdapter(
    private val onEdit   : (InventoryItem) -> Unit,
    private val onDelete : (InventoryItem) -> Unit
) : ListAdapter<InventoryItem, InventoryAdapter.VH>(Diff()) {

    inner class VH(val b: ItemInventoryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvNamaItem.text = item.namaItem
        holder.b.tvKodeSku.text  = item.kodeSku
        holder.b.tvDetail.text   = "${item.jenis} | ${item.karat}K | ${FormatHelper.formatWeight(item.beratGram)}"
        holder.b.tvStok.text     = "Stok: ${item.stok}"
        holder.b.btnEdit.setOnClickListener   { onEdit(item) }
        holder.b.btnDelete.setOnClickListener { onDelete(item) }
    }

    class Diff : DiffUtil.ItemCallback<InventoryItem>() {
        override fun areItemsTheSame(a: InventoryItem, b: InventoryItem) = a.id == b.id
        override fun areContentsTheSame(a: InventoryItem, b: InventoryItem) = a == b
    }
}
