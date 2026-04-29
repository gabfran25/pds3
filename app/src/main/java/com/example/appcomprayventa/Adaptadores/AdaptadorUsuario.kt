package com.example.appcomprayventa.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Chat.ChatActivity
import com.example.appcomprayventa.Modelos.Usuario
import com.example.appcomprayventa.R


class AdaptadorUsuario(context: Context, listaUsuario: List<Usuario>): RecyclerView.Adapter<AdaptadorUsuario.ViewHolder?>() {

    private val context: Context
    private val listaUsuario: List<Usuario>

    init {
        this.context = context
        this.listaUsuario = listaUsuario
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return listaUsuario.size
    }

    override fun onBindViewHolder(holder: AdaptadorUsuario.ViewHolder, position: Int) {
        val usuario: Usuario = listaUsuario[position]
        holder.uid.text = usuario.uid
        holder.email.text = usuario.email
        holder.nombres.text = usuario.nombres
        Glide.with(context).load(usuario.imagen).placeholder(R.drawable.img_perfil).into(holder.imagen)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uid", holder.uid.text)
            Toast.makeText(context,
                "Has seleccionado al usuario: ${holder.nombres.text}",
                Toast.LENGTH_SHORT).show()
            context.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var uid: TextView
        var email: TextView
        var nombres:TextView
        var imagen: ImageView

        init {
            uid = itemView.findViewById(R.id.item_uid)
            email = itemView.findViewById(R.id.item_email)
            nombres = itemView.findViewById(R.id.item_nombre)
            imagen = itemView.findViewById(R.id.item_imagen)
        }

    }

}
