package com.example.socialmediamobieapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme

@Composable
fun BottomNavigationBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconButton(onClick = {
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }) {
            Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
        }
        IconButton(onClick = {
            navController.navigate("search") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
        }
        IconButton(onClick = { /* TODO: Xử lý Add */ }) {
            Icon(imageVector = Icons.Default.AddBox, contentDescription = "Add")
        }
        IconButton(onClick = { /* TODO: Xử lý Reels */ }) {
            Icon(imageVector = Icons.Default.PlayCircleOutline, contentDescription = "Reels")
        }
        IconButton(onClick = { navController.navigate("profile/self") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        } }) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
        }
    }
}

