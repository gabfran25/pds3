package com.example.appcomprayventa.Fragmentos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appcomprayventa.Adaptadores.AdaptadorAnuncio
import com.example.appcomprayventa.Modelo.ModeloAnuncio
import com.example.appcomprayventa.databinding.FragmentMisAnunciosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentMisAnuncios : Fragment() {

    private lateinit var binding: FragmentMisAnunciosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var anunciosArrayList: ArrayList<ModeloAnuncio>
    private lateinit var adaptadorAnuncio: AdaptadorAnuncio

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMisAnunciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        cargarMisAnuncios()
    }

    private fun cargarMisAnuncios() {
        anunciosArrayList = ArrayList()

        // Configurar el RecyclerView antes de la consulta
        binding.RVMisAnuncios.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        // El filtro por "uid" es correcto siempre y cuando en Firebase el campo diga "uid" en minúsculas
        ref.orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    anunciosArrayList.clear()
                    for (ds in snapshot.children) {
                        try {
                            val modelo = ds.getValue(ModeloAnuncio::class.java)
                            if (modelo != null) {
                                anunciosArrayList.add(modelo)
                            }
                        } catch (e: Exception) {
                            // Esto te avisará en el Logcat si hay un error de conversión
                            android.util.Log.e("ERROR_FIREBASE", "Error al convertir: ${e.message}")
                        }
                    }
                    // EL ADAPTADOR SE CREA FUERA DEL BUCLE FOR
                    adaptadorAnuncio = AdaptadorAnuncio(requireContext(), anunciosArrayList)
                    binding.RVMisAnuncios.adapter = adaptadorAnuncio
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("ERROR_FIREBASE", "Error en consulta: ${error.message}")
                }
            })
    }
}