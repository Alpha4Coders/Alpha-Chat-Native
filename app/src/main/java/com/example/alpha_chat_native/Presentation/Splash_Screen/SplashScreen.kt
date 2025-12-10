package com.example.alpha_chat_native.Presentation.Splash_Screen


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.alpha_chat_native.R
import com.example.alpha_chat_native.Presentation.Navigation.Routes
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navHostController: NavHostController)
{

    LaunchedEffect(Unit) {
        delay(1000)
        navHostController.navigate(Routes.WelcomeScreen)
        {
            popUpTo<Routes.SplashScreen>
            {
                inclusive = true
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize())
    {
        Image(painter = painterResource(id = R.drawable.logo),
            contentDescription = null ,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.Center))

        Column (modifier = Modifier.align(Alignment.BottomCenter), horizontalAlignment = Alignment.CenterHorizontally){
            Text(text = "From" , fontSize = 18.sp , fontWeight = FontWeight.SemiBold)

            Row{

                Icon(painter =painterResource(id = R.drawable.logo) ,
                    contentDescription= null ,
                    modifier= Modifier.size(24.dp),
                    tint = colorResource(id = R.color.light_Green)
                )
                Text(text = "Meta" , fontSize = 18.sp , fontWeight = FontWeight.SemiBold)
            }
        }
    }

}