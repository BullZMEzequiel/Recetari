package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.databinding.ActivityRecetasCarpetaBinding
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.vista.adaptadores.RecetaAdapter
import com.example.recetarioboliviano.vistamodelo.CarpetaViewModel
import com.example.recetarioboliviano.vistamodelo.CarpetaViewModelFactory
import com.example.recetarioboliviano.vistamodelo.RecetaViewModel

class RecetasCarpetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecetasCarpetaBinding
    private val carpetaViewModel: CarpetaViewModel by viewModels {
        CarpetaViewModelFactory((application as RecetarioApp).repository)
    }
    private val recetaViewModel: RecetaViewModel by viewModels()
    private lateinit var adapter: RecetaAdapter
    private var carpetaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecetasCarpetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carpetaId = intent.getIntExtra("carpeta_id", -1)
        val carpetaNombre = intent.getStringExtra("carpeta_nombre") ?: "Colección"

        if (carpetaId != -1) {
            carpetaViewModel.setCarpetaActual(carpetaId)
        }

        setupToolbar(carpetaNombre)
        setupSearchView()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar(nombre: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = nombre
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                carpetaViewModel.buscarEnCarpeta(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                carpetaViewModel.buscarEnCarpeta(newText ?: "")
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = RecetaAdapter(
            onItemClick = { receta ->
                val intent = Intent(this, RecetaDetalleActivity::class.java)
                intent.putExtra("receta_id", receta.id)
                startActivity(intent)
            },
            onFavoritoClick = { receta -> recetaViewModel.toggleFavorito(receta) }
        )
        binding.rvRecetas.layoutManager = GridLayoutManager(this, 2)
        binding.rvRecetas.adapter = adapter
    }

    private fun observeData() {
        carpetaViewModel.recetasDeCarpeta.observe(this) { recetas ->
            adapter.submitList(recetas)
            binding.tvVacio.visibility = if (recetas.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
