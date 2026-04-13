package com.example.appcomprayventa.Fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appcomprayventa.Adaptadores.AdaptadorUsuario
import com.example.appcomprayventa.Modelos.Usuario
import com.example.appcomprayventa.databinding.FragmentChatsBinding
import android.content.Context
import android.view.View


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
        return binding.root

    }


}