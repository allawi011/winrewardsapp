package io.kodular.allawi.getmoney.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.kodular.allawi.getmoney.utils.Constants
import kotlin.random.Random

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getUserDoc(uid: String) =
        db.collection(Constants.COL_USERS).document(uid)

    fun createUserIfNotExists(
        uid: String,
        name: String,
        email: String,
        onDone: (Boolean, String) -> Unit
    ) {
        val ref = getUserDoc(uid)
        ref.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onDone(true, "exists")
                } else {
                    val inviteCode = generateInviteCode()
                    val now = System.currentTimeMillis()
                    val user = User(
                        uid = uid,
                        name = name,
                        email = email,
                        points = 0L,
                        inviteCode = inviteCode,
                        invitedBy = "",
                        createdAt = now,
                        updatedAt = now
                    )
                    ref.set(user)
                        .addOnSuccessListener { onDone(true, "created") }
                        .addOnFailureListener { onDone(false, it.message ?: "error") }
                }
            }
            .addOnFailureListener { onDone(false, it.message ?: "error") }
    }

    fun listenUserPoints(uid: String, onChange: (Long) -> Unit) {
        getUserDoc(uid).addSnapshotListener { snap, _ ->
            val points = snap?.getLong(Constants.FIELD_POINTS) ?: 0L
            onChange(points)
        }
    }

    fun addPoints(uid: String, amount: Long, onDone: (Boolean, String) -> Unit) {
        if (amount <= 0) {
            onDone(false, "invalid amount")
            return
        }
        getUserDoc(uid).update(
            mapOf(
                Constants.FIELD_POINTS to FieldValue.increment(amount),
                Constants.FIELD_UPDATED_AT to System.currentTimeMillis()
            )
        ).addOnSuccessListener {
            onDone(true, "added")
        }.addOnFailureListener {
            onDone(false, it.message ?: "error")
        }
    }

    fun applyInviteCode(newUserUid: String, inviteCode: String, onDone: (Boolean, String) -> Unit) {
        if (inviteCode.isBlank()) {
            onDone(false, "empty")
            return
        }

        // ابحث عن صاحب كود الدعوة
        db.collection(Constants.COL_USERS)
            .whereEqualTo(Constants.FIELD_INVITE_CODE, inviteCode.trim())
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    onDone(false, "code_not_found")
                    return@addOnSuccessListener
                }

                val inviterDoc = query.documents.first()
                val inviterUid = inviterDoc.getString(Constants.FIELD_UID) ?: ""

                if (inviterUid.isBlank() || inviterUid == newUserUid) {
                    onDone(false, "invalid_code")
                    return@addOnSuccessListener
                }

                // تأكد المستخدم الجديد ما مستخدم كود قبل
                getUserDoc(newUserUid).get()
                    .addOnSuccessListener { newUserDoc ->
                        val already = newUserDoc.getString(Constants.FIELD_INVITED_BY) ?: ""
                        if (already.isNotBlank()) {
                            onDone(false, "already_used")
                            return@addOnSuccessListener
                        }

                        val batch = db.batch()

                        // ثبت invitedBy على المستخدم الجديد
                        batch.update(
                            getUserDoc(newUserUid),
                            mapOf(
                                Constants.FIELD_INVITED_BY to inviteCode.trim(),
                                Constants.FIELD_UPDATED_AT to System.currentTimeMillis()
                            )
                        )

                        // أعطي نقاط للمستخدم الجديد
                        batch.update(
                            getUserDoc(newUserUid),
                            Constants.FIELD_POINTS,
                            FieldValue.increment(Constants.POINTS_PER_INVITE)
                        )

                        // أعطي نقاط لصاحب الدعوة
                        batch.update(
                            getUserDoc(inviterUid),
                            Constants.FIELD_POINTS,
                            FieldValue.increment(Constants.POINTS_PER_INVITE)
                        )

                        batch.commit()
                            .addOnSuccessListener { onDone(true, "invite_applied") }
                            .addOnFailureListener { onDone(false, it.message ?: "error") }
                    }
                    .addOnFailureListener { onDone(false, it.message ?: "error") }
            }
            .addOnFailureListener { onDone(false, it.message ?: "error") }
    }

    fun createWithdrawRequest(
        uid: String,
        method: String,
        account: String,
        onDone: (Boolean, String) -> Unit
    ) {
        val userRef = getUserDoc(uid)
        userRef.get()
            .addOnSuccessListener { doc ->
                val points = doc.getLong(Constants.FIELD_POINTS) ?: 0L
                if (points < Constants.POINTS_PER_USD) {
                    onDone(false, "not_enough_points")
                    return@addOnSuccessListener
                }

                val now = System.currentTimeMillis()
                val request = hashMapOf(
                    Constants.FIELD_UID to uid,
                    Constants.FIELD_METHOD to method,
                    Constants.FIELD_ACCOUNT to account,
                    Constants.FIELD_AMOUNT_POINTS to Constants.POINTS_PER_USD,
                    Constants.FIELD_AMOUNT_USD to 1.0,
                    Constants.FIELD_STATUS to "pending",
                    "createdAt" to now
                )

                val batch = db.batch()

                // خصم نقاط السحب
                batch.update(
                    userRef,
                    Constants.FIELD_POINTS,
                    FieldValue.increment(-Constants.POINTS_PER_USD)
                )

                // إضافة طلب السحب
                val reqRef = db.collection(Constants.COL_WITHDRAW_REQUESTS).document()
                batch.set(reqRef, request)

                batch.commit()
                    .addOnSuccessListener { onDone(true, "withdraw_created") }
                    .addOnFailureListener { onDone(false, it.message ?: "error") }
            }
            .addOnFailureListener { onDone(false, it.message ?: "error") }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
