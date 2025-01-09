package com.ibv.transactions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ibv.transactions.data.repository.MainRepository
import com.ibv.transactions.data.util.ApiState
import com.ibv.transactions.userAuth.loginBean.LoginBean
import com.ibv.transactions.userAuth.userVM.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

@ExperimentalCoroutinesApi
class UserViewModelTest
{

    // Run tests synchronously
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var mockRepository: MainRepository

    private lateinit var userViewModel: UserViewModel

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Initialize the ViewModel and use the test dispatcher
        userViewModel = UserViewModel(mockRepository)
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `test login success`() = runTest {
        // Given: A mocked successful response
        val mockLoginBean = LoginBean("Success", true, "mock_token")
        `when`(mockRepository.login(any())).thenReturn(flowOf(ApiState.Success(mockLoginBean)))

        // When: We call the login function with mock data
        userViewModel.login("username", "password")

        // Then: Check if the API state is success and token is saved
        userViewModel.apiStateFlow.collect {
            when (it) {
                is ApiState.Success -> {
                    assertEquals("mock_token", it.data.token)
                }
                else -> fail("Expected success state")
            }
        }
    }

    @Test
    fun `test login failure`() = runTest {
        // Given: A mocked failure response
        val errorMessage = "Login failed"
        `when`(mockRepository.login(any())).thenReturn(flowOf(ApiState.Failure(Exception(errorMessage))))

        // When: We call the login function with mock data
        userViewModel.login("username", "password")

        // Then: Check if the API state is failure
        userViewModel.apiStateFlow.collect {
            when (it) {
                is ApiState.Failure -> {
                    assertEquals(errorMessage, it.error.message)
                }
                else -> fail("Expected failure state")
            }
        }
    }

    @Test
    fun `test loading state during login`() = runTest {
        // Given: A loading state before any API call
        `when`(mockRepository.login(any())).thenReturn(flowOf(ApiState.Loading))

        // When: We call the login function with mock data
        userViewModel.login("username", "password")

        // Then: Check if the API state is loading
        userViewModel.apiStateFlow.collect {
            when (it) {
                is ApiState.Loading -> {
                    // Assert that we are in loading state
                    assertEquals(ApiState.Loading, it)
                }
                else -> fail("Expected loading state")
            }
        }
    }

    @Test
    fun `test empty response`() = runTest {
        // Given: A mocked empty response from the repository
        `when`(mockRepository.login(any())).thenReturn(flowOf(ApiState.Empty))

        // When: We call the login function with mock data
        userViewModel.login("username", "password")

        // Then: Check if the API state is empty
        userViewModel.apiStateFlow.collect {
            assertEquals(ApiState.Empty, it)
        }
    }
}
