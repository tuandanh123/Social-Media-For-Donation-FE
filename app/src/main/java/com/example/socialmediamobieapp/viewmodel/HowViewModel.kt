package com.example.socialmediamobieapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.socialmediamobieapp.model.PostWithUser
import com.example.socialmediamobieapp.model.StoriesState
import com.example.socialmediamobieapp.model.PostsState
import com.example.socialmediamobieapp.model.dto.request.DonationCreationRequest
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.repository.StoryRepository
import com.example.socialmediamobieapp.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer

open class HomeViewModel(
    private val tokenManager: TokenManager,
    private val storyRepository: StoryRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _storiesState = MutableStateFlow<StoriesState>(StoriesState.Loading)
    open val storiesState: StateFlow<StoriesState> = _storiesState.asStateFlow()

    private val _postsState = MutableStateFlow<PostsState>(PostsState.Loading)
    open val postsState: StateFlow<PostsState> = _postsState.asStateFlow()

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> get() = _hasUnreadNotifications

    private val timer = Timer()

    init {
        loadStories()
        loadPosts()
        startTokenRefreshPolling() // Thêm polling để làm mới token
    }

    private fun startTokenRefreshPolling() {
        viewModelScope.launch {
            while (true) {
                delay(240_000L) // 4 phút = 240 giây
                refreshToken()
            }
        }
    }

    private suspend fun refreshToken() {
        try {
            // Giả sử tokenManager có phương thức lấy refresh token
            val currentRefreshToken = tokenManager.getAccessToken() ?: run {
                println("Refresh token is null, cannot refresh token")
                return
            }

            val response = RetrofitInstance.api.refreshToken(
                currentRefreshToken
            )

            if (response.code == 1000 && response.result != null) {
                val newAccessToken = response.result.token

                tokenManager.saveAccessToken(newAccessToken)
                println("Token refreshed successfully: $newAccessToken")
            } else {
                println("Failed to refresh token: ${response.message ?: "Unknown error"}")
                handleTokenRefreshFailure()
            }
        } catch (e: Exception) {
            println("Error refreshing token: ${e.message}")
            handleTokenRefreshFailure()
        }
    }

    private fun handleTokenRefreshFailure() {
        // Nếu làm mới token thất bại, xóa token và yêu cầu đăng nhập lại
        tokenManager.clear()
        _postsState.value = PostsState.Error("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại")
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(100000L) // Đợi 5 giây
                loadPosts() // Gọi lại để lấy số liệu mới
            }
        }
    }



    fun loadStories() {
        viewModelScope.launch {
            _storiesState.value = StoriesState.Loading
            val profileId = tokenManager.getProfileId() ?: run {
                _storiesState.value = StoriesState.Error("Profile ID is null")
                return@launch
            }

            val myStoryResult = storyRepository.getMyStory(profileId)
            delay(700L) // Thêm delay để tránh gọi API quá nhanh
            val allStoriesResult = storyRepository.getAllStories()

            when {
                myStoryResult.isSuccess && allStoriesResult.isSuccess -> {
                    val myStory = myStoryResult.getOrNull()
                    val allStories = allStoriesResult.getOrNull() ?: emptyList()

                    // Lấy username của myStory (nếu myStory không null)
                    val myUsername = myStory?.username

                    // Lọc bỏ story có username trùng với myStory
                    val filteredStories = if (myUsername != null) {
                        allStories.filter { story ->
                            story.username != myUsername
                        }
                    } else {
                        allStories // Nếu myStory null, không lọc
                    }

                    _storiesState.value = StoriesState.Success(
                        myStory = myStory,
                        userStories = filteredStories
                    )
                }
                else -> {
                    val errorMessage = myStoryResult.exceptionOrNull()?.message
                        ?: allStoriesResult.exceptionOrNull()?.message
                        ?: "Failed to load stories"
                    _storiesState.value = StoriesState.Error(errorMessage)
                }
            }
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _postsState.value = PostsState.Loading
            val profileId = tokenManager.getProfileId() ?: run {
                _postsState.value = PostsState.Error("Profile ID is null")
                return@launch
            }

            val postsResult = postRepository.getAllPosts(page = 1, size = 10)
            postsResult.fold(
                onSuccess = { pageResponse ->
                    val posts = pageResponse.data
                    val profileIds = posts.map { it.profileId }.distinct()
                    val userInfoResult = postRepository.getUserInfo(profileIds)
                    userInfoResult.fold(
                        onSuccess = { profilesMap ->
                            // Lấy danh sách bài viết đã lưu để kiểm tra trạng thái isSaved
                            val savedPostsResult = postRepository.getSavedPosts(profileId)
                            val savedPostIds = savedPostsResult.getOrElse { emptyList() }.map { it.id }.toSet()

                            val postsWithUser = posts.mapNotNull { post ->
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
                            _postsState.value = PostsState.Success(postsWithUser)
                        },
                        onFailure = { error ->
                            _postsState.value = PostsState.Error(error.message ?: "Failed to load user info")
                        }
                    )
                },
                onFailure = { error ->
                    _postsState.value = PostsState.Error(error.message ?: "Failed to load posts")
                }
            )
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val profileId = tokenManager.getProfileId() ?: return@launch
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getNotifications(profileId)
                }
                if (response.code == 1000 && response.result != null) {
                    _hasUnreadNotifications.value = response.result.any { !it.isRead }
                }
            } catch (e: Exception) {
                // Bỏ qua lỗi, giữ trạng thái mặc định
            }
        }
    }

    fun savePost(postId: String) {
        viewModelScope.launch {
            val result = postRepository.savePost(postId)
            result.fold(
                onSuccess = {
                    // Cập nhật trạng thái isSaved của bài viết
                    val currentPosts = (_postsState.value as? PostsState.Success)?.posts ?: emptyList()
                    val updatedPosts = currentPosts.map { postWithUser ->
                        if (postWithUser.post.id == postId) {
                            postWithUser.copy(isSaved = true)
                        } else {
                            postWithUser
                        }
                    }
                    _postsState.value = PostsState.Success(updatedPosts)
                },
                onFailure = { error ->
                    _postsState.value = PostsState.Error(error.message ?: "Failed to save post")
                }
            )
        }
    }

    fun unsavePost(postId: String) {
        viewModelScope.launch {
            val result = postRepository.unsavePost(postId)
            result.fold(
                onSuccess = {
                    // Cập nhật trạng thái isSaved của bài viết
                    val currentPosts = (_postsState.value as? PostsState.Success)?.posts ?: emptyList()
                    val updatedPosts = currentPosts.map { postWithUser ->
                        if (postWithUser.post.id == postId) {
                            postWithUser.copy(isSaved = false)
                        } else {
                            postWithUser
                        }
                    }
                    _postsState.value = PostsState.Success(updatedPosts)
                },
                onFailure = { error ->
                    _postsState.value = PostsState.Error(error.message ?: "Failed to unsave post")
                }
            )
        }
    }

    fun donate(request: DonationCreationRequest, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = postRepository.donate(request)
            result.fold(
                onSuccess = { donationResponse ->
                    onResult(Result.success(donationResponse.payUrl))
                },
                onFailure = { error ->
                    onResult(Result.failure(error))
                    _postsState.value = PostsState.Error(error.message ?: "Failed to donate")
                }
            )
        }
    }

    class Factory(
        private val tokenManager: TokenManager,
        private val storyRepository: StoryRepository = StoryRepository(RetrofitInstance.api),
        private val postRepository: PostRepository = PostRepository(RetrofitInstance.api, tokenManager)
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(tokenManager, storyRepository, postRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}