package com.example.appcafe.vistaUI

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.upsjb.sesion07.navegacion.ManejadorNav
import com.example.appcafe.navegacion.BarraInferior



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(esAdmin: Boolean,onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BarraInferior(
                navControlador = navController,
                esAdmin = esAdmin
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ManejadorNav(
                navControlador = navController,
                onLogout = onLogout,
                esAdmin = esAdmin,
            )
        }
    }
}