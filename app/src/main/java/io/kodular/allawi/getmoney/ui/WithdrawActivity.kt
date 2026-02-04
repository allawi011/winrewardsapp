package io.kodular.allawi.getmoney.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kodular.allawi.getmoney.databinding.ActivityWithdrawBinding

class WithdrawActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWithdrawBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val methods = listOf("ZainCash", "PUBG UC", "Google Play Card")
        binding.spMethod.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            methods
        )

        binding.btnSend.setOnClickListener {
            sendWithdraw()
        }
    }

    private fun sendWithdraw() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "سجل دخول أولاً", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val pointsNeed = 1000L

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val points = doc.getLong("points") ?: 0L

                if (points < pointsNeed) {
                    Toast.makeText(this, "لا تملك نقاط كافية (1000 نقطة للسحب = 1$)", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val method = binding.spMethod.selectedItem.toString()
                val account = binding.etAccount.text.toString().trim()

                if (account.isEmpty()) {
                    Toast.makeText(this, "اكتب رقم/حساب السحب", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val data = hashMapOf(
                    "uid" to uid,
                    "method" to method,
                    "account" to account,
                    "points" to pointsNeed,
                    "status" to "pending",
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("withdraws").add(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "تم إرسال طلب السحب ✅", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "خطأ: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "خطأ: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
