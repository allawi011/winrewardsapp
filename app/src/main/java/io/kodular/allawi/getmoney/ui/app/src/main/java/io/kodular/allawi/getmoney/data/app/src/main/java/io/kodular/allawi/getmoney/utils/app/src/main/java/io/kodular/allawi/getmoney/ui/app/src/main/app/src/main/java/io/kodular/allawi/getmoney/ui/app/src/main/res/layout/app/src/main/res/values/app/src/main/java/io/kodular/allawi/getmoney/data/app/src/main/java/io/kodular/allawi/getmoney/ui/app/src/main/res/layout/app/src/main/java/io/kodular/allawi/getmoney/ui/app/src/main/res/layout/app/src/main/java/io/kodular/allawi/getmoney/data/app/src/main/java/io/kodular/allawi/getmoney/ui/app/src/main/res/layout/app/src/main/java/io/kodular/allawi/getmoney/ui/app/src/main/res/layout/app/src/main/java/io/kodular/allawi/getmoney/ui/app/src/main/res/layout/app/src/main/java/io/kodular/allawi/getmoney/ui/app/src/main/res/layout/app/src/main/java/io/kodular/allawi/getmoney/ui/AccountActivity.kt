package io.kodular.allawi.getmoney.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kodular.allawi.getmoney.databinding.ActivityAccountBinding

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadData()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadData() {
        val user = auth.currentUser ?: return
        binding.tvEmail.text = user.email ?: ""

        db.collection("users").document(user.uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    binding.tvName.text = doc.getString("name") ?: ""
                    binding.tvPoints.text = (doc.getLong("points") ?: 0L).toString()
                    binding.tvCode.text = doc.getString("inviteCode") ?: ""
                }
            }
    }
}
