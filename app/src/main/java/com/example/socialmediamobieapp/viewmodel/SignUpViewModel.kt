package com.example.socialmediamobieapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.network.dto.request.SignUpRequest
import com.example.socialmediamobieapp.network.dto.response.SignUpApiResponse
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SignUpViewModel: ViewModel() {
    var firstname = mutableStateOf("")
    var lastname  = mutableStateOf("")
    var dob       = mutableStateOf<LocalDate?>(null) // ✅ LocalDate   // "yyyy-MM-dd"
    var email     = mutableStateOf("")
    var username  = mutableStateOf("")
    var password  = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var errorMsg  = mutableStateOf<String?>(null)
    var success   = mutableStateOf<Boolean?>(null)
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSignUpClick() {
        errorMsg.value = null
        isLoading.value = true
        viewModelScope.launch {
            try {
                val formattedDob = dob.value?.format(formatter) ?: ""

                val req = SignUpRequest(
                    username = username.value,
                    password = password.value,
                    firstname = firstname.value,
                    lastname = lastname.value,
                    dob = formattedDob,
                    email = email.value
                )
                val resp: SignUpApiResponse = RetrofitInstance.api.signUp(req)
                if (resp.code == 1000) {
                    success.value = true
                } else {
                    errorMsg.value = resp.message
                    success.value = false
                }
            } catch (e: Exception) {
                errorMsg.value = e.localizedMessage
                success.value = false
            } finally {
                isLoading.value = false
            }
        }
    }
}