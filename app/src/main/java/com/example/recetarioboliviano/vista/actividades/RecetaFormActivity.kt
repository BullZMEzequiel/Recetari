package com.example.recetarioboliviano.vista.actividades

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.databinding.ActivityRecetaFormBinding
import com.example.recetarioboliviano.modelo.entidades.PasoPreparacion
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.util.Constantes
import com.example.recetarioboliviano.modelo.util.ImageHelper
import com.example.recetarioboliviano.vistamodelo.RecetaViewModel
import com.example.recetarioboliviano.vistamodelo.UsuarioViewModel
import com.example.recetarioboliviano.vista.adaptadores.PasoAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity para crear o editar recetas.
 */
class RecetaFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecetaFormBinding
    private val viewModel: RecetaViewModel by viewModels()
    private val usuarioViewModel: UsuarioViewModel by viewModels()

    private var imagenUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var recetaExistente: Receta? = null
    private var esModoEdicion: Boolean = false

    // Pasos de preparación
    private val pasos = mutableListOf<PasoPreparacion>()
    private lateinit var pasoAdapter: PasoAdapter
    private var pasoEditandoImagen: Int = -1

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tomarFoto()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            seleccionarDeGaleria()
        } else {
            Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                val uri = Uri.fromFile(File(path))
                val optimizedPath = ImageHelper.copiarImagenAArchivoLocal(this, uri)
                if (optimizedPath != null) {
                    // Borrar el temporal original pesado
                    File(path).delete()
                    val optimizedUri = Uri.fromFile(File(optimizedPath))
                    imagenUri = optimizedUri // 👈 Guardamos el URI local optimizado
                    ImageHelper.cargarImagen(binding.ivReceta, optimizedUri.toString())
                }
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
                imagenUri = localUri // 👈 Guardamos el URI local optimizado
                ImageHelper.cargarImagen(binding.ivReceta, localUri.toString())
            }
        }
    }

    // Para imágenes de pasos
    private val takePictureLauncherForPaso = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pasoEditandoImagen > 0) {
            currentPhotoPath?.let { path ->
                val uri = Uri.fromFile(File(path))
                val optimizedPath = ImageHelper.copiarImagenAArchivoLocal(this, uri)
                if (optimizedPath != null) {
                    File(path).delete()
                    val localUri = Uri.fromFile(File(optimizedPath))
                    actualizarImagenPaso(pasoEditandoImagen, localUri.toString())
                }
            }
        }
    }

    private val pickImageLauncherForPaso = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = ImageHelper.copiarImagenAArchivoLocal(this, it)
            if (pasoEditandoImagen > 0 && path != null) {
                val localUri = Uri.fromFile(File(path))
                actualizarImagenPaso(pasoEditandoImagen, localUri.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecetaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recetaId = intent.getIntExtra("receta_id", -1)
        if (recetaId != -1) {
            esModoEdicion = true
            cargarRecetaExistente(recetaId)
        }

        setupToolbar()
        setupUserHeader()
        setupSpinners()
        setupPasosRecyclerView()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (esModoEdicion) "Editar Receta" else "Nueva Receta"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupUserHeader() {
        usuarioViewModel.usuarioActual.observe(this) { usuario ->
            if (usuario != null) {
                binding.tvNombreUsuario.text = usuario.nombre
                binding.tvSaludo.text = "¡Hola!"
                ImageHelper.cargarAvatar(binding.ivAvatar, usuario.avatarUri.orEmpty())
            }
        }
    }

    private fun setupSpinners() {
        // Spinner Departamentos
        val adapterDeptos = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Constantes.DEPARTAMENTOS_BOLIVIA
        )
        adapterDeptos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDepartamento.adapter = adapterDeptos

        // Spinner Categorías
        val adapterCategorias = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Constantes.CATEGORIAS
        )
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapterCategorias

        // Spinner Unidad de Tiempo (Nuevo)
        val unidades = arrayOf("min", "h")
        val adapterUnidades = ArrayAdapter(this, android.R.layout.simple_spinner_item, unidades)
        adapterUnidades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUnidadTiempo.adapter = adapterUnidades
    }

    private fun setupPasosRecyclerView() {
        pasoAdapter = PasoAdapter(
            onEliminarPaso = { numero -> eliminarPaso(numero) },
            onAgregarImagen = { numero -> mostrarDialogoImagenPaso(numero) },
            onQuitarImagen = { numero -> quitarImagenPaso(numero) },
            onDescripcionChanged = { numero, desc -> actualizarDescripcionPaso(numero, desc) }
        )
        binding.rvPasos.layoutManager = LinearLayoutManager(this)
        binding.rvPasos.adapter = pasoAdapter
        actualizarListaPasos()
    }

    private fun setupClickListeners() {
        binding.cardImagen.setOnClickListener { mostrarDialogoSeleccionarImagen() }
        binding.btnTomarFoto.setOnClickListener { verificarPermisoCamara() }
        binding.btnSeleccionarGaleria.setOnClickListener { verificarPermisoAlmacenamiento() }
        binding.btnAgregarPaso.setOnClickListener { agregarPaso() }
        binding.btnGuardar.setOnClickListener { validarYGuardar() }
        binding.btnEliminar.setOnClickListener {
            if (esModoEdicion && recetaExistente != null) confirmarEliminar()
        }
    }

    private fun cargarRecetaExistente(recetaId: Int) {
        viewModel.obtenerRecetaPorId(recetaId).observe(this) { receta ->
            receta?.let {
                recetaExistente = it
                binding.etTitulo.setText(it.titulo)
                binding.etCantidad.setText(it.cantidadPersonas)
                binding.etIngredientes.setText(it.ingredientes)

                // Procesar tiempo (Ej: "45 min" -> "45" y "min")
                val tiempoPartes = it.tiempoPreparacion.split(" ")
                if (tiempoPartes.isNotEmpty()) {
                    binding.etTiempo.setText(tiempoPartes[0])
                    if (tiempoPartes.size > 1) {
                        val unidad = tiempoPartes[1]
                        val index = if (unidad.contains("h")) 1 else 0
                        binding.spinnerUnidadTiempo.setSelection(index)
                    }
                }

                val deptoPosition = Constantes.DEPARTAMENTOS_BOLIVIA.indexOf(it.departamento)
                if (deptoPosition >= 0) binding.spinnerDepartamento.setSelection(deptoPosition)

                val catPosition = Constantes.CATEGORIAS.indexOf(it.categoria)
                if (catPosition >= 0) binding.spinnerCategoria.setSelection(catPosition)

                it.imagenUri?.let { uri ->
                    imagenUri = Uri.parse(uri)
                    ImageHelper.cargarImagen(binding.ivReceta, uri)
                }

                // Cargar pasos
                try {
                    val type = object : TypeToken<List<PasoPreparacion>>() {}.type
                    val pasosCargados: List<PasoPreparacion> = Gson().fromJson(it.preparacion, type)
                    pasos.clear()
                    pasos.addAll(pasosCargados)
                    actualizarListaPasos()
                } catch (e: Exception) {
                    if (it.preparacion.isNotEmpty()) {
                        pasos.clear()
                        pasos.add(PasoPreparacion(1, it.preparacion))
                        actualizarListaPasos()
                    }
                }

                binding.btnEliminar.visibility = if (it.esCreadaPorUsuario) View.VISIBLE else View.GONE
            }
        }
    }

    private fun agregarPaso() {
        val numeroSiguiente = pasos.size + 1
        pasos.add(PasoPreparacion(numeroSiguiente, ""))
        actualizarListaPasos()
    }

    private fun eliminarPaso(numero: Int) {
        pasos.removeIf { it.numero == numero }
        val nuevosPasos = pasos.mapIndexed { index, paso ->
            paso.copy(numero = index + 1)
        }
        pasos.clear()
        pasos.addAll(nuevosPasos)
        actualizarListaPasos()
    }

    private fun actualizarDescripcionPaso(numero: Int, descripcion: String) {
        val index = pasos.indexOfFirst { it.numero == numero }
        if (index != -1) {
            pasos[index] = pasos[index].copy(descripcion = descripcion)
        }
    }

    private fun actualizarImagenPaso(numero: Int, uri: String) {
        val index = pasos.indexOfFirst { it.numero == numero }
        if (index != -1) {
            pasos[index] = pasos[index].copy(imagenUri = uri)
            actualizarListaPasos()
        }
    }

    private fun quitarImagenPaso(numero: Int) {
        val index = pasos.indexOfFirst { it.numero == numero }
        if (index != -1) {
            pasos[index] = pasos[index].copy(imagenUri = null)
            actualizarListaPasos()
        }
    }

    private fun actualizarListaPasos() {
        pasoAdapter.submitList(pasos.toList())
        binding.tvSinPasos.visibility = if (pasos.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogoImagenPaso(numeroPaso: Int) {
        pasoEditandoImagen = numeroPaso
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        AlertDialog.Builder(this)
            .setTitle("Imagen para Paso $numeroPaso")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamaraPaso()
                    1 -> verificarPermisoAlmacenamientoPaso()
                }
            }
            .show()
    }

    private fun verificarPermisoCamaraPaso() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            tomarFotoPaso()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun verificarPermisoAlmacenamientoPaso() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            seleccionarDeGaleriaPaso()
        } else {
            storagePermissionLauncher.launch(permission)
        }
    }

    private fun tomarFotoPaso() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        val photoFile = File.createTempFile("PASO_${timeStamp}_", ".jpg", storageDir)
        currentPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePictureLauncherForPaso.launch(photoUri)
    }

    private fun seleccionarDeGaleriaPaso() {
        pickImageLauncherForPaso.launch("image/*")
    }

    private fun mostrarDialogoSeleccionarImagen() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        AlertDialog.Builder(this)
            .setTitle("Imagen de la Receta")
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
        val photoFile = File.createTempFile("RECETA_${timeStamp}_", ".jpg", storageDir)
        currentPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(photoUri)
    }

    private fun seleccionarDeGaleria() {
        pickImageLauncher.launch("image/*")
    }

    private fun validarYGuardar() {
        val titulo = binding.etTitulo.text.toString().trim()
        val tiempoNum = binding.etTiempo.text.toString().trim()
        val unidadTiempo = binding.spinnerUnidadTiempo.selectedItem.toString()
        val tiempoFormateado = if (tiempoNum.isNotEmpty()) "$tiempoNum $unidadTiempo" else ""
        
        val cantidad = binding.etCantidad.text.toString().trim()
        val ingredientes = binding.etIngredientes.text.toString().trim()
        val departamento = binding.spinnerDepartamento.selectedItem.toString()
        val categoria = binding.spinnerCategoria.selectedItem.toString()

        if (titulo.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val preparacionJson = Gson().toJson(pasos)

        val receta = if (esModoEdicion && recetaExistente != null) {
            recetaExistente!!.copy(
                titulo = titulo,
                tiempoPreparacion = tiempoFormateado,
                cantidadPersonas = cantidad,
                ingredientes = ingredientes,
                preparacion = preparacionJson,
                categoria = categoria,
                departamento = departamento,
                imagenUri = imagenUri?.toString()
            )
        } else {
            Receta(
                titulo = titulo,
                tiempoPreparacion = tiempoFormateado,
                cantidadPersonas = cantidad,
                ingredientes = ingredientes,
                preparacion = preparacionJson,
                categoria = categoria,
                departamento = departamento,
                imagenUri = imagenUri?.toString(),
                esCreadaPorUsuario = true
            )
        }

        if (esModoEdicion) {
            viewModel.actualizarReceta(receta) { success, error ->
                if (success) {
                    Toast.makeText(this, "Receta actualizada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, error ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            viewModel.crearReceta(receta) { success, error ->
                if (success) {
                    Toast.makeText(this, "Receta creada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, error ?: "Error al crear", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmarEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Receta")
            .setMessage("¿Estás seguro de que deseas eliminar esta receta?")
            .setPositiveButton("Eliminar") { _, _ ->
                recetaExistente?.let {
                    viewModel.eliminarReceta(it) { success, error ->
                        if (success) {
                            Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, error ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
