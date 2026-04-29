package com.example.appcomprayventa


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appcomprayventa.Anuncios.CrearAnuncio
import com.example.appcomprayventa.Fragmentos.FragmentInicio
import com.example.appcomprayventa.Fragmentos.FragmentCuenta
import com.example.appcomprayventa.Fragmentos.FragmentChats
import com.example.appcomprayventa.Fragmentos.FragmentMisAnuncios
import com.example.appcomprayventa.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseStorage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        verFragmentInicio()
        binding.BottomNV.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.Item_Inicio->{
                    verFragmentInicio()
                    true
                }
                R.id.Item_Chats->{
                    verFragmentChats()
                    true
                }
                R.id.Item_Mis_Anuncios->{
                    verFragmentMisAnuncios()
                    true
                }
                R.id.Item_Cuenta->{
                    verFragmentCuenta()
                    true
                }
                else -> {
                    false
                }
            }
        }

        binding.FAB.setOnClickListener {
            startActivity(Intent(this, CrearAnuncio::class.java))
        }
    }

    private fun comprobarSesion(){
        if (firebaseAuth.currentUser == null){
            startActivity(Intent(this, OpcionesLogin::class.java))
            finishAffinity()
        }
    }
    private fun verFragmentInicio(){
        binding.TituloRL.text = "Inicio"
        val fragment = FragmentInicio()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmenteTransition.commit()
    }
    private fun verFragmentChats(){
        binding.TituloRL.text = "Chats"
        val fragment = FragmentChats()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentChats")
        fragmenteTransition.commit()
    }
    private fun verFragmentMisAnuncios(){
        binding.TituloRL.text = "Mis Anuncios"
        val fragment = FragmentMisAnuncios()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentMisAnuncios")
        fragmenteTransition.commit()
    }
    private fun verFragmentCuenta(){
        binding.TituloRL.text = "Cuenta"
        val fragment = FragmentCuenta()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmenteTransition.commit()
    }
}