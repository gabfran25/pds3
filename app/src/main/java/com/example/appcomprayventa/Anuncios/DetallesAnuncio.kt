package com.example.appcomprayventa.Anuncios

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityDetallesAnuncioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetallesAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetallesAnuncioBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnuncio = ""

    private var mialike = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallesAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibimos el ID que mandamos desde el adaptador
        firebaseAuth = FirebaseAuth.getInstance()
        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""

        // Aquí irán las funciones de cargar info, likes y comentarios
        if (idAnuncio.isNotEmpty()) {
            cargarDetallesAnuncio()
        }
    }

    private fun cargarDetallesAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    // Sacamos la info del snapshot
                    val modelo = snapshot.getValue(com.example.appcomprayventa.Modelo.ModeloAnuncio::class.java)

                    if (modelo != null) {
                        // Seteamos los textos en el diseño
                        binding.TvTituloDetalle.text = modelo.titulo
                        binding.TvPrecioDetalle.text = "$${modelo.precio}"
                        binding.TvDescripcionDetalle.text = modelo.descripcion

                        // Aquí cargaríamos la imagen con Glide después
                        cargarImagenesAnuncio()
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    // Manejar error si es necesario
                }
            })
    }

    private fun cargarImagenesAnuncio() {
        // Por ahora cargaremos solo la primera imagen para no complicarnos
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").limitToFirst(1)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            com.bumptech.glide.Glide.with(this@DetallesAnuncio)
                                .load(imagenUrl)
                                .placeholder(R.drawable.item_imagen)
                                .into(binding.ImgDetalle)
                        } catch (e: Exception) { }
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
    }
}
