package com.kelompok.goldms.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityUserManagementBinding
import com.kelompok.goldms.ui.auth.RegisterActivity
import kotlinx.coroutines.*

class UserManagementActivity : AppCompatActivity() {

    private lateinit var binding  : ActivityUserManagementBinding
    private val repository        = GoldRepository()
    private lateinit var adapter  : UserAdapter
    private val scope             = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = UserAdapter { user ->
            val action = if (user.isActive) "nonaktifkan" else "aktifkan"
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("${action.replaceFirstChar { it.uppercase() }} akun ${user.nama}?")
                .setPositiveButton("Ya") { _, _ ->
                    scope.launch {
                        withContext(Dispatchers.IO) { repository.updateUserActive(user.uid, !user.isActive) }
                        Toast.makeText(this@UserManagementActivity, "Akun berhasil di-${action}", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    }
                }
                .setNegativeButton("Batal", null).show()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter       = adapter
        binding.fab.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        loadUsers()
    }

    private fun loadUsers() {
        scope.launch {
            val list = withContext(Dispatchers.IO) { repository.getAllKasir() }
            adapter.submitList(list)
        }
    }

    override fun onResume()  { super.onResume(); loadUsers() }
    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
