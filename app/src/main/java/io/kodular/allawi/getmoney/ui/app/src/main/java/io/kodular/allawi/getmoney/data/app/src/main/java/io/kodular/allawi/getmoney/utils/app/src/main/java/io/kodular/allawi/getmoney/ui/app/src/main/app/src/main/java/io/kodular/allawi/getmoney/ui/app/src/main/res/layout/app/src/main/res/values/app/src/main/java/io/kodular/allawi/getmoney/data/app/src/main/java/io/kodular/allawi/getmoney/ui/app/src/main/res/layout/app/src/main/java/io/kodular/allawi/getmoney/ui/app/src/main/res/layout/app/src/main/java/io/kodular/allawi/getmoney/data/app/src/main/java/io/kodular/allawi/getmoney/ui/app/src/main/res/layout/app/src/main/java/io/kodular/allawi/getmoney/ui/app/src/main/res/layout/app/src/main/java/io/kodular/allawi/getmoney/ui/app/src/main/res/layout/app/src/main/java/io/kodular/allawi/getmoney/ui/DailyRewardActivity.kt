package io.kodular.allawi.getmoney.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import io.kodular.allawi.getmoney.data.PointsRepo
import io.kodular.allawi.getmoney.databinding.ActivityDailyBinding

class DailyRewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyBinding
    private val repo = PointsRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClaim.setOnClickListener { claimDaily() }
    }

    private fun claimDaily() {
        val ref = repo.getUserDocRef()
        ref.get().addOnSuccessListener { doc ->
            val last = doc.getLong("lastDaily") ?: 0L
            val now = System.currentTimeMillis()

            val day = 24L * 60L * 60L * 1000L
            if (now - last < day) {
                Toast.makeText(this, "تم الاستلام اليوم، ارجع باجر", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            ref.update(
                mapOf(
                    "lastDaily" to now,
                    "points" to FieldValue.increment(25)
                )
            )
            Toast.makeText(this, "+25 نقطة 🎁", Toast.LENGTH_SHORT).show()
        }
    }
}
