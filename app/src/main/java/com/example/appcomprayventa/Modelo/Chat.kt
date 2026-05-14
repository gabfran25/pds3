package com.example.appcomprayventa.Modelo

class Chat {

    // Atributos
    var idMensaje : String = ""

    var tipoMensaje : String = ""

    var mensaje : String = ""

    var emisorUid : String = ""

    var receptorUid : String = ""

    var tiempo : Long = 0

    var visto : Boolean = false

    // Constructor vacío
    constructor()

    // Constructor con todos los atributos
    constructor(idMensaje: String, tipoMensaje: String, mensaje: String, emisorUid: String, receptorUid: String, tiempo: Long, visto : Boolean) {
        this.idMensaje = idMensaje
        this.tipoMensaje = tipoMensaje
        this.mensaje = mensaje
        this.emisorUid = emisorUid
        this.receptorUid = receptorUid
        this.tiempo = tiempo
        this.visto = visto
    }
}