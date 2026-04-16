package com.example.appcomprayventa.Modelo

class ModeloComentario {
    var id: String = ""
    var idAnuncio: String = ""
    var uid: String = ""
    var comentario: String = ""
    var tiempo: Long = 0

    constructor()
    constructor(id: String, idAnuncio: String, uid: String, comentario: String, tiempo: Long) {
        this.id = id
        this.idAnuncio = idAnuncio
        this.uid = uid
        this.comentario = comentario
        this.tiempo = tiempo
    }
}