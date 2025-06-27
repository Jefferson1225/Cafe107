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
import com.example.appcafe.vistaUI.admin.AgregarDeliveryUI
import com.example.appcafe.vistaUI.admin.AgregarProductoUI
import com.example.appcare.vistaUI.CarritoUI
import com.example.cafeteria.db.AuthService
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun ManejadorNav(
    navControlador: NavHostController,
    onLogout: () -> Unit,
    esAdmin: Boolean = false
) {
    NavHost(
        navController = navControlador,
        startDestination = PantallasNav.ItemInicio.ruta
    ) {
        composable(PantallasNav.ItemInicio.ruta) {
            InicioUI(navControlador)
        }

        //pantalla para usuarios normales
        if (!esAdmin) {
            // Ruta original de pedidos (sin parámetros)
            composable(PantallasNav.ItemPedidos.ruta) {
                val authService = remember { AuthService() }
                val usuarioState = produceState<Usuario?>(initialValue = null) {
                    value = authService.obtenerUsuarioActual()
                }
                val usuarioActual = usuarioState.value

                if (usuarioActual != null) {
                    PedidosUI(
                        userId = usuarioActual.id,
                        onNavigateBack = {
                            navControlador.popBackStack()
                        },
                        onNavigateToDetalle = { pedidoId ->
                            navControlador.navigate("detalle_pedido/$pedidoId")
                        }
                    )
                } else {
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
                                "Cargando pedidos...",
                                color = Color(0xFF3E2C1C)
                            )
                        }
                    }
                }
            }

            // Nueva ruta para pedidos con userId (para navegación desde carrito)
            composable("pedidos/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                PedidosUI(
                    userId = userId,
                    onNavigateBack = {
                        navControlador.popBackStack()
                    },
                    onNavigateToDetalle = { pedidoId ->
                        navControlador.navigate("detalle_pedido/$pedidoId")
                    }
                )
            }

            // Nueva ruta para el detalle del pedido
            composable("detalle_pedido/{pedidoId}") { backStackEntry ->
                val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""

                DetallePedidosUI(
                    pedidoId = pedidoId,
                    onNavigateBack = {
                        navControlador.popBackStack()
                    }
                )
            }

            composable(PantallasNav.ItemCarrito.ruta) {
                // Obtener el usuario actual para pasarle el userId
                val authService = remember { AuthService() }

                val usuarioState = produceState<Usuario?>(initialValue = null) {
                    value = authService.obtenerUsuarioActual()
                }

                val usuarioActual = usuarioState.value

                if (usuarioActual != null) {
                    CarritoUI(
                        userId = usuarioActual.id,
                        onNavigateBack = {
                            navControlador.popBackStack()
                        },
                        onNavigateToPedidos = {
                            // AQUÍ ESTÁ LA CORRECCIÓN: Cambiar onNavigateToPayment por onNavigateToPedidos
                            navControlador.navigate("pedidos/${usuarioActual.id}") {
                                // Opcional: limpiar el stack hasta la pantalla de inicio
                                popUpTo(PantallasNav.ItemInicio.ruta) {
                                    inclusive = false
                                }
                            }
                        }
                    )
                } else {
                    // Mostrar loading mientras se carga el usuario
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
                                "Cargando carrito...",
                                color = Color(0xFF3E2C1C)
                            )
                        }
                    }
                }
            }
        }

        //pantallas para admin
        if (esAdmin) {
            composable(PantallasNav.ItemAgregarProducto.ruta) {
                AgregarProductoUI()
            }
            composable(PantallasNav.ItemAgregarDelivery.ruta) {
                AgregarDeliveryUI()
            }
        }

        composable(PantallasNav.ItemPerfil.ruta) {
            val authService = AuthService()

            val usuarioState = produceState<Usuario?>(initialValue = null) {
                value = authService.obtenerUsuarioActual()
            }

            val usuarioActual = usuarioState.value

            if (usuarioActual != null) {
                PerfilUI(
                    usuario = usuarioActual,
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
            } else {
                // Puedes mostrar un CircularProgressIndicator o redirigir al login
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        //nueva pantalla favoritos - FIXED
        composable("favoritos") {
            val authService = remember { AuthService() }
            val favoritosService = remember { FavoritosService() }

            // Estado para forzar la recarga cuando se elimina un favorito
            var recargarKey by remember { mutableStateOf(0) }

            val usuarioState = produceState<Usuario?>(
                initialValue = null,
                key1 = recargarKey
            ) {
                value = authService.obtenerUsuarioActual()
            }

            val usuarioActual = usuarioState.value

            if (usuarioActual != null) {
                // Estado para los favoritos
                val favoritosState = produceState(
                    initialValue = emptyList(),
                    key1 = recargarKey,
                    key2 = usuarioActual.id
                ) {
                    favoritosService.obtenerFavoritos(
                        userId = usuarioActual.id,
                        onSuccess = { favoritos ->
                            value = favoritos
                        },
                        onError = { error ->
                            // Manejar error si es necesario
                            println("Error al obtener favoritos: ${error.message}")
                            value = emptyList()
                        }
                    )
                }

                FavoritosUI(
                    favoritos = favoritosState.value,
                    userId = usuarioActual.id,
                    onBack = {
                        navControlador.popBackStack()
                    },
                    onFavoritoEliminado = {
                        // Incrementar el key para recargar los favoritos
                        recargarKey++
                    },
                    onProductoClick = { productoId ->
                        navControlador.navigate("producto_detalle/$productoId")
                    }
                )
            } else {
                // Mostrar loading mientras se cargan los datos del usuario
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
                            "Cargando favoritos...",
                            color = Color(0xFF3E2C1C)
                        )
                    }
                }
            }
        }

        // Nueva pantalla de direcciones
        composable("direcciones") {
            val authService = remember { AuthService() }

            // Estado para forzar la recarga cuando se agrega/elimina una dirección
            var recargarKey by remember { mutableStateOf(0) }

            val usuarioState = produceState<Usuario?>(
                initialValue = null,
                key1 = recargarKey // Se recarga cuando cambia este valor
            ) {
                value = authService.obtenerUsuarioActual()
            }

            val usuarioActual = usuarioState.value

            if (usuarioActual != null) {
                DireccionesUI(
                    direcciones = usuarioActual.direcciones,
                    userId = usuarioActual.id,
                    onAgregarDireccion = {
                        // Esta función ya no se usa porque el diálogo se maneja internamente
                    },
                    onEditarDireccion = { direccion ->
                        // Aquí puedes implementar la navegación a una pantalla de edición
                        // Por ejemplo: navControlador.navigate("editar_direccion/${direccion.id}")
                        println("Editar dirección: ${direccion.descripcion}")
                    },
                    onEliminarDireccion = { direccion ->
                        // Esta función también se maneja internamente ahora
                    },
                    onBack = {
                        navControlador.popBackStack()
                    },
                    onDireccionAgregada = {
                        // Este callback se llama cuando se agrega o elimina una dirección
                        // Incrementamos el key para forzar la recarga de los datos del usuario
                        recargarKey++
                    }
                )
            } else {
                // Mostrar loading mientras se cargan los datos del usuario
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
                            "Cargando direcciones...",
                            color = Color(0xFF3E2C1C)
                        )
                    }
                }
            }
        }

        composable("producto_detalle/{productoId}") { backStackEntry ->
            val productoId = URLDecoder.decode(
                backStackEntry.arguments?.getString("productoId") ?: "",
                StandardCharsets.UTF_8.toString()
            )

            ProductoDetalleUI(
                navController = navControlador,
                productoId = productoId,
                esAdmin = esAdmin
            )
        }
    }
}