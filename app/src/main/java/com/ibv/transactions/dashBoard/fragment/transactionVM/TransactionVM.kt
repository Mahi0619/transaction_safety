package com.ibv.transactions.dashBoard.fragment.transactionVM

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibv.transactions.dashBoard.fragment.model.TransactionBean
import com.ibv.transactions.data.repository.MainRepository
import com.ibv.transactions.data.util.ApiState
import com.ibv.transactions.offlineData.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class TransactionVM @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _apiStateFlow: MutableStateFlow<ApiState<List<TransactionBean>>> =
        MutableStateFlow(ApiState.Empty)
    val apiStateFlow: StateFlow<ApiState<List<TransactionBean>>> = _apiStateFlow

    private val _roomStateFlow: MutableStateFlow<List<Transaction>> =
        MutableStateFlow(emptyList())
    val roomStateFlow: StateFlow<List<Transaction>> = _roomStateFlow

    // Fetch transactions from network and save them in Room
    fun getTransaction() {
        viewModelScope.launch {
            mainRepository.getTransaction()
                .collect { state ->
                    when (state) {
                        is ApiState.Success<List<TransactionBean>> -> {
                            _apiStateFlow.value = state
                        }
                        is ApiState.Failure -> {
                            handleFailure(state.error)
                        }
                        else -> {
                            _apiStateFlow.value = ApiState.Loading
                            Log.d("API_State", "Loading...")
                        }
                    }
                }
        }
    }

    // Fetch transactions from Room database
    fun getTransactionsFromRoom() {
        viewModelScope.launch {
            mainRepository.getTransactionsFromRoom()
                .collect { transactions ->
                    _roomStateFlow.value = transactions
                }
        }
    }

    private fun handleFailure(error: Throwable) {
        // Simplified error handling and provide more informative message
        when (val httpException = error as? HttpException) {
            null -> {
                // Non-HTTP errors
                Log.e("API_State_Error", "Unknown Error: ${error.message}")
                _apiStateFlow.value = ApiState.Failure(Exception("Unknown error occurred"))
            }
            else -> {
                // HTTP errors
                val errorMessage = when (httpException.code()) {
                    400 -> "Bad Request: Invalid parameters or request body"
                    401 -> "Unauthorized: Invalid username or password"
                    403 -> "Forbidden: You don't have permission to access this resource"
                    500 -> "Internal Server Error: Something went wrong on the server"
                    else -> "HTTP Error ${httpException.code()}: ${httpException.message()}"
                }

                Log.e("API_State_Error_${httpException.code()}", "Error: $errorMessage")
                _apiStateFlow.value = ApiState.Failure(Exception(errorMessage))
            }
        }
    }
}
