package com.ibv.transactions.userAuth.userVM

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibv.transactions.data.repository.MainRepository
import com.ibv.transactions.data.util.ApiState
import com.ibv.transactions.userAuth.loginBean.LoginRequest
import com.ibv.transactions.userAuth.loginBean.LoginBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {

    private val _apiStateFlow: MutableStateFlow<ApiState<LoginBean>> =
        MutableStateFlow(ApiState.Empty)
    val apiStateFlow: StateFlow<ApiState<LoginBean>> = _apiStateFlow
    var loginId = ObservableField<String>()
    var password = ObservableField<String>()
    val isValidLoginId = ObservableField<String>()
    val isValidPassword = ObservableField<String>()

    fun login(
        username: String,
        password: String
    ) {

        val request = LoginRequest(username, password)

        viewModelScope.launch {
            viewModelScope.launch {
                mainRepository.login(request)
                    .catch { e ->
                        e.printStackTrace()
                        _apiStateFlow.value = ApiState.Failure(e)
                        Log.e("API_State_Error", "Error: ${e.message}")
                    }
                    .collect { state ->
                        when (state) {
                            is ApiState.Success<LoginBean> -> {
                                _apiStateFlow.value = state

                            }
                            is ApiState.Failure -> {
                                _apiStateFlow.value = state
                                Log.e("API_State_Error", "Error: ${state.error.message}")

                            }
                            else -> {
                                _apiStateFlow.value = state
                                Log.d("API_State", "Loading...")
                            }
                        }
                    }
            }
        }
    }
}

