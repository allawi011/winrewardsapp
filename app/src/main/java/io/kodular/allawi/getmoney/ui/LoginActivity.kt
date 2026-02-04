
package io.kodular.allawi.getmoney.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.kodular.allawi.getmoney.R
import io.kodular.allawi.getmoney.data.FirestoreRepository
import io.kodular.allawi.getmoney.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { FirestoreRepository() }

    private lateinit var googleClient: GoogleSignInClient

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken == null) {
                    showLoading(false)
                    Toast.makeText(this, "Google Token error", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                firebaseAuthWithGoogle(idToken)

            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // اذا المستخدم اصلاً مسجل
        if (auth.currentUser != null) {
            goSplash()
            return
        }

        setupGoogle()

        binding.btnLogin.setOnClickListener { loginEmail() }
        binding.btnRegister.setOnClickListener { registerEmail() }
        binding.btnGoogle.setOnClickListener { signInGoogle() }
    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInGoogle() {
        showLoading(true)
        googleLauncher.launch(googleClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val user = auth.currentUser ?: return@addOnSuccessListener

                val name = user.displayName ?: "User"
                val email = user.email ?: ""

                repo.createUserIfNotExists(user.uid, name, email) { ok, _ ->
                    runOnUiThread {
                        showLoading(false)
                        if (ok) {
                            // اذا المستخدم كتب كود دعوة (اختياري)
                            val invite = binding.etInviteCode.text.toString().trim()
                            if (invite.isNotEmpty()) {
                                repo.applyInviteCode(user.uid, invite) { _, _ ->
                                    goSplash()
                                }
                            } else {
                                goSplash()
                            }
                        } else {
                            Toast.makeText(this, "Firestore error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loginEmail() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "اكتب ايميل وباسورد", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                showLoading(false)
                goSplash()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "فشل تسجيل الدخول", Toast.LENGTH_SHORT).show()
            }
    }

    private fun registerEmail() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "اكتب ايميل وباسورد", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                val user = auth.currentUser ?: return@addOnSuccessListener

                val name = email.substringBefore("@")
                repo.createUserIfNotExists(user.uid, name, email) { ok, _ ->
                    runOnUiThread {
                        showLoading(false)
                        if (ok) {
                            // اذا المستخدم كتب كود دعوة
                            val invite = binding.etInviteCode.text.toString().trim()
                            if (invite.isNotEmpty()) {
                                repo.applyInviteCode(user.uid, invite) { _, _ ->
                                    goSplash()
                                }
                            } else {
                                goSplash()
                            }
                        } else {
                            Toast.makeText(this, "Firestore error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "فشل إنشاء الحساب", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goSplash() {
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progress.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.btnRegister.isEnabled = !show
        binding.btnGoogle.isEnabled = !show
    }
}
