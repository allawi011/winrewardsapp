package io.kodular.allawi.getmoney.data

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val points: Long = 0L,
    val inviteCode: String = "",
    val invitedBy: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
