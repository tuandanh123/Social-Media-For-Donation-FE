package com.example.socialmediamobieapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialmediamobieapp.R
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import com.example.socialmediamobieapp.viewmodel.SignUpViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    vm: SignUpViewModel = viewModel()
) {
    val firstname by vm.firstname
    val lastname by vm.lastname
    val dob by vm.dob
    val email by vm.email
    val username by vm.username
    val password by vm.password
    val isLoading by vm.isLoading
    val errorMsg by vm.errorMsg
    val success by vm.success

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dateDialogState = rememberMaterialDialogState()

    LaunchedEffect(success) {
        if (success == true) onSignUpSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF833ab4),
                        Color(0xFFfd1d1d),
                        Color(0xFFfcb045)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )

            Text("Sign Up", style = MaterialTheme.typography.h5, color = Color.White)

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = firstname,
                    onValueChange = vm.firstname::value::set,
                    label = { Text("First name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = lastname,
                    onValueChange = vm.lastname::value::set,
                    label = { Text("Last name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = dob?.format(formatter) ?: "",
                onValueChange = {},
                label = { Text("Date of Birth") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { dateDialogState.show() },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = vm.email::value::set,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = vm.username::value::set,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = vm.password::value::set,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = vm::onSignUpClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3897f0))
            ) {
                Text(if (isLoading) "Signing up..." else "Register", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Already have an account? Log In",
                color = Color.White,
                modifier = Modifier
                    .clickable { onLoginClick() }
                    .padding(8.dp)
            )
        }
    }

    // Date Picker Dialog
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton("OK")
            negativeButton("Cancel")
        },
        backgroundColor = Color(0xFF2C2C2C)
    ) {
        datepicker(
            initialDate = dob ?: LocalDate.now(),
            title = "Select your date of birth"
        ) {
            vm.dob.value = it
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewSignUpScreen() {
    SocialMediaMobieAppTheme {
        SignUpScreen(onSignUpSuccess = {}, onLoginClick = {})
    }
}
