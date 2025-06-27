package com.example.appcafe.SubsUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appcafe.db.Favorito
import com.example.appcafe.Services.FavoritosService
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosUI(
    favoritos: List<Favorito>,
    userId: String,
    onBack: () -> Unit,
    onFavoritoEliminado: () -> Unit = {},
    onProductoClick: (String) -> Unit = {}
) {
    val CafeFondo = Color(0xFFF5EDD8)
    val CafeBorde = Color(0xFFD68C45)
    val CafeTexto = Color(0xFF3E2C1C)

    val favoritosService = remember { FavoritosService() }

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var favoritoAEliminar by remember { mutableStateOf<Favorito?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    fun formatearFecha(timestamp: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(Date(timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeFondo)
    ) {
        TopAppBar(
            title = { Text("Mis Favoritos", color = CafeTexto) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = CafeTexto)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CafeBorde)
        )

        mensajeError?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (favoritos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = CafeTexto.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No tienes favoritos",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CafeTexto,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Explora nuestros productos y agrega los que más te gusten",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CafeTexto.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoritos) { favorito ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductoClick(favorito.productoId) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Imagen del producto
                            Card(
                                modifier = Modifier.size(70.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = CafeFondo)
                            ) {
                                val imageUrl = getDirectImageUrl(favorito.imagenProducto)
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = favorito.nombreProducto,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                                    error = painterResource(id = android.R.drawable.ic_dialog_alert)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = favorito.nombreProducto,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CafeTexto,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (favorito.descripcionProducto.isNotBlank()) {
                                    Text(
                                        text = favorito.descripcionProducto,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CafeTexto.copy(alpha = 0.7f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "S/ ${String.format("%.2f", favorito.precioProducto)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CafeBorde
                                    )

                                    if (favorito.categoria.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = favorito.categoria,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            modifier = Modifier
                                                .background(
                                                    CafeTexto.copy(alpha = 0.8f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Agregado: ${formatearFecha(favorito.fechaAgregado)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CafeTexto.copy(alpha = 0.5f)
                                )
                            }

                            IconButton(
                                onClick = {
                                    favoritoAEliminar = favorito
                                    mostrarDialogoEliminar = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Eliminar de favoritos",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = {
                if (!cargando) {
                    mostrarDialogoEliminar = false
                    favoritoAEliminar = null
                }
            },
            title = { Text("Eliminar de favoritos", color = CafeTexto) },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar \"${favoritoAEliminar?.nombreProducto ?: ""}\" de tus favoritos?",
                    color = CafeTexto
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        favoritoAEliminar?.let { favorito ->
                            cargando = true
                            mensajeError = null
                            favoritosService.eliminarFavorito(
                                userId = userId,
                                favoritoId = favorito.id,
                                onSuccess = {
                                    cargando = false
                                    mostrarDialogoEliminar = false
                                    favoritoAEliminar = null
                                    onFavoritoEliminado()
                                },
                                onError = { error ->
                                    cargando = false
                                    mensajeError = "No se pudo eliminar el favorito: ${error.message}"
                                }
                            )
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Eliminar", color = Color.White)
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        if (!cargando) {
                            mostrarDialogoEliminar = false
                            favoritoAEliminar = null
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeBorde)
                ) {
                    Text("Cancelar", color = Color(0xFF2D2D2D))
                }
            },
            containerColor = Color(0xFFF5F0E1)
        )
    }
}

fun getDirectImageUrl(url: String): String {
    return when {
        url.startsWith("https://i.imgur.com/") -> url
        url.contains("imgur.com/") -> {
            val id = url.substringAfterLast("/").substringBefore(".")
            "https://i.imgur.com/$id.jpg"
        }
        url.matches(Regex("^[a-zA-Z0-9]+$")) -> {
            "https://i.imgur.com/$url.jpg"
        }
        else -> url
    }
}
