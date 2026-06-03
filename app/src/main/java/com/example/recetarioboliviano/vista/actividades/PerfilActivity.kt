package com.example.recetarioboliviano.vista.actividades

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.recetarioboliviano.databinding.ActivityPerfilBinding
import com.example.recetarioboliviano.modelo.entidades.Usuario
import com.example.recetarioboliviano.modelo.util.Constantes
import com.example.recetarioboliviano.modelo.util.ImageHelper
import com.example.recetarioboliviano.vistamodelo.UsuarioViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private val viewModel: UsuarioViewModel by viewModels()

    private var usuarioActual: Usuario? = null
    private var nuevoAvatarUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) tomarFoto()
        else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) seleccionarDeGaleria()
        else Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                val uri = Uri.fromFile(File(path))
                nuevoAvatarUri = uri
                ImageHelper.cargarAvatar(binding.ivAvatar, uri.toString())
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = ImageHelper.copiarImagenAArchivoLocal(this, it)
            if (localPath != null) {
                val localUri = Uri.fromFile(File(localPath))
                nuevoAvatarUri = localUri
                ImageHelper.cargarAvatar(binding.ivAvatar, localUri.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSpinner()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constantes.DEPARTAMENTOS_BOLIVIA)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDepartamento.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.usuarioActual.observe(this) { usuario ->
            if (usuario != null) {
                usuarioActual = usuario
                // Solo rellenamos los campos si están vacíos para no molestar al usuario mientras escribe
                if (binding.etNombre.text.isNullOrBlank()) {
                    mostrarDatosUsuario(usuario)
                }
            }
        }
    }

    private fun mostrarDatosUsuario(usuario: Usuario) {
        binding.etNombre.setText(usuario.nombre)
        binding.etPais.setText(usuario.pais)
        val deptoIndex = Constantes.DEPARTAMENTOS_BOLIVIA.indexOf(usuario.departamento)
        if (deptoIndex >= 0) binding.spinnerDepartamento.setSelection(deptoIndex)
        ImageHelper.cargarAvatar(binding.ivAvatar, usuario.avatarUri)
    }

    private fun setupClickListeners() {
        binding.btnCambiarAvatar.setOnClickListener { mostrarDialogoSeleccionarImagen() }
        binding.btnGuardar.setOnClickListener { 
            Toast.makeText(this, "Guardando...", Toast.LENGTH_SHORT).show()
            guardarCambios() 
        }
    }

    private fun mostrarDialogoSeleccionarImagen() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        AlertDialog.Builder(this)
            .setTitle("Cambiar Avatar")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamara()
                    1 -> verificarPermisoAlmacenamiento()
                }
            }
            .show()
    }

    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            tomarFoto()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun verificarPermisoAlmacenamiento() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            seleccionarDeGaleria()
        } else {
            storagePermissionLauncher.launch(permission)
        }
    }

    private fun tomarFoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        val photoFile = File.createTempFile("AVATAR_${timeStamp}_", ".jpg", storageDir)
        currentPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(photoUri)
    }

    private fun seleccionarDeGaleria() {
        pickImageLauncher.launch("image/*")
    }

    private fun guardarCambios() {
        val nombre = binding.etNombre.text.toString().trim()
        val pais = binding.etPais.text.toString().trim()
        val departamento = binding.spinnerDepartamento.selectedItem?.toString() ?: ""

        if (nombre.isEmpty()) {
            binding.etNombre.error = "Ingresa tu nombre"
            return
        }

        // Si usuarioActual es null, creamos uno nuevo con ID 1
        // Si ya existe, usamos .copy() para mantener su ID y actualizar datos
        val usuarioA_Guardar = if (usuarioActual == null) {
            Usuario(
                id = 1, // Forzamos ID 1 para que sea el usuario principal
                nombre = nombre,
                departamento = departamento,
                pais = pais,
                avatarUri = nuevoAvatarUri?.toString()
            )
        } else {
            usuarioActual!!.copy(
                nombre = nombre,
                departamento = departamento,
                pais = pais,
                avatarUri = nuevoAvatarUri?.toString() ?: usuarioActual!!.avatarUri
            )
        }

        // Usamos registrarUsuario si es nuevo o actualizarUsuario si ya existe
        if (usuarioActual == null) {
            viewModel.registrarUsuario(usuarioA_Guardar) { success, error ->
                manejarResultado(success, error)
            }
        } else {
            viewModel.actualizarUsuario(usuarioA_Guardar) { success, error ->
                manejarResultado(success, error)
            }
        }
    }

    private fun manejarResultado(success: Boolean, error: String?) {
        if (success) {
            Toast.makeText(this, "¡Perfil guardado correctamente!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
        }
    }
}
