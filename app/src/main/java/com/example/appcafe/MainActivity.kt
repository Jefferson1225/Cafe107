package com.example.appcafe

import SplashUI
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.appcafe.db.Usuario
import com.example.appcafe.ui.theme.AppCafeTheme
import com.example.appcafe.vistaUI.*
import com.example.cafeteria.db.AuthService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authService = AuthService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCafeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
                    val scope = rememberCoroutineScope()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashUI { usuario ->
                                usuarioActual = usuario
                                navController.navigate(if (usuario != null) "main_app" else "login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }

                        composable("login") {
                            LoginUI(
                                navController = navController,
                                onLoginSuccess = {
                                    scope.launch {
                                        usuarioActual = authService.obtenerUsuarioActual()
                                        navController.navigate("main_app") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("registro") {
                            RegistroUI(navController = navController)
                        }

                        composable("main_app") {
                            usuarioActual?.let { usuario ->
                                MainAppScreen(
                                    usuario = usuario,
                                    onLogout = {
                                        authService.cerrarSesion()
                                        usuarioActual = null
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
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
}
