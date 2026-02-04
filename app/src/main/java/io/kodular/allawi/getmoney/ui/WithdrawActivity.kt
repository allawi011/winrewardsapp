package io.kodular.allawi.getmoney.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.kodular.allawi.getmoney.data.FirestoreRepository
import io.kodular.allawi.getmoney.databinding.ActivityWithdrawBinding
import io.kodular.allawi.getmoney.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class WithdrawActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWithdrawBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { FirestoreRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // طرق السحب
        val methods = listOf("ZainCash", "PUBG UC", "Google Play Card")
        binding.spMethod.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            methods
        )

        // عرض شرط السحب
        binding.tvInfo.text = "كل 1000 نقطة = 1$"

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

        val method = binding.spMethod.selectedItem.toString()
        val account = binding.etAccount.text.toString().trim()

        if (account.isEmpty()) {
            Toast.makeText(this, "اكتب رقم/حساب السحب", Toast.LENGTH_SHORT).show()
            return
        }

        // تعطيل الزر حتى ما يصير ضغط متكرر
        binding.btnSend.isEnabled = false

        repo.createWithdrawRequest(
            uid = uid,
            method = method,
            account = account
        ) { ok, msg ->
            binding.btnSend.isEnabled = true

            if (ok) {
                Toast.makeText(this, "تم إرسال طلب السحب ✅", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val text = when (msg) {
                    "not_enough_points" -> "نقاطك غير كافية (لازم ${Constants.POINTS_PER_USD} نقطة)"
                    else -> "خطأ: $msg"
                }
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
