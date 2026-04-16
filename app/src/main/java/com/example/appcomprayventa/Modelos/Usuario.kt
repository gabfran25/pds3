package com.example.appcomprayventa.Modelos

class Usuario {
    var uid: String = ""
    var nombres: String = ""
    var email: String = ""
    var imagen: String = ""


    constructor()

    constructor(uid: String, nombres: String, email: String, imagen: String) {
        this.uid = uid
        this.nombres = nombres
        this.email = email
        this.imagen = imagen
    }

}