package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.R

class AdaptadorImagenSlider(
    private val context: Context,
    private val imagenArrayList: ArrayList<String> // Aquí recibes la lista de URLs de Firebase
) : RecyclerView.Adapter<AdaptadorImagenSlider.HolderImagen>() {

    inner class HolderImagen(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.Img_Slider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagen {
        val view = LayoutInflater.from(context).inflate(R.layout.item_imagen_slider, parent, false)
        return HolderImagen(view)
    }

    override fun onBindViewHolder(holder: HolderImagen, position: Int) {
        val urlImagen = imagenArrayList[position]
        Glide.with(context)
            .load(urlImagen)
            .placeholder(R.drawable.item_imagen)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imagenArrayList.size
}