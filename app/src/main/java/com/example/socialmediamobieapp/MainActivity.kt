package com.example.socialmediamobieapp

import android.R
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.LocalImageLoader
import com.example.socialmediamobieapp.network.RetrofitInstance
import com.example.socialmediamobieapp.repository.PostRepository
import com.example.socialmediamobieapp.ui.*
import com.example.socialmediamobieapp.ui.theme.SocialMediaMobieAppTheme
import com.example.socialmediamobieapp.utils.JwtUtils
import com.example.socialmediamobieapp.utils.TokenManager
import com.example.socialmediamobieapp.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenManager = TokenManager(this)
        RetrofitInstance.init(tokenManager)

        val postRepository = PostRepository(
            apiService = RetrofitInstance.api,
            tokenManager = tokenManager
        )

        val homeViewModel: HomeViewModel by viewModels { HomeViewModel.Factory(tokenManager) }

        val imageLoader = ImageLoader.Builder(this)
            .error(R.drawable.ic_menu_gallery)
            .build()

        setContent {
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                SocialMediaMobieAppTheme {
                    var isTokenRefreshed by remember { mutableStateOf(false) }
                    var initialScreen by remember { mutableStateOf("login") }

                    LaunchedEffect(Unit) {
                        try {
                            val currentToken = tokenManager.getAccessToken()
                            println("Current token: $currentToken")
                            if (currentToken != null) {
                                val response = withContext(Dispatchers.IO) {
                                    RetrofitInstance.api.refreshToken(currentToken)
                                }
                                println("Refresh token response: code=${response.code}, result=${response.result}")
                                if (response.code == 1000 && response.result != null) {
                                    tokenManager.saveAccessToken(response.result.token)
                                    println("New token saved: ${tokenManager.getAccessToken()}")
                                    initialScreen = "home"
                                } else {
                                    println("Refresh token failed: ${response.message}")
                                    tokenManager.clear()
                                    initialScreen = "login"
                                }
                            } else {
                                println("No current token found")
                                initialScreen = "login"
                            }
                        } catch (e: Exception) {
                            println("Refresh token error: ${e.message}")
                            tokenManager.clear()
                            initialScreen = "login"
                        } finally {
                            isTokenRefreshed = true
                        }
                    }

                    if (isTokenRefreshed) {
                        AppNavigation(
                            context = this@MainActivity,
                            tokenManager = tokenManager,
                            postRepository = postRepository,
                            homeViewModel = homeViewModel,
                            startDestination = initialScreen
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    context: Context,
    tokenManager: TokenManager,
    postRepository: PostRepository,
    homeViewModel: HomeViewModel,
    startDestination: String
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Chỉ hiển thị BottomNavigationBar cho home và search
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == "home" || currentRoute == "search") {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    tokenManager = tokenManager,
                    onLoginSuccess = { token ->
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                tokenManager.saveAccessToken(token)
                                val profiles = withContext(Dispatchers.IO) {
                                    RetrofitInstance.api.getMyProfiles()
                                }
                                val firstProfile = profiles.result?.firstOrNull()
                                    ?: throw Exception("No profiles found")
                                withContext(Dispatchers.IO) {
                                    RetrofitInstance.api.setActiveProfile(firstProfile.profileId)
                                }
                                tokenManager.saveProfileId(firstProfile.profileId)
                                // Kiểm tra vai trò admin
                                val isAdmin = JwtUtils.isAdmin(token)
                                val destination = if (isAdmin) "admin_home" else "home"
                                navController.navigate(destination) {
                                    popUpTo("login") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                tokenManager.clear()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate("signup")
                    }
                )
            }
            composable("signup") {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate("login")
                    },
                    onLoginClick = {
                        navController.navigate("login")
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    postRepository = postRepository,
                    tokenManager = tokenManager,
                    navController = navController
                )
            }
            composable("search") {
                SearchScreen(navController = navController)
            }
            composable("comment/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                CommentScreen(
                    postId = postId,
                    postRepository = postRepository,
                    tokenManager = tokenManager,
                    navController = navController
                )
            }
            composable("donation/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                DonationScreen(
                    postId = postId,
                    tokenManager = tokenManager,
                    navController = navController,
                    onDonate = { request, onResult ->
                        homeViewModel.donate(request, onResult)
                    }
                )
            }
            composable("profile/{profileId}") { backStackEntry ->
                val profileId = backStackEntry.arguments?.getString("profileId") ?: "self"
                val actualProfileId = if (profileId == "self") tokenManager.getProfileId() ?: "" else profileId
                ProfileScreen(
                    profileId = actualProfileId,
                    tokenManager = tokenManager,
                    postRepository = postRepository,
                    navController = navController,
                    viewModel = homeViewModel
                )
            }

            composable("userPosts/{profileId}/{postType}") { backStackEntry ->
                val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
                val postType = backStackEntry.arguments?.getString("postType") ?: "posts"
                UserPostsScreen(
                    profileId = profileId,
                    postType = postType,
                    tokenManager = tokenManager,
                    postRepository = postRepository,
                    viewModel = homeViewModel,
                    navController = navController
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    tokenManager = tokenManager,
                    navController = navController
                )
            }
            composable("report/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                ReportScreen(
                    postId = postId,
                    tokenManager = tokenManager,
                    navController = navController
                )
            }
            composable("donation_stats/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                DonationStatsScreen(
                    postId = postId,
                    postRepository = postRepository,
                    navController = navController
                )
            }
            composable("admin_home") {
                AdminHomeScreen(
                    viewModel = homeViewModel,
                    postRepository = postRepository,
                    tokenManager = tokenManager,
                    navController = navController
                )
            }
        }
    }
}