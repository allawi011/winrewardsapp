package io.kodular.allawi.getmoney.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kodular.allawi.getmoney.databinding.ActivityDailyRewardBinding
import io.kodular.allawi.getmoney.utils.Constants

class DailyRewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyRewardBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDailyRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnClaim.setOnClickListener {
            claimDailyReward()
        }
    }

    private fun claimDailyReward() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "يجب تسجيل الدخول", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userRef = db.collection(Constants.COL_USERS).document(uid)

        db.runTransaction { transaction ->
            val snap = transaction.get(userRef)

            val currentPoints = snap.getLong(Constants.FIELD_POINTS) ?: 0L
            val lastDaily = snap.getLong("lastDailyReward") ?: 0L

            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            // اذا استلم قبل اقل من 24 ساعة
            if (now - lastDaily < dayMillis) {
                throw Exception("NOT_READY")
            }

            val newPoints = currentPoints + Constants.POINTS_DAILY

            transaction.update(userRef, Constants.FIELD_POINTS, newPoints)
            transaction.update(userRef, "lastDailyReward", now)

            newPoints
        }.addOnSuccessListener {
            Toast.makeText(this, "🎁 تم استلام المكافأة اليومية +${Constants.POINTS_DAILY} نقطة", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            if (e.message == "NOT_READY") {
                Toast.makeText(this, "❌ استلمت مكافأتك اليوم، تعال باچر", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
