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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                                        nav.navigate(Routes.WelcomeScreen) {
                                            popUpTo(Routes.SplashScreen) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable<Routes.WelcomeScreen> {
                            WelcomeScreen(
                                onNavigateToLogin = {
                                    nav.navigate(Routes.LOGIN)
                                },
                                onNavigateToRegister = {
                                    nav.navigate(Routes.UserRegistrationScreen)
                                }
                            )
                        }
                        composable<Routes.UserRegistrationScreen> {
                            UserRegistrationScreen(
                                onRegisterSuccess = {
                                    nav.navigate(Routes.HomeScreen) {
                                        popUpTo(Routes.WelcomeScreen) { inclusive = true }
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
                                },
                                onSignUpClick = {
                                    nav.navigate(Routes.UserRegistrationScreen)
                                }
                            )
                        }
                        composable<Routes.HomeScreen> {
                            HomeScreen(
                                onChatClick = { chatId ->
                                    nav.navigate(Routes.CHAT(chatId))
                                },
                                onNavigateToProfile = {
                                    nav.navigate(Routes.ProfileScreen)
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
                        composable<Routes.ProfileScreen> {
                            ProfileScreen(
                                onNavigateHome = {
                                    nav.navigate(Routes.HomeScreen) {
                                        popUpTo(Routes.ProfileScreen) { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    nav.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.HomeScreen) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
