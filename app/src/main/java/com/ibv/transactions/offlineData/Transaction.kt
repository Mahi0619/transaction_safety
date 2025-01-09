package com.ibv.transactions.offlineData
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: Int,
    val date: String,
    val amount: Int,
    val category: String,
    val description: String
)

