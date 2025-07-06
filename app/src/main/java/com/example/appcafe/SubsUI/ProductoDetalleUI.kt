package com.example.appcafe.SubsUI

import android.R
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appcafe.db.Producto
import com.example.appcafe.Services.ProductosService
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcafe.Services.CarritoService
import com.example.appcafe.db.ItemCarrito
import com.example.appcafe.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetalleUI(
    navController: NavHostController,
    productoId: String,
    esAdmin: Boolean
) {
    var producto by remember { mutableStateOf<Producto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSize by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Variables para manejar Toast de manera segura
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastError by remember { mutableStateOf<String?>(null) }

    // Variables para el diálogo de edición
    var editNombre by remember { mutableStateOf("") }
    var editDescripcionCorta by remember { mutableStateOf("") }
    var editDescripcionLarga by remember { mutableStateOf("") }
    var editPrecio by remember { mutableStateOf("") }
    var editCategoria by remember { mutableStateOf("") }
    var editTamaños by remember { mutableStateOf("") }
    var editImagenUrl by remember { mutableStateOf("") }
    var editDisponible by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val firebaseService = ProductosService()
    val productoViewModel: ProductoViewModel = viewModel()

    // Observar estados del ViewModel
    val isLoadingViewModel by productoViewModel.isLoading.collectAsState()
    val operacionExitosa by productoViewModel.operacionExitosa.collectAsState()
    val errorMessage by productoViewModel.errorMessage.collectAsState()

    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val carritoService = remember { CarritoService(firestore, auth) }

    // Manejar mensajes de éxito
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    // Manejar mensajes de error
    LaunchedEffect(toastError) {
        toastError?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            toastError = null
        }
    }

    // Cargar el producto por ID
    LaunchedEffect(productoId) {
        isLoading = true
        try {
            producto = firebaseService.getProductoPorId(productoId)
            producto?.let { prod ->
                selectedSize = prod.tamaños.firstOrNull() ?: ""
                // Inicializar valores para edición
                editNombre = prod.nombre
                editDescripcionCorta = prod.descripcionCorta
                editDescripcionLarga = prod.descripcionLarga
                editPrecio = prod.precio.toString()
                editCategoria = prod.categoria
                editTamaños = prod.tamaños.joinToString(", ")
                editImagenUrl = prod.imagenUrl
                editDisponible = prod.disponible
            }
        } catch (e: Exception) {
            toastError = "Error al cargar producto"
        }
        isLoading = false
    }

    // Manejar operación exitosa (eliminación)
    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            toastMessage = "Producto eliminado exitosamente"
            productoViewModel.limpiarEstados()
            navController.popBackStack()
        }
    }

    // Mostrar errores del ViewModel
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            toastError = error
            productoViewModel.limpiarEstados()
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que deseas eliminar este producto? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        producto?.let { prod ->
                            if (prod.id.isNotBlank()) {
                                productoViewModel.eliminarProducto(prod.id)
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de edición
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Producto") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editDescripcionCorta,
                        onValueChange = { editDescripcionCorta = it },
                        label = { Text("Descripción Corta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        maxLines = 2
                    )

                    OutlinedTextField(
                        value = editDescripcionLarga,
                        onValueChange = { editDescripcionLarga = it },
                        label = { Text("Descripción Larga") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = editPrecio,
                        onValueChange = { editPrecio = it },
                        label = { Text("Precio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editCategoria,
                        onValueChange = { editCategoria = it },
                        label = { Text("Categoría") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editTamaños,
                        onValueChange = { editTamaños = it },
                        label = { Text("Tamaños (separados por comas)") },
                        placeholder = { Text("Pequeño, Mediano, Grande") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editImagenUrl,
                        onValueChange = { editImagenUrl = it },
                        label = { Text("URL de la Imagen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = editDisponible,
                            onCheckedChange = { editDisponible = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF8B4513)
                            )
                        )
                        Text(
                            text = "Disponible",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val precioDouble = editPrecio.toDoubleOrNull()
                        if (precioDouble != null && editNombre.isNotBlank() && editCategoria.isNotBlank()) {
                            val tamañosList = editTamaños.split(",").map { it.trim() }.filter { it.isNotBlank() }

                            val productoActualizado = producto!!.copy(
                                nombre = editNombre,
                                descripcionCorta = editDescripcionCorta,
                                descripcionLarga = editDescripcionLarga,
                                precio = precioDouble,
                                categoria = editCategoria,
                                tamaños = tamañosList,
                                imagenUrl = editImagenUrl,
                                disponible = editDisponible
                            )

                            scope.launch {
                                try {
                                    firebaseService.actualizarProducto(productoActualizado)
                                    producto = productoActualizado
                                    // Actualizar el selectedSize si ya no está disponible
                                    if (!tamañosList.contains(selectedSize)) {
                                        selectedSize = tamañosList.firstOrNull() ?: ""
                                    }
                                    toastMessage = "Producto actualizado exitosamente"
                                    showEditDialog = false
                                } catch (e: Exception) {
                                    toastError = "Error al actualizar: ${e.message}"
                                }
                            }
                        } else {
                            toastError = "Por favor completa todos los campos requeridos"
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Blue)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF8B4513))
        }
        return
    }

    val currentProducto = producto
    if (currentProducto == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Producto no encontrado")
        }
        return
    }

    // Precios por tamaño
    fun getPrecioBySize(size: String, basePrice: Double): Double {
        return when (size.lowercase()) {
            "pequeño" -> basePrice
            "mediano" -> basePrice + 2.0
            "grande" -> basePrice + 4.0
            else -> basePrice
        }
    }

    val precioActual = getPrecioBySize(selectedSize, currentProducto.precio)

    // Función para convertir URLs de Imgur a formato directo
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B4513))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen del producto que ocupa la mitad superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                contentAlignment = Alignment.BottomCenter
            ) {
                val imageUrl = getImgurDirectUrl(currentProducto.imagenUrl)

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .allowHardware(false)
                        .build(),
                    contentDescription = currentProducto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_menu_gallery),
                    error = painterResource(id = R.drawable.ic_dialog_alert)
                )

                // Box del nombre del producto en la parte inferior de la imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(horizontal = 17.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = currentProducto.nombre,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Contenido principal (ocupa la mitad inferior)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        color = Color(0xFFF5F5DC),
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    // Descripción
                    Text(
                        text = "Descripción",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = currentProducto.descripcionLarga,
                        fontSize = 14.sp,
                        color = Color(0xFF000000),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Tamaños disponibles
                    if (currentProducto.tamaños.isNotEmpty()) {
                        Text(
                            text = "Tamaños disponible",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            currentProducto.tamaños.forEach { size ->
                                Button(
                                    onClick = { selectedSize = size },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedSize == size) Color(0xFF563826) else Color(0xFFD68C45),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = size,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Precio y botón al fondo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color(0xFFFAF3E0))
                        .border(
                            width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Precio",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "S/ ${String.format("%.0f", precioActual)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B4513)
                            )
                        }

                        Button(
                            onClick = {
                                if (userId != null) {
                                    val item = ItemCarrito(
                                        id = "", // se genera con UUID en la función
                                        productoId = currentProducto.id,
                                        nombre = currentProducto.nombre,
                                        imagenUrl = currentProducto.imagenUrl,
                                        tamaño = selectedSize,
                                        cantidad = 1,
                                        precio = precioActual
                                    )

                                    scope.launch {
                                        try {
                                            carritoService.agregarItem(item)
                                            toastMessage = "Producto agregado al carrito"
                                        } catch (e: Exception) {
                                            toastError = "Error al agregar al carrito: ${e.message}"
                                        }
                                    }
                                } else {
                                    toastError = "Usuario no autenticado"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD68C45)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.height(48.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp)
                        ) {
                            Text(
                                text = "Agregar al carrito",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFFFAF3E0))
                    )
                }
            }
        }

        // Botones flotantes
        Row(
            modifier = Modifier
                .padding(16.dp)
                .zIndex(1f)
        ) {
            // Botón de regresar
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color(0xFF8B4513)
                )
            }

            // Botones de admin (solo si isAdmin es true)
            if (esAdmin) {
                Spacer(modifier = Modifier.width(8.dp))

                // Botón de editar
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .background(
                            color = Color.Blue.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de eliminar
                IconButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isLoadingViewModel,
                    modifier = Modifier
                        .background(
                            color = if (isLoadingViewModel) Color.Gray.copy(alpha = 0.5f)
                            else Color.Red.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    if (isLoadingViewModel) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}