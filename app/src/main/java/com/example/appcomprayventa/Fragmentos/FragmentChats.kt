package com.example.appcomprayventa.Fragmentos

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appcomprayventa.Adaptadores.AdaptadorUsuario
import com.example.appcomprayventa.Modelos.Usuario
import com.example.appcomprayventa.databinding.FragmentChatsBinding
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FragmentChats : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var mContext: Context
    private var usuarioAdaptador: AdaptadorUsuario? = null
    private var usuarioLista: List<Usuario>? = null

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        binding = FragmentChatsBinding.inflate(layoutInflater, container, false)
        binding.RVUsuarios.setHasFixedSize(true)
        binding.RVUsuarios.layoutManager = LinearLayoutManager(mContext)

        usuarioLista = ArrayList()

        binding.EtBuscarUsuario.doOnTextChanged { usuario, start, before, count ->
            buscarUsuario(usuario.toString())
        }

        listarUsuarios()

        return binding.root

    }

    private fun listarUsuarios() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios").orderByChild("nombres")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (usuarioLista as ArrayList<Usuario>).clear()

                for (sn in snapshot.children){
                    val usuario : Usuario? = sn.getValue(Usuario::class.java)

                    // Filtramos para no mostrarnos a nosotros mismos
                    if(!(usuario!!.uid).equals(firebaseUser)){
                        (usuarioLista as ArrayList<Usuario>).add(usuario)
                    }
                }

                //Si la lista está vacía, mostramos el mensaje y ocultamos el RecyclerView
                if ((usuarioLista as java.util.ArrayList<Usuario>).isEmpty()) {
                    binding.tvSinUsuario.visibility = View.VISIBLE
                    binding.RVUsuarios.visibility = View.GONE

                // Si hay más usuarios, ocultamos el mensaje y mostramos la lista
                } else {
                    binding.tvSinUsuario.visibility = View.GONE
                    binding.RVUsuarios.visibility = View.VISIBLE

                    // Actualizamos el adaptador
                    usuarioAdaptador = AdaptadorUsuario(mContext, usuarioLista!!)
                    binding.RVUsuarios.adapter = usuarioAdaptador
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error al leer usuarios: ${error.message}")
                Toast.makeText(mContext,
                    "Error al cargar datos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun buscarUsuario(usuario : String) {
        // Obtenemos el uid del usuario actual y gestionamos la búsqueda a través del nombre
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = FirebaseDatabase.getInstance().reference
            .child("Usuarios")
            .orderByChild("nombres")
            .startAt(usuario)
            .endAt(usuario + "\uf8ff")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (usuarioLista as ArrayList<Usuario>).clear()

                for (ss in snapshot.children) {
                    val usuario : Usuario ?= ss.getValue(Usuario::class.java)

                    // Filtramos para no buscarnos a nosotros mismos
                    if (!(usuario!!.uid).equals(firebaseUser)) {
                        (usuarioLista as ArrayList<Usuario>).add(usuario)
                    }
                }

                // Actualizamos el adaptador
                usuarioAdaptador = AdaptadorUsuario(context!!,usuarioLista!!)
                binding.RVUsuarios.adapter = usuarioAdaptador
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error al buscar a los usuarios: ${error.message}")
                Toast.makeText(mContext,
                    "Error al buscar: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}