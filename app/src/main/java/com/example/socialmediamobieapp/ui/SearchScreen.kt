package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.network.dto.response.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SearchScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Khoảng cách phía trên thanh tìm kiếm
            Spacer(modifier = Modifier.height(16.dp))

            // TextField để nhập tên user
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm kiếm") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(24.dp)) // Bo tròn hơn
                    .border(0.dp, Color.Transparent, shape = RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        // Gọi API khi nhấn biểu tượng kính lúp
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    RetrofitInstance.api.searchUsers(searchQuery).result
                                }
                                if (response != null) {
                                    searchResults = response
                                }
                            } catch (e: Exception) {
                                errorMessage = "Lỗi khi tìm kiếm: ${e.message}"
                                if (e.message?.contains("Phiên đăng nhập hết hạn") == true) {
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = Color.Gray
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        // Gọi API khi nhấn Enter
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    RetrofitInstance.api.searchUsers(searchQuery).result
                                }
                                if (response != null) {
                                    searchResults = response
                                }
                            } catch (e: Exception) {
                                errorMessage = "Lỗi khi tìm kiếm: ${e.message}"
                                if (e.message?.contains("Phiên đăng nhập hết hạn") == true) {
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ),
                singleLine = true
            )

            // Hiển thị lỗi nếu có
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            // Hiển thị loading
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Hiển thị kết quả tìm kiếm
                if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                    Text("Không tìm thấy người dùng", fontSize = 16.sp, color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { profile ->
                            ProfileItem(profile = profile, onClick = {
                                // TODO: Xử lý khi nhấn vào profile (ví dụ: xem chi tiết profile)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(profile: Profile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        if (profile.avatarUrl?.isNotBlank() == true) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.username.first().toString().uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Username và tên đầy đủ
        Column {
            Text(
                text = profile.username,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${profile.firstName} ${profile.lastName}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}