package com.ibv.transactions.dashBoard.fragment.model
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TransactionBean(
    @SerializedName("id")
    @Expose
    val id: Int,

    @SerializedName("date")
    @Expose
    val date: String,

    @SerializedName("amount")
    @Expose
    val amount: Int,

    @SerializedName("category")
    @Expose
    val category: String,

    @SerializedName("description")
    @Expose
    val description: String
)
