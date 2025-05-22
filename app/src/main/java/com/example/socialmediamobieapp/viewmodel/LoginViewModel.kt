package com.example.socialmediamobieapp.viewmodel
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialmediamobieapp.model.UiState
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.network.dto.request.LoginRequest
import com.example.socialmediamobieapp.network.dto.response.LoginApiResponse
import com.example.socialmediamobieapp.network.dto.response.LoginResponse
import com.example.socialmediamobieapp.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class LoginViewModel(private val tokenManager: TokenManager) : ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible

    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginState: StateFlow<UiState<String>> = _loginState

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    /** Cập nhật giá trị email */
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    /** Cập nhật giá trị password */
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    /** Hàm login - gọi API đăng nhập */
    fun login(onLoginSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val response: LoginApiResponse = RetrofitInstance.api.login(
                    LoginRequest(email.value, password.value)
                )
                when (response.code) {
                    1000 -> {
                        val token = response.result?.token
                        if (token != null) {
                            // Lưu token vào TokenManager
                            tokenManager.saveAccessToken(token)
                            // Cập nhật trạng thái thành công
                            _loginState.value = UiState.Success(token)
                            // Gọi callback để chuyển sang HomeScreen
                            onLoginSuccess(token)
                        } else {
                            _loginState.value = UiState.Error("Token is null")
                        }
                    }
                    else -> {
                        _loginState.value = UiState.Error(response.message ?: "Login failed")
                    }
                }
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Error during login")
            }
        }
    }
}