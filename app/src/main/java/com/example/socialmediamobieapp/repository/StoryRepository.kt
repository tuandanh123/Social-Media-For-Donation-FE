package com.example.socialmediamobieapp.repository

import com.example.socialmediamobieapp.enums.ReactionType
import com.example.socialmediamobieapp.model.MockData
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

open class StoryRepository(private val apiService: ApiService) {

    // Cache để lưu trữ kết quả của getProfile
    private val profileCache = mutableMapOf<String, Result<Story>>()

    open suspend fun getMyStory(profileId: String, retries: Int = 2): Result<Story> {
        // Kiểm tra cache trước
        profileCache[profileId]?.let { return it }

        return withContext(Dispatchers.IO) {
            repeat(retries) { attempt ->
                try {
                    println("Calling getProfile with profileId: $profileId, attempt: ${attempt + 1}")
                    val response = apiService.getProfile(profileId)
                    println("Response from getProfile: $response")
                    if (response.code == 1000 && response.result != null) {
                        val story = Story(
                            username = response.result.username,
                            avatarUrl = response.result.avatarUrl,
                            mediaUrl = response.result.avatarUrl
                        )
                        profileCache[profileId] = Result.success(story)
                        return@withContext Result.success(story)
                    } else {
                        val error = Exception(response.message ?: "Failed to load profile")
                        profileCache[profileId] = Result.failure(error)
                        return@withContext Result.failure(error)
                    }
                } catch (e: Exception) {
                    println("Error in getProfile: ${e.message}")
                    profileCache[profileId] = Result.failure(e)
                    if (attempt == retries - 1) return@withContext Result.failure(e)
                    delay(1000L) // Delay 1 giây trước khi thử lại
                }
            }
            Result.failure(Exception("Failed to load profile after $retries attempts"))
        }
    }

    open suspend fun getAllStories(): Result<List<Story>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllProfiles()
                if (response.code == 1000 && response.result != null) {
                    val stories = mutableListOf<Story>()
                    response.result.forEachIndexed { index, profile ->
                        if (index > 0) delay(500L) // Delay 500ms giữa các lần gọi (nếu cần gọi getProfile)
                        // Nếu getAllProfiles đã trả về đầy đủ thông tin, không cần gọi getProfile nữa
                        val story = Story(
                            username = profile.username,
                            avatarUrl = profile.avatarUrl,
                            mediaUrl = profile.avatarUrl
                        )
                        stories.add(story)
                    }
                    Result.success(stories)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load profiles"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Xóa cache nếu cần
    fun clearCache() {
        profileCache.clear()
    }
}

class MockStoryRepository : StoryRepository(apiService = object : ApiService {
    override suspend fun getAllPosts(page: Int, size: Int): ApiResponse<PageResponse<PostResponse>> {
        throw NotImplementedError()
    }

    override suspend fun getProfile(profileId: String): ApiResponse<ProfileResponse> {
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

    override suspend fun getAllProfiles(): ApiResponse<List<ProfileResponse>> {
        throw NotImplementedError()
    }

    override suspend fun getMyProfiles(): ApiResponse<List<ProfileResponse>> {
        throw NotImplementedError()
    }

    override suspend fun searchUsers(username: String): ApiResponse<List<Profile>> {
        TODO("Not yet implemented")
    }

    override suspend fun donate(request: DonationCreationRequest): ApiResponse<DonationResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun unsavePost(profileId: String, postId: String): ApiResponse<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getSavedPosts(profileId: String): ApiResponse<List<PostResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun savePost(savedPostRequest: SavedPostRequest): ApiResponse<SavedPostResponse> {
        TODO("Not yet implemented")
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
        throw NotImplementedError()
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

    override suspend fun reactToPost(reactionCreationRequest: ReactionCreationRequest): ApiResponse<ReactionResponse> {
        throw NotImplementedError()
    }

    override suspend fun reactToComment(reactionCreationRequest: ReactionCreationRequest): ApiResponse<ReactionResponse> {
        throw NotImplementedError()
    }

    override suspend fun getReactionsByPost(
        postId: String,
        page: Int,
        size: Int
    ): ApiResponse<PageResponse<ReactionResponse>> {
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
        throw NotImplementedError()
    }

    override suspend fun countReactionsByComment(commentId: String): ApiResponse<Long> {
        throw NotImplementedError()
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
        throw NotImplementedError()
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

    override suspend fun getActiveProfile(userId: String): ApiResponse<String> {
        throw NotImplementedError()
    }

    override suspend fun setActiveProfile(profileId: String): ApiResponse<String> {
        throw NotImplementedError()
    }
}) {
    override suspend fun getMyStory(profileId: String, retries: Int): Result<Story> {
        return Result.success(MockData.mockStory)
    }

    override suspend fun getAllStories(): Result<List<Story>> {
        return Result.success(MockData.mockUserStories)
    }
}