package com.example.appcafe.vistaUI

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appcafe.Services.FavoritosService
import com.example.appcafe.db.Producto
import com.example.appcafe.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets        

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioUI(
    navController: NavHostController,
    viewModel: ProductoViewModel = viewModel()
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Cafes") } // Cambiado a la primera categoría correcta

    // Estados del ViewModel
    val productos by viewModel.productos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val favoritosService = remember { FavoritosService() }
    val scope = rememberCoroutineScope()

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    var mensajeSnackbar by remember { mutableStateOf<String?>(null) }

    // Categorías corregidas
    val categorias = listOf("Cafes", "Postres", "Desayunos", "Almuerzos")

    // Cargar productos inicialmente
    LaunchedEffect(Unit) {
        viewModel.obtenerProductos()
    }

    // Cargar productos por categoría cuando cambia la selección
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == "Todos") {
            viewModel.obtenerProductos()
        } else {
            viewModel.obtenerProductosPorCategoria(selectedCategory)
        }
    }

    // Mostrar Snackbar si hay mensaje
    LaunchedEffect(mensajeSnackbar) {
        mensajeSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            mensajeSnackbar = null
        }
    }

    // Mostrar error del ViewModel como Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.limpiarEstados()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F5DC), Color(0xFFE8E8E8))
                )
            )
    ) {
        // Campo de búsqueda
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Buscar productos..") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color(0xff8B4513))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.White, shape = RoundedCornerShape(20.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B4513),
                unfocusedBorderColor = Color(0xFFD2691E)
            ),
            shape = RoundedCornerShape(20.dp)
        )

        // SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Título de categorías
        Text(
            text = "Categorías",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B4513),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Grid de categorías - 3 columnas en primera fila, 2 en segunda
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // Primera fila: Cafes, Postres, Desayunos
            items(categorias.take(3)) { categoria ->
                FilterChip(
                    onClick = { selectedCategory = categoria },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(getIconForCategory(categoria), null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = categoria, textAlign = TextAlign.Center)
                        }
                    },
                    selected = selectedCategory == categoria,
                    modifier = Modifier.size(99.dp, 40.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8B4513),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFD2691E),
                        labelColor = Color.White
                    )
                )
            }

            // Segunda fila: Almuerzos y Todos
            item {
                FilterChip(
                    onClick = { selectedCategory = "Almuerzos" },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(getIconForCategory("Almuerzos"), null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Almuerzos", textAlign = TextAlign.Center)
                        }
                    },
                    selected = selectedCategory == "Almuerzos",
                    modifier = Modifier.size(99.dp, 40.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8B4513),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFD2691E),
                        labelColor = Color.White
                    )
                )
            }

            item {
                FilterChip(
                    onClick = {
                        selectedCategory = "Todos"
                        viewModel.obtenerProductos()
                    },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Apps, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Todos", textAlign = TextAlign.Center)
                        }
                    },
                    selected = selectedCategory == "Todos",
                    modifier = Modifier.size(99.dp, 40.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8B4513),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFD2691E),
                        labelColor = Color.White
                    )
                )
            }
        }

        // Contenido principal
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF8B4513))
                    Text(
                        text = "Cargando productos...",
                        color = Color(0xFF8B4513),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else if (productos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "No hay productos disponibles",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    if (selectedCategory != "Todos") {
                        Text(
                            text = "en la categoría \"$selectedCategory\"",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            // Grid de productos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    productos.filter {
                        it.nombre.contains(searchText, ignoreCase = true) ||
                                it.descripcionCorta.contains(searchText, ignoreCase = true)
                    }
                ) { producto ->
                    ProductoCard(
                        producto = producto,
                        onFavoriteClick = {
                            scope.launch {
                                favoritosService.esFavorito(
                                    userId = userId,
                                    productoId = producto.id,
                                    onResult = { esFavorito ->
                                        if (!esFavorito) {
                                            favoritosService.agregarFavorito(
                                                userId = userId,
                                                productoId = producto.id,
                                                nombreProducto = producto.nombre,
                                                descripcionProducto = producto.descripcionCorta,
                                                precioProducto = producto.precio,
                                                imagenProducto = producto.imagenUrl,
                                                categoria = producto.categoria,
                                                onSuccess = {
                                                    mensajeSnackbar = "Producto agregado a favoritos"
                                                },
                                                onError = { error ->
                                                    Log.e("Favorito", "Error: ${error.message}")
                                                    mensajeSnackbar = "Error al agregar a favoritos"
                                                }
                                            )
                                        } else {
                                            mensajeSnackbar = "Este producto ya está en favoritos"
                                        }
                                    },
                                    onError = { error ->
                                        Log.e("Favorito", "Error al verificar: ${error.message}")
                                        mensajeSnackbar = "Error al verificar favoritos"
                                    }
                                )
                            }
                        },
                        onProductoClick = {
                            val encodedId = URLEncoder.encode(producto.id, StandardCharsets.UTF_8.toString())
                            navController.navigate("producto_detalle/$encodedId")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onFavoriteClick: () -> Unit,
    onProductoClick: () -> Unit
) {
    val context = LocalContext.current

    fun getImgurDirectUrl(url: String): String {
        return when {
            url.startsWith("https://i.imgur.com/") -> url
            url.contains("imgur.com/") && !url.contains("i.imgur.com") -> {
                val imageId = url.substringAfterLast("/").substringBefore(".")
                "https://i.imgur.com/$imageId.jpg"
            }
            url.matches(Regex("^[a-zA-Z0-9]+$")) -> {
                "https://i.imgur.com/$url.jpg"
            }
            else -> url
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable { onProductoClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC)), // Color beige suave
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Color(0xFFD2691E),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = getImgurDirectUrl(producto.imagenUrl)

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .allowHardware(false)
                        .build(),
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_dialog_alert)
                )

                // Indicador de disponibilidad
                if (!producto.disponible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO DISPONIBLE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Botón de favorito en la esquina superior derecha
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = Color(0xFF8B4513),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Información del producto
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Nombre del producto
                Text(
                    text = producto.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2C1C), // Marrón oscuro
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Descripción corta
                Text(
                    text = producto.descripcionCorta,
                    fontSize = 12.sp,
                    color = Color(0xFF3E2C1C), // Marrón medio
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Precio y botón "Ver más"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Precio: S/${producto.precio}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA3A380)
                    )

                    // Botón "Ver más" estilo de la imagen
                    Button(
                        onClick = onProductoClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA3A380),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(20.dp)
                            .width(52.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Ver más",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun getIconForCategory(categoria: String): ImageVector {
    return when (categoria.lowercase()) {
        "cafes" -> Icons.Default.LocalCafe
        "postres" -> Icons.Default.Cake
        "desayunos" -> Icons.Default.FreeBreakfast
        "almuerzos" -> Icons.Default.Restaurant
        else -> Icons.Default.Category
    }
}