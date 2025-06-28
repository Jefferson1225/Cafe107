package com.upsjb.sesion07.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appcafe.Services.FavoritosService
import com.example.appcafe.SubsUI.DetallePedidosUI
import com.example.appcafe.db.Usuario
import com.example.appcafe.vistaUI.InicioUI
import com.example.appcafe.vistaUI.PedidosUI
import com.example.appcafe.vistaUI.PerfilUI
import com.example.appcafe.SubsUI.ProductoDetalleUI
import com.example.appcafe.SubsUI.DireccionesUI
import com.example.appcafe.SubsUI.FavoritosUI
import com.example.appcafe.vistaUI.admin.AgregarProductoUI
import com.example.appcafe.vistaUI.admin.AgregarRepartidorUI
import com.example.appcafe.vistaUI.admin.PedidosPreparar
import com.example.appcafe.vistaUI.repartidores.VerYAceptarPedidosUI
import com.example.appcare.vistaUI.CarritoUI
import com.example.cafeteria.db.AuthService
import com.example.cafeteria.db.TipoUsuario
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun ManejadorNav(
    navControlador: NavHostController,
    onLogout: () -> Unit,
    usuario: Usuario
) {
    val authService = remember { AuthService() }

    // ACTUALIZADO: Usar el método del AuthService para obtener el tipo de usuario
    val tipoUsuarioState = produceState(initialValue = TipoUsuario.NO_AUTENTICADO) {
        value = authService.obtenerTipoUsuario()
    }

    val tipoUsuario = tipoUsuarioState.value

    // Verificar si el usuario está autenticado
    if (tipoUsuario == TipoUsuario.NO_AUTENTICADO) {
        MostrarCargando("Cargando usuario...")
        return
    }

    NavHost(
        navController = navControlador,
        startDestination = PantallasNav.ItemInicio.ruta
    ) {
        // Pantalla de Inicio - Común para todos los tipos de usuario
        composable(PantallasNav.ItemInicio.ruta) {
            InicioUI(navControlador)
        }

        // RUTAS PARA CLIENTE (usuario normal)
        if (tipoUsuario == TipoUsuario.CLIENTE) {
            composable(PantallasNav.ItemPedidos.ruta) {
                PedidosUI(
                    userId = usuario.id,
                    onNavigateBack = { navControlador.popBackStack() },
                    onNavigateToDetalle = { pedidoId ->
                        navControlador.navigate("detalle_pedido/$pedidoId")
                    }
                )
            }

            composable("pedidos/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                PedidosUI(
                    userId = userId,
                    onNavigateBack = { navControlador.popBackStack() },
                    onNavigateToDetalle = { pedidoId ->
                        navControlador.navigate("detalle_pedido/$pedidoId")
                    }
                )
            }

            composable("detalle_pedido/{pedidoId}") { backStackEntry ->
                val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
                DetallePedidosUI(
                    pedidoId = pedidoId,
                    onNavigateBack = { navControlador.popBackStack() }
                )
            }

            composable(PantallasNav.ItemCarrito.ruta) {
                CarritoUI(
                    userId = usuario.id,
                    onNavigateBack = { navControlador.popBackStack() },
                    onNavigateToPedidos = {
                        navControlador.navigate("pedidos/${usuario.id}") {
                            popUpTo(PantallasNav.ItemInicio.ruta) { inclusive = false }
                        }
                    }
                )
            }
        }

        // RUTAS PARA ADMIN
        if (tipoUsuario == TipoUsuario.ADMIN) {
            composable(PantallasNav.ItemAgregarProducto.ruta) {
                AgregarProductoUI()
            }

            composable(PantallasNav.ItemAgregarDelivery.ruta) {
                AgregarRepartidorUI()
            }

            composable(PantallasNav.ItemPedidosPreparar.ruta) {
                PedidosPreparar(
                    onNavigateBack = { navControlador.popBackStack() }
                )
            }
        }

        // RUTAS PARA REPARTIDOR
        if (tipoUsuario == TipoUsuario.REPARTIDOR) {
            composable(PantallasNav.ItemVerYAceptarPedidos.ruta) {
                VerYAceptarPedidosUI(
                    repartidorId = usuario.id,
                    onNavigateBack = { navControlador.popBackStack() }
                )
            }
        }

        // PERFIL - Común para todos los tipos de usuario autenticados
        composable(PantallasNav.ItemPerfil.ruta) {
            PerfilUI(
                usuario = usuario,
                tipoUsuario = tipoUsuario,
                onLogout = {
                    authService.cerrarSesion()
                    onLogout()
                },
                onClickDirecciones = {
                    navControlador.navigate("direcciones")
                },
                onClickFavoritos = {
                    navControlador.navigate("favoritos")
                }
            )
        }

        // FAVORITOS - Solo para clientes
        if (tipoUsuario == TipoUsuario.CLIENTE) {
            composable("favoritos") {
                val favoritosService = remember { FavoritosService() }
                var recargarKey by remember { mutableStateOf(0) }

                val favoritosState = produceState(
                    initialValue = emptyList(),
                    key1 = recargarKey,
                    key2 = usuario.id
                ) {
                    favoritosService.obtenerFavoritos(
                        userId = usuario.id,
                        onSuccess = { favoritos -> value = favoritos },
                        onError = { value = emptyList() }
                    )
                }

                FavoritosUI(
                    favoritos = favoritosState.value,
                    userId = usuario.id,
                    onBack = { navControlador.popBackStack() },
                    onFavoritoEliminado = { recargarKey++ },
                    onProductoClick = { productoId ->
                        navControlador.navigate("producto_detalle/$productoId")
                    }
                )
            }
        }

        // DIRECCIONES - Solo para clientes
        if (tipoUsuario == TipoUsuario.CLIENTE) {
            composable("direcciones") {
                var recargarKey by remember { mutableStateOf(0) }
                DireccionesUI(
                    direcciones = usuario.direcciones,
                    userId = usuario.id,
                    onAgregarDireccion = {},
                    onEditarDireccion = { println("Editar: ${it.descripcion}") },
                    onEliminarDireccion = {},
                    onBack = { navControlador.popBackStack() },
                    onDireccionAgregada = { recargarKey++ }
                )
            }
        }

        // DETALLE PRODUCTO - Común, pero el comportamiento varía según el tipo de usuario
        composable("producto_detalle/{productoId}") { backStackEntry ->
            val productoId = URLDecoder.decode(
                backStackEntry.arguments?.getString("productoId") ?: "",
                StandardCharsets.UTF_8.toString()
            )

            ProductoDetalleUI(
                navController = navControlador,
                productoId = productoId,
                esAdmin = tipoUsuario == TipoUsuario.ADMIN
            )
        }
    }
}

@Composable
private fun MostrarCargando(mensaje: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFFD68C45)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                mensaje,
                color = Color(0xFF3E2C1C)
            )
        }
    }
}