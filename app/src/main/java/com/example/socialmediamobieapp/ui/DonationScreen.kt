package com.example.socialmediamobieapp.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialmediamobieapp.R
import com.example.socialmediamobieapp.model.dto.request.DonationCreationRequest
import com.example.socialmediamobieapp.utils.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun DonationScreen(
    postId: String,
    tokenManager: TokenManager,
    navController: NavController,
    onDonate: (DonationCreationRequest, (Result<String>) -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("Momo") }
    var errorMessage by remember { mutableStateOf<String?>(null) } // Khai báo errorMessage
    val context = LocalContext.current // Khai báo context
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quyên góp", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trường nhập số tiền
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Số tiền (VNĐ)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Trường nhập tin nhắn
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Tin nhắn") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Checkbox ẩn danh
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAnonymous,
                    onCheckedChange = { isAnonymous = it }
                )
                Text(
                    text = "Quyên góp ẩn danh",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Bảng lựa chọn phương thức thanh toán
            Text(
                text = "Phương thức thanh toán",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Momo
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { paymentMethod = "Momo" },
                    backgroundColor = if (paymentMethod == "Momo") Color(0xFFE0F7FA) else Color.White,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.momo_icon), // Thêm hình ảnh Momo vào res/drawable
                            contentDescription = "Momo",
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Momo", fontSize = 14.sp)
                    }
                }

                // Ngân hàng
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { paymentMethod = "Ngân hàng" },
                    backgroundColor = if (paymentMethod == "Ngân hàng") Color(0xFFE0F7FA) else Color.White,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bank_icon), // Thêm hình ảnh Ngân hàng vào res/drawable
                            contentDescription = "Ngân hàng",
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Ngân hàng", fontSize = 14.sp)
                    }
                }
            }

            // Nút xác nhận quyên góp
            Button(
                onClick = {
                    val amountValue = amount.toBigDecimalOrNull()
                    if (amountValue == null || amountValue <= BigDecimal.ZERO) {
                        // Hiển thị lỗi nếu số tiền không hợp lệ
                        return@Button
                    }

                    val profileId = tokenManager.getProfileId()
                    if (profileId == null) {
                        // Hiển thị lỗi nếu không lấy được profileId
                        return@Button
                    }

                    val request = DonationCreationRequest(
                        postId = postId,
                        donorId = profileId,
                        amount = amountValue,
                        message = message,
                        isAnonymous = isAnonymous,
                        paymentMethod = paymentMethod
                    )

                    coroutineScope.launch {
                        onDonate(request) { result ->
                            result.fold(
                                onSuccess = { payUrl ->
                                    // Mở payUrl trong trình duyệt
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        errorMessage = "Không thể mở trang thanh toán: ${e.message}"
                                        return@fold
                                    }

                                    // Hiển thị thông báo thành công sau 1 giây
                                    coroutineScope.launch {
                                        delay(1_000L) // Delay 1 giây để đảm bảo trình duyệt mở
                                        showSuccessDialog = true
                                    }
                                },
                                onFailure = { error ->
                                    errorMessage = error.message ?: "Lỗi khi tạo thanh toán"
                                    if (error.message?.contains("Phiên đăng nhập hết hạn") == true) {
                                        tokenManager.clear()
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = amount.isNotBlank()
            ) {
                Text("Xác nhận quyên góp", fontSize = 16.sp)
            }
        }
    }

    // Hiển thị AlertDialog khi quyên góp thành công
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Quyên góp thành công!") },
            text = { Text("Cảm ơn bạn đã quyên góp. Nhấn Home để quay về trang chính.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("Home")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}