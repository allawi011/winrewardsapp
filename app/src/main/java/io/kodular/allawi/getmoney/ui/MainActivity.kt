
package io.kodular.allawi.getmoney.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import io.kodular.allawi.getmoney.data.FirestoreRepository
import io.kodular.allawi.getmoney.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { FirestoreRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // عرض اسم المستخدم
        val displayName = user.displayName ?: "User"
        binding.tvUserName.text = displayName

        // مراقبة النقاط Live
        repo.listenUserPoints(user.uid) { points ->
            binding.tvPoints.text = points.toString()
        }

        // زر دولاب الحظ
        binding.btnWheel.setOnClickListener {
            startActivity(Intent(this, WheelActivity::class.java))
        }

        // زر دعوة صديق
        binding.btnInvite.setOnClickListener {
            startActivity(Intent(this, InviteActivity::class.java))
        }

        // زر مشاهدة اعلان Reward
        binding.btnWatchAd.setOnClickListener {
            startActivity(Intent(this, RewardsAdActivity::class.java))
        }

        // زر المكافئة اليومية
        binding.btnDailyReward.setOnClickListener {
            startActivity(Intent(this, DailyRewardActivity::class.java))
        }

        // زر حسابي
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // زر سحب المال
        binding.btnWithdraw.setOnClickListener {
            startActivity(Intent(this, WithdrawActivity::class.java))
        }
    }
}
