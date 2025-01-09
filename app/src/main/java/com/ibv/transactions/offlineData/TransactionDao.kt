package com.ibv.transactions.offlineData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactions: List<Transaction>)

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<Transaction>>
}

