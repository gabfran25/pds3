package com.example.appcomprayventa.Anuncios

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityDetallesAnuncioBinding
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetallesAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetallesAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnuncio = ""
    private var mialike = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallesAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Recibimos el ID del anuncio desde el adaptador
        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""

        if (idAnuncio.isNotEmpty()) {
            cargarDetallesAnuncio()
            comprobarLike() // Verifica si el usuario actual ya dio like
            contarLikes()   // Escucha cuántos likes tiene el anuncio en total
        }

        // Configuración del botón de Like
        binding.BtnLike.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Debes iniciar sesión para dar like", Toast.LENGTH_SHORT).show()
            } else {
                if (mialike) {
                    quitarLike()
                } else {
                    darLike()
                }
            }
        }
    }

    private fun cargarDetallesAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modelo = snapshot.getValue(ModeloAnuncio::class.java)
                    if (modelo != null) {
                        binding.TvTituloDetalle.text = modelo.titulo
                        binding.TvPrecioDetalle.text = "$${modelo.precio}"
                        binding.TvDescripcionDetalle.text = modelo.descripcion

                        cargarImagenesAnuncio()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarImagenesAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
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
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // --- SECCIÓN DE LIKES ---

    private fun comprobarLike() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Likes").child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mialike = snapshot.exists()
                    if (mialike) {
                        // Si existe el like, ponemos la imagen de "like" (relleno)
                        binding.BtnLike.setImageResource(R.drawable.like)
                    } else {
                        // Si no existe, la de "dislike" (vacío)
                        binding.BtnLike.setImageResource(R.drawable.dislike)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun darLike() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Likes").child(firebaseAuth.uid!!)
            .setValue(true)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun quitarLike() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Likes").child(firebaseAuth.uid!!)
            .removeValue()
    }

    private fun contarLikes() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio).child("Likes")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numeroLikes = snapshot.childrenCount
                binding.TvLikesDetalle.text = "$numeroLikes Likes"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}