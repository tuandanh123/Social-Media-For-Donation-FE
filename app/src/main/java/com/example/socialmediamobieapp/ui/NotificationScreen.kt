package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.socialmediamobieapp.model.Notification
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.network.dto.response.ProfileResponse
import com.example.socialmediamobieapp.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
@Composable
fun NotificationsScreen(
    tokenManager: TokenManager,
    navController: NavController
) {
    var username by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val profileId = tokenManager.getProfileId() ?: ""

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            // Lấy username từ API getProfile
            val profileResponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getProfile(profileId)
            }
            if (profileResponse.code == 1000 && profileResponse.result != null) {
                username = profileResponse.result.username ?: "Người dùng"
            } else {
                errorMessage = profileResponse.message ?: "Không thể tải thông tin người dùng"
            }

            // Lấy danh sách thông báo
            val notificationsResponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getNotifications(profileId)
            }
            if (notificationsResponse.code == 1000 && notificationsResponse.result != null) {
                notifications = notificationsResponse.result
            } else {
                errorMessage = notificationsResponse.message ?: "Không thể tải thông báo"
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(username, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay về"
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Đã xảy ra lỗi",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification = notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar bên trái
        Image(
            painter = rememberAsyncImagePainter(
                model = notification.avatarUrlOfSender,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            ),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Nội dung bên phải
        Column {
            Text(
                text = (notification.firstNameOfSender + " " + notification.lastNameOfSender)
                    ?: "Người dùng",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = notification.content?: "",
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                text = notification.createdAt?: "",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Nút "Đang theo dõi" hoặc hình ảnh (nếu có)
        if (notification.content?.contains("đã bắt đầu theo dõi bạn") == true) {
            Button(
                onClick = { /* TODO: Xử lý theo dõi lại */ },
                modifier = Modifier
                    .height(28.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Đang theo dõi", fontSize = 12.sp)
            }
        } else if (notification.content?.contains("đã đăng") == true) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = notification.avatarUrlOfSender,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                ),
                contentDescription = "Post Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}