package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.databinding.ActivityRecetaDetalleBinding
import com.example.recetarioboliviano.modelo.entidades.PasoPreparacion
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.util.ImageHelper
import com.example.recetarioboliviano.vista.adaptadores.PasoDetalleAdapter
import com.example.recetarioboliviano.vistamodelo.CarpetaViewModel
import com.example.recetarioboliviano.vistamodelo.CarpetaViewModelFactory
import com.example.recetarioboliviano.vistamodelo.RecetaViewModel
import com.google.android.material.chip.Chip
import coil.load
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Activity para mostrar los detalles de una receta.
 */
class RecetaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecetaDetalleBinding
    private val viewModel: RecetaViewModel by viewModels()
    private val carpetaViewModel: CarpetaViewModel by viewModels {
        CarpetaViewModelFactory((application as RecetarioApp).repository)
    }
    private lateinit var pasoAdapter: PasoDetalleAdapter

    private var recetaActual: Receta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecetaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recetaId = intent.getIntExtra("receta_id", -1)
        if (recetaId != -1) {
            setupRecyclerView()
            cargarReceta(recetaId)
        } else {
            Toast.makeText(this, "Error al cargar receta", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        pasoAdapter = PasoDetalleAdapter()
        binding.rvPasos.layoutManager = LinearLayoutManager(this)
        binding.rvPasos.adapter = pasoAdapter
    }

    private fun setupClickListeners() {
        binding.btnFavorito.setOnClickListener {
            onFavoritoClick()
        }
    }

    private fun cargarReceta(recetaId: Int) {
        viewModel.obtenerRecetaPorId(recetaId).observe(this) { receta ->
            receta?.let {
                recetaActual = it
                mostrarReceta(it)
            }
        }

        carpetaViewModel.obtenerCarpetasDeReceta(recetaId).observe(this) { recetaConCarpetas ->
            binding.cgCarpetas.removeAllViews()
            recetaConCarpetas?.carpetas?.forEach { carpeta ->
                val chip = Chip(this)
                chip.text = carpeta.nombre
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    carpetaViewModel.eliminarRecetaDeCarpeta(recetaId, carpeta.id)
                }
                binding.cgCarpetas.addView(chip)
            }
        }
    }

    private fun mostrarReceta(receta: Receta) {
        binding.tvTitulo.text = receta.titulo
        binding.tvDepartamento.text = receta.departamento
        binding.tvCategoria.text = receta.categoria
        binding.tvTiempo.text = receta.tiempoPreparacion
        binding.tvCantidad.text = receta.cantidadPersonas
        binding.tvIngredientes.text = receta.ingredientes

        // Mostrar badge si es del usuario
        binding.chipMiReceta.visibility = if (receta.esCreadaPorUsuario) View.VISIBLE else View.GONE

        // Actualizar icono de favorito
        actualizarIconoFavorito(receta.esFavorito)

        // Cargar imagen principal de forma híbrida (Internet / Memoria local / Drawable)
        val uriPortada = receta.imagenUri
        if (!uriPortada.isNullOrEmpty()) {
            if (uriPortada.startsWith("http://") || uriPortada.startsWith("https://") || uriPortada.startsWith("content://") || uriPortada.startsWith("/")) {
                binding.ivReceta.load(uriPortada) {
                    crossfade(true)
                    placeholder(R.drawable.ic_image_placeholder)
                    error(R.drawable.ic_image_placeholder)
                }
            } else {
                val resourceId = resources.getIdentifier(uriPortada, "drawable", packageName)
                if (resourceId != 0) {
                    binding.ivReceta.load(resourceId) { crossfade(true) }
                } else {
                    binding.ivReceta.load(R.drawable.ic_image_placeholder)
                }
            }
        } else {
            binding.ivReceta.load(R.drawable.ic_image_placeholder)
        }

        // Procesar pasos de preparación
        procesarPasos(receta.preparacion)
    }

    private fun procesarPasos(preparacion: String) {
        try {
            // Intentar parsear como JSON (para recetas nuevas)
            val type = object : TypeToken<List<PasoPreparacion>>() {}.type
            val pasos: List<PasoPreparacion> = Gson().fromJson(preparacion, type)
            
            if (pasos.isNotEmpty()) {
                binding.rvPasos.visibility = View.VISIBLE
                binding.tvPreparacionLegacy.visibility = View.GONE
                pasoAdapter.submitList(pasos)
            } else {
                mostrarPreparacionLegacy(preparacion)
            }
        } catch (e: Exception) {
            // Si falla, es formato texto antiguo (recetas predefinidas)
            mostrarPreparacionLegacy(preparacion)
        }
    }

    private fun mostrarPreparacionLegacy(preparacion: String) {
        binding.rvPasos.visibility = View.GONE
        binding.tvPreparacionLegacy.visibility = View.VISIBLE
        binding.tvPreparacionLegacy.text = preparacion
    }

    private fun actualizarIconoFavorito(esFavorito: Boolean) {
        val iconRes = if (esFavorito) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        binding.btnFavorito.setImageResource(iconRes)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detalle_receta, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_organizar -> {
                mostrarDialogoOrganizar()
                true
            }
            R.id.action_editar -> {
                if (recetaActual?.esCreadaPorUsuario == true) {
                    val intent = Intent(this, RecetaFormActivity::class.java)
                    intent.putExtra("receta_id", recetaActual!!.id)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Solo puedes editar tus recetas", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_eliminar -> {
                if (recetaActual?.esCreadaPorUsuario == true) {
                    confirmarEliminar()
                } else {
                    Toast.makeText(this, "Solo puedes eliminar tus recetas", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onFavoritoClick() {
        recetaActual?.let { receta ->
            viewModel.toggleFavorito(receta)
            val mensaje = if (!receta.esFavorito) "Añadido a favoritos" else "Eliminado de favoritos"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Receta")
            .setMessage("¿Está seguro de eliminar esta receta?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarReceta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarReceta() {
        recetaActual?.let { receta ->
            viewModel.eliminarReceta(receta) { success, error ->
                if (success) {
                    Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, error ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarDialogoOrganizar() {
        carpetaViewModel.todasLasCarpetas.observe(this) { carpetas ->
            if (carpetas.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Sin Colecciones")
                    .setMessage("No tienes colecciones creadas. ¿Deseas crear una ahora?")
                    .setPositiveButton("Crear") { _, _ ->
                        startActivity(Intent(this, CarpetasActivity::class.java))
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                val nombres = carpetas.map { it.nombre }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Añadir a Colección")
                    .setItems(nombres) { _, which ->
                        val carpeta = carpetas[which]
                        recetaActual?.let { receta ->
                            carpetaViewModel.agregarRecetaACarpeta(receta.id, carpeta.id)
                            Toast.makeText(this, "Añadida a ${carpeta.nombre}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }
}
