package com.example.socialmediamobieapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialmediamobieapp.model.CommentWithUser
import com.example.socialmediamobieapp.model.CommentsState
import com.example.socialmediamobieapp.model.dto.response.CommentResponse
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentViewModel(
    private val postId: String,
    private val postRepository: PostRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _commentsState = MutableStateFlow<CommentsState>(CommentsState.Loading)
    val commentsState: StateFlow<CommentsState> get() = _commentsState

    init {
        loadComments()
    }

    fun createComment(content: String, parentId : String? = null) {
        viewModelScope.launch {
            try {
                // Gọi API tạo bình luận
                val result = postRepository.createComment(postId, content, parentId)

                // Kiểm tra kết quả từ Result
                when {
                    result.isSuccess -> {
                        val newComment = result.getOrThrow() // Lấy CommentResponse từ Result
                        val currentComments = (_commentsState.value as? CommentsState.Success)?.comments ?: emptyList()

                        // Lấy profileId của bình luận mới
                        val profileId = newComment.profileId

                        // Gọi API để lấy thông tin người dùng (username, avatarUrl)
                        val userInfoResult = postRepository.getUserInfo(listOf(profileId))
                        userInfoResult.fold(
                            onSuccess = { userInfoMap ->
                                val userInfo = userInfoMap[profileId] ?: throw Exception("User info not found")

                                // Gọi API để lấy số lượt thích
                                val likeCountResult = postRepository.getCommentLikeCount(newComment.id)
                                val likeCount = likeCountResult.getOrElse { 0 }

                                // Chuyển đổi CommentResponse thành CommentWithUser
                                val newCommentWithUser = CommentWithUser(
                                    comment = CommentResponse(
                                        id = newComment.id,
                                        content = newComment.content,
                                        createdAt = newComment.createdAt,
                                        profileId = newComment.profileId,
                                        postId = newComment.postId,
                                        parentId = newComment.parentId,
                                        fileIds = newComment.fileIds,
                                        updatedAt = newComment.updatedAt
                                    ),
                                    username = userInfo.username,
                                    avatarUrl = userInfo.avatarUrl,
                                    likeCount = likeCount
                                )

                                // Cập nhật danh sách bình luận
                                if (parentId != null) {
                                    // Nếu là bình luận trả lời, thêm vào replies của bình luận mẹ
                                    val updatedComments = addReplyToComment(currentComments, parentId, newCommentWithUser)
                                    _commentsState.value = CommentsState.Success(updatedComments)
                                } else {
                                    // Nếu là bình luận chính, thêm vào danh sách hiện tại (giữ logic cũ)
                                    _commentsState.value = CommentsState.Success(currentComments + newCommentWithUser)
                                }
                            },
                            onFailure = { error ->
                                _commentsState.value = CommentsState.Error("Không thể lấy thông tin người dùng: ${error.message}")
                            }
                        )
                    }
                    result.isFailure -> {
                        val exception = result.exceptionOrNull() ?: Exception("Unknown error")
                        _commentsState.value = CommentsState.Error("Không thể thêm bình luận: ${exception.message}")
                    }
                }
            } catch (e: Exception) {
                _commentsState.value = CommentsState.Error("Không thể thêm bình luận: ${e.message}")
            }


        }
    }

    // Hàm hỗ trợ thêm bình luận trả lời vào bình luận mẹ
    private fun addReplyToComment(
        comments: List<CommentWithUser>,
        parentId: String,
        reply: CommentWithUser
    ): List<CommentWithUser> {
        return comments.map { comment ->
            if (comment.comment.id == parentId) {
                comment.copy(replies = comment.replies + reply)
            } else {
                comment.copy(replies = addReplyToComment(comment.replies, parentId, reply))
            }
        }
    }

    fun loadComments(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _commentsState.value = CommentsState.Loading
            try {
                // Lấy danh sách bình luận
                val commentsResult = postRepository.getCommentsByPost(postId, page, size)
                commentsResult.fold(
                    onSuccess = { pageResponse ->
                        val comments = pageResponse.data
                        // Lấy profileIds từ danh sách bình luận
                        val profileIds = comments.map { it.profileId }.distinct()
                        // Lấy thông tin người dùng (username, avatarUrl)
                        val userInfoResult = postRepository.getUserInfo(profileIds)
                        userInfoResult.fold(
                            onSuccess = { userInfoMap ->
                                // Lấy số lượt thích cho mỗi bình luận
                                val commentsWithUser = comments.map { comment ->
                                    val userInfo = userInfoMap[comment.profileId] ?: throw Exception("User info not found")
                                    val likeCountResult = postRepository.getCommentLikeCount(comment.id)
                                    val likeCount = likeCountResult.getOrElse { 0 }
                                    CommentWithUser(
                                        comment = comment,
                                        username = userInfo.username,
                                        avatarUrl = userInfo.avatarUrl,
                                        likeCount = likeCount
                                    )
                                }
                                _commentsState.value = CommentsState.Success(commentsWithUser)
                            },
                            onFailure = { error ->
                                _commentsState.value = CommentsState.Error(error.message ?: "Failed to load user info")
                            }
                        )
                    },
                    onFailure = { error ->
                        _commentsState.value = CommentsState.Error(error.message ?: "Failed to load comments")
                    }
                )
            } catch (e: Exception) {
                _commentsState.value = CommentsState.Error(e.message ?: "An error occurred")
            }
        }
    }
}