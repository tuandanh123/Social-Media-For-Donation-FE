package com.example.socialmediamobieapp.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.socialmediamobieapp.viewmodel.LoginViewModel
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import com.example.socialmediamobieapp.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialmediamobieapp.model.UiState
import com.example.socialmediamobieapp.utils.TokenManager


@Composable
fun LoginScreen(
    tokenManager: TokenManager,
    onLoginSuccess: (String) -> Unit,
    onSignUpClick: () -> Unit,
    vm: LoginViewModel = viewModel(factory = LoginViewModelFactory(tokenManager))
) {

    // Xóa token khi vào màn hình đăng nhập
    LaunchedEffect(Unit) {
        tokenManager.clear()
    }
    val context = LocalContext.current
    // Dùng collectAsState() để UI được cập nhật khi có thay đổi
    val email by vm.email.collectAsState()
    val password by vm.password.collectAsState()
    val isPasswordVisible by vm.isPasswordVisible.collectAsState()
    val loginState by vm.loginState.collectAsState()



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF833ab4), Color(0xFFfd1d1d), Color(0xFFfcb045))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 32.dp)
            )

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { newValue ->
                    if (email != newValue) {
                        Log.d("EmailDebug", "Changed to: $newValue")
                    }
                    vm.onEmailChange(newValue)
                },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                singleLine = true, // Đảm bảo chỉ nhập một dòng
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next // Chuyển focus sang trường tiếp theo khi nhấn "Next"
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    textColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input with visibility toggle
            OutlinedTextField(
                value = password,
                onValueChange = { newValue ->
                    println("Password input: $newValue")
                    vm.onPasswordChange(newValue)
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { vm.togglePasswordVisibility() }) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { /* Ẩn bàn phím nếu cần */ }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    textColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Forgotten password?",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.End)               // căn phải dưới ô mật khẩu
                    .clickable {  }
                    .padding(vertical = 4.dp)
            )


            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = { vm.login(onLoginSuccess) },
                enabled = loginState !is UiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3897f0))
            ) {
                Text(if (loginState is UiState.Loading) "Loading..." else "Log In", color = Color.White)
            }

            // Error Text
            if (loginState is UiState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (loginState as UiState.Error).message,
                    color = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OR Separator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.White)
                Text("  OR  ", color = Color.White)
                Divider(modifier = Modifier.weight(1f), color = Color.White)
            }

            // Facebook + Google login buttons
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Facebook button trên dòng 1
                Button(
                    onClick = { /* Handle Facebook login */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF3897f0), // Màu xanh nước biển
                        contentColor = Color.White // Màu chữ trắng
                    )
                ) {
                    // icon Facebook
                    Icon(
                        painter = painterResource(id = R.drawable.ic_facebook),
                        contentDescription = "Facebook",
                        modifier = Modifier.size(24.dp), // Giữ kích thước icon
                        tint = Color.Unspecified // Đảm bảo logo giữ màu gốc
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log in By Facebook",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google button trên dòng 2
                Button(
                    onClick = { /* Handle Google login */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF4285F4), // Màu xanh Google
                        contentColor = Color.White // Màu chữ trắng
                    )
                ) {
                    // icon Google
                    Icon(
                        painter = painterResource(id = R.drawable.ic_goggle),
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp), // Giữ kích thước icon
                        tint = Color.Unspecified // Đảm bảo logo giữ màu gốc
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log in By Google",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Don’t have an account? Sign up",
                color = Color.White,
                modifier = Modifier
                    .clickable { onSignUpClick()}   // Toàn bộ text nhấn được
                    .padding(vertical = 8.dp)         // Khoảng cách trên dưới
            )
        }
    }
}

@Preview(
    showBackground = false,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun PreviewLoginScreen() {
    // Tạo một TokenManager giả cho preview
    val mockTokenManager = object : TokenManager(null) {
        override fun saveAccessToken(token: String) {}
        override fun getAccessToken(): String? = null
    }

    // Tạo một LoginViewModel giả cho preview
    val mockViewModel = object : LoginViewModel(mockTokenManager) {
        init {

        }
    }

    SocialMediaMobieAppTheme {
        LoginScreen(
            tokenManager = mockTokenManager,
            onLoginSuccess = {},
            onSignUpClick = {},
            vm = mockViewModel
        )
    }
}
