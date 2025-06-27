package com.example.appcafe.componentesUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.colorPrincipal()=this.background(
    Brush.linearGradient(colors = listOf(Color(0xff6F4E37),Color(0xff6F4E37)))
)

fun Modifier.colorSecundario()=this.background(
    color = Color(0xffA3A380)
)

@Composable
fun ColumnaGN(modifier: Modifier = Modifier,
              contenido: @Composable ColumnScope.() -> Unit) {
    Column(modifier=modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content =contenido )
}
@Composable
fun FilaGN(modifier: Modifier = Modifier,
           contenido: @Composable RowScope.() -> Unit) {
    Row (modifier=modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content =contenido )
}
//Componente de la barra de titulo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraTitulo(titulo: String) {
    CenterAlignedTopAppBar(
        title = { Text(text = titulo ) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = Modifier.colorPrincipal()
    )
}