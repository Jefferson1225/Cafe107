package com.example.appcafe.vistaUI

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.upsjb.sesion07.navegacion.ManejadorNav
import com.example.appcafe.navegacion.BarraInferior
import com.example.appcafe.db.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    usuario: Usuario,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BarraInferior(
                navControlador = navController,
                usuario = usuario
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ManejadorNav(
                navControlador = navController,
                onLogout = onLogout,
                usuario = usuario
            )
        }
    }
}
