package com.kelompok.goldms.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.goldms.data.model.User
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityRegisterBinding
import com.kelompok.goldms.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding   : ActivityRegisterBinding
    private val auth               = FirebaseAuth.getInstance()
    private val repository         = GoldRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Tambah Akun"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnRegister.setOnClickListener {
            val nama     = binding.etNama.text.toString().trim()
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val role     = if (binding.rbAdmin.isChecked) Constants.ROLE_ADMIN else Constants.ROLE_KASIR

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid  = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val user = User(uid = uid, nama = nama, email = email, role = role, isActive = true)
                        CoroutineScope(Dispatchers.IO).launch {
                            repository.saveUser(user)
                            withContext(Dispatchers.Main) {
                                showLoading(false)
                                Toast.makeText(this@RegisterActivity, "Akun $nama berhasil dibuat", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled  = !show
    }
}