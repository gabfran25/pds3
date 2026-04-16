package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appcomprayventa.Anuncios.DetallesAnuncio
import com.example.appcomprayventa.Modelo.ModeloComentario
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemComentarioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class AdaptadorComentario(
    private val context: Context,
    private val comentarioArrayList: ArrayList<ModeloComentario>
) : RecyclerView.Adapter<AdaptadorComentario.HolderComentario>() {

    // private lateinit var binding: ItemComentarioBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        // Inflamos el binding para cada item individualmente
        val layoutInflater = LayoutInflater.from(parent.context)
        val bindingItem = ItemComentarioBinding.inflate(layoutInflater, parent, false)
        return HolderComentario(bindingItem)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]
        val uidActual = com.google.firebase.auth.FirebaseAuth.getInstance().uid!!

        val idAnuncio = modelo.idAnuncio
        val idComentario = modelo.id
        val uid = modelo.uid
        val comentario = modelo.comentario
        val tiempo = modelo.tiempo

        // Formatear fecha
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = tiempo
        val fecha = DateFormat.format("dd/MM/yyyy", calendar).toString()

        // Setear datos
        holder.tvTexto.text = comentario
        holder.tvFecha.text = fecha

        // Cargar nombre del usuario desde Firebase (opcional si tienes tabla Usuarios)
        cargarInfoUsuario(modelo, holder)
        cargarRespuestas(modelo.idAnuncio,modelo.id,holder)

        holder.itemView.setOnClickListener {
            // Llamamos a una función en DetallesAnuncio para responder
            if (context is DetallesAnuncio) {
                context.dialogResponderComentario(modelo)
            }
        }

        comprobarLike(modelo,holder)
        contarLikes(modelo,holder)

        holder.bindingItem.BtnLikeComento.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
                .child(modelo.idAnuncio).child("Comentarios").child(modelo.id).child("Likes")

            ref.child(uidActual).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        ref.child(uidActual).removeValue() // Quitar like
                    } else {
                        ref.child(uidActual).setValue(true) // Dar like
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun cargarRespuestas(idAnuncio: String, idComentarioPadre: String, holder: HolderComentario) {
        val respuestasList = ArrayList<ModeloComentario>()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")

        ref.child(idAnuncio).child("Comentarios").child(idComentarioPadre).child("Respuestas")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        respuestasList.clear()
                        for (ds in snapshot.children) {
                            val modelo = ds.getValue(ModeloComentario::class.java)
                            if (modelo != null) respuestasList.add(modelo)
                        }

                        // Configurar el RecyclerView interno
                        holder.bindingItem.RvRespuestas.visibility = View.VISIBLE
                        holder.bindingItem.RvRespuestas.layoutManager = LinearLayoutManager(context)
                        val adaptadorRespuestas = AdaptadorComentario(context, respuestasList)
                        holder.bindingItem.RvRespuestas.adapter = adaptadorRespuestas
                    } else {
                        holder.bindingItem.RvRespuestas.visibility = View.GONE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarInfoUsuario(modelo: ModeloComentario, holder: HolderComentario) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(modelo.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre = "${snapshot.child("nombres").value}"
                    holder.tvNombre.text = nombre
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int = comentarioArrayList.size

    inner class HolderComentario(val bindingItem: ItemComentarioBinding) : RecyclerView.ViewHolder(bindingItem.root) {
        val tvNombre = bindingItem.TvNombreComentario
        val tvTexto = bindingItem.TvTextoComentario
        val tvFecha = bindingItem.TvFechaComentario
        val rvRespuestas = bindingItem.RvRespuestas // Agrega esto también para que sea más fácil de usar
    }

    private fun comprobarLike(modelo: ModeloComentario, holder: HolderComentario) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(modelo.idAnuncio).child("Comentarios").child(modelo.id).child("Likes")
            .child(com.google.firebase.auth.FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        holder.bindingItem.BtnLikeComento.setImageResource(R.drawable.like)
                    } else {
                        holder.bindingItem.BtnLikeComento.setImageResource(R.drawable.dislike)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun contarLikes(modelo: ModeloComentario, holder: HolderComentario) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(modelo.idAnuncio).child("Comentarios").child(modelo.id).child("Likes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val numeroLikes = snapshot.childrenCount
                    holder.bindingItem.TvLikesComento.text = "$numeroLikes"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}