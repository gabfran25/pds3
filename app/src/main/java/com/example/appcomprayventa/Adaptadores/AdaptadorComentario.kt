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

        // Setear datos básicos
        holder.bindingItem.TvTextoComentario.text = modelo.comentario

        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = modelo.tiempo
        holder.bindingItem.TvFechaComentario.text = DateFormat.format("dd/MM/yyyy", calendar).toString()

        cargarInfoUsuario(modelo, holder)

        // Solo intentamos cargar respuestas si es un comentario raíz (evita bucles)
        if (modelo.idPadre.isEmpty()) {
            cargarRespuestas(modelo.idAnuncio, modelo.id, holder)
        } else {
            holder.bindingItem.RvRespuestas.visibility = View.GONE
        }

        // --- GESTIÓN DE VOTOS ---
        comprobarLike(modelo, holder)
        contarLikes(modelo, holder)
        comprobarDislike(modelo, holder)
        contarDislikes(modelo, holder)

        holder.bindingItem.BtnLikeComento.setOnClickListener {
            if (uidActual == null) Toast.makeText(context, "Inicia sesión", Toast.LENGTH_SHORT).show()
            else gestionarVoto(modelo, isLike = true)
        }

        holder.bindingItem.BtnDislikeComento.setOnClickListener {
            if (uidActual == null) Toast.makeText(context, "Inicia sesión", Toast.LENGTH_SHORT).show()
            else gestionarVoto(modelo, isLike = false)
        }

        holder.bindingItem.BtnResponderComento.setOnClickListener {
            if (context is DetallesAnuncio) {
                context.dialogResponderComentario(modelo)
            }
        }
    }

    private fun gestionarVoto(modelo: ModeloComentario, isLike: Boolean) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = obtenerRefComentario(modelo)

        val nodoVoto = if (isLike) "Likes" else "Dislikes"
        val nodoOpuesto = if (isLike) "Dislikes" else "Likes"

        ref.child(nodoVoto).child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    ref.child(nodoVoto).child(uid).removeValue()
                } else {
                    ref.child(nodoOpuesto).child(uid).removeValue()
                    ref.child(nodoVoto).child(uid).setValue(true)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun comprobarLike(modelo: ModeloComentario, holder: HolderComentario) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        obtenerRefComentario(modelo).child("Likes").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val icon = if (snapshot.exists()) R.drawable.like else R.drawable.anteslike
                    holder.bindingItem.BtnLikeComento.setImageResource(icon)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun comprobarDislike(modelo: ModeloComentario, holder: HolderComentario) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        obtenerRefComentario(modelo).child("Dislikes").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val icon = if (snapshot.exists()) R.drawable.dislike else R.drawable.antesdislike
                    holder.bindingItem.BtnDislikeComento.setImageResource(icon)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun contarLikes(modelo: ModeloComentario, holder: HolderComentario) {
        obtenerRefComentario(modelo).child("Likes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.bindingItem.TvLikesComento.text = "${snapshot.childrenCount}"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun contarDislikes(modelo: ModeloComentario, holder: HolderComentario) {
        obtenerRefComentario(modelo).child("Dislikes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.bindingItem.TvDislikesComento.text = "${snapshot.childrenCount}"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun obtenerRefComentario(modelo: ModeloComentario): DatabaseReference {
        val baseRef = FirebaseDatabase.getInstance().getReference("Anuncios").child(modelo.idAnuncio).child("Comentarios")
        return if (modelo.idPadre.isEmpty()) baseRef.child(modelo.id)
        else baseRef.child(modelo.idPadre).child("Respuestas").child(modelo.id)
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
                        // SOLUCIÓN AL ERROR 1970: Solo agregar si el nodo tiene el campo "comentario"
                        if (ds.hasChild("comentario")) {
                            val mod = ds.getValue(ModeloComentario::class.java)
                            if (mod != null) respuestasList.add(mod)
                        }
                    }
                    if (respuestasList.isNotEmpty()) {
                        holder.bindingItem.RvRespuestas.visibility = View.VISIBLE
                        holder.bindingItem.RvRespuestas.layoutManager = LinearLayoutManager(context)
                        holder.bindingItem.RvRespuestas.adapter = AdaptadorComentario(context, respuestasList)
                    } else {
                        holder.bindingItem.RvRespuestas.visibility = View.GONE
                    }
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