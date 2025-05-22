package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import com.example.socialmediamobieapp.viewmodel.HomeViewModel

@Composable
fun InstagramTopBar(
    navController: NavController? = null,
    viewModel: HomeViewModel? = null
) {
    val hasUnreadNotifications by viewModel?.hasUnreadNotifications?.collectAsState() ?: remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Saver",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Cursive
        )
        Row {
            IconButton(onClick = {
                navController?.navigate("notifications") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                Box {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Notifications",
                        tint = Color.Black
                    )
                    if (hasUnreadNotifications) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red, shape = CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
            IconButton(onClick = { /* Xử lý tin nhắn */ }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Messages",
                    tint = Color.Black
                )
            }
        }
    }
}

