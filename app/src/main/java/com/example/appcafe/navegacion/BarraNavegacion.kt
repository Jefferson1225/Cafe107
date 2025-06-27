package com.example.appcafe.navegacion

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.upsjb.sesion07.navegacion.PantallasNav

// Lista de pantallas para usuarios normales
val listaPantallasNavUsuario = listOf(
    PantallasNav.ItemInicio,
    PantallasNav.ItemPedidos,
    PantallasNav.ItemCarrito,
    PantallasNav.ItemPerfil
)

// Lista de pantallas para admin
val listaPantallasNavAdmin = listOf(
    PantallasNav.ItemInicio,
    PantallasNav.ItemAgregarProducto,
    PantallasNav.ItemAgregarDelivery,
    PantallasNav.ItemPerfil
)

@Composable
fun BarraInferior(navControlador:NavHostController,
                  esAdmin: Boolean = false) {
    val listaPantallas = if (esAdmin) listaPantallasNavAdmin else listaPantallasNavUsuario

    NavigationBar(
        modifier = Modifier,
        containerColor = Color(0xffA3A380)
    ) {
        listaPantallas.forEach{ pantalla ->
            val seleccion=navControlador
                .currentBackStackEntryAsState().value?.destination?.route
            NavigationBarItem(
                selected = seleccion==pantalla.ruta,
                onClick = {navControlador.navigate(pantalla.ruta)},
                icon = {
                    Icon(imageVector = pantalla.icono,
                        contentDescription = pantalla.titulo,
                        tint = pantalla.colorIcon
                    )
                },
                label = { Text(text = pantalla.titulo)},
                colors = NavigationBarItemDefaults.colors(
                    
                    indicatorColor = Color(0xFFD68C45),
                    selectedTextColor = Color(0xFF6F4E37),
                    unselectedTextColor = Color(0xFF6F4E37)

                )
            )
        }
    }
}