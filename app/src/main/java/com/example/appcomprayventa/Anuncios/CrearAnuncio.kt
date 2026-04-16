package com.example.appcomprayventa.Anuncios

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Adaptadores.AdaptadorImagenSeleccionada
import com.example.appcomprayventa.Modelo.ModeloImagenSeleccionada
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityCrearAnuncioBinding
import com.google.firebase.auth.FirebaseAuth

class CrearAnuncio : AppCompatActivity() {
    private lateinit var binding : ActivityCrearAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    private var imagenUri : Uri?=null

    private lateinit var imagenSelecArrayList : ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSel : AdaptadorImagenSeleccionada

    // ... tus otras variables (firebaseAuth, progressDialog, etc)
    private var idAnuncio = ""
    private var marca = ""
    private var categoria = ""
    private var condicion = ""
    private var precio = ""
    private var titulo = ""
    private var descripcion = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        val adaptadorCat = ArrayAdapter(this, R.layout.item_categoria, Constantes.categorias)
        binding.Categoria.setAdapter(adaptadorCat)

        val adaptadorCon = ArrayAdapter(this, R.layout.item_condicion, Constantes.condiciones)
        binding.Condicion.setAdapter(adaptadorCon)

        // Inicializamos la lista y el adaptador
        imagenSelecArrayList = ArrayList()
        adaptadorImagenSel = AdaptadorImagenSeleccionada(this, imagenSelecArrayList)
        binding.RVImagenes.adapter = adaptadorImagenSel

        // Le damos clic al ícono de la imagen grande
        binding.agregarImg.setOnClickListener {
            selec_imagen_de()
        }

        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
        }

    }

    private fun validarDatos() {
        marca = binding.EtMarca.text.toString().trim()
        categoria = binding.Categoria.text.toString().trim()
        condicion = binding.Condicion.text.toString().trim()
        precio = binding.EtPrecio.text.toString().trim()
        titulo = binding.EtTitulo.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim() // Verifica que este sea el ID de tu EditText de descripción

        if (titulo.isEmpty() || descripcion.isEmpty() || precio.isEmpty() || categoria.isEmpty() || condicion.isEmpty()) {
            android.widget.Toast.makeText(this, "Por favor llena todos los campos", android.widget.Toast.LENGTH_SHORT).show()
        } else if (imagenSelecArrayList.isEmpty()) {
            android.widget.Toast.makeText(this, "Agrega al menos una imagen", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            // Si todo está lleno, subimos a la base de datos
            subirAnuncioBD()
        }
    }

    private fun subirAnuncioBD() {
        progressDialog.setMessage("Publicando anuncio...")
        progressDialog.show()

        val tiempo = System.currentTimeMillis()
        idAnuncio = tiempo.toString()

        // Creamos un "paquete" con toda la info
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = idAnuncio
        hashMap["uid"] = firebaseAuth.uid!! // Quien creó el anuncio
        hashMap["marca"] = marca
        hashMap["categoria"] = categoria
        hashMap["condicion"] = condicion
        hashMap["precio"] = precio
        hashMap["titulo"] = titulo
        hashMap["descripcion"] = descripcion
        hashMap["tiempo"] = tiempo
        // Dejamos preparados los contadores para después
        hashMap["likes"] = 0
        hashMap["comentarios"] = 0

        // Subimos a Realtime Database
        val ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).setValue(hashMap)
            .addOnSuccessListener {
                // Si el texto se subió bien, ahora subimos las fotos
                subirImagenesStorage()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                android.widget.Toast.makeText(this, "Error al subir: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirImagenesStorage() {
        val rutaCarpeta = "Anuncios/$idAnuncio"
        var imagenesSubidas = 0

        for (i in 0 until imagenSelecArrayList.size) {
            val modeloImagen = imagenSelecArrayList[i]
            val nombreImagen = modeloImagen.id
            val rutaArchivo = "$rutaCarpeta/$nombreImagen"

            val reference = com.google.firebase.storage.FirebaseStorage.getInstance().getReference(rutaArchivo)
            reference.putFile(modeloImagen.imagenUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Obtenemos el link de la imagen en internet
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = modeloImagen.id
                        hashMap["imagenUrl"] = uri.toString()

                        // Guardamos ese link en la base de datos, adentro del anuncio
                        val refBd = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Anuncios")
                        refBd.child(idAnuncio).child("Imagenes").child(modeloImagen.id).setValue(hashMap)

                        imagenesSubidas++
                        // Si ya se subieron todas las fotos de la lista
                        if (imagenesSubidas == imagenSelecArrayList.size) {
                            progressDialog.dismiss()
                            android.widget.Toast.makeText(this@CrearAnuncio, "¡Anuncio publicado con éxito!", android.widget.Toast.LENGTH_SHORT).show()
                            finish() // Cierra la pantalla de crear anuncio
                        }
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    android.widget.Toast.makeText(this, "Error en imagen: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun selec_imagen_de() {
        // OJO: Aquí cambiamos FABCambiarImg por agregarImg que es el ID de tu pantalla de anuncio
        val popupMenu = android.widget.PopupMenu(this, binding.agregarImg)

        popupMenu.menu.add(android.view.Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(android.view.Menu.NONE, 2, 2, "Galería")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                // camara
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
                    concederPermisosCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    concederPermisosCamara.launch(arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            } else if (itemId == 2) {
                // galería
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
                    imagenGaleria()
                } else {
                    concederPermisosAlmacenamiento.launch(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private val concederPermisosCamara =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
            var concedidoTodos = true
            for (seConcede in resultado.values) {
                concedidoTodos = concedidoTodos && seConcede
            }

            if(concedidoTodos) {
                imagenCamara()
            } else {
                android.widget.Toast.makeText(
                    this,
                    "El permiso de la cámara o almacenamiento se denegaron.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val concederPermisosAlmacenamiento =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { resultado ->
            if(resultado) {
                imagenGaleria()
            } else {
                android.widget.Toast.makeText(this, "No se concedieron los permisos", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

    private fun imagenGaleria() {
        val intent = android.content.Intent(android.content.Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private fun imagenCamara() {
        val contentValues = android.content.ContentValues()
        contentValues.put(android.provider.MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(android.provider.MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imagenUri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()){ resultado->
            if(resultado.resultCode == RESULT_OK) {
                val data = resultado.data
                imagenUri = data!!.data

                // --- LA SOLUCIÓN ESTÁ AQUÍ ---
                val modelo = ModeloImagenSeleccionada() // 1. Creamos el modelo vacío
                modelo.id = System.currentTimeMillis().toString() // Generamos un ID único temporal
                modelo.imagenUri = imagenUri // 2. Le pasamos el Uri directamente
                modelo.deInternet = false

                imagenSelecArrayList.add(modelo)
                adaptadorImagenSel.notifyDataSetChanged()

            } else {
                android.widget.Toast.makeText(this, "La selección de imagen se canceló", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

    private val resultadoCamara_ARL =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { resultado->
            if(resultado.resultCode == RESULT_OK) {

                // --- LA MISMA SOLUCIÓN AQUÍ ---
                val modelo = ModeloImagenSeleccionada()
                modelo.id = System.currentTimeMillis().toString()
                modelo.imagenUri = imagenUri
                modelo.deInternet = false

                imagenSelecArrayList.add(modelo)
                adaptadorImagenSel.notifyDataSetChanged()

            } else {
                android.widget.Toast.makeText(this, "La captura de imagen se canceló", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

}