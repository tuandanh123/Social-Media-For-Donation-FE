package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.socialmediamobieapp.R
import com.example.socialmediamobieapp.model.DonationWithUser
import com.example.socialmediamobieapp.model.PostWithDonations
import com.example.socialmediamobieapp.model.Report
import com.example.socialmediamobieapp.model.dto.response.UserResponse
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.network.dto.response.PostResponse
import com.example.socialmediamobieapp.network.dto.response.UsernameAndAvatar
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import com.example.socialmediamobieapp.utils.TokenManager
import com.example.socialmediamobieapp.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun AdminHomeScreen(
    viewModel: HomeViewModel,
    postRepository: PostRepository,
    tokenManager: TokenManager,
    navController: NavController
) {
    val adminNavController = rememberNavController()
    val currentRoute by adminNavController.currentBackStackEntryAsState().value?.destination?.route.orEmpty().let { route ->
        remember { mutableStateOf(route) }
    }

    SocialMediaMobieAppTheme {
        Scaffold(
            topBar = { InstagramTopBar(navController = navController, viewModel = viewModel) },
            bottomBar = {
                AdminBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        adminNavController.navigate(route) {
                            popUpTo(adminNavController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = adminNavController,
                startDestination = "admin_stats",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("admin_stats") {
                    AdminDonationStatsScreen(
                        postRepository = postRepository,
                        navController = adminNavController
                    )
                }
                composable("admin_users") {
                    AdminUsersScreen(navController = adminNavController)
                }
                composable("admin_posts") {
                    AdminPostsScreen(
                        postRepository = postRepository,
                        navController = adminNavController
                    )
                }
                composable("admin_reports") {
                    AdminReportsScreen(navController = adminNavController)
                }
            }
        }
    }
}

@Composable
fun AdminBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        contentColor = Color.Black
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Insights, contentDescription = "Thống kê") },
            label = { Text("Thống kê", fontSize = 12.sp) },
            selected = currentRoute == "admin_stats",
            onClick = { onNavigate("admin_stats") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.People, contentDescription = "Quản lý người dùng") },
            label = { Text("Người dùng", fontSize = 12.sp) },
            selected = currentRoute == "admin_users",
            onClick = { onNavigate("admin_users") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Article, contentDescription = "Quản lý bài viết") },
            label = { Text("Bài viết", fontSize = 12.sp) },
            selected = currentRoute == "admin_posts",
            onClick = { onNavigate("admin_posts") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Report, contentDescription = "Báo cáo") },
            label = { Text("Báo cáo", fontSize = 12.sp) },
            selected = currentRoute == "admin_reports",
            onClick = { onNavigate("admin_reports") }
        )
    }
}

@Composable
fun AdminDonationStatsScreen(
    postRepository: PostRepository,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var postsWithDonations by remember { mutableStateOf<List<PostWithDonations>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalAmount by remember { mutableStateOf(BigDecimal.ZERO) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            // Lấy danh sách tất cả bài post
            val postsResponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getAllPosts(page = 1, size = 100)
            }
            if (postsResponse.code == 1000 && postsResponse.result != null) {
                val posts = postsResponse.result.data
                val allPostsWithDonations = mutableListOf<PostWithDonations>()
                var tempTotalAmount = BigDecimal.ZERO

                // Lấy username của tất cả người tạo bài post
                val creatorIds = posts.map { it.profileId }.distinct()
                val userInfoResult = postRepository.getUserInfo(creatorIds)
                userInfoResult.fold(
                    onSuccess = { profilesMap ->
                        // Lấy donation cho từng bài post
                        posts.forEach { post ->
                            val donationsResponse = withContext(Dispatchers.IO) {
                                RetrofitInstance.api.getAllDonationOfPost(post.id, page = 1, size = 100)
                            }
                            if (donationsResponse.code == 1000 && donationsResponse.result != null) {
                                val donations = donationsResponse.result.data
                                tempTotalAmount += donations.sumOf { it.amount }
                                val donorIds = donations.filter { !it.isAnonymous }.map { it.donorId }.distinct()
                                val donorInfoResult = postRepository.getUserInfo(donorIds)
                                donorInfoResult.fold(
                                    onSuccess = { donorProfilesMap ->
                                        val donationWithUsers = donations.map { donation ->
                                            if (donation.isAnonymous) {
                                                DonationWithUser(
                                                    donation = donation,
                                                    username = "Anonymous",
                                                    avatarUrl = null
                                                )
                                            } else {
                                                donorProfilesMap[donation.donorId]?.let { userInfo ->
                                                    DonationWithUser(
                                                        donation = donation,
                                                        username = userInfo.username,
                                                        avatarUrl = userInfo.avatarUrl
                                                    )
                                                } ?: DonationWithUser(
                                                    donation = donation,
                                                    username = "Unknown",
                                                    avatarUrl = null
                                                )
                                            }
                                        }
                                        val creatorUsername = profilesMap[post.profileId]?.username ?: "Unknown"
                                        allPostsWithDonations.add(
                                            PostWithDonations(
                                                post = post,
                                                creatorUsername = creatorUsername,
                                                donations = donationWithUsers
                                            )
                                        )
                                    },
                                    onFailure = { error ->
                                        errorMessage = "Không thể tải thông tin người dùng: ${error.message}"
                                    }
                                )
                            }
                        }
                        postsWithDonations = allPostsWithDonations
                        totalAmount = tempTotalAmount
                    },
                    onFailure = { error ->
                        errorMessage = "Không thể tải thông tin người tạo bài post: ${error.message}"
                    }
                )
            } else {
                errorMessage = postsResponse.message ?: "Không thể tải danh sách bài post"
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải dữ liệu: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage ?: "Đã xảy ra lỗi",
                color = Color.Red,
                fontSize = 14.sp
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                postsWithDonations.forEach { postWithDonations ->
                    item {
                        // Tiêu đề bài post
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 4.dp,
                            backgroundColor = Color(0xFFE3F2FD)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = "Bài post: ${postWithDonations.post.content.ifEmpty { "Không có tiêu đề" }}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Người tạo: ${postWithDonations.creatorUsername}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        // Đường phân cách
                        Text(
                            text = "---------------",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    items(postWithDonations.donations) { donationWithUser ->
                        DonationStatItem(donationWithUser = donationWithUser)
                    }
                }
            }
            // Tổng số tiền donation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = 4.dp,
                backgroundColor = Color(0xFFF0F0F0)
            ) {
                Text(
                    text = "Tổng số tiền: ${formatAmount(totalAmount)} VNĐ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(16.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun AdminUsersScreen(
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getUsers()
            }
            if (response.code == 1000 && response.result != null) {
                users = response.result
            } else {
                errorMessage = response.message ?: "Không thể tải danh sách người dùng"
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải dữ liệu: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserItem(
                            user = user,
                            onBlockClick = { userId ->
                                coroutineScope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitInstance.api.deleteUser(userId)
                                        }
                                        if (response.code == 1000) {
                                            users = users.filter { it.id != userId }
                                            snackbarHostState.showSnackbar(
                                                message = "Người dùng đã bị xóa",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = response.message ?: "Không thể xóa người dùng",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            message = "Lỗi khi xóa người dùng: ${e.message}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: UserResponse,
    onBlockClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thông tin người dùng
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "Username: ${user.username}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Họ: ${user.firstName ?: ""}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Tên: ${user.lastName ?: ""}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Ngày sinh: ${user.dob ?: ""}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Email: ${user.email ?: ""}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Trạng thái: ${if (user.blocked) "Bị khóa" else "Hoạt động"}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Nhà cung cấp: ${user.provider}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Vai trò: ${user.roles.joinToString(", ") { it.name }}",
                    fontSize = 14.sp
                )
            }
            // Nút Block/Unblock
            Button(
                onClick = { onBlockClick(user.id) },
                modifier = Modifier
                    .height(36.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (user.blocked) Color.Gray else Color.Red,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = if (user.blocked) "Mở khóa" else "Khóa",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AdminPostsScreen(
    postRepository: PostRepository,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userInfoMap by remember { mutableStateOf<Map<String, UsernameAndAvatar>>(emptyMap()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val postsResponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getAllPosts(page = 1, size = 100)
            }
            if (postsResponse.code == 1000 && postsResponse.result != null) {
                posts = postsResponse.result.data
                val profileIds = posts.map { it.profileId }.distinct()
                val userInfoResult = postRepository.getUserInfo(profileIds)
                userInfoResult.fold(
                    onSuccess = { profilesMap ->
                        userInfoMap = profilesMap
                    },
                    onFailure = { error ->
                        errorMessage = "Không thể tải thông tin người dùng: ${error.message}"
                    }
                )
            } else {
                errorMessage = postsResponse.message ?: "Không thể tải danh sách bài post"
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải dữ liệu: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            username = userInfoMap[post.profileId]?.username ?: "Unknown",
                            avatarUrl = userInfoMap[post.profileId]?.avatarUrl,
                            onDeleteClick = { postId ->
                                coroutineScope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitInstance.api.deletePost(postId)
                                        }
                                        if (response.code == 1000) {
                                            posts = posts.filter { it.id != postId }
                                            snackbarHostState.showSnackbar(
                                                message = "Bài viết đã bị xóa",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = response.message ?: "Không thể xóa bài viết",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            message = "Lỗi khi xóa bài viết: ${e.message}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: PostResponse,
    username: String,
    avatarUrl: String?,
    onDeleteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar và thông tin bài viết
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = avatarUrl,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                    ),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ID: ${post.id}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Người đăng: $username",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Nội dung: ${post.content}",
                        fontSize = 14.sp
                    )
                }
            }
            // Nút xóa
            Button(
                onClick = { onDeleteClick(post.id) },
                modifier = Modifier
                    .height(36.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = "Xóa",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AdminReportsScreen(
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getAllReports()
            }
            if (response.code == 1000 && response.result != null) {
                reports = response.result
            } else {
                errorMessage = response.message ?: "Không thể tải danh sách báo cáo"
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải dữ liệu: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reports) { report ->
                        ReportItem(report = report)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    report: Report
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "ID: ${report.id}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Profile ID: ${report.profileId}",
                fontSize = 14.sp
            )
            Text(
                text = "Post ID: ${report.postId}",
                fontSize = 14.sp
            )
            Text(
                text = "Nội dung báo cáo: ${report.message}",
                fontSize = 14.sp
            )
        }
    }
}