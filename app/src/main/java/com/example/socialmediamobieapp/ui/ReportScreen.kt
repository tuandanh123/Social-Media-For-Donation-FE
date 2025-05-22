package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialmediamobieapp.model.dto.request.ReportRequest
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReportScreen(
    postId: String,
    tokenManager: TokenManager,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var isReported by remember { mutableStateOf(false) } // Trạng thái báo cáo thành công
    var errorMessage by remember { mutableStateOf<String?>(null) } // Thông báo lỗi
    val profileId = tokenManager.getProfileId() ?: ""

    // Danh sách các lý do báo cáo
    val reportReasons = listOf(
        "Chỉ là tui không thích nội dung này",
        "Bật nạt hoặc liên hệ theo cách không mong muốn",
        "Tự tử, tự gây thương tích hoặc chứng rối loạn ăn uống",
        "Bạo lực, thù ghét hoặc bóc lột",
        "Bạn học quan cả mạt hàng bị hạn chế",
        "Ẩn khóa thân học hoạt động tình dục",
        "Lừa đảo, gian lận hoặc spam",
        "Thông tin sai sự thật",
        "Quyền sở hữu trí tuệ"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        if (isReported) {
            // Trạng thái báo cáo thành công
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Báo cáo thành công",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Trở về trang chủ", fontSize = 16.sp)
                }
            }
        } else {
            // Giao diện chọn lý do báo cáo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tại sao bạn báo cáo bài viết này?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Báo cáo của bạn sẽ được ẩn danh. Nếu ai đó đang gặp nguy hiểm, đừng chần chừ mà hãy báo ngay cho dịch vụ khẩn cấp tại địa phương.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(reportReasons) { reason ->
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val reportRequest = ReportRequest(
                                        profileId = profileId,
                                        postId = postId,
                                        message = reason
                                    )
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitInstance.api.reportPort(reportRequest)
                                        }
                                        if (response.code == 1000) {
                                            isReported = true
                                        } else {
                                            errorMessage = response.message ?: "Không thể báo cáo bài viết"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Lỗi: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black
                            ),
                            elevation = ButtonDefaults.elevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = reason,
                                    fontSize = 16.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                        Divider()
                    }
                }
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Đã xảy ra lỗi",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}