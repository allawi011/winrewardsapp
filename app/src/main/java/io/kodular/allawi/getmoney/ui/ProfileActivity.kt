
package io.kodular.allawi.getmoney.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kodular.allawi.getmoney.databinding.ActivityProfileBinding
import io.kodular.allawi.getmoney.utils.Constants
import io.kodular.allawi.getmoney.utils.Prefs

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnCopyCode.setOnClickListener {
            copyInviteCode()
        }

        binding.btnShareInvite.setOnClickListener {
            shareInvite()
        }

        loadProfile()
    }

    private fun loadProfile() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "يجب تسجيل الدخول", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val uid = user.uid
        val userRef = db.collection(Constants.COL_USERS).document(uid)

        // معلومات من FirebaseAuth
        binding.tvName.text = user.displayName ?: "User"
        binding.tvEmail.text = user.email ?: "-"

        // كود الدعوة من Prefs (او من Firestore اذا تحب)
        val inviteCode = Prefs.getInviteCode(this)
        binding.tvInviteCode.text = inviteCode

        // نقاط + كود الدعوة من Firestore
        userRef.get()
            .addOnSuccessListener { doc ->
                val points = doc.getLong(Constants.FIELD_POINTS) ?: 0L
                binding.tvPoints.text = points.toString()

                // اذا كود الدعوة غير موجود بالـ prefs نجيبه من فايرستور
                val codeFromDb = doc.getString(Constants.FIELD_INVITE_CODE)
                if (!codeFromDb.isNullOrEmpty()) {
                    binding.tvInviteCode.text = codeFromDb
                    Prefs.saveInviteCode(this, codeFromDb)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "فشل جلب البيانات", Toast.LENGTH_SHORT).show()
            }
    }

    private fun copyInviteCode() {
        val code = binding.tvInviteCode.text.toString().trim()
        if (code.isEmpty()) {
            Toast.makeText(this, "ماكو كود دعوة", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Invite Code", code))
        Toast.makeText(this, "✅ تم نسخ كود الدعوة", Toast.LENGTH_SHORT).show()
    }

    private fun shareInvite() {
        val code = binding.tvInviteCode.text.toString().trim()
        if (code.isEmpty()) {
            Toast.makeText(this, "ماكو كود دعوة", Toast.LENGTH_SHORT).show()
            return
        }

        val shareText =
            "🎁 Get Money App\n" +
            "استخدم كود الدعوة الخاص بي حتى تحصل نقاط:\n" +
            "Code: $code\n" +
            "حمل التطبيق وجربه 👇"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "مشاركة الدعوة"))
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
