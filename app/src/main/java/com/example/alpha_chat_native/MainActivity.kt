package com.example.alpha_chat_native

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.example.alpha_chat_native.ui.theme.AlphaChatNativeTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.alpha_chat_native.ui.nav.Routes
import com.example.alpha_chat_native.ui.screens.ChatScreen
import com.example.alpha_chat_native.ui.screens.LoginScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlphaChatNativeTheme {
                Surface(modifier = Modifier) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = Routes.LOGIN) {
                        composable(Routes.LOGIN) {
                            LoginScreen(onLogin = { nav.navigate(Routes.CHAT) })
                        }
                        composable(Routes.CHAT_LIST) {
                            ChatScreen(onChatClick = { nav.navigate(Routes.CHAT) })
                        }
                        composable(Routes.CHAT) {
                            ChatScreen()
                        }
                    }
                }
            }
        }
    }
}
