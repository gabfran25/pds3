package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appcomprayventa.Modelo.ModeloComentario
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

    private lateinit var binding: ItemComentarioBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        binding = ItemComentarioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComentario(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]

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

    inner class HolderComentario(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre = binding.TvNombreComentario
        val tvTexto = binding.TvTextoComentario
        val tvFecha = binding.TvFechaComentario
    }
}