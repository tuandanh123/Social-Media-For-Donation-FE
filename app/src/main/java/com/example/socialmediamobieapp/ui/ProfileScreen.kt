package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
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
import com.example.socialmediamobieapp.model.dto.request.FollowRequest
import com.example.socialmediamobieapp.model.dto.request.LogoutRequest
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.network.dto.response.PostResponse
import com.example.socialmediamobieapp.network.dto.response.ProfileResponse
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import com.example.socialmediamobieapp.utils.TokenManager
import com.example.socialmediamobieapp.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    profileId: String,
    tokenManager: TokenManager,
    postRepository: PostRepository,
    navController: NavController,
    viewModel: HomeViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<ProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var suggestedProfiles by remember { mutableStateOf<List<ProfileResponse>>(emptyList()) }
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var savedPosts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var postCount by remember { mutableStateOf(0) }
    var followerCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf("posts") }
    var isFollowing by remember { mutableStateOf(false) } // Trạng thái theo dõi
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUserProfileId = tokenManager.getProfileId() ?: ""
    val isOwnProfile = profileId == currentUserProfileId

    LaunchedEffect(profileId) {
        isLoading = true
        errorMessage = null
        try {
            val profileResponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getProfile(profileId)
            }
            if (profileResponse.code == 1000 && profileResponse.result != null) {
                profile = profileResponse.result
            } else {
                errorMessage = profileResponse.message ?: "Không thể tải thông tin profile"
                return@LaunchedEffect
            }

            val followersResponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getFollowers(profileId)
            }
            if (followersResponse.code == 1000 && followersResponse.result != null) {
                followerCount = followersResponse.result.size
                // Kiểm tra xem người dùng hiện tại có trong danh sách followers không
                isFollowing = followersResponse.result.any { it.profileId == currentUserProfileId }
            } else {
                errorMessage = followersResponse.message ?: "Không thể tải danh sách followers"
            }

            val followingResponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getFollowing(profileId)
            }
            if (followingResponse.code == 1000 && followingResponse.result != null) {
                followingCount = followingResponse.result.size
            } else {
                errorMessage = followingResponse.message ?: "Không thể tải danh sách following"
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải profile: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(profileId) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getAllPosts(page = 1, size = 100).result?.data?.filter {
                    it.profileId == profileId
                } ?: emptyList()
            }
            postCount = response.size
            if (isOwnProfile) {
                posts = response
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải số bài viết: ${e.message}"
        }
    }

    LaunchedEffect(isOwnProfile) {
        if (isOwnProfile) {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getAllProfiles()
                }
                if (response.code == 1000 && response.result != null) {
                    suggestedProfiles = response.result.filter { it.profileId != profileId }
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi khi tải gợi ý: ${e.message}"
            }
        }
    }

    LaunchedEffect(selectedTab, isOwnProfile) {
        if (isOwnProfile && selectedTab == "saved") {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getSavedPosts(profileId).result ?: emptyList()
                }
                savedPosts = response
            } catch (e: Exception) {
                errorMessage = "Lỗi khi tải bài viết đã lưu: ${e.message}"
            }
        }
    }

    Scaffold(
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
        } else if (profile != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Header Profile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = profile?.avatarUrl,
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                        ),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = postCount.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(text = "bài viết", fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = followerCount.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(text = "người theo dõi", fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = followingCount.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(text = "đang theo dõi", fontSize = 12.sp)
                        }

                        if (isOwnProfile) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            val currentToken = tokenManager.getAccessToken() ?: return@launch
                                            val logoutRequest = LogoutRequest(token = currentToken)
                                            val response = withContext(Dispatchers.IO) {
                                                RetrofitInstance.api.logout(logoutRequest)
                                            }
                                            if (response.code == 1000) {
                                                tokenManager.clear()
                                                navController.navigate("login") {
                                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                if (response.message?.contains("Phiên đăng nhập hết hạn") == true) {
                                                    tokenManager.clear()
                                                    navController.navigate("login") {
                                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                } else {
                                                    snackbarHostState.showSnackbar(
                                                        message = response.message ?: "Không thể đăng xuất",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(
                                                message = "Lỗi khi đăng xuất: ${e.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Đăng xuất",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }


                // Tên và mô tả
                Text(
                    text = profile?.username ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = profile?.bio ?: "thích mởi G.ạ",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Nút Theo dõi/Chỉnh sửa hồ sơ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (isOwnProfile) {
                        Button(
                            onClick = { /* TODO: Xử lý chỉnh sửa hồ sơ */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .padding(end = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Chỉnh sửa hồ sơ", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { /* TODO: Xử lý chia sẻ hồ sơ */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .padding(start = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Chia sẻ hồ sơ", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (isFollowing) {
                                        // Unfollow
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitInstance.api.unfollowUser(profileId)
                                        }
                                        if (response.code == 1000) {
                                            isFollowing = false
                                            followerCount -= 1
                                        }
                                    } else {
                                        // Follow
                                        val followRequest = FollowRequest(profileId = profileId)
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitInstance.api.followUser(followRequest)
                                        }
                                        if (response.code == 1000) {
                                            isFollowing = true
                                            followerCount += 1
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (isFollowing) Color.White else Color(0xFF0095F6),
                                contentColor = if (isFollowing) Color.Black else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Đang theo dõi" else "Theo dõi",
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Nếu là trang cá nhân, hiển thị gợi ý và bài viết
                if (isOwnProfile) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gợi ý cho bạn",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Xem tất cả",
                                color = Color(0xFF0095F6),
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { /* TODO: Xem tất cả gợi ý */ }
                            )
                        }
                        LazyRow(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(suggestedProfiles) { suggestedProfile ->
                                SuggestedProfileItem(
                                    suggestedProfile = suggestedProfile,
                                    currentUserProfileId = currentUserProfileId
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabButton(
                            text = "Bài viết",
                            isSelected = selectedTab == "posts",
                            onClick = { selectedTab = "posts" }
                        )
                        TabButton(
                            text = "Đã lưu",
                            isSelected = selectedTab == "saved",
                            onClick = { selectedTab = "saved" }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedTab == "posts") {
                            items(posts) { post ->
                                PostItemSimple(
                                    post = post,
                                    onClick = {
                                        navController.navigate("userPosts/$profileId/posts")
                                    }
                                )
                            }
                        } else {
                            items(savedPosts) { post ->
                                PostItemSimple(
                                    post = post,
                                    onClick = {
                                        navController.navigate("userPosts/$profileId/saved")
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Tài khoản riêng tư",
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Đây là tài khoản riêng tư.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Hãy theo dõi tài khoản này để xem ảnh và video của họ.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedProfileItem(suggestedProfile: ProfileResponse, currentUserProfileId: String) {
    val coroutineScope = rememberCoroutineScope()
    var isFollowing by remember { mutableStateOf(false) }

    // Kiểm tra trạng thái theo dõi ban đầu
    LaunchedEffect(suggestedProfile.profileId) {
        val followersResponse = withContext(Dispatchers.IO) {
            RetrofitInstance.api.getFollowers(suggestedProfile.profileId)
        }
        if (followersResponse.code == 1000 && followersResponse.result != null) {
            isFollowing = followersResponse.result.any { it.profileId == currentUserProfileId }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .padding(vertical = 4.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = suggestedProfile.avatarUrl,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            ),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = suggestedProfile.username,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    if (isFollowing) {
                        // Unfollow
                        val response = withContext(Dispatchers.IO) {
                            RetrofitInstance.api.unfollowUser(suggestedProfile.profileId)
                        }
                        if (response.code == 1000) {
                            isFollowing = false
                        }
                    } else {
                        // Follow
                        val followRequest = FollowRequest(profileId = suggestedProfile.profileId)
                        val response = withContext(Dispatchers.IO) {
                            RetrofitInstance.api.followUser(followRequest)
                        }
                        if (response.code == 1000) {
                            isFollowing = true
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(top = 4.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isFollowing) Color.White else Color(0xFF0095F6),
                contentColor = if (isFollowing) Color.Black else Color.White
            )
        ) {
            Text(
                text = if (isFollowing) "Đang theo dõi" else "Theo dõi",
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Color.Black else Color.Gray,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp)
    )
}

@Composable
fun PostItemSimple(post: PostResponse, onClick: () -> Unit) {
    Image(
        painter = rememberAsyncImagePainter(
            model = post.fileIds.firstOrNull() ?: "",
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
        ),
        contentDescription = "Post Image",
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentScale = ContentScale.Crop
    )
}

