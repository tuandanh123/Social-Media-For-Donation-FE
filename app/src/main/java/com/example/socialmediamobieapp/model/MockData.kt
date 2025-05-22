package com.example.socialmediamobieapp.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.socialmediamobieapp.enums.Privacy
import com.example.socialmediamobieapp.model.dto.response.CommentResponse
import com.example.socialmediamobieapp.network.dto.response.PostResponse
import com.example.socialmediamobieapp.network.dto.response.Story
import java.time.LocalDateTime

object MockData {

    object MockData {
        // Các dữ liệu hiện có...

        @RequiresApi(Build.VERSION_CODES.O)
        val mockComments = listOf(
            CommentResponse(
                id = "comment_1",
                postId = "post_123",
                profileId = "profile_1",
                content = "Trà lồi",
                createdAt = "",
                updatedAt = null,
                parentId = "",
                fileIds = listOf(),
            ),
            CommentResponse(
                id = "comment_2",
                postId = "post_123",
                profileId = "profile_2",
                content = "căn mà này lên tuter mét cúp và in4 mi già =)))) xinh dcd",
                createdAt = "",
                updatedAt = null,
                parentId = "",
                fileIds = listOf()
            ),
            CommentResponse(
                id = "comment_3",
                postId = "post_123",
                profileId = "profile_3",
                content = "Kiếu chồng hồng, toe tét cùng vài đooc",
                createdAt = "",
                updatedAt = null,
                parentId = "",
                fileIds = listOf()
            )
        )
    }

    val mockStory = Story(
        username = "MyUser",
        avatarUrl = "https://picsum.photos/40",
        mediaUrl = "https://picsum.photos/40"
    )

    val mockUserStories = listOf(
        Story(
            username = "User1",
            avatarUrl = "https://picsum.photos/40",
            mediaUrl = "https://picsum.photos/40"
        ),
        Story(
            username = "User2",
            avatarUrl = "https://picsum.photos/40",
            mediaUrl = "https://picsum.photos/40"
        )
    )

    val mockPostWithUser = PostWithUser(
        post = PostResponse(
            id = "post_123",
            content = "This is a sample post",
            profileId = "user_456",
            createdAt = "2 hours ago",
            updatedAt = "1 hour ago",
            fileIds = listOf("https://picsum.photos/300"),
            tags = listOf("tag1", "tag2"),
            privacy = Privacy.PUBLIC,
            point = Point(5.6, 2.3),
            donationStartTime = null,
            donationEndTime = null
        ),
        username = "Minh Thu",
        avatarUrl = "https://picsum.photos/40",
        tymCount = 5385,
        commentCount = 19,
        shareCount = 323
    )

    val mockPosts = listOf(mockPostWithUser, mockPostWithUser.copy(post = mockPostWithUser.post.copy(id = "post_124")))
}