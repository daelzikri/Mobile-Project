package com.kelompok.goldms.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.databinding.ActivityLoginBinding
import com.kelompok.goldms.ui.dashboard.DashboardAdminActivity
import com.kelompok.goldms.ui.dashboard.DashboardKasirActivity
import com.kelompok.goldms.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding   : ActivityLoginBinding
    private val auth               = FirebaseAuth.getInstance()
    private val repository         = GoldRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        CoroutineScope(Dispatchers.IO).launch {
                            val user = repository.getUserByUid(uid)
                            withContext(Dispatchers.Main) {
                                showLoading(false)
                                if (user == null) {
                                    Toast.makeText(this@LoginActivity, "Data akun tidak ditemukan", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    return@withContext
                                }
                                if (!user.isActive) {
                                    Toast.makeText(this@LoginActivity, "Akun Anda telah dinonaktifkan", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    return@withContext
                                }
                                val intent = if (user.role == Constants.ROLE_ADMIN) {
                                    Intent(this@LoginActivity, DashboardAdminActivity::class.java)
                                } else {
                                    Intent(this@LoginActivity, DashboardKasirActivity::class.java)
                                }
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled     = !show
    }
}