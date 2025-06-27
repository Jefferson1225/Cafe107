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
fun LoginUI(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
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
            // Imagen superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
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
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "INICIAR SESIÓN",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6F4E37),
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Formulario
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text("Correo:", fontSize = 14.sp, color = Color(0xFF6F4E37), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text("Ingresar usuario...", color = Color(0xFF6F4E37), fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD2691E),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color(0xFF6F4E37),
                        unfocusedTextColor = Color(0xFF6F4E37),
                        focusedContainerColor = Color(0xFFF0EAD6),
                        unfocusedContainerColor = Color(0xFFF0EAD6),
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                Text("Contraseña:", fontSize = 14.sp, color = Color(0xFF6F4E37), modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp), textAlign = TextAlign.Start)

                OutlinedTextField(
                    value = contraseña,
                    onValueChange = { contraseña = it },
                    placeholder = { Text("Ingresar contraseña...", color = Color(0xFF6F4E37), fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD2691E),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color(0xFF6F4E37),
                        unfocusedTextColor = Color(0xFF6F4E37),
                        focusedContainerColor = Color(0xFFF0EAD6),
                        unfocusedContainerColor = Color(0xFFF0EAD6),
                        cursorColor = Color.White
                    )
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        if (correo.isNotEmpty() && contraseña.isNotEmpty()) {
                            isLoading = true
                            errorMessage = ""
                            scope.launch {
                                val result = authService.iniciarSesion(correo, contraseña)
                                isLoading = false
                                if (result.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message
                                        ?: "Error al iniciar sesión"
                                }
                            }
                        } else {
                            errorMessage = "Complete todos los campos"
                        }
                    },
                    modifier = Modifier
                        .width(132.dp)
                        .height(39.dp)
                        .clip(RoundedCornerShape(25.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD68C45)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Iniciar Sesión", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.padding(bottom = 40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NO TIENES CUENTA AÚN? ", color = Color(0xFF888888), fontSize = 12.sp)
                    TextButton(onClick = { navController.navigate("registro") }) {
                        Text("REGÍSTRATE AQUÍ", color = Color(0xFFD2691E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
