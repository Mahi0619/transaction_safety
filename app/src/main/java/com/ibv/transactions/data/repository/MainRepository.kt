package com.ibv.transactions.data.repository

import android.util.Log
import com.ibv.transactions.dashBoard.fragment.model.TransactionBean
import com.ibv.transactions.data.network.ApiService
import com.ibv.transactions.data.util.ApiState
import com.ibv.transactions.offlineData.Transaction
import com.ibv.transactions.offlineData.TransactionDao
import com.ibv.transactions.userAuth.loginBean.LoginRequest
import com.ibv.transactions.userAuth.loginBean.LoginBean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response

import javax.inject.Inject


class MainRepository @Inject constructor(private val apiService: ApiService, private val transactionDao: TransactionDao ) {

    suspend fun login(request: LoginRequest): Flow<ApiState<LoginBean>> = flow {
        emit(ApiState.Loading) // Emit loading state
        try {
            // Make the network request
            val response: Response<LoginBean> = apiService.login(request)

            if (response.isSuccessful) { // If response is successful (status code 200)
                response.body()?.let {
                    emit(ApiState.Success(it)) // Emit success with the response body (LoginBean)
                } ?: emit(ApiState.Failure(Exception("Empty response body")))
            } else {
                // Handle failure (e.g., 401 Unauthorized or any error)
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorJson = JSONObject(errorBody)
                    errorJson.optString(
                        "message",
                        "Unknown error"
                    ) // Extract the message from the error body
                } catch (e: Exception) {
                    "Unknown error"
                }

                Log.e("Repository", "Error: $errorMessage")  // Log the error message
                emit(ApiState.Failure(Exception(errorMessage))) // Emit failure with the message extracted from the error
            }
        } catch (e: Exception) {
            // Handle other types of exceptions, like network issues
            Log.e("Repository", "Error: ${e.message}")
            emit(ApiState.Failure(e))
        }
    }


/*
    suspend fun getTransaction(): Flow<ApiState<List<TransactionBean>>> = flow {
        emit(ApiState.Loading) // Emit loading state

        try {
            // Make the network request
            val response: Response<List<TransactionBean>> = apiService.getTransactionsList()

            if (response.isSuccessful) { // If response is successful (status code 200)
                response.body()?.let {
                    emit(ApiState.Success(it)) // Emit success with the response body (List of Transactions)
                } ?: emit(ApiState.Failure(Exception("Empty response body")))
            } else {
                // Handle failure (e.g., 401 Unauthorized or any error)
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorJson = JSONObject(errorBody)
                    errorJson.optString(
                        "message",
                        "Unknown error"
                    ) // Extract the message from the error body
                } catch (e: Exception) {
                    "Unknown error"
                }

                Log.e("Repository", "Error: $errorMessage")  // Log the error message
                emit(ApiState.Failure(Exception(errorMessage))) // Emit failure with the message extracted from the error
            }
        } catch (e: Exception) {
            // Handle other types of exceptions, like network issues
            Log.e("Repository", "Error: ${e.message}")
            emit(ApiState.Failure(e))
        }
    }
*/






    suspend fun getTransaction(): Flow<ApiState<List<TransactionBean>>> = flow {
        emit(ApiState.Loading) // Emit loading state
        try {
            // Make the network request
            val response: Response<List<TransactionBean>> = apiService.getTransactionsList()

            if (response.isSuccessful) { // If response is successful (status code 200)
                response.body()?.let { transactionList ->
                    // Emit success with the response body (List of Transactions)
                    emit(ApiState.Success(transactionList))

                    // Save transactions to the Room database
                    val transactionEntities = transactionList.map {
                        Transaction(
                            id = it.id,
                            date = it.date,
                            amount = it.amount,
                            category = it.category,
                            description = it.description
                        )
                    }
                    transactionDao.insertTransaction(transactionEntities) // Insert into Room
                } ?: emit(ApiState.Failure(Exception("Empty response body")))
            } else {
                // Handle failure (e.g., 401 Unauthorized or any error)
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorJson = JSONObject(errorBody)
                    errorJson.optString("message", "Unknown error")
                } catch (e: Exception) {
                    "Unknown error"
                }

                Log.e("Repository", "Error: $errorMessage")  // Log the error message
                emit(ApiState.Failure(Exception(errorMessage))) // Emit failure with the message extracted from the error
            }
        } catch (e: Exception) {
            // Handle other types of exceptions, like network issues
            Log.e("Repository", "Error: ${e.message}")
            emit(ApiState.Failure(e))
        }
    }

    // Method to fetch transactions from the Room database
    fun getTransactionsFromRoom(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }


}
