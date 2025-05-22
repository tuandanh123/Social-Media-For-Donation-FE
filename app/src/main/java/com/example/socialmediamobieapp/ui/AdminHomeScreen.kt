//package com.example.socialmediamobieapp.ui
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.example.socialmediamobieapp.repository.PostRepository
//import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
//import com.example.socialmediamobieapp.utils.TokenManager
//import com.example.socialmediamobieapp.viewmodel.HomeViewModel
//
//@Composable
//fun AdminHomeScreen(
//    viewModel: HomeViewModel,
//    postRepository: PostRepository,
//    tokenManager: TokenManager,
//    navController: NavController
//) {
//    val adminNavController = rememberNavController()
//    val currentRoute = adminNavController.currentBackStackEntryAsState().value?.destination?.route
//
//    SocialMediaMobieAppTheme {
//        Scaffold(
//            topBar = { InstagramTopBar(navController = navController, viewModel = viewModel) },
//            bottomBar = {
//                BottomNavigationBarAdmin(
//                    currentRoute = currentRoute,
//                    onNavigate = { route -> adminNavController.navigate(route) {
//                        popUpTo(adminNavController.graph.startDestinationId) { inclusive = true }
//                        launchSingleTop = true
//                    } }
//                )
//            }
//        ) { paddingValues ->
//            NavHost(
//                navController = adminNavController,
//                startDestination = "admin_stats",
//                modifier = Modifier.padding(paddingValues)
//            ) {
//                composable("admin_stats") {
//                    AdminDonationStatsScreen(
//                        postRepository = postRepository,
//                        navController = navController
//                    )
//                }
//                composable("admin_users") {
//                    AdminUsersScreen(navController = navController)
//                }
//                composable("admin_posts") {
//                    AdminPostsScreen(navController = navController)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BottomNavigationBarAdmin(
//    currentRoute: String?,
//    onNavigate: (String) -> Unit
//) {
//    BottomNavigation(
//        backgroundColor = MaterialTheme.colors.background,
//        contentColor = Color.Black
//    ) {
//        BottomNavigationItem(
//            icon = { Icon(Icons.Default.Insights, contentDescription = "Thống kê") },
//            label = { Text("Thống kê", fontSize = 12.sp) },
//            selected = currentRoute == "admin_stats",
//            onClick = { onNavigate("admin_stats") }
//        )
//        BottomNavigationItem(
//            icon = { Icon(Icons.Default.People, contentDescription = "Quản lý người dùng") },
//            label = { Text("Người dùng", fontSize = 12.sp) },
//            selected = currentRoute == "admin_users",
//            onClick = { onNavigate("admin_users") }
//        )
//        BottomNavigationItem(
//            icon = { Icon(Icons.Default.Article, contentDescription = "Quản lý bài viết") },
//            label = { Text("Bài viết", fontSize = 12.sp) },
//            selected = currentRoute == "admin_posts",
//            onClick = { onNavigate("admin_posts") }
//        )
//    }
//}
//
//@Composable
//fun AdminUsersScreen(navController: NavController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text("Quản lý người dùng", fontSize = 20.sp, fontWeight = FontWeight.Bold)
//        // TODO: Thêm logic quản lý người dùng
//    }
//}
//
//@Composable
//fun AdminPostsScreen(navController: NavController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text("Quản lý bài viết", fontSize = 20.sp, fontWeight = FontWeight.Bold)
//        // TODO: Thêm logic quản lý bài viết
//    }
//}