
package io.kodular.allawi.getmoney.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import io.kodular.allawi.getmoney.data.FirestoreRepository
import io.kodular.allawi.getmoney.databinding.ActivityWheelBinding
import kotlin.random.Random

class WheelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWheelBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { FirestoreRepository() }

    private var rewardedAd: RewardedAd? = null
    private var isSpinning = false

    // AdMob Rewarded Unit ID
    private val rewardedUnitId = "ca-app-pub-5117598639994532/7210737452"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWheelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        loadRewardAd()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSpin.setOnClickListener {
            spinWheel()
        }
    }

    private fun loadRewardAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            rewardedUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    private fun spinWheel() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "سجل دخول أولاً", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (isSpinning) return
        isSpinning = true
        binding.btnSpin.isEnabled = false

        // جوائز + نسب ظهور (الأقل أكثر)
        val rewards = listOf(
            10L to 40,   // 40%
            25L to 25,   // 25%
            50L to 15,   // 15%
            100L to 10,  // 10%
            150L to 7,   // 7%
            200L to 3    // 3%
        )

        val selectedReward = pickWeightedReward(rewards)

        // Animation fake (3 ثواني)
        binding.tvResult.text = "..."
        binding.tvStatus.text = "يدور الدولاب 🎡"
        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvResult.text = "+$selectedReward"
            binding.tvStatus.text = "فزت بـ $selectedReward نقطة 🎉"

            // بعد الفوز: اعلان ثم اضافة نقاط
            showAdThenGivePoints(uid, selectedReward)

        }, 3000)
    }

    private fun showAdThenGivePoints(uid: String, rewardPoints: Long) {
        val ad = rewardedAd
        if (ad == null) {
            // ماكو اعلان جاهز
            repo.addPoints(uid, rewardPoints) { ok, _ ->
                runOnUiThread {
                    doneSpin(ok)
                }
            }
            loadRewardAd()
            return
        }

        ad.show(this) { _: RewardItem ->
            // المستخدم شاف الاعلان
            repo.addPoints(uid, rewardPoints) { ok, _ ->
                runOnUiThread {
                    doneSpin(ok)
                }
            }
        }

        rewardedAd = null
        loadRewardAd()
    }

    private fun doneSpin(ok: Boolean) {
        isSpinning = false
        binding.btnSpin.isEnabled = true

        if (ok) {
            Toast.makeText(this, "تمت إضافة النقاط ✅", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "صار خطأ بإضافة النقاط", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickWeightedReward(list: List<Pair<Long, Int>>): Long {
        val total = list.sumOf { it.second }
        val r = Random.nextInt(total) + 1
        var cumulative = 0
        for ((value, weight) in list) {
            cumulative += weight
            if (r <= cumulative) return value
        }
        return list.first().first
    }
}
