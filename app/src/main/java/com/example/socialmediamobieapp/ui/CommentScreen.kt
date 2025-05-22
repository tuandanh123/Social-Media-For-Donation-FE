package com.example.socialmediamobieapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.socialmediamobieapp.model.CommentWithUser
import com.example.socialmediamobieapp.model.CommentsState
import com.example.socialmediamobieapp.repository.MockPostRepository
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import com.example.socialmediamobieapp.utils.TokenManager
import com.example.socialmediamobieapp.viewmodel.CommentViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentScreen(
    postId: String,
    postRepository: PostRepository,
    tokenManager: TokenManager,
    navController: NavController
) {
    val viewModel = remember { CommentViewModel(postId, postRepository, tokenManager) }
    val commentsState by viewModel.commentsState.collectAsState()

    var commentText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var replyingToUsername by remember { mutableStateOf<String?>(null) }

    SocialMediaMobieAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bình luận", fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                        }
                    },
                    backgroundColor = Color.White,
                    elevation = 0.dp
                )
            },
            bottomBar = {
                CommentInputBar(
                    commentText = commentText,
                    onCommentTextChange = { commentText = it },
                    onSendClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.createComment(commentText, replyingToCommentId)
                            commentText = ""
                            replyingToCommentId = null
                            replyingToUsername = null
                        }
                    },
                    replyingToUsername = replyingToUsername,
                    onCancelReply = {
                        replyingToCommentId = null
                        replyingToUsername = null
                    }
                )
            }
        ) { paddingValues ->
            when (commentsState) {
                is CommentsState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is CommentsState.Success -> {
                    val comments = (commentsState as CommentsState.Success).comments
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        items(comments) { commentWithUser ->
                            CommentItem(
                                commentWithUser = commentWithUser,
                                onReplyClick = { commentId, username ->
                                    replyingToCommentId = commentId
                                    replyingToUsername = username
                                }
                            )
                        }
                    }
                }
                is CommentsState.Error -> {
                    Text(
                        text = (commentsState as CommentsState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(
    commentWithUser: CommentWithUser,
    onReplyClick: (String, String) -> Unit = { _, _ -> },
    depth: Int = 0
) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(commentWithUser.likeCount) }

    val timeDisplay = commentWithUser.comment.createdAt?.let { createdAtString ->
        try {
            val createdAt = LocalDateTime.parse(createdAtString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val currentTime = LocalDateTime.now()
            val hoursDiff = java.time.Duration.between(createdAt, currentTime).toHours()
            "$hoursDiff giờ"
        } catch (e: Exception) {
            "Không xác định"
        }
    } ?: "Không xác định"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = (16 + depth * 16).dp, // Thụt vào theo độ sâu
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = commentWithUser.avatarUrl,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                ),
                contentDescription = "Ảnh đại diện",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = commentWithUser.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = commentWithUser.comment.content,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeDisplay,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trả lời",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.clickable {
                            onReplyClick(commentWithUser.comment.id, commentWithUser.username)
                        }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        likeCount += if (isLiked) 1 else -1
                        // TODO: Gọi API reactToComment
                    }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Thích",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                }
                if (likeCount > 0) {
                    Text(
                        text = likeCount.toString(),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }

        // Hiển thị các bình luận trả lời (nếu có)
        commentWithUser.replies.forEach { reply ->
            CommentItem(
                commentWithUser = reply,
                onReplyClick = onReplyClick,
                depth = depth + 1
            )
        }
    }
}

@Composable
fun CommentInputBar(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    replyingToUsername: String? = null,
    onCancelReply: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        if (replyingToUsername != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đang trả lời $replyingToUsername",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onCancelReply) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hủy trả lời",
                        tint = Color.Gray
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: Mở danh sách emoji */ }) {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Biểu cảm",
                    tint = Color.Black
                )
            }
            BasicTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                decorationBox = { innerTextField ->
                    if (commentText.isEmpty()) {
                        Text(
                            text = "Bắt đầu trò chuyện...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSendClick) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Gửi",
                    tint = if (commentText.isNotEmpty()) Color.Blue else Color.Gray
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun CommentScreenPreview() {
    SocialMediaMobieAppTheme {
        val tokenManager = object : TokenManager(null) {
            override fun saveAccessToken(token: String) {}
            override fun getAccessToken(): String? = "dummy_token"
            override fun saveProfileId(profileId: String) {}
            override fun getProfileId(): String? = "dummy_profile_id"
            override fun clear() {}
        }

        val postRepository = MockPostRepository(tokenManager)

        CommentScreen(
            postId = "post_123",
            postRepository = postRepository,
            tokenManager = tokenManager,
            navController = rememberNavController()
        )
    }
}