package com.example.appcafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appcafe.ui.theme.AppCafeTheme
import com.example.appcafe.vistaUI.LoginUI
import com.example.appcafe.vistaUI.RegistroUI
import com.example.appcafe.vistaUI.MainAppScreen
import com.example.cafeteria.db.AuthService
import dagger.hilt.android.AndroidEntryPoint


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

                    // Determinar pantalla inicial según estado de autenticación
                    val startDestination = if (authService.estaLogueado()) {
                        "main_app" // Cambié a main_app que contiene tu ManejadorNav
                    } else {
                        "login"
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("login") {
                            LoginUI(navController = navController)
                        }

                        composable("main_app") {
                            // Aquí usamos tu ManejadorNav
                            val esAdmin = authService.esAdmin()

                            MainAppScreen(
                                esAdmin = esAdmin,
                                onLogout = {
                                    authService.cerrarSesion()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }



                        composable("registro") {
                            RegistroUI(navController = navController)
                        }
                    }
                }
            }
        }
    }
}