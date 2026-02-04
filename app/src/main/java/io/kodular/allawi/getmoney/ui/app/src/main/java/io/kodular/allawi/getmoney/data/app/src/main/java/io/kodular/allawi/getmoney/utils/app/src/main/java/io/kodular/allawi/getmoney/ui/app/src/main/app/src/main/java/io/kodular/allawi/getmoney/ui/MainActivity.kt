package io.kodular.allawi.getmoney.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kodular.allawi.getmoney.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Buttons
        binding.btnWheel.setOnClickListener { startActivity(Intent(this, WheelActivity::class.java)) }
        binding.btnInvite.setOnClickListener { startActivity(Intent(this, InviteActivity::class.java)) }
        binding.btnRewardAd.setOnClickListener { startActivity(Intent(this, RewardAdActivity::class.java)) }
        binding.btnDaily.setOnClickListener { startActivity(Intent(this, DailyRewardActivity::class.java)) }
        binding.btnAccount.setOnClickListener { startActivity(Intent(this, AccountActivity::class.java)) }
        binding.btnWithdraw.setOnClickListener { startActivity(Intent(this, WithdrawActivity::class.java)) }

        listenUser()
    }

    private fun listenUser() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        db.collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    binding.tvName.text = snap.getString("name") ?: "User"
                    binding.tvPoints.text = (snap.getLong("points") ?: 0L).toString()
                }
            }
    }
}
