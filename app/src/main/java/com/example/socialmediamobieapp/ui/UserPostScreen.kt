package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialmediamobieapp.model.PostWithUser
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.utils.TokenManager
import com.example.socialmediamobieapp.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun UserPostsScreen(
    profileId: String,
    postType: String, // "posts" hoặc "saved"
    tokenManager: TokenManager,
    postRepository: PostRepository,
    viewModel: HomeViewModel,
    navController: NavController
) {
    var postsWithUser by remember { mutableStateOf<List<PostWithUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val posts = if (postType == "posts") {
                withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getAllPosts(page = 1, size = 100).result?.data?.filter {
                        it.profileId == profileId
                    } ?: emptyList()
                }
            } else {
                withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getSavedPosts(profileId).result ?: emptyList()
                }
            }

            val profileIds = posts.map { it.profileId }.distinct()
            val userInfoResult = withContext(Dispatchers.IO) {
                postRepository.getUserInfo(profileIds)
            }
            userInfoResult.fold(
                onSuccess = { profilesMap ->
                    val savedPostsResult = withContext(Dispatchers.IO) {
                        postRepository.getSavedPosts(profileId)
                    }
                    val savedPostIds = savedPostsResult.getOrElse { emptyList() }.map { it.id }.toSet()

                    postsWithUser = posts.mapNotNull { post ->
                        profilesMap[post.profileId]?.let { userInfo ->
                            val tymCountResult = postRepository.getTymCount(post.id)
                            val commentCountResult = postRepository.getCommentCount(post.id)
                            val userReactionResult = postRepository.getUserReaction(post.id, profileId)

                            val tymCount = tymCountResult.getOrElse { 0 }
                            val commentCount = commentCountResult.getOrElse { 0 }
                            val shareCount = (tymCount + commentCount) / 2
                            val isLiked = userReactionResult.getOrElse { false }
                            val isSaved = savedPostIds.contains(post.id)

                            PostWithUser(
                                post = post,
                                username = userInfo.username,
                                avatarUrl = userInfo.avatarUrl,
                                tymCount = tymCount,
                                commentCount = commentCount,
                                shareCount = shareCount,
                                isLiked = isLiked,
                                isSaved = isSaved
                            )
                        }
                    }
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Failed to load user info"
                }
            )
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải bài viết: ${e.message}"
        } finally {
            isLoading = false
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
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
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
                items(postsWithUser) { postWithUser ->
                    PostItem(
                        postWithUser = postWithUser,
                        postRepository = postRepository,
                        tokenManager = tokenManager,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}