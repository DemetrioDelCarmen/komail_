package mx.com.david.komali.utility

import java.io.Serializable


data class Tienda(
    var nombre: String = "",
    var correo: String = "",
    var precio: String = "",
    var telefono: String = "",
    var posicion: Posicion = Posicion()
) : Serializable

data class Repartidor(
    var nombre: String = "",
    var telefono: String = "",
    var contrasenia: String = "",
    var jefe: String = "",
    var posicion: Posicion = Posicion(),
    var UIDD:String =""
) : Serializable

data class Cliente(var nombre: String = "", var posicion: Posicion = Posicion()) : Serializable
data class Posicion(
    var latitude: Double = 0.0, var longitude: Double = 0.0, var altitude: Double = 0.0
) : Serializable
