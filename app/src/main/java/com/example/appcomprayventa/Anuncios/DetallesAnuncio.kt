package com.example.appcomprayventa.Anuncios

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Adaptadores.AdaptadorComentario
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityDetallesAnuncioBinding
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.Modelo.ModeloComentario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetallesAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetallesAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnuncio = ""
    private var mialike = false

    private lateinit var comentarioArrayList: ArrayList<ModeloComentario>
    private lateinit var adaptadorComentario: AdaptadorComentario

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
            cargarComentarios()
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

        binding.BtnComentar.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Inicia sesión para comentar", Toast.LENGTH_SHORT).show()
            } else {
                dialogComentar()
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
                        binding.TvMarcaDetalle.text = "Marca: ${modelo.marca}"
                        binding.TvCategoriaDetalle.text = "Categoría: ${modelo.categoria}"
                        binding.TvCondicionDetalle.text = "Condición: ${modelo.condicion}"

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

    private fun dialogComentar() {
        // Inflamos el diseño de un cuadrito de texto (puedes crear uno rápido o usar un EditText simple)
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Agregar comentario")

        val etComentario = android.widget.EditText(this)
        etComentario.hint = "Escribe tu comentario aquí..."
        builder.setView(etComentario)

        builder.setPositiveButton("Enviar") { _, _ ->
            val texto = etComentario.text.toString().trim()
            if (texto.isNotEmpty()) {
                subirComentario(texto)
            } else {
                Toast.makeText(this, "No puedes enviar un comentario vacío", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun subirComentario(comentario: String) {
        val tiempo = System.currentTimeMillis()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        val idComentario = "${ref.push().key}" // Generamos un ID único

        val info = HashMap<String, Any>()
        info["id"] = idComentario
        info["idAnuncio"] = idAnuncio
        info["uid"] = firebaseAuth.uid!!
        info["comentario"] = comentario
        info["tiempo"] = tiempo

        ref.child(idAnuncio).child("Comentarios").child(idComentario)
            .setValue(info)
            .addOnSuccessListener {
                Toast.makeText(this, "Comentario publicado", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarComentarios() {
        comentarioArrayList = ArrayList()

        binding.RvComentarios.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Comentarios")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    comentarioArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelo = ds.getValue(ModeloComentario::class.java)
                        if (modelo != null) {
                            comentarioArrayList.add(modelo)
                        }
                    }
                    // Configuramos el adaptador
                    adaptadorComentario = AdaptadorComentario(this@DetallesAnuncio, comentarioArrayList)
                    binding.RvComentarios.adapter = adaptadorComentario
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun dialogResponderComentario(modelo: ModeloComentario) {
        val etRespuesta = android.widget.EditText(this)
        etRespuesta.hint = "Respondiendo a ${modelo.comentario}..."
        etRespuesta.setPadding(40, 40, 40, 40)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Responder comentario")
            .setView(etRespuesta)
            .setPositiveButton("Responder") { _, _ ->
                val texto = etRespuesta.text.toString().trim()
                if (texto.isNotEmpty()) {
                    subirRespuesta(texto, modelo.id) // Pasamos el ID del comentario original
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun subirRespuesta(respuesta: String, idComentarioPadre: String) {
        val tiempo = System.currentTimeMillis()
        val refAnuncios = FirebaseDatabase.getInstance().getReference("Anuncios")
        val idRespuesta = refAnuncios.push().key!!

        val info = HashMap<String, Any>()
        info["id"] = idRespuesta
        info["idAnuncio"] = idAnuncio
        info["uid"] = firebaseAuth.uid!!
        info["comentario"] = respuesta
        info["tiempo"] = tiempo
        info["idPadre"] = idComentarioPadre // Guardamos la referencia al padre

        // Lo guardamos dentro del comentario original
        refAnuncios.child(idAnuncio)
            .child("Comentarios")
            .child(idComentarioPadre)
            .child("Respuestas")
            .child(idRespuesta)
            .setValue(info)
            .addOnSuccessListener {
                Toast.makeText(this, "Respuesta enviada", Toast.LENGTH_SHORT).show()
            }
    }

}