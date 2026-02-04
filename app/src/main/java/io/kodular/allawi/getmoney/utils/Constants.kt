package io.kodular.allawi.getmoney.utils

object Constants {

    // Firestore Collections
    const val COL_USERS = "users"
    const val COL_WITHDRAW_REQUESTS = "withdraw_requests"

    // User Fields
    const val FIELD_UID = "uid"
    const val FIELD_NAME = "name"
    const val FIELD_EMAIL = "email"
    const val FIELD_POINTS = "points"
    const val FIELD_INVITE_CODE = "inviteCode"
    const val FIELD_INVITED_BY = "invitedBy"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_UPDATED_AT = "updatedAt"

    // Withdraw Fields
    const val FIELD_AMOUNT_POINTS = "amountPoints"
    const val FIELD_AMOUNT_USD = "amountUsd"
    const val FIELD_METHOD = "method"
    const val FIELD_ACCOUNT = "account"
    const val FIELD_STATUS = "status"

    // Points
    const val POINTS_PER_INVITE = 100L
    const val POINTS_PER_AD = 25L
    const val POINTS_DAILY = 25L

    // Convert
    const val POINTS_PER_USD = 1000L
}
