package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ItemAnuncioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.appcomprayventa.Anuncios.DetallesAnuncio

class AdaptadorAnuncio(
    private val context: Context,
    private val anuncioArrayList: ArrayList<ModeloAnuncio>
) : RecyclerView.Adapter<AdaptadorAnuncio.HolderAnuncio>() {

    private lateinit var binding: ItemAnuncioBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {
        binding = ItemAnuncioBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAnuncio(binding.root)
    }

    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {
        val modelo = anuncioArrayList[position]

        // Seteamos los textos
        holder.tvTitulo.text = modelo.titulo
        holder.tvPrecio.text = "$${modelo.precio}"
        holder.tvCondicion.text = modelo.condicion

        // Cargamos la primera imagen del anuncio desde Firebase
        cargarPrimeraImagen(modelo, holder)

        // Evento al dar click en el anuncio (Lo usaremos para ver detalles después)
        holder.itemView.setOnClickListener {
            // Aquí irá la lógica para abrir el Activity de detalles
            val intent = android.content.Intent(context, DetallesAnuncio::class.java)
            intent.putExtra("idAnuncio", modelo.id) // Pasamos el ID para saber qué anuncio mostrar
            context.startActivity(intent)
        }
    }

    private fun cargarPrimeraImagen(modelo: ModeloAnuncio, holder: HolderAnuncio) {
        val idAnuncio = modelo.id
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(context)
                                .load(imagenUrl)
                                .placeholder(R.drawable.item_imagen)
                                .into(holder.imgAnuncio)
                        } catch (e: Exception) { }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int = anuncioArrayList.size

    inner class HolderAnuncio(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAnuncio = binding.ImgAnuncio
        val tvTitulo = binding.TvTitulo
        val tvPrecio = binding.TvPrecio
        val tvCondicion = binding.TvCondicion
    }
}