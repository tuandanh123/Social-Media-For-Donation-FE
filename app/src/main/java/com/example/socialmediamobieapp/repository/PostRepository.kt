package com.example.socialmediamobieapp.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.socialmediamobieapp.enums.ReactionType
import com.example.socialmediamobieapp.model.CommentWithUser
import com.example.socialmediamobieapp.model.MockData
import com.example.socialmediamobieapp.model.dto.request.CommentCreationRequest
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
import com.example.socialmediamobieapp.network.dto.response.UsernameAndAvatar
import com.example.socialmediamobieapp.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.time.LocalDateTime

open class PostRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    fun Any.toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    suspend fun donate(request: DonationCreationRequest): Result<DonationResponse> {
        return try {
            val response = apiService.donate(request)
            if (response.code == 1000 && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.message ?: "Failed to save post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePost(postId: String): Result<SavedPostResponse> {
        val profileId = tokenManager.getProfileId() ?: return Result.failure(Exception("Profile ID is null"))
        val request = SavedPostRequest(profileId, postId)
        return try {
            val response = apiService.savePost(request)
            if (response.code == 1000 && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.message ?: "Failed to save post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unsavePost(postId: String): Result<String> {
        val profileId = tokenManager.getProfileId() ?: return Result.failure(Exception("Profile ID is null"))
        return try {
            val response = apiService.unsavePost(profileId, postId)
            if (response.code == 1000 && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.message ?: "Failed to unsave post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSavedPosts(profileId: String): Result<List<PostResponse>> {
        return try {
            val response = apiService.getSavedPosts(profileId)
            if (response.code == 1000 && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get saved posts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm kiểm tra trạng thái lưu bài viết (dựa trên getSavedPosts)
    suspend fun getSavedPostStatus(postId: String, profileId: String): Result<Boolean> {
        return try {
            val savedPostsResult = getSavedPosts(profileId)
            savedPostsResult.fold(
                onSuccess = { savedPosts ->
                    val isSaved = savedPosts.any { it.id == postId }
                    Result.success(isSaved)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun createComment(postId: String, content: String, parentId : String?, mediaFiles: List<File>? = null): Result<CommentResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Tạo CommentCreationRequest
                val request = CommentCreationRequest(postId = postId, content = content, parentId = parentId)

                // Chuyển CommentCreationRequest thành JSON string
                val requestBody = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    request.toJson()
                )

                // Tạo MultipartBody.Part cho CommentCreationRequest
                val commentCreationPart = MultipartBody.Part.createFormData(
                    "commentCreationRequest",
                    null,
                    requestBody
                )

                // Xử lý file media (nếu có)
                val files: List<MultipartBody.Part>? = mediaFiles?.map { file ->
                    val fileBody = RequestBody.create(
                        "multipart/form-data".toMediaTypeOrNull(),
                        file
                    )
                    MultipartBody.Part.createFormData(
                        "files", // Tên của part
                        file.name,
                        fileBody
                    )
                }

                // Gọi API
                val response = apiService.createComment(commentCreationPart, files)

                // Kiểm tra kết quả
                if (response.code == 1000 && response.result != null) {
                    Result.success(response.result)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to create comment"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun getCommentsByPost(postId: String, page: Int, size: Int): Result<PageResponse<CommentResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCommentsByPost(postId, page, size)
                if (response.code == 1000 && response.result != null) {
                    Result.success(response.result)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load comments"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun getAllPosts(page: Int, size: Int): Result<PageResponse<PostResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllPosts(page, size)
                if (response.code == 1000 && response.result != null) {
                    Result.success(response.result)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load posts"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun getUserInfo(profileIds: List<String>): Result<Map<String, UsernameAndAvatar>> {
        return coroutineScope {
            try {
                val profilesMap = profileIds.distinct().map { profileId ->
                    async(Dispatchers.IO) {
                        val response = apiService.getProfile(profileId)
                        if (response.code != 1000 || response.result == null) {
                            throw Exception("Failed to load profile for $profileId")
                        }
                        profileId to UsernameAndAvatar(
                            username = response.result.username,
                            avatarUrl = response.result.avatarUrl
                        )
                    }
                }.map { it.await() }.toMap()
                Result.success(profilesMap)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun getTymCount(postId: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.countReactionsByPost(postId)
                if (response.code == 1000 && response.result != null) {
                    Result.success(response.result.toInt())
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load reactions"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun getCommentCount(postId: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.countComments(postId)
                if (response.code == 1000 && response.result != null) {
                    Result.success(response.result.toInt())
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load comments"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun createReaction(reactionRequest: ReactionCreationRequest): Result<ReactionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.reactToPost(reactionRequest)
                if (response.code == 1000 && response.result != null) {
                    Result.success(response.result)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to create reaction"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun getUserReaction(postId: String, profileId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserReaction(postId, profileId)
                if (response.code == 1000 && response.result != null) {
                    Result.success(response.result != ReactionType.NONE)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get user reaction"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun getCommentLikeCount(commentId: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.countReactionsByComment(commentId)
                if (response.code == 1000 && response.result != null) {
                    Result.success(response.result.toInt())
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load comment likes"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

class MockPostRepository(
    private val tokenManager: TokenManager
) : PostRepository(
    apiService = object : ApiService {


        // State cục bộ để theo dõi trạng thái "liked" của bài viết trong mock
        private val likedPosts = mutableMapOf<String, Boolean>()

        override suspend fun getAllPosts(page: Int, size: Int): ApiResponse<PageResponse<PostResponse>> {
            throw NotImplementedError()
        }

        override suspend fun getProfile(profileId: String): ApiResponse<ProfileResponse> {
            throw NotImplementedError()
        }

        override suspend fun getAllProfiles(): ApiResponse<List<ProfileResponse>> {
            throw NotImplementedError()
        }

        override suspend fun getActiveProfile(userId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun setActiveProfile(profileId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun getMyProfiles(): ApiResponse<List<ProfileResponse>> {
            throw NotImplementedError()
        }

        override suspend fun login(request: LoginRequest): LoginApiResponse {
            throw NotImplementedError()
        }

        override suspend fun signUp(request: SignUpRequest): SignUpApiResponse {
            throw NotImplementedError()
        }

        override suspend fun refreshToken(request: String): ApiResponse<AuthenticationResponse> {
            throw NotImplementedError()
        }


        override suspend fun getReactionsByPost(
            postId: String,
            page: Int,
            size: Int
        ): ApiResponse<PageResponse<ReactionResponse>> {
            return ApiResponse(
                code = 1000,
                message = null.toString(),
                result = PageResponse(
                    data = emptyList(),
                    currentPage = 1,
                    pageSize = 10,
                    totalPages = 1,
                    totalElements = 5385L
                )
            )
        }

        override suspend fun searchUsers(username: String): ApiResponse<List<Profile>> {
            TODO("Not yet implemented")
        }

        override suspend fun donate(request: DonationCreationRequest): ApiResponse<DonationResponse> {
            throw NotImplementedError()
        }

        override suspend fun unsavePost(profileId: String, postId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun getSavedPosts(profileId: String): ApiResponse<List<PostResponse>> {
            throw NotImplementedError()
        }

        override suspend fun savePost(savedPostRequest: SavedPostRequest): ApiResponse<SavedPostResponse> {
            throw NotImplementedError()
        }

        override suspend fun createComment(
            commentCreationRequest: MultipartBody.Part,
            files: List<MultipartBody.Part>?
        ): ApiResponse<CommentResponse> {
            throw NotImplementedError()
        }

        override suspend fun updateComment(
            commentUpdateRequest: MultipartBody.Part,
            files: List<MultipartBody.Part>?,
            filesToRemove: List<String>?,
            commentId: String
        ): ApiResponse<CommentResponse> {
            throw NotImplementedError()
        }

        override suspend fun deleteComment(commentId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun deleteReply(commentId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun getCommentsByPost(
            postId: String,
            page: Int,
            size: Int
        ): ApiResponse<PageResponse<CommentResponse>> {
            throw NotImplementedError()
        }

        override suspend fun getReplies(
            commentId: String,
            page: Int,
            size: Int
        ): ApiResponse<PageResponse<CommentResponse>> {
            throw NotImplementedError()
        }

        override suspend fun countReplies(commentId: String): ApiResponse<Long> {
            throw NotImplementedError()
        }

        override suspend fun countComments(postId: String): ApiResponse<Long> {
            return ApiResponse(code = 1000, message = null.toString(), result = 19L)
        }

        override suspend fun getOneComment(commentId: String): ApiResponse<CommentResponse> {
            throw NotImplementedError()
        }

        override suspend fun getUserComments(
            profileId: String,
            page: Int,
            size: Int
        ): ApiResponse<PageResponse<CommentResponse>> {
            throw NotImplementedError()
        }

        override suspend fun deleteAllCommentsInPost(postId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun deleteAllCommentsInProfile(profileId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun reactToPost(
            reactionCreationRequest: ReactionCreationRequest
        ): ApiResponse<ReactionResponse> {
            val profileId = tokenManager.getProfileId() ?: throw IllegalStateException("Profile ID is null")
            val postId = reactionCreationRequest.postId

            // Kiểm tra trạng thái "liked" hiện tại của bài viết
            val currentlyLiked = likedPosts[postId] ?: false
            // Đảo ngược trạng thái: nếu đã "liked" thì chuyển sang "removed", nếu chưa thì "liked"
            likedPosts[postId] = !currentlyLiked

            return ApiResponse(
                code = 1000,
                message = null.toString(),
                result = ReactionResponse(
                    id = "reaction_123",
                    postId = postId,
                    commentId = null,
                    reactionType = if (!currentlyLiked) ReactionType.LIKE else ReactionType.NONE,
                    profileId = profileId,
                    createdAt = null,
                    updatedAt = null,
                    action = if (!currentlyLiked) "like" else "removed"
                )
            )
        }

        override suspend fun reactToComment(
            reactionCreationRequest: ReactionCreationRequest
        ): ApiResponse<ReactionResponse> {
            throw NotImplementedError()
        }

        override suspend fun getReactionsByComment(
            commentId: String,
            page: Int,
            size: Int
        ): ApiResponse<PageResponse<ReactionResponse>> {
            throw NotImplementedError()
        }

        override suspend fun countReactionsByPost(postId: String): ApiResponse<Long> {
            return ApiResponse(code = 1000, message = null.toString(), result = 5385L)
        }

        override suspend fun countReactionsByComment(commentId: String): ApiResponse<Long> {
            return ApiResponse(
                code = 1000,
                message = null.toString(),
                result = 1L // Sử dụng 1L để biểu diễn kiểu Long
            )
        }

        override suspend fun getSummaryOfReactionInPost(postId: String): ApiResponse<Map<ReactionType, Long>> {
            throw NotImplementedError()
        }

        override suspend fun getSummaryOfReactionInComment(commentId: String): ApiResponse<Map<ReactionType, Long>> {
            throw NotImplementedError()
        }

        override suspend fun getUserReaction(
            postId: String,
            profileId: String
        ): ApiResponse<ReactionType> {
            val isLiked = likedPosts[postId] ?: false
            return ApiResponse(code = 1000, message = null.toString(), result = if (isLiked) ReactionType.LIKE else ReactionType.NONE)
        }

        override suspend fun getUserReactionForComment(
            commentId: String,
            profileId: String
        ): ApiResponse<ReactionType> {
            throw NotImplementedError()
        }

        override suspend fun removeReactionInPost(postId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun removeReactionInComment(commentId: String): ApiResponse<String> {
            throw NotImplementedError()
        }

        override suspend fun removeProfileReaction(profileId: String): ApiResponse<String> {
            throw NotImplementedError()
        }
    },
    tokenManager = tokenManager
) {
    override suspend fun getAllPosts(page: Int, size: Int): Result<PageResponse<PostResponse>> {
        return Result.success(
            PageResponse(
                data = MockData.mockPosts.map { it.post },
                currentPage = 1,
                pageSize = 10,
                totalPages = 1,
                totalElements = MockData.mockPosts.size.toLong()
            )
        )
    }

    override suspend fun getUserInfo(profileIds: List<String>): Result<Map<String, UsernameAndAvatar>> {
        return Result.success(
            MockData.mockPosts.associate { post ->
                post.post.profileId to UsernameAndAvatar(post.username, post.avatarUrl)
            }
        )
    }

    override suspend fun getTymCount(postId: String): Result<Int> {
        return Result.success(5385) // Giá trị mock
    }

    override suspend fun getCommentCount(postId: String): Result<Int> {
        return Result.success(19) // Giá trị mock
    }

    override suspend fun getUserReaction(postId: String, profileId: String): Result<Boolean> {
        return Result.success(false) // Giả lập trạng thái chưa thích
    }
}