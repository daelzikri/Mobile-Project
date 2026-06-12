package com.kelompok.goldms.ui.expense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelompok.goldms.data.model.Expense
import com.kelompok.goldms.databinding.ItemExpenseBinding
import com.kelompok.goldms.utils.FormatHelper

class ExpenseAdapter : ListAdapter<Expense, ExpenseAdapter.VH>(Diff()) {

    inner class VH(val b: ItemExpenseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvKeterangan.text = item.keterangan
        holder.b.tvKategori.text   = item.kategori
        holder.b.tvKasir.text      = "Kasir: ${item.namaKasir}"
        holder.b.tvJumlah.text     = FormatHelper.formatRupiah(item.jumlah)
    }

    class Diff : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(a: Expense, b: Expense) = a.id == b.id
        override fun areContentsTheSame(a: Expense, b: Expense) = a == b
    }
}
