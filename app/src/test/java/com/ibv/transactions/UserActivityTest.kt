package com.ibv.transactions

import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import com.ibv.transactions.base.Toaster
import com.ibv.transactions.dashBoard.DashboardActivity
import com.ibv.transactions.data.util.ApiState
import com.ibv.transactions.userAuth.UserActivity
import com.ibv.transactions.userAuth.userVM.UserViewModel
import com.ibv.transactions.userAuth.loginBean.LoginBean
import com.ibv.transactions.base.SharedPref
import com.ibv.transactions.others.BiometricPromptUtils
import com.ibv.transactions.others.Loader
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@HiltAndroidTest
@RunWith(MockitoJUnitRunner::class)
class UserActivityTest {

    private lateinit var activity: UserActivity

    @Mock
    private lateinit var mockLoader: Loader

    @Mock
    private lateinit var mockToaster: Toaster

    @Mock
    private lateinit var viewModel: UserViewModel

    @Before
    fun setup() {
        // Setup the necessary components for the activity
        activity = mock(UserActivity::class.java)
        mockLoader = mock(Loader::class.java)
        mockToaster = mock(Toaster::class.java)
        viewModel = mock(UserViewModel::class.java)

        // Binding viewModel to the activity
        activity.mainViewModel = viewModel

        // Mock necessary dependencies like SharedPref, Toaster, etc.
        SharedPref.getInstance().saveToken("mock_token")

        // Set up the activity (mock) scenario using Robolectric or other
        Robolectric.buildActivity(UserActivity::class.java).create().start().resume()
    }

    @Test
    fun testLoginButtonClick() {
        // Simulate button click for login
        activity.binding.etLoginId.setText("testuser")
        activity.binding.etLoginPassword.setText("password123")

        // Perform the login button click action
        activity.binding.btnLogin.performClick()

        // Verify if the viewModel's login method was called with correct parameters
        verify(viewModel).login("testuser", "password123")
    }

    @Test
    fun testApiStateSuccess() {
        // Simulate the API success response (mocked)
        val mockResponse = ApiState.Success(LoginBean(
            message = "message",
            success = true,
            token = "mock_token"
        ))

        // Mock the MutableStateFlow to return success state
        val mutableStateFlow = MutableStateFlow<ApiState<LoginBean>>(ApiState.Empty)
        `when`(viewModel.apiStateFlow).thenReturn(mutableStateFlow)

        // Simulate setting the success response
        mutableStateFlow.value = mockResponse

        // Observe the API state
        activity.observeApiState()

        // Verify loader dismiss and token save to SharedPref
        verify(mockLoader).dismiss()
        verify(SharedPref.getInstance()).saveToken("mock_token")

        // Verify if the DashboardActivity is started
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        verify(activity).startActivity(intentCaptor.capture())
        assertEquals(DashboardActivity::class.java.name, intentCaptor.value.component?.className)
    }


    @Test
    fun testApiStateFailure() {
        // Simulate failure response from API
        val errorResponse = ApiState.Failure(Throwable("Login Error"))

        // Mock the MutableStateFlow to return failure state
        val mutableStateFlow = MutableStateFlow<ApiState<LoginBean>>(ApiState.Empty)
        `when`(viewModel.apiStateFlow).thenReturn(mutableStateFlow)

        // Simulate setting the error state
        mutableStateFlow.value = errorResponse

        // Observe the API state
        activity.observeApiState()

        // Verify that the error dialog is shown and loader is dismissed
        verify(mockLoader).dismiss()
        verify(mockToaster).somethingWentWrong()
    }


    @Test
    fun testCheckValidation_emptyUsername() {
        // Simulate empty username input
        activity.binding.etLoginId.setText("")
        activity.binding.etLoginPassword.setText("password123")

        // Run validation and assert that it fails
        val isValid = activity.checkValidation()
        assertFalse(isValid)
        assertNotNull(activity.binding.etLoginId.error)
    }

    @Test
    fun testCheckValidation_invalidPassword() {
        // Simulate invalid password input
        activity.binding.etLoginId.setText("testuser")
        activity.binding.etLoginPassword.setText("")

        // Run validation and assert that it fails
        val isValid = activity.checkValidation()
        assertFalse(isValid)
        assertNotNull(activity.binding.etLoginPassword.error)
    }

    @Test
    fun testBiometricAuthentication() {
        // Simulate biometric authentication being successful
        `when`(BiometricPromptUtils.canAuthenticateWithBiometrics(activity)).thenReturn(true)

        // Simulate clicking the face recognition view
        activity.binding.rlFace.performClick()

        // Verify that token retrieval and activity transition occur
        verify(SharedPref.getInstance()).getToken()
        verify(activity).startActivity(any(Intent::class.java))
    }

    @Test
    fun testUiElementsVisibility_BiometricNotSupported() {
        // Simulate case when biometric authentication is not supported
        `when`(BiometricPromptUtils.canAuthenticateWithBiometrics(activity)).thenReturn(false)

        // Check UI visibility for both login and face recognition views
        assertTrue(activity.binding.rlLoginCred.isVisible)
        assertFalse(activity.binding.rlFace.isVisible)
        assertEquals(View.GONE, activity.binding.tvCred.visibility)
    }

    @Test
    fun testUiElementsVisibility_BiometricSupportedWithoutToken() {
        // Simulate case when biometric is supported but no token found
        `when`(BiometricPromptUtils.canAuthenticateWithBiometrics(activity)).thenReturn(true)
        `when`(SharedPref.getInstance().token).thenReturn(null)

        // Check visibility of UI elements
        assertTrue(activity.binding.rlLoginCred.isVisible)
        assertFalse(activity.binding.rlFace.isVisible)
    }

    @Test
    fun testUiElementsVisibility_BiometricSupportedWithToken() {
        // Simulate case when biometric is supported and token exists
        `when`(BiometricPromptUtils.canAuthenticateWithBiometrics(activity)).thenReturn(true)
        `when`(SharedPref.getInstance().token).thenReturn("mock_token")

        // Check UI visibility for login and biometric views
        assertFalse(activity.binding.rlLoginCred.isVisible)
        assertTrue(activity.binding.rlFace.isVisible)
    }
}
