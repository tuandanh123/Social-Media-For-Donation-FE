package com.example.socialmediamobieapp.ui

import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.socialmediamobieapp.enums.ReactionType
import com.example.socialmediamobieapp.model.MockData
import com.example.socialmediamobieapp.model.PostWithUser
import com.example.socialmediamobieapp.model.dto.request.ReactionCreationRequest
import com.example.socialmediamobieapp.repository.MockPostRepository
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import com.example.socialmediamobieapp.utils.TokenManager
import com.example.socialmediamobieapp.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
fun PostItem(
    postWithUser: PostWithUser,
    postRepository: PostRepository,
    tokenManager: TokenManager,
    navController: NavController, // Thêm NavController để điều hướng
    viewModel: HomeViewModel?
) {
    // State cục bộ để quản lý trạng thái "liked" và số "tym"
    var isLiked by remember { mutableStateOf(postWithUser.isLiked) }
    var localTymCount by remember { mutableStateOf(postWithUser.tymCount) }
    var isSaved by remember { mutableStateOf(postWithUser.isSaved) }
    val coroutineScope = rememberCoroutineScope()
    var showMoreOptions by remember { mutableStateOf(false) } // Trạng thái hiển thị menu ba chấm

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = postWithUser.avatarUrl,
                    placeholder = painterResource(id = R.drawable.ic_menu_gallery)
                ),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate("profile/${postWithUser.post.profileId}")
                    },
                contentScale = ContentScale.Crop
            )
            Text(
                text = postWithUser.username,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            Box {
                IconButton(onClick = { showMoreOptions = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMoreOptions,
                    onDismissRequest = { showMoreOptions = false }
                ) {
                    DropdownMenuItem(onClick = { showMoreOptions = false
                        navController.navigate("donation_stats/${postWithUser.post.id}")}) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = "Thống kê",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thống kê", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = { showMoreOptions = false /* TODO: Xử lý Mã QR */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Mã QR",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mã QR", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = { showMoreOptions = false /* TODO: Xử lý Thêm vào mục yêu thích */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = "Thêm vào mục yêu thích",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thêm vào mục yêu thích", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = { showMoreOptions = false /* TODO: Xử lý Bỏ theo dõi */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonRemove,
                                contentDescription = "Bỏ theo dõi",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bỏ theo dõi", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = { showMoreOptions = false /* TODO: Xử lý Giới thiệu về tài khoản này */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Giới thiệu về tài khoản này",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Giới thiệu về tài khoản này", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = { showMoreOptions = false /* TODO: Xử lý Bản dịch */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Bản dịch",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bản dịch", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = { showMoreOptions = false /* TODO: Xử lý Phụ đề */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = "Phụ đề",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Phụ đề", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = { showMoreOptions = false /* TODO: Xử lý Tài sao bạn nhìn thấy bài viết này */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Tài sao bạn nhìn thấy bài viết này",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tài sao bạn nhìn thấy bài viết này", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = { showMoreOptions = false /* TODO: Xử lý Ẩn */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Ẩn",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ẩn", fontSize = 14.sp)
                        }
                    }
                    DropdownMenuItem(onClick = {
                        showMoreOptions = false
                        navController.navigate("report/${postWithUser.post.id}")
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Báo cáo",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Red
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Báo cáo", fontSize = 14.sp, color = Color.Red)
                        }
                    }
                }
            }
            }
        }

        Image(
            painter = rememberAsyncImagePainter(
                model = postWithUser.post.fileIds.firstOrNull() ?: "",
                placeholder = painterResource(id = R.drawable.ic_menu_gallery)
            ),
            contentDescription = "Post Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        // Optimistic update
                        val previousIsLiked = isLiked
                        val previousTymCount = localTymCount
                        if (isLiked) {
                            // Unlike: Giảm số tym và đổi màu biểu tượng
                            isLiked = false
                            localTymCount -= 1
                        } else {
                            // Like: Tăng số tym và đổi màu biểu tượng
                            isLiked = true
                            localTymCount += 1
                        }

                        // Gửi yêu cầu API createReaction
                        coroutineScope.launch {
                            val profileId = tokenManager.getProfileId() ?: return@launch
                            val reactionRequest = ReactionCreationRequest(
                                postId = postWithUser.post.id,
                                reactionType = ReactionType.LIKE,
                                commentId = null.toString()
                            )
                            val result = postRepository.createReaction(reactionRequest)
                            result.fold(
                                onSuccess = { response ->
                                    // Cập nhật giao diện dựa trên phản hồi từ server
                                    if (response.action == "REMOVED") {
                                        isLiked = false
                                        localTymCount = previousTymCount - 1
                                    } else {
                                        isLiked = true
                                        localTymCount = previousTymCount + 1
                                    }
                                },
                                onFailure = {
                                    // Thất bại, quay lại trạng thái ban đầu
                                    isLiked = previousIsLiked
                                    localTymCount = previousTymCount
                                }
                            )
                        }
                    }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Black
                        )
                    }
                    Text(
                        text = formatNumber(localTymCount),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        // Điều hướng đến CommentScreen với postId
                        navController.navigate("comment/${postWithUser.post.id}")
                    }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = formatNumber(postWithUser.commentCount),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
                // Nút $ (mở giao diện Donation)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        // Mở màn hình Donation với postId
                        navController.navigate("donation/${postWithUser.post.id}")
                    }) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Donate",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = formatNumber(postWithUser.shareCount),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
            // Nút Save (giữ nguyên icon, thêm logic save/unsave)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (isSaved) {
                        viewModel?.unsavePost(postWithUser.post.id)
                        isSaved = false // Optimistic update
                    } else {
                        viewModel?.savePost(postWithUser.post.id)
                        isSaved = true // Optimistic update
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) Color.Yellow else Color.Black
                    )
                }
            }
        }

        Text(
            text = postWithUser.post.content,
            modifier = Modifier.padding(horizontal = 8.dp),
            fontSize = 14.sp
        )

        Text(
            text = postWithUser.post.createdAt,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }

// Hàm định dạng số (ví dụ: 5385 thành "5,385")
private fun formatNumber(number: Int): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(number)
}

@Preview(showBackground = true)
@Composable
fun PostItemPreview() {
    SocialMediaMobieAppTheme {
        PostItem(
            postWithUser = PostWithUser(
                post = MockData.mockPostWithUser.post,
                username = MockData.mockPostWithUser.username,
                avatarUrl = MockData.mockPostWithUser.avatarUrl,
                tymCount = 5385,
                commentCount = 19,
                shareCount = 323,
                isLiked = false
            ),
            postRepository = MockPostRepository(
                tokenManager = object : TokenManager(null) {
                    override fun saveAccessToken(token: String) {}
                    override fun getAccessToken(): String? = "dummy_token"
                    override fun saveProfileId(profileId: String) {}
                    override fun getProfileId(): String? = "dummy_profile_id"
                    override fun clear() {}
                }
            ),
            tokenManager = object : TokenManager(null) {
                override fun saveAccessToken(token: String) {}
                override fun getAccessToken(): String? = "dummy_token"
                override fun saveProfileId(profileId: String) {}
                override fun getProfileId(): String? = "dummy_profile_id"
                override fun clear() {}
            },
            navController = rememberNavController(),
            viewModel = null // Thêm NavController cho preview
        )
    }
}