package io.kodular.allawi.getmoney.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PointsRepo {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun addPoints(amount: Long, onDone: (() -> Unit)? = null) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("points", FieldValue.increment(amount))
            .addOnSuccessListener { onDone?.invoke() }
    }

    fun getUserDocRef() =
        db.collection("users").document(auth.currentUser?.uid ?: "none")
}
