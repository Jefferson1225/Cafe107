package com.example.appcafe.vistaUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appcafe.db.Usuario
import com.example.appcafe.Services.FavoritosService
import com.example.appcafe.db.Favorito
import com.example.cafeteria.db.AuthService
import com.example.cafeteria.db.TipoUsuario
import com.example.cafeteria.db.actualizarTelefono

@Composable
fun PerfilUI(
    usuario: Usuario,
    tipoUsuario: TipoUsuario,
    onLogout: () -> Unit,
    onClickDirecciones: () -> Unit,
    onClickFavoritos: () -> Unit,
    onClickHistorialOrdenes: () -> Unit,
    onClickMisEntregas: () -> Unit
) {
    val CafeOscuro = Color(0xFF3E2C1C)
    val MarronIcono = Color(0xFF6F4837)

    var telefonoSoloDigitos by remember { mutableStateOf(usuario.telefono.removePrefix("+51")) }
    var editando by remember { mutableStateOf(usuario.telefono.isEmpty()) }

    val favoritosService = remember { FavoritosService() }
    var favoritos by remember { mutableStateOf<List<Favorito>>(emptyList()) }


    // Obtener favoritos al cargar
    LaunchedEffect(usuario.id) {
        favoritosService.obtenerFavoritos(
            userId = usuario.id,
            onSuccess = { favoritos = it },
            onError = { favoritos = emptyList() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5EDD8))
    ) {
        // Encabezado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFD68C45)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Foto de perfil",
                    tint = Color.Black,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${usuario.nombre} ${usuario.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información personal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Correo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MarronIcono)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = usuario.correo, color = CafeOscuro)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Teléfono
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MarronIcono)
                    Spacer(modifier = Modifier.width(8.dp))

                    if (editando) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "+51",
                                color = CafeOscuro,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            OutlinedTextField(
                                value = telefonoSoloDigitos,
                                onValueChange = {
                                    if (it.length <= 9 && it.all(Char::isDigit)) {
                                        telefonoSoloDigitos = it
                                    }
                                },
                                label = { Text("Teléfono") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = CafeOscuro,
                                    unfocusedTextColor = CafeOscuro,
                                    focusedLabelColor = CafeOscuro,
                                    unfocusedLabelColor = CafeOscuro
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = "+51 ${telefonoSoloDigitos.chunked(3).joinToString(" ")}",
                            color = CafeOscuro,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { editando = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MarronIcono
                            )
                        }
                    }
                }
            }
        }

        // Botón para guardar teléfono
        if (editando && telefonoSoloDigitos.length == 9) {
            Button(
                onClick = {
                    val telefonoCompleto = "+51$telefonoSoloDigitos"
                    actualizarTelefono(usuario.id, telefonoCompleto)
                    editando = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD68C45))
            ) {
                Text("Guardar número", color = Color.White)
            }
        }

        if (tipoUsuario == TipoUsuario.CLIENTE) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    // Mis Direcciones
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickDirecciones() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MarronIcono)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mis direcciones",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CafeOscuro
                            )
                            Text(
                                text = "${usuario.direcciones.size} direcciones guardadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = CafeOscuro.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Divider(color = Color.LightGray, thickness = 1.dp)

                    // Favoritos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickFavoritos() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MarronIcono
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mis favoritos",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CafeOscuro
                            )
                            Text(
                                text = "${favoritos.size} productos favoritos",
                                style = MaterialTheme.typography.bodySmall,
                                color = CafeOscuro.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        if (tipoUsuario == TipoUsuario.ADMIN) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onClickHistorialOrdenes() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MarronIcono)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Historial de órdenes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CafeOscuro
                    )
                }
            }
        }

        if (tipoUsuario == TipoUsuario.REPARTIDOR) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onClickMisEntregas() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFF6F4837)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mis entregas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF3E2C1C)
                    )
                }
            }
        }


        // Cerrar sesión
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            TextButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = Color.Red)
            }
        }
    }
}


