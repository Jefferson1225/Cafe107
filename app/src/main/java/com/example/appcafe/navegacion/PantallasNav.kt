package com.upsjb.sesion07.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ImageAspectRatio
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class  PantallasNav(val titulo:String,
    val ruta:String, val icono:ImageVector,
    val colorIcon:Color
    ) {
    //nav usuarios
    object ItemInicio:PantallasNav(RutasNav.Inicio.name,
        RutasNav.Inicio.name,Icons.Outlined.Home,
        Color(0xFF6F4E37)
        )
    object ItemPedidos:PantallasNav(RutasNav.Pedidos.name,
        RutasNav.Pedidos.name, Icons.AutoMirrored.Outlined.List,
        Color(0xFF6F4E37)
    )
    object ItemCarrito:PantallasNav(RutasNav.Carrito.name,
        RutasNav.Carrito.name,Icons.Outlined.ShoppingCart,
        Color(0xFF6F4E37)
    )
    object ItemPerfil:PantallasNav(RutasNav.Perfil.name,
        RutasNav.Perfil.name,Icons.Outlined.PersonOutline,
        Color(0xFF6F4E37)
    )

    //pantalla admin
    object ItemAgregarProducto:PantallasNav(RutasNav.AgregarProduct.name,
        RutasNav.AgregarProduct.name,Icons.Outlined.Add,
        Color(0xFF6F4E37)
    )
    object ItemAgregarDelivery:PantallasNav(RutasNav.AgregarRepartidor.name,
        RutasNav.AgregarRepartidor.name,Icons.Outlined.Add,
        Color(0xFF6F4E37)
    )
    object ItemPedidosPreparar:PantallasNav(RutasNav.PedidosPreparar.name,
        RutasNav.PedidosPreparar.name,Icons.Outlined.Restaurant,
        Color(0xFF6F4E37)
    )
    //pantalla repartidores
    object ItemVerYAceptarPedidos:PantallasNav(RutasNav.VerYAceptarPedidos.name,
        RutasNav.VerYAceptarPedidos.name,Icons.Outlined.DeliveryDining,
        Color(0xFF6F4E37)
    )
}