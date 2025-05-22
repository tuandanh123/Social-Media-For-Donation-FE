package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.socialmediamobieapp.R
import com.example.socialmediamobieapp.model.DonationWithUser
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun DonationStatsScreen(
    postId: String,
    postRepository: PostRepository,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var donationsWithUser by remember { mutableStateOf<List<DonationWithUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalAmount by remember { mutableStateOf(BigDecimal.ZERO) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            // Lấy danh sách donation
            val donationsResponse = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getAllDonationOfPost(postId, page = 1, size = 100)
            }
            if (donationsResponse.code == 1000 && donationsResponse.result != null) {
                val donations = donationsResponse.result.data
                // Tính tổng số tiền
                totalAmount = donations.sumOf { donation -> donation.amount }
                // Lấy danh sách donorId (bỏ qua nếu isAnonymous)
                val donorIds = donations.filter { !it.isAnonymous }.map { it.donorId }.distinct()
                // Lấy thông tin người dùng
                val userInfoResult = postRepository.getUserInfo(donorIds)
                userInfoResult.fold(
                    onSuccess = { profilesMap ->
                        donationsWithUser = donations.map { donation ->
                            if (donation.isAnonymous) {
                                DonationWithUser(
                                    donation = donation,
                                    username = "Anonymous",
                                    avatarUrl = null
                                )
                            } else {
                                profilesMap[donation.donorId]?.let { userInfo ->
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
                    },
                    onFailure = { error ->
                        errorMessage = "Không thể tải thông tin người dùng: ${error.message}"
                    }
                )
            } else {
                errorMessage = donationsResponse.message ?: "Không thể tải danh sách donation"
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải dữ liệu: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê Donation", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                    items(donationsWithUser) { donationWithUser ->
                        DonationStatItem(donationWithUser = donationWithUser)
                    }
                }
                // Tổng số tiền donation
                Text(
                    text = "Tổng số tiền: ${formatAmount(totalAmount)} VNĐ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

@Composable
fun DonationStatItem(donationWithUser: DonationWithUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar và username
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = donationWithUser.avatarUrl,
                    placeholder = painterResource(
                        id = if (donationWithUser.donation.isAnonymous) {
                            R.drawable.anonymous_avatar
                        } else {
                            android.R.drawable.ic_menu_gallery
                        }
                    )
                ),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = donationWithUser.username,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        // Thông tin donation
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = "Số tiền: ${formatAmount(donationWithUser.donation.amount)} VNĐ",
                fontSize = 14.sp
            )
            Text(
                text = "Trạng thái: ${donationWithUser.donation.status}",
                fontSize = 14.sp
            )
            Text(
                text = "Phương thức: ${donationWithUser.donation.paymentMethod}",
                fontSize = 14.sp
            )
            Text(
                text = "Thời gian: ${donationWithUser.donation.paidAt ?: "Chưa thanh toán"}",
                fontSize = 14.sp
            )
        }
    }
}

// Hàm định dạng số tiền
fun formatAmount(amount: BigDecimal): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount)
}