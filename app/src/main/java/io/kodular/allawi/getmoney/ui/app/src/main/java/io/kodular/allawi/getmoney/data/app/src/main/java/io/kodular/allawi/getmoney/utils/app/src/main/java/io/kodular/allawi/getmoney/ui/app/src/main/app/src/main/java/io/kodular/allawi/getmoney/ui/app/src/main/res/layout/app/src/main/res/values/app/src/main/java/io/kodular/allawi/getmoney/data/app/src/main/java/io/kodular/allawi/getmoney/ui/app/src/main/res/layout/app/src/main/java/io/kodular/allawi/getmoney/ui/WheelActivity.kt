package io.kodular.allawi.getmoney.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.kodular.allawi.getmoney.data.PointsRepo
import io.kodular.allawi.getmoney.databinding.ActivityWheelBinding
import kotlin.random.Random

class WheelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWheelBinding
    private val repo = PointsRepo()

    private var rewardedAd: RewardedAd? = null
    private val adUnitId = "ca-app-pub-5117598639994532/7210737452"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWheelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)
        loadAd()

        binding.btnSpin.setOnClickListener {
            val result = spinResult()
            binding.tvResult.text = result

            if (result == "حظ اوفر") {
                Toast.makeText(this, "حظ اوفر 😅", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val points = result.toLong()
            showAdThenReward(points)
        }
    }

    private fun spinResult(): String {
        val r = Random.nextInt(1, 101)

        return when {
            r <= 30 -> "10"
            r <= 55 -> "25"
            r <= 73 -> "50"
            r <= 85 -> "100"
            r <= 93 -> "150"
            r <= 98 -> "200"
            else -> "حظ اوفر"
        }
    }

    private fun loadAd() {
        rewardedAd = null
        RewardedAd.load(
            this,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            }
        )
    }

    private fun showAdThenReward(points: Long) {
        val ad = rewardedAd
        if (ad == null) {
            Toast.makeText(this, "الاعلان غير جاهز، حاول مرة ثانية", Toast.LENGTH_SHORT).show()
            loadAd()
            return
        }

        ad.show(this) { _: RewardItem ->
            repo.addPoints(points)
            Toast.makeText(this, "+$points نقطة", Toast.LENGTH_SHORT).show()
            loadAd()
        }
    }
}
