package com.example.socialmediamobieapp.network

import com.example.socialmediamobieapp.enums.ReactionType
import com.example.socialmediamobieapp.model.Notification
import com.example.socialmediamobieapp.model.Report
import com.example.socialmediamobieapp.model.dto.request.DonationCreationRequest
import com.example.socialmediamobieapp.model.dto.request.FollowRequest
import com.example.socialmediamobieapp.model.dto.request.LogoutRequest
import com.example.socialmediamobieapp.model.dto.request.ReactionCreationRequest
import com.example.socialmediamobieapp.model.dto.request.ReportRequest
import com.example.socialmediamobieapp.model.dto.request.SavedPostRequest
import com.example.socialmediamobieapp.model.dto.response.AuthenticationResponse
import com.example.socialmediamobieapp.model.dto.response.CommentResponse
import com.example.socialmediamobieapp.model.dto.response.DonationResponse
import com.example.socialmediamobieapp.model.dto.response.FollowResponse
import com.example.socialmediamobieapp.model.dto.response.SavedPostResponse
import com.example.socialmediamobieapp.model.dto.response.UserResponse
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
import com.example.socialmediamobieapp.network.dto.response.SignUpResponse
import com.example.socialmediamobieapp.network.dto.response.Story
import com.example.socialmediamobieapp.network.dto.response.UsernameAndAvatar
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @GET("post/postUsers/getAllReport")
    suspend fun getAllReports():ApiResponse<List<Report>>

    @DELETE("post/postUsers/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String
    ):ApiResponse<String>

    @GET("identity/users")
    suspend fun getUsers():ApiResponse<List<UserResponse>>

    @DELETE("identity/users/{userId}")
    suspend fun  deleteUser(
        @Path("userId") userId: String
    ):ApiResponse<String>

    @GET("donation/userDonation/ofPost/{postId}")
    suspend fun getAllDonationOfPost(
        @Path("postId") postId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PageResponse<DonationResponse>>

    @POST("identity/auth/logout")
    suspend fun logout(
        @Body logoutRequest: LogoutRequest
    ):ApiResponse<Void>

    @POST("post/postUsers/report")
    suspend fun reportPort(
        @Body reportRequest: ReportRequest
    ): ApiResponse<String>

    @GET("notification/polling/{userId}")
    suspend fun getNotifications(
        @Path("userId") userId:String
    ) : ApiResponse<List<Notification>>


    @POST("profile/follow")
    suspend fun followUser(
        @Body followRequest: FollowRequest
    ): ApiResponse<FollowResponse>

    @DELETE("profile/follow/{followingId}/unfollow")
    suspend fun unfollowUser(
        @Path("followingId") followingId:String
    ): ApiResponse<String>

    @GET("profile/follow/followers/{profileId}")
    suspend fun getFollowers(
        @Path("profileId") profileId:String
    ) : ApiResponse<List<Profile>>

    @GET("profile/follow/{profileId}")
    suspend fun getFollowing(
        @Path("profileId") profileId:String
    ) : ApiResponse<List<Profile>>

    @GET("profile/userProfiles/searchProfile")
    suspend fun searchUsers(
        @Query("username") username:String
    ) : ApiResponse<List<Profile>>

    @POST("donation/userDonation")
    suspend fun donate(
        @Body request: DonationCreationRequest
    ): ApiResponse<DonationResponse>

    @DELETE("post/postUsers/unsavePost")
    suspend fun unsavePost(
        @Query("profileId") profileId: String,
        @Query("postId") postId: String
    ): ApiResponse<String>

    @GET("post/postUsers/getSavedPost/{profileId}")
    suspend fun getSavedPosts(
        @Path("profileId") profileId: String
    ): ApiResponse<List<PostResponse>>

    @POST("post/postUsers/savePost")
    suspend fun savePost(
        @Body savedPostRequest: SavedPostRequest
    ): ApiResponse<SavedPostResponse>

    @Multipart
    @POST("post/comments")
    suspend fun createComment(
        @Part commentCreationRequest: MultipartBody.Part,  // CommentCreationRequest
        @Part files: List<MultipartBody.Part>?  // Danh sách file media (có thể null)
    ): ApiResponse<CommentResponse>

    @Multipart
    @PUT("comments/{commentId}")
    suspend fun updateComment(
        @Part commentUpdateRequest: MultipartBody.Part,  // CommentUpdateRequest
        @Part files: List<MultipartBody.Part>?,  // Danh sách file media (có thể null)
        @Query("filesToRemove") filesToRemove: List<String>?,  // Danh sách file cần xóa
        @Path("commentId") commentId: String
    ): ApiResponse<CommentResponse>

    @DELETE("post/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: String
    ): ApiResponse<String>

    @DELETE("post/comments/reply/{commentId}")
    suspend fun deleteReply(
        @Path("commentId") commentId: String
    ): ApiResponse<String>

    @GET("post/comments/inPost/{postId}")
    suspend fun getCommentsByPost(
        @Path("postId") postId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<CommentResponse>>

    @GET("post/comments/inComment/{commentId}")
    suspend fun getReplies(
        @Path("commentId") commentId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<CommentResponse>>

    @GET("post/comments/countReplies/{commentId}")
    suspend fun countReplies(
        @Path("commentId") commentId: String
    ): ApiResponse<Long>

    @GET("post/comments/countComments/{postId}")
    suspend fun countComments(
        @Path("postId") postId: String
    ): ApiResponse<Long>

    @GET("post/comments/oneComment/{commentId}")
    suspend fun getOneComment(
        @Path("commentId") commentId: String
    ): ApiResponse<CommentResponse>

    @GET("post/comments/userComments/{profileId}")
    suspend fun getUserComments(
        @Path("profileId") profileId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<CommentResponse>>

    @DELETE("post/comments/inPost/{postId}")
    suspend fun deleteAllCommentsInPost(
        @Path("postId") postId: String
    ): ApiResponse<String>

    @DELETE("post/comments/ofProfile/{profileId}")
    suspend fun deleteAllCommentsInProfile(
        @Path("profileId") profileId: String
    ): ApiResponse<String>

    // --- Reaction API ---

    @POST("post/reactions/reactToPost")
    suspend fun reactToPost(
        @Body reactionCreationRequest: ReactionCreationRequest
    ): ApiResponse<ReactionResponse>

    @POST("post/reactions/reactToComment")
    suspend fun reactToComment(
        @Body reactionCreationRequest: ReactionCreationRequest
    ): ApiResponse<ReactionResponse>

    @GET("post/reactions/inPost/{postId}")
    suspend fun getReactionsByPost(
        @Path("postId") postId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<ReactionResponse>>

    @GET("post/reactions/inComment/{commentId}")
    suspend fun getReactionsByComment(
        @Path("commentId") commentId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<ReactionResponse>>

    @GET("post/reactions/counts/inPost/{postId}")
    suspend fun countReactionsByPost(
        @Path("postId") postId: String
    ): ApiResponse<Long>

    @GET("post/reactions/counts/inComment/{commentId}")
    suspend fun countReactionsByComment(
        @Path("commentId") commentId: String
    ): ApiResponse<Long>

    @GET("post/reactions/summary/inPost/{postId}")
    suspend fun getSummaryOfReactionInPost(
        @Path("postId") postId: String
    ): ApiResponse<Map<ReactionType, Long>>

    @GET("post/reactions/summary/inComment/{commentId}")
    suspend fun getSummaryOfReactionInComment(
        @Path("commentId") commentId: String
    ): ApiResponse<Map<ReactionType, Long>>

    @GET("post/reactions/user/inPost/{postId}/{profileId}")
    suspend fun getUserReaction(
        @Path("postId") postId: String,
        @Path("profileId") profileId: String
    ): ApiResponse<ReactionType>

    @GET("post/reactions/user/inComment/{commentId}/{profileId}")
    suspend fun getUserReactionForComment(
        @Path("commentId") commentId: String,
        @Path("profileId") profileId: String
    ): ApiResponse<ReactionType>

    @DELETE("post/reactions/{postId}")
    suspend fun removeReactionInPost(
        @Path("postId") postId: String
    ): ApiResponse<String>

    @DELETE("post/reactions/{commentId}")
    suspend fun removeReactionInComment(
        @Path("commentId") commentId: String
    ): ApiResponse<String>

    @DELETE("post/reactions/{profileId}")
    suspend fun removeProfileReaction(
        @Path("profileId") profileId: String
    ): ApiResponse<String>


    @GET("profile/userProfiles/get-active-profile/{userId}")
    suspend fun getActiveProfile(
        @Path("userId") userId: String
    ): ApiResponse<String>

    @POST("profile/userProfiles/set-active-profile")
    suspend fun setActiveProfile(@Query("profileId") profileId: String): ApiResponse<String>

    @GET("profile/userProfiles")
    suspend fun getAllProfiles(): ApiResponse<List<ProfileResponse>>

    @GET("profile/userProfiles/myProfiles")
    suspend fun getMyProfiles(): ApiResponse<List<ProfileResponse>>

    @GET("profile/userProfiles/{profileId}")
    suspend fun getProfile(
        @Path("profileId") profileId: String
    ): ApiResponse<ProfileResponse>

    @POST("identity/auth/token")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @POST("identity/users/registration")
    suspend fun signUp(@Body request: SignUpRequest): SignUpApiResponse


    @POST("identity/auth/refresh")
    suspend fun refreshToken(@Body request: String): ApiResponse<AuthenticationResponse>

    @GET("post/postUsers/getAllPosts")
    suspend fun getAllPosts(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<PostResponse>>
}