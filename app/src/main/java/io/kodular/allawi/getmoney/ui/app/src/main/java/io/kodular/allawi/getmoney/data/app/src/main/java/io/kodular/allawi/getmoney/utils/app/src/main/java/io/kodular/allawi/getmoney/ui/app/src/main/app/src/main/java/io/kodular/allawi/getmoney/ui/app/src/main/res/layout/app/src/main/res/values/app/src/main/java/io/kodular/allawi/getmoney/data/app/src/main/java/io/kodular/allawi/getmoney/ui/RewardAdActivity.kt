package io.kodular.allawi.getmoney.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.kodular.allawi.getmoney.R
import io.kodular.allawi.getmoney.data.PointsRepo
import io.kodular.allawi.getmoney.databinding.ActivityRewardAdBinding

class RewardAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardAdBinding
    private var rewardedAd: RewardedAd? = null
    private val repo = PointsRepo()

    private val adUnitId = "ca-app-pub-5117598639994532/7210737452"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)

        binding.btnLoad.setOnClickListener { loadAd() }
        binding.btnShow.setOnClickListener { showAd() }

        loadAd()
    }

    private fun loadAd() {
        binding.tvStatus.text = "جاري تحميل الاعلان..."
        rewardedAd = null

        RewardedAd.load(
            this,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    binding.tvStatus.text = "تم تحميل الاعلان ✅"
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    binding.tvStatus.text = "فشل التحميل ❌"
                }
            }
        )
    }

    private fun showAd() {
        val ad = rewardedAd
        if (ad == null) {
            Toast.makeText(this, "الاعلان غير جاهز", Toast.LENGTH_SHORT).show()
            return
        }

        ad.show(this) { _: RewardItem ->
            repo.addPoints(25)
            Toast.makeText(this, "+25 نقطة", Toast.LENGTH_SHORT).show()
            loadAd()
        }
    }
}
