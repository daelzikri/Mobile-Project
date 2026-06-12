package com.kelompok.goldms.ui.report

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelompok.goldms.data.model.Transaction
import com.kelompok.goldms.databinding.ItemTransactionBinding
import com.kelompok.goldms.utils.FormatHelper

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.VH>(Diff()) {

    inner class VH(val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = getItem(position)
        holder.b.tvNamaItem.text  = t.namaItem
        holder.b.tvBadgeTipe.text = t.tipe
        holder.b.tvBadgeTipe.setBackgroundColor(
            if (t.tipe == "PENJUALAN") Color.parseColor("#2E7D32") else Color.parseColor("#E65100")
        )
        holder.b.tvDetail.text    = "${t.karat}K | ${t.beratGram}gr | Harga API: ${FormatHelper.formatRupiah(t.hargaEmasApi)}/gr"
        holder.b.tvKasirDate.text = "${t.namaKasir} • ${t.tanggal?.toDate()?.let { FormatHelper.formatDate(it) } ?: "-"}"
        holder.b.tvTotal.text     = FormatHelper.formatRupiah(t.totalHarga)
    }

    class Diff : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(a: Transaction, b: Transaction) = a.id == b.id
        override fun areContentsTheSame(a: Transaction, b: Transaction) = a == b
    }
}
