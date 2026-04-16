package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appcomprayventa.Anuncios.DetallesAnuncio
import com.example.appcomprayventa.Modelo.ModeloComentario
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemComentarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class AdaptadorComentario(
    private val context: Context,
    private val comentarioArrayList: ArrayList<ModeloComentario>
) : RecyclerView.Adapter<AdaptadorComentario.HolderComentario>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        val bindingItem = ItemComentarioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComentario(bindingItem)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]
        val uidActual = FirebaseAuth.getInstance().uid

        // Setear datos básicos del comentario
        holder.bindingItem.TvTextoComentario.text = modelo.comentario

        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = modelo.tiempo
        holder.bindingItem.TvFechaComentario.text = DateFormat.format("dd/MM/yyyy", calendar).toString()

        cargarInfoUsuario(modelo, holder)

        // Solo cargamos respuestas si el comentario NO es ya una respuesta (para evitar bucles infinitos)
        if (modelo.idPadre.isEmpty()) {
            cargarRespuestas(modelo.idAnuncio, modelo.id, holder)
        } else {
            holder.bindingItem.RvRespuestas.visibility = View.GONE
        }

        // --- SISTEMA DE VOTOS ---
        comprobarLike(modelo, holder)
        contarLikes(modelo, holder)
        comprobarDislike(modelo, holder)
        contarDislikes(modelo, holder)

        // Botón Like
        holder.bindingItem.BtnLikeComento.setOnClickListener {
            if (uidActual == null) {
                Toast.makeText(context, "Inicia sesión", Toast.LENGTH_SHORT).show()
            } else {
                gestionarVoto(modelo, isLike = true)
            }
        }

        // Botón Dislike
        holder.bindingItem.BtnDislikeComento.setOnClickListener {
            if (uidActual == null) {
                Toast.makeText(context, "Inicia sesión", Toast.LENGTH_SHORT).show()
            } else {
                gestionarVoto(modelo, isLike = false)
            }
        }

        // Botón Responder
        holder.bindingItem.BtnResponderComento.setOnClickListener {
            if (context is DetallesAnuncio) {
                context.dialogResponderComentario(modelo)
            }
        }
    }

    // Función unificada para gestionar Like y Dislike sin errores de nulidad
    private fun gestionarVoto(modelo: ModeloComentario, isLike: Boolean) {
        val uid = FirebaseAuth.getInstance().uid ?: return

        // IMPORTANTE: Construimos la ruta dependiendo de si es comentario raíz o respuesta
        val ref = if (modelo.idPadre.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("Anuncios")
                .child(modelo.idAnuncio).child("Comentarios").child(modelo.id)
        } else {
            FirebaseDatabase.getInstance().getReference("Anuncios")
                .child(modelo.idAnuncio).child("Comentarios").child(modelo.idPadre)
                .child("Respuestas").child(modelo.id)
        }

        val nodoPrincipal = if (isLike) "Likes" else "Dislikes"
        val nodoOpuesto = if (isLike) "Dislikes" else "Likes"

        ref.child(nodoPrincipal).child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Si ya existe el voto, lo quitamos (Toggle off)
                    ref.child(nodoPrincipal).child(uid).removeValue()
                } else {
                    // Si no existe, quitamos el opuesto y ponemos el nuevo (Toggle on)
                    ref.child(nodoOpuesto).child(uid).removeValue()
                    ref.child(nodoPrincipal).child(uid).setValue(true)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun comprobarLike(modelo: ModeloComentario, holder: HolderComentario) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = obtenerReferenciaVotos(modelo).child("Likes").child(uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    holder.bindingItem.BtnLikeComento.setImageResource(R.drawable.like)
                } else {
                    holder.bindingItem.BtnLikeComento.setImageResource(R.drawable.anteslike)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun comprobarDislike(modelo: ModeloComentario, holder: HolderComentario) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = obtenerReferenciaVotos(modelo).child("Dislikes").child(uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    holder.bindingItem.BtnDislikeComento.setImageResource(R.drawable.dislike)
                } else {
                    holder.bindingItem.BtnDislikeComento.setImageResource(R.drawable.antesdislike)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun contarLikes(modelo: ModeloComentario, holder: HolderComentario) {
        obtenerReferenciaVotos(modelo).child("Likes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    holder.bindingItem.TvLikesComento.text = "${snapshot.childrenCount}"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun contarDislikes(modelo: ModeloComentario, holder: HolderComentario) {
        obtenerReferenciaVotos(modelo).child("Dislikes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    holder.bindingItem.TvDislikesComento.text = "${snapshot.childrenCount}"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Función auxiliar para no repetir código de rutas de Firebase
    private fun obtenerReferenciaVotos(modelo: ModeloComentario): DatabaseReference {
        return if (modelo.idPadre.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("Anuncios")
                .child(modelo.idAnuncio).child("Comentarios").child(modelo.id)
        } else {
            FirebaseDatabase.getInstance().getReference("Anuncios")
                .child(modelo.idAnuncio).child("Comentarios").child(modelo.idPadre)
                .child("Respuestas").child(modelo.id)
        }
    }

    private fun cargarInfoUsuario(modelo: ModeloComentario, holder: HolderComentario) {
        FirebaseDatabase.getInstance().getReference("Usuarios").child(modelo.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre = snapshot.child("nombres").value?.toString() ?: "Usuario"
                    holder.bindingItem.TvNombreComentario.text = nombre
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarRespuestas(idAnuncio: String, idComentarioPadre: String, holder: HolderComentario) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
            .child(idAnuncio).child("Comentarios").child(idComentarioPadre).child("Respuestas")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val respuestasList = ArrayList<ModeloComentario>()
                    for (ds in snapshot.children) {
                        val mod = ds.getValue(ModeloComentario::class.java)
                        if (mod != null) respuestasList.add(mod)
                    }
                    holder.bindingItem.RvRespuestas.visibility = View.VISIBLE
                    holder.bindingItem.RvRespuestas.layoutManager = LinearLayoutManager(context)
                    holder.bindingItem.RvRespuestas.adapter = AdaptadorComentario(context, respuestasList)
                } else {
                    holder.bindingItem.RvRespuestas.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int = comentarioArrayList.size

    inner class HolderComentario(val bindingItem: ItemComentarioBinding) : RecyclerView.ViewHolder(bindingItem.root)
}