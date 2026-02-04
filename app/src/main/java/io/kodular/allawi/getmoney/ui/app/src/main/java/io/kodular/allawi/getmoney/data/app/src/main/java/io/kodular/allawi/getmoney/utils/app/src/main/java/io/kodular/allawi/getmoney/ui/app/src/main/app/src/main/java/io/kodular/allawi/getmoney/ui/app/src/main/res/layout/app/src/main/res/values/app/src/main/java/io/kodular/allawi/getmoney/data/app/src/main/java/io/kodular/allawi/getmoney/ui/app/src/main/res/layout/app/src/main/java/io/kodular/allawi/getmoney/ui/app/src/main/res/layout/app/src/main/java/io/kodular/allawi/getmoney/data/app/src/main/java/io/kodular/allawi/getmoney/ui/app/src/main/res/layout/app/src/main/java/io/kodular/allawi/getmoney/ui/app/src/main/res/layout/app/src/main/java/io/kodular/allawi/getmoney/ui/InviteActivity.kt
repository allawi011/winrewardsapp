package io.kodular.allawi.getmoney.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kodular.allawi.getmoney.databinding.ActivityInviteBinding

class InviteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInviteBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInviteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadMyCode()

        binding.btnShare.setOnClickListener {
            val code = binding.tvMyCode.text.toString()
            val msg = "Get Money 💙\nاستخدم كود الدعوة حتى تربح نقاط: $code"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            startActivity(Intent.createChooser(intent, "Share"))
        }

        binding.btnApplyCode.setOnClickListener {
            val code = binding.etCode.text.toString().trim().uppercase()
            if (code.isEmpty()) {
                Toast.makeText(this, "اكتب كود الدعوة", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            applyInviteCode(code)
        }
    }

    private fun loadMyCode() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            binding.tvMyCode.text = doc.getString("inviteCode") ?: "----"
        }
    }

    private fun applyInviteCode(code: String) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).get().addOnSuccessListener { myDoc ->
            val invitedBy = myDoc.getString("invitedBy") ?: ""
            if (invitedBy.isNotEmpty()) {
                Toast.makeText(this, "تم استخدام كود دعوة سابقاً", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            db.collection("users")
                .whereEqualTo("inviteCode", code)
                .get()
                .addOnSuccessListener { qs ->
                    if (qs.isEmpty) {
                        Toast.makeText(this, "كود غير صحيح", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val inviterDoc = qs.documents[0]
                    val inviterUid = inviterDoc.getString("uid") ?: ""

                    if (inviterUid == myUid) {
                        Toast.makeText(this, "ما تكدر تستخدم كودك", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // set invitedBy for me
                    db.collection("users").document(myUid)
                        .update("invitedBy", code)
                        .addOnSuccessListener {
                            // add 100 points to inviter
                            db.collection("users").document(inviterUid)
                                .update("points", com.google.firebase.firestore.FieldValue.increment(100))
                            Toast.makeText(this, "تمت الدعوة +100 لصاحب الكود", Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }
}
