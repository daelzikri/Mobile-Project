package com.kelompok.goldms.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelompok.goldms.data.model.User
import com.kelompok.goldms.databinding.ItemUserBinding

class UserAdapter(
    private val onToggleActive: (User) -> Unit
) : ListAdapter<User, UserAdapter.VH>(Diff()) {

    inner class VH(val b: ItemUserBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = getItem(position)
        holder.b.tvNama.text  = user.nama
        holder.b.tvEmail.text = user.email
        holder.b.tvRole.text  = "Role: ${user.role}"
        holder.b.btnToggleActive.text = if (user.isActive) "Nonaktifkan" else "Aktifkan"
        holder.b.btnToggleActive.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (user.isActive) android.graphics.Color.parseColor("#C62828")
                else android.graphics.Color.parseColor("#2E7D32")
            )
        holder.b.btnToggleActive.setOnClickListener { onToggleActive(user) }
    }

    class Diff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(a: User, b: User) = a.uid == b.uid
        override fun areContentsTheSame(a: User, b: User) = a == b
    }
}
