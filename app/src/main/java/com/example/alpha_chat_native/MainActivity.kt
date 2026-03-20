package com.example.alpha_chat_native

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.example.alpha_chat_native.ui.theme.AlphaChatNativeTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.alpha_chat_native.Presentation.Navigation.Routes
import com.example.alpha_chat_native.ui.screens.*
import timber.log.Timber

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Timber.d("FCM Notification Permission Granted")
        } else {
            Timber.w("FCM Notification Permission Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Timber.e(e, "Failed to enable edge to edge")
        }

        askNotificationPermission()

        setContent {
            AlphaChatNativeTheme {
                Surface(modifier = Modifier) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = Routes.SplashScreen) {
                        composable<Routes.SplashScreen> {
                            SplashScreen(
                                onNavigateToNext = { isLoggedIn ->
                                    if (isLoggedIn) {
                                        nav.navigate(Routes.HomeScreen) {
                                            popUpTo(Routes.SplashScreen) { inclusive = true }
                                        }
                                    } else {
                                        // Go directly to LoginScreen, skip redundant WelcomeScreen
                                        nav.navigate(Routes.LOGIN) {
                                            popUpTo(Routes.SplashScreen) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable<Routes.UserRegistrationScreen> {
                            UserRegistrationScreen(
                                onRegisterSuccess = {
                                    nav.navigate(Routes.HomeScreen) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    nav.navigate(Routes.LOGIN)
                                }
                            )
                        }
                        composable<Routes.LOGIN> {
                            LoginScreen(
                                onLoginSuccess = {
                                    nav.navigate(Routes.HomeScreen) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable<Routes.HomeScreen> {
                            HomeScreen(
                                onConversationClick = { chatId ->
                                    nav.navigate(Routes.CHAT(chatId))
                                },
                                onNewChatClick = {
                                    nav.navigate(Routes.SelectUserScreen)
                                },
                                onLogout = {
                                    nav.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.HomeScreen) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable<Routes.SelectUserScreen> {
                            SelectUserScreen(
                                onUserSelected = { chatId ->
                                    nav.navigate(Routes.CHAT(chatId)) {
                                        popUpTo(Routes.SelectUserScreen) { inclusive = true }
                                    }
                                },
                                onBack = {
                                    nav.popBackStack()
                                }
                            )
                        }
                        composable<Routes.CHAT> { backStackEntry ->
                            val chatRoute: Routes.CHAT = backStackEntry.toRoute()
                            ChatScreen(
                                chatId = chatRoute.chatId,
                                onBack = {
                                    nav.popBackStack()
                                }
                            )
                        }
                        // ProfileScreen removed as per request
                        /*
                        composable<Routes.ProfileScreen> {
                             ...
                        }
                        */
                        composable<Routes.CommunityScreen> {
                            CommunityScreen()
                        }
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // FCM SDK (and your app) can post notifications.
                    Timber.d("FCM Notification Permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // User denied previously, show education UI and then standard dialog if they agree (Skipping education UI for brevity)
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
