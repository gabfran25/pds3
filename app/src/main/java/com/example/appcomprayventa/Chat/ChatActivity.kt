package com.example.appcomprayventa.Chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.appcomprayventa.Adaptadores.AdaptadorChat
import com.example.appcomprayventa.Constantes
import com.example.appcomprayventa.Modelo.Chat
import com.example.appcomprayventa.EditarPerfil
import com.example.appcomprayventa.R
import com.example.appcomprayventa.databinding.ActivityChatBinding
import com.google.firebase.FirebaseAppLifecycleListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    private var uid = ""

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var miUid = ""
    private var chatRuta = ""
    private var imagenUri : Uri? = null

    private var yoBloqueado = false
    private lateinit var referenceVisto : com.google.firebase.database.DatabaseReference

    private var vistoListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        uid = intent.getStringExtra("uid")!!
        miUid = firebaseAuth.uid!!

        chatRuta = Constantes.rutaChat(uid, miUid)

        binding.adjuntarFAB.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                imagenGaleria()
            } else {
                solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.enviarFAB.setOnClickListener {
            validarMensaje()
        }

        binding.IbMasOpciones.setOnClickListener {
            verMasOpciones() // Se dispara al tocar los tres puntos
        }

        cargarInfo()
        cargarMensajes()
        verificarBloqueo()
        mensajeVisto()
    }

    private fun cargarMensajes() {
        val mensajesArrayList = ArrayList<Chat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatRuta)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mensajesArrayList.clear()
                    for (ds : DataSnapshot in snapshot.children) {
                        try {
                            val chat = ds.getValue(Chat::class.java)
                            mensajesArrayList.add(chat!!)
                        } catch (e: Exception) {
                            Log.e("FirebaseError","Error al cargar los mensajes: ${e.message}")
                        }
                    }
                    val adaptadorChat = AdaptadorChat(this@ChatActivity, mensajesArrayList)
                    binding.chatsRV.adapter = adaptadorChat
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error al cargar los mensajes: ${error.message}")
                }
            })
    }

    private fun validarMensaje() {
        val mensaje = binding.EtMensajeChat.text.toString().trim()
        val tiempo = Constantes.obtenerTiempoDis()

        if (mensaje.isEmpty()) {
            Toast.makeText(this, "Ingrese un mensaje", Toast.LENGTH_SHORT).show()
        } else {
            enviarMensaje(Constantes.MENSAJE_TIPO_TEXTO, mensaje, tiempo)
        }
    }

    private fun cargarInfo() {
        // Referencia al usuario con el que estamos hablando (usando la variable 'uid')
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Obtener datos del receptor
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"

                    // Establecer el nombre en el TextView de tu barra de chat
                    // Nota: Ajusta 'TvNombreChat' por el ID real que tengas en activity_chat.xml
                    binding.TxtNombreUsuario.text = nombres

                    // Cargar la imagen de perfil del receptor
                    try {
                        Glide.with(this@ChatActivity)
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.ToolbarIV) // Ajusta el ID según tu XML
                    } catch (e: Exception) {
                        Log.e("ChatInfo", "Error al cargar imagen: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error al cargar info del usuario: ${error.message}")
                }
            })



    }

    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleriaARL.launch(intent)
    }

    private val resultadoGaleriaARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                val data = resultado.data
                imagenUri = data!!.data
                subirImgStorage()
            } else {
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val solicitarPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            if (esConcedido) {
                imagenGaleria()
            } else {
                Toast.makeText(
                this,
                "El permiso de almacenamiento no ha sido concedido",
                Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun subirImgStorage() {
        progressDialog.setMessage("Subiendo imagen")
        progressDialog.show()

        val tiempo = Constantes.obtenerTiempoDis()
        val nombreRutaImg = "ImagenesChat/$tiempo"
        val storageRef = FirebaseStorage.getInstance().getReference(nombreRutaImg)
        storageRef.putFile(imagenUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                var urlImagen = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    enviarMensaje(Constantes.MENSAJE_TIPO_IMAGEN,urlImagen,tiempo)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    "No se pudo enviar la imagen debido a ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }

    }

    private fun enviarMensaje(tipoMensaje: String, mensaje: String, tiempo: Long) {
        progressDialog.setMessage("Enviando mensaje")
        progressDialog.show()

        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String,Any>()

        hashMap["idMensaje"] = "${keyId}"
        hashMap["tipoMensaje"] = "${tipoMensaje}"
        hashMap["mensaje"] = "${mensaje}"
        hashMap["emisorUid"] = "${miUid}"
        hashMap["receptorUid"] = "${uid}"
        hashMap["tiempo"] = tiempo

        refChat.child(chatRuta)
            .child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.EtMensajeChat.setText("")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se pudo enviar el mensaje debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun mensajeVisto() {
        referenceVisto = FirebaseDatabase.getInstance().getReference("Chats").child(chatRuta)
        vistoListener = referenceVisto.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val chat = ds.getValue(Chat::class.java)
                    if (chat != null && chat.receptorUid == miUid && !chat.visto) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["visto"] = true
                        ds.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun verMasOpciones() {
        val popupMenu = PopupMenu(this, binding.IbMasOpciones)
        popupMenu.menu.add(0,0,0,if (yoBloqueado) "Desbloquear usuario" else "Bloquear usuario")

        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == 0) {
                if (yoBloqueado) {
                    desbloquearUsuario()
                } else {
                    bloquearUsuario()
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun bloquearUsuario(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(miUid).child("Bloqueados").child(uid).setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario bloqueado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun desbloquearUsuario(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(miUid).child("Bloqueados").child(uid).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario desbloqueado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun verificarBloqueo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")

        // Verificar si YO bloqueé al usuario
        ref.child(miUid).child("Bloqueados").child(uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    yoBloqueado = snapshot.exists()
                    actualizarUIBloqueo()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // Verificar si el USUARIO me bloqueó a mí
        ref.child(uid).child("Bloqueados").child(miUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.EtMensajeChat.hint = "No puedes enviar mensajes a este usuario"
                        binding.EtMensajeChat.isEnabled = false
                        binding.enviarFAB.isEnabled = false
                        binding.adjuntarFAB.isEnabled = false
                    } else if (!yoBloqueado) {
                        // Solo habilitar si yo tampoco lo tengo bloqueado
                        binding.EtMensajeChat.hint = "Escribe un mensaje... "
                        binding.EtMensajeChat.isEnabled = true
                        binding.enviarFAB.isEnabled = true
                        binding.adjuntarFAB.isEnabled = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }

    private fun actualizarUIBloqueo() {
        if (yoBloqueado) {
            binding.EtMensajeChat.hint = "Has bloqueado a este usuario"
            binding.EtMensajeChat.isEnabled = false
            binding.enviarFAB.isEnabled = false
            binding.adjuntarFAB.isEnabled = false
        } else {
            // Se habilita solo si el otro no nos tiene bloqueado ( se maneja en verificarBloqueo)
            binding.EtMensajeChat.hint = "Escribe un mensaje..."
            binding.EtMensajeChat.isEnabled = true
            binding.enviarFAB.isEnabled = true
            binding.adjuntarFAB.isEnabled = true
        }
    }

}