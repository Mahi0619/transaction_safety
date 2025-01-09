package com.ibv.transactions.userAuth.loginBean


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginBean(
    @SerializedName("message")
    @Expose
    val message: String,
    @SerializedName("status") // Should match the "success" field in the JSON
    @Expose
    val success: Boolean, // Changed the name to "success" for clarity
    @SerializedName("token")
    @Expose
    val token: String // Fixed typo from "toke" to "token"
)

