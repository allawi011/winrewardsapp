
package io.kodular.allawi.getmoney.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import io.kodular.allawi.getmoney.data.FirestoreRepository
import io.kodular.allawi.getmoney.databinding.ActivityDailyRewardBinding
import io.kodular.allawi.getmoney.utils.Constants
import io.kodular.allawi.getmoney.utils.Prefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyRewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyRewardBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { FirestoreRepository() }
    private val prefs by lazy { Prefs(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnClaim.setOnClickListener {
            claimDaily()
        }
    }

    private fun claimDaily() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "سجل دخول أولاً", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val last = prefs.getString("last_daily", "")

        if (last == today) {
            Toast.makeText(this, "أخذت مكافأة اليوم مسبقاً ✅", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnClaim.isEnabled = false

        repo.addPoints(uid, Constants.POINTS_DAILY) { ok, msg ->
            runOnUiThread {
                binding.btnClaim.isEnabled = true

                if (ok) {
                    prefs.setString("last_daily", today)
                    Toast.makeText(
                        this,
                        "تمت إضافة ${Constants.POINTS_DAILY} نقطة 🎁",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this, "خطأ: $msg", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
