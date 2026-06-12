package com.kelompok.goldms.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.goldms.R
import com.kelompok.goldms.data.repository.GoldRepository
import com.kelompok.goldms.ui.dashboard.DashboardAdminActivity
import com.kelompok.goldms.ui.dashboard.DashboardKasirActivity
import com.kelompok.goldms.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private val auth       = FirebaseAuth.getInstance()
    private val repository = GoldRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Ambil role dari Firestore
                CoroutineScope(Dispatchers.IO).launch {
                    val user = repository.getUserByUid(currentUser.uid)
                    withContext(Dispatchers.Main) {
                        if (user != null && user.isActive) {
                            val intent = if (user.role == Constants.ROLE_ADMIN) {
                                Intent(this@SplashActivity, DashboardAdminActivity::class.java)
                            } else {
                                Intent(this@SplashActivity, DashboardKasirActivity::class.java)
                            }
                            startActivity(intent)
                        } else {
                            auth.signOut()
                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        }
                        finish()
                    }
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 2000L)
    }
}