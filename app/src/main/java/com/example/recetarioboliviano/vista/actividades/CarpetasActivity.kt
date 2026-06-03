package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.databinding.ActivityCarpetasBinding
import com.example.recetarioboliviano.modelo.entidades.Carpeta
import com.example.recetarioboliviano.vista.adaptadores.CarpetaAdapter
import com.example.recetarioboliviano.vistamodelo.CarpetaViewModel
import com.example.recetarioboliviano.vistamodelo.CarpetaViewModelFactory

class CarpetasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarpetasBinding
    private val viewModel: CarpetaViewModel by viewModels {
        CarpetaViewModelFactory((application as RecetarioApp).repository)
    }
    private lateinit var adapter: CarpetaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarpetasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Mis Colecciones"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = CarpetaAdapter(
            onItemClick = { carpeta ->
                val intent = Intent(this, RecetasCarpetaActivity::class.java)
                intent.putExtra("carpeta_id", carpeta.id)
                intent.putExtra("carpeta_nombre", carpeta.nombre)
                startActivity(intent)
            },
            onEliminarClick = { carpeta ->
                mostrarDialogoEliminar(carpeta)
            }
        )
        binding.rvCarpetas.layoutManager = LinearLayoutManager(this)
        binding.rvCarpetas.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAgregarCarpeta.setOnClickListener {
            mostrarDialogoNuevaCarpeta()
        }
    }

    private fun observeData() {
        viewModel.todasLasCarpetas.observe(this) { carpetas ->
            adapter.submitList(carpetas)
            binding.tvVacio.visibility = if (carpetas.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun mostrarDialogoNuevaCarpeta() {
        val input = EditText(this)
        input.hint = "Nombre de la colección"
        
        AlertDialog.Builder(this)
            .setTitle("Nueva Colección")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = input.text.toString()
                if (nombre.isNotBlank()) {
                    viewModel.crearCarpeta(nombre)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminar(carpeta: Carpeta) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Colección")
            .setMessage("¿Estás seguro de que deseas eliminar '${carpeta.nombre}'? Las recetas no se borrarán del sistema.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarCarpeta(carpeta)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
