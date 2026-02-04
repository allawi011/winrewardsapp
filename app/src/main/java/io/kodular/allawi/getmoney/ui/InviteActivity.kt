
package io.kodular.allawi.getmoney.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import io.kodular.allawi.getmoney.data.FirestoreRepository
import io.kodular.allawi.getmoney.databinding.ActivityInviteBinding
import io.kodular.allawi.getmoney.utils.Constants

class InviteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInviteBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { FirestoreRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInviteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "سجل دخول أولاً", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // عرض كود الدعوة الخاص بالمستخدم
        repo.getUserDoc(uid).get()
            .addOnSuccessListener { doc ->
                val myCode = doc.getString(Constants.FIELD_INVITE_CODE) ?: ""
                binding.tvMyCode.text = myCode.ifBlank { "----" }
            }
            .addOnFailureListener {
                binding.tvMyCode.text = "----"
            }

        // مشاركة كود الدعوة
        binding.btnShare.setOnClickListener {
            val code = binding.tvMyCode.text.toString().trim()
            if (code.isBlank() || code == "----") {
                Toast.makeText(this, "لا يوجد كود دعوة", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val shareText =
                "🎁 Get Money App\n" +
                        "استخدم كود الدعوة الخاص بي للحصول على نقاط مجانية: $code\n" +
                        "حمّل التطبيق وسجل حسابك 🔥"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "مشاركة كود الدعوة"))
        }

        // تطبيق كود دعوة لشخص ثاني
        binding.btnApplyCode.setOnClickListener {
            val inviteCode = binding.etInviteCode.text.toString().trim().uppercase()

            if (inviteCode.isBlank()) {
                Toast.makeText(this, "اكتب كود الدعوة", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnApplyCode.isEnabled = false
            repo.applyInviteCode(uid, inviteCode) { ok, msg ->
                runOnUiThread {
                    binding.btnApplyCode.isEnabled = true

                    if (ok) {
                        Toast.makeText(
                            this,
                            "تم تطبيق كود الدعوة ✅ +${Constants.POINTS_PER_INVITE} نقطة",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.etInviteCode.setText("")
                    } else {
                        val text = when (msg) {
                            "code_not_found" -> "الكود غير موجود"
                            "already_used" -> "أنت مستخدم كود دعوة سابقاً"
                            "invalid_code" -> "كود غير صالح"
                            else -> "فشل: $msg"
                        }
                        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // رجوع
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
