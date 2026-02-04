package io.kodular.allawi.getmoney.data

data class WithdrawRequest(
    val id: String = "",
    val uid: String = "",
    val method: String = "",
    val account: String = "",
    val amountPoints: Long = 0L,
    val amountUsd: Double = 0.0,
    val status: String = "pending",
    val createdAt: Long = 0L
)
