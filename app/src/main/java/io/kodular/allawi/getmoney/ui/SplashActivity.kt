
package io.kodular.allawi.getmoney.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // مسجل دخول
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // مو مسجل
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
