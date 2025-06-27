import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.appcafe.db.Usuario
import com.example.cafeteria.db.AuthService
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import com.example.appcafe.R

@Composable
fun SplashUI(onUsuarioListo: (Usuario?) -> Unit) {
    val authService = remember { AuthService() }

    // Cargar animación Lottie desde res/raw
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.coffee_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(Unit) {
        delay(1000) // Espera opcional para dar tiempo a la animación
        val usuario = authService.obtenerUsuarioActual()
        onUsuarioListo(usuario)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(220.dp)
        )
        // Puedes añadir un texto opcional si quieres
        Text(
            text = "Cargando...",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            color = Color(0xFFF5F0E1),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
