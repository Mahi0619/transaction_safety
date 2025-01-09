package com.ibv.transactions.userAuth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ibv.transactions.base.ErrorDialogHelper
import com.ibv.transactions.base.SharedPref
import com.ibv.transactions.base.Toaster
import com.ibv.transactions.dashBoard.DashboardActivity
import com.ibv.transactions.data.util.ApiState
import com.ibv.transactions.databinding.ActivityUserBinding
import com.ibv.transactions.others.*
import com.ibv.transactions.userAuth.loginBean.LoginBean
import com.ibv.transactions.userAuth.userVM.UserViewModel

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class UserActivity : AppCompatActivity() {
    lateinit var loader: Loader
    lateinit var binding: ActivityUserBinding
    val mainViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loader = Loader(this)

        val isNightModeOn = SharedPref.getInstance().nightMode // Retrieve night mode state
        val isFirstStart = SharedPref.getInstance().isFirstStart // Check if it's the first start


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isFirstStart ){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else{
            when {
                isNightModeOn -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            }
        }


        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding.btnLogin.setOnClickListener {
            if (checkValidation()) {
                loader.show()
                mainViewModel.login(
                    binding.etLoginId.text.toString().trim(),
                    binding.etLoginPassword.text.toString().trim()
                )
            }
        }

        binding.tvCred.setOnClickListener {
            binding.rlLoginCred.isVisible = true
            binding.rlFace.isVisible = false
            binding.tvCred.visibility=View.GONE
        }

        observeApiState()


        val biometricSupported = BiometricPromptUtils.canAuthenticateWithBiometrics(this)

        when {
            // Case 1: Biometric not supported
            !biometricSupported -> {
                binding.rlLoginCred.isVisible = true
                binding.rlFace.isVisible = false
                binding.tvCred.visibility = View.GONE
            }

            // Case 2: Biometric supported and no token found
            biometricSupported && SharedPref.getInstance().token.isNullOrEmpty() -> {
                binding.rlLoginCred.isVisible = true
                binding.rlFace.isVisible = false
            }

            // Default case for when biometrics are supported, and token exists
            else -> {
                binding.rlLoginCred.isVisible = false
                binding.rlFace.isVisible = true
            }
        }

// Handle biometric authentication
        binding.rlFace.setOnClickListener {
            BiometricPromptUtils.authenticate(this) { authResult ->
                try {
                    val token = SharedPref.getInstance().token
                    if (!token.isNullOrEmpty()) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Log.e("BiometricAuth", "Token is empty or null after authentication")
                    }
                } catch (e: Exception) {
                    Log.e("BiometricAuth", "Error during biometric authentication", e)
                }
            }
        }


    }

    fun observeApiState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.apiStateFlow.collect { state ->
                    when (state) {
                        is ApiState.Loading -> showLoadingState()
                        is ApiState.Success<LoginBean> -> showSuccessState(state)
                        is ApiState.Failure -> showErrorState(state.error)
                        is ApiState.Empty -> hideLoadingState()

                    }
                }
            }
        }
    }


    private fun showSuccessState(response: ApiState.Success<LoginBean>) {
        loader.dismiss()
        val loginBean = response.data

        if (loginBean.token.isNotEmpty()){
            SharedPref.getInstance().saveToken(loginBean.token);
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }else{
            Toaster.somethingWentWrong()
        }
    }

    private fun showLoadingState() {
        loader.show()
    }

    private fun showErrorState(error: Throwable) {

        ErrorDialogHelper().ErrorMessage(this,error.message.toString(),"Login Error")
        loader.dismiss()
    }

    private fun hideLoadingState() {
        loader.dismiss()
    }

    fun checkValidation(): Boolean {
        var valid = true

        // Check if Username is empty
        if (MyUtils.isEmptyString(binding.etLoginId.text.toString().trim())) {
            binding.etLoginId.error = "Please enter Username"
            binding.etLoginId.requestFocus()  // Set focus to the Username field
            valid = false
        }

        // Check if Password is empty or invalid
        if (MyUtils.isEmptyString(binding.etLoginPassword.text.toString().trim()) || !MyUtils.isValidPassword(binding.etLoginPassword.text.toString().trim())) {
            binding.etLoginPassword.error = "Please enter a valid password"
            binding.etLoginPassword.requestFocus()  // Set focus to the Password field
            valid = false
        }

        return valid
    }





}
