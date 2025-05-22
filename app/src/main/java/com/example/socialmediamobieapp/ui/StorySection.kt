package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.socialmediamobieapp.model.MockData
import com.example.socialmediamobieapp.network.dto.response.Story
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme

@Composable
fun StoriesSection(
    myStory: Story?,
    userStories: List<Story>
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Box chứa cả vòng tròn story và button
                Box(
                    modifier = Modifier.size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Vòng tròn story
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFbc1888), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (myStory != null && !myStory.avatarUrl.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = myStory.avatarUrl,
                                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                                ),
                                contentDescription = "Your Story",
                                modifier = Modifier.size(52.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                // Không cần nội dung bên trong vì không có ảnh
                            }
                        }
                    }
                    // Button "+" là vòng tròn nhỏ, nửa trong nửa ngoài
                    Box(
                        modifier = Modifier
                            .size(20.dp) // Kích thước button nhỏ
                            .align(Alignment.BottomEnd)
                            .offset(x = 1.dp, y = 1.dp) // Dịch chuyển để nửa ngoài vòng tròn story
                            .background(Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "Your Story",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        items(userStories) { user ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFFbc1888), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = user.avatarUrl,
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                        ),
                        contentDescription = "${user.username}'s story",
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = user.username,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StoriesSectionPreview() {
    SocialMediaMobieAppTheme {
        StoriesSection(
            myStory = MockData.mockStory,
            userStories = MockData.mockUserStories
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StoriesSectionEmptyMyStoryPreview() {
    SocialMediaMobieAppTheme {
        StoriesSection(
            myStory = null,
            userStories = MockData.mockUserStories
        )
    }
}