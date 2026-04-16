package com.example.appcomprayventa.Modelo

import com.google.firebase.database.PropertyName

class ModeloAnuncio {
    var id: String = ""
    var uid: String = ""

    @get:PropertyName("Marca")
    @set:PropertyName("Marca")
    var marca: String = ""

    @get:PropertyName("Categoria")
    @set:PropertyName("Categoria")
    var categoria: String = ""

    @get:PropertyName("Condicion")
    @set:PropertyName("Condicion")
    var condicion: String = ""

    @get:PropertyName("Precio")
    @set:PropertyName("Precio")
    var precio: String = ""

    @get:PropertyName("Titulo")
    @set:PropertyName("Titulo")
    var titulo: String = ""

    @get:PropertyName("Descripcion")
    @set:PropertyName("Descripcion")
    var descripcion: String = ""

    var tiempo: Long = 0
    var likes: Int = 0

    var dislikes: Int = 0
    var comentarios: Int = 0

    constructor()

    constructor(
        id: String, uid: String, marca: String, categoria: String,
        condicion: String, precio: String, titulo: String,
        descripcion: String, tiempo: Long, likes: Int, dislikes: Int, comentarios: Int
    ) {
        this.id = id
        this.uid = uid
        this.marca = marca
        this.categoria = categoria
        this.condicion = condicion
        this.precio = precio
        this.titulo = titulo
        this.descripcion = descripcion
        this.tiempo = tiempo
        this.likes = likes
        this.dislikes = dislikes
        this.comentarios = comentarios
    }
}