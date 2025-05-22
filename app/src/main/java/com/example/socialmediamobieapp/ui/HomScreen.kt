package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.socialmediamobieapp.enums.ReactionType
import com.example.socialmediamobieapp.model.MockData
import com.example.socialmediamobieapp.model.PostsState
import com.example.socialmediamobieapp.model.StoriesState
import com.example.socialmediamobieapp.model.dto.request.DonationCreationRequest
import com.example.socialmediamobieapp.model.dto.request.ReactionCreationRequest
import com.example.socialmediamobieapp.model.dto.request.SavedPostRequest
import com.example.socialmediamobieapp.model.dto.response.AuthenticationResponse
import com.example.socialmediamobieapp.model.dto.response.CommentResponse
import com.example.socialmediamobieapp.model.dto.response.DonationResponse
import com.example.socialmediamobieapp.model.dto.response.SavedPostResponse
import com.example.socialmediamobieapp.network.ApiService
import com.example.socialmediamobieapp.network.dto.ApiResponse
import com.example.socialmediamobieapp.network.dto.PageResponse
import com.example.socialmediamobieapp.network.dto.request.LoginRequest
import com.example.socialmediamobieapp.network.dto.request.SignUpRequest
import com.example.socialmediamobieapp.network.dto.response.LoginApiResponse
import com.example.socialmediamobieapp.network.dto.response.PostResponse
import com.example.socialmediamobieapp.network.dto.response.Profile
import com.example.socialmediamobieapp.network.dto.response.ProfileResponse
import com.example.socialmediamobieapp.network.dto.response.ReactionResponse
import com.example.socialmediamobieapp.network.dto.response.SignUpApiResponse
import com.example.socialmediamobieapp.network.dto.response.Story
import com.example.socialmediamobieapp.network.dto.response.UsernameAndAvatar
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.repository.StoryRepository
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import com.example.socialmediamobieapp.utils.TokenManager
import com.example.socialmediamobieapp.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MultipartBody

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    postRepository: PostRepository,
    tokenManager: TokenManager,
    navController: NavController // Thêm tham số navController
) {
    val storiesState by viewModel.storiesState.collectAsState()
    val postsState by viewModel.postsState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.loadStories()
        viewModel.loadPosts()
    }

    Scaffold(
        topBar = { InstagramTopBar(
            navController = navController,
            viewModel = viewModel
        ) },
        bottomBar = { BottomNavigationBar(
            navController = navController
        ) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (storiesState) {
                is StoriesState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is StoriesState.Success -> {
                    StoriesSection(
                        myStory = (storiesState as StoriesState.Success).myStory,
                        userStories = (storiesState as StoriesState.Success).userStories
                    )
                }
                is StoriesState.Error -> {
                    Text(
                        text = (storiesState as StoriesState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            when (val state = postsState) {
                is PostsState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is PostsState.Success -> {
                    LazyColumn {
                        items(state.posts) { postWithUser ->
                            PostItem(
                                postWithUser = postWithUser,
                                postRepository = postRepository,
                                tokenManager = tokenManager,
                                navController = navController, // Truyền navController vào PostItem
                                viewModel = viewModel
                            )
                        }
                    }
                }
                is PostsState.Error -> {
                    Text(
                        text = (postsState as PostsState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

