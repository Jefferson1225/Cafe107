package com.example.appcafe.navegacion

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appcafe.db.Usuario
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.upsjb.sesion07.navegacion.PantallasNav
import kotlinx.coroutines.tasks.await

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
    PantallasNav.ItemPedidosPreparar,
    PantallasNav.ItemPerfil
)

// Lista de pantallas para repartidores
val listaPantallasNavRepartidor = listOf(
    PantallasNav.ItemInicio,
    PantallasNav.ItemVerYAceptarPedidos,
    PantallasNav.ItemPerfil
)

@Composable
fun BarraInferior(
    navControlador: NavHostController,
    usuario: Usuario? = null
) {
    val firestore = Firebase.firestore

    val rolState = produceState(initialValue = Triple<Boolean, Boolean, Boolean>(false, false, false), usuario) {
        if (usuario != null) {
            val esAdmin = usuario.esAdmin
            val repartidorSnap = firestore.collection("repartidores")
                .document(usuario.id)
                .get()
                .await()

            val esRepartidor = repartidorSnap.exists()
            val esUsuarioNormal = !esAdmin && !esRepartidor
            value = Triple(esAdmin, esRepartidor, esUsuarioNormal)
        }
    }

    val (esAdmin, esRepartidor, esUsuarioNormal) = rolState.value

    val listaPantallas = when {
        esAdmin -> listaPantallasNavAdmin
        esRepartidor -> listaPantallasNavRepartidor
        else -> listaPantallasNavUsuario
    }

    NavigationBar(
        modifier = Modifier,
        containerColor = Color(0xffA3A380)
    ) {
        val seleccion = navControlador.currentBackStackEntryAsState().value?.destination?.route

        listaPantallas.forEach { pantalla ->
            NavigationBarItem(
                selected = seleccion == pantalla.ruta,
                onClick = {
                    navControlador.navigate(pantalla.ruta) {
                        popUpTo(navControlador.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = pantalla.icono,
                        contentDescription = pantalla.titulo,
                        tint = pantalla.colorIcon
                    )
                },
                label = {
                    Text(
                        text = pantalla.titulo,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFD68C45),
                    selectedTextColor = Color(0xFF6F4E37),
                    unselectedTextColor = Color(0xFF6F4E37)
                )
            )
        }
    }
}
