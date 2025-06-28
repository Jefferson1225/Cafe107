package com.example.appcafe.vistaUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cafeteria.db.AuthService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroUI(navController: NavController) {
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var confirmarContraseña by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authService = AuthService()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Imagen sin padding lateral
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = "https://i.imgur.com/oCCjSII.jpeg",
                    contentDescription = "Coffee",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(60.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "REGISTRO",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6F4E37),
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Formulario con padding horizontal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Campo Nombre
                Text(
                    text = "Nombre:",
                    fontSize = 14.sp,
                    color = Color(0xFF6F4E37),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = {
                        Text("Ingresar su nombre", color = Color(0xFF6F4E37), fontSize = 14.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD2691E),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color(0xFF6F4E37),
                        unfocusedTextColor = Color(0xFF6F4E37),
                        focusedPlaceholderColor = Color(0xFF666666),
                        unfocusedPlaceholderColor = Color(0xFF666666),
                        focusedContainerColor = Color(0xFFF0EAD6),
                        unfocusedContainerColor = Color(0xFFF0EAD6),
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )

                // Campo Apellidos
                Text(
                    text = "Apellidos:",
                    fontSize = 14.sp,
                    color = Color(0xFF6F4E37),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, top = 12.dp),
                    textAlign = TextAlign.Start
                )

                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    placeholder = {
                        Text("Ingresar sus apellidos", color = Color(0xFF6F4E37), fontSize = 14.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD2691E),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color(0xFF6F4E37),
                        unfocusedTextColor = Color(0xFF6F4E37),
                        focusedPlaceholderColor = Color(0xFF666666),
                        unfocusedPlaceholderColor = Color(0xFF666666),
                        focusedContainerColor = Color(0xFFF0EAD6),
                        unfocusedContainerColor = Color(0xFFF0EAD6),
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )

                // Campo Correo
                Text(
                    text = "Correo:",
                    fontSize = 14.sp,
                    color = Color(0xFF6F4E37),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, top = 12.dp),
                    textAlign = TextAlign.Start
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = {
                        Text("Ingresar su correo", color = Color(0xFF6F4E37), fontSize = 14.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD2691E),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color(0xFF6F4E37),
                        unfocusedTextColor = Color(0xFF6F4E37),
                        focusedPlaceholderColor = Color(0xFF666666),
                        unfocusedPlaceholderColor = Color(0xFF666666),
                        focusedContainerColor = Color(0xFFF0EAD6),
                        unfocusedContainerColor = Color(0xFFF0EAD6),
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                // Campo Contraseña
                Text(
                    text = "Contraseña:",
                    fontSize = 14.sp,
                    color = Color(0xFF6F4E37),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, top = 12.dp),
                    textAlign = TextAlign.Start
                )

                OutlinedTextField(
                    value = contraseña,
                    onValueChange = { contraseña = it },
                    placeholder = {
                        Text("Ingresar su contraseña", color = Color(0xFF6F4E37), fontSize = 14.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD2691E),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color(0xFF6F4E37),
                        unfocusedTextColor = Color(0xFF6F4E37),
                        focusedPlaceholderColor = Color(0xFF666666),
                        unfocusedPlaceholderColor = Color(0xFF666666),
                        focusedContainerColor = Color(0xFFF0EAD6),
                        unfocusedContainerColor = Color(0xFFF0EAD6),
                        cursorColor = Color.White
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                // Campo Confirmar Contraseña
                Text(
                    text = "Confirmar Contraseña:",
                    fontSize = 14.sp,
                    color = Color(0xFF6F4E37),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, top = 12.dp),
                    textAlign = TextAlign.Start
                )

                OutlinedTextField(
                    value = confirmarContraseña,
                    onValueChange = { confirmarContraseña = it },
                    placeholder = {
                        Text("Ingresar su contraseña otra vez", color = Color(0xFF6F4E37), fontSize = 14.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD2691E),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color(0xFF6F4E37),
                        unfocusedTextColor = Color(0xFF6F4E37),
                        focusedPlaceholderColor = Color(0xFF666666),
                        unfocusedPlaceholderColor = Color(0xFF666666),
                        focusedContainerColor = Color(0xFFF0EAD6),
                        unfocusedContainerColor = Color(0xFFF0EAD6),
                        cursorColor = Color.White
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        when {
                            nombre.isEmpty() || apellidos.isEmpty() || correo.isEmpty() ||
                                    contraseña.isEmpty() || confirmarContraseña.isEmpty() -> {
                                errorMessage = "Complete todos los campos"
                            }
                            !esNombreValido(nombre) -> {
                                errorMessage = "Nombre inválido. Solo letras y espacios"
                            }
                            !esNombreValido(apellidos) -> {
                                errorMessage = "Apellidos inválidos. Solo letras y espacios"
                            }
                            contraseña != confirmarContraseña -> {
                                errorMessage = "Las contraseñas no coinciden"
                            }
                            contraseña.length < 6 -> {
                                errorMessage = "La contraseña debe tener al menos 6 caracteres"
                            }
                            else -> {
                                isLoading = true
                                errorMessage = ""
                                scope.launch {
                                    val result = authService.registrarUsuario(
                                        nombre = nombre,
                                        apellidos = apellidos,
                                        correo = correo,
                                        contraseña = contraseña,
                                        esAdmin = false
                                    )
                                    isLoading = false
                                    if (result.isSuccess) {
                                        navController.navigate("login") {
                                            popUpTo("registro") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message
                                            ?: "Error al registrar usuario"
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .width(132.dp)
                        .height(39.dp)
                        .clip(RoundedCornerShape(25.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD68C45)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Registrarse",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.padding(bottom = 40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YA TIENES UNA CUENTA? ",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                    TextButton(
                        onClick = { navController.navigate("login") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "INICIA SESIÓN AQUÍ",
                            color = Color(0xFFD2691E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

    }
}

fun esNombreValido(texto: String): Boolean {
    return texto.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+\$"))
}
