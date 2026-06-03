package com.example.recetarioboliviano.vista.adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recetarioboliviano.databinding.ItemCarpetaBinding
import com.example.recetarioboliviano.modelo.entidades.Carpeta

class CarpetaAdapter(
    private val onItemClick: (Carpeta) -> Unit,
    private val onEliminarClick: (Carpeta) -> Unit,
    private val onEditarClick: (Carpeta) -> Unit
) : ListAdapter<Carpeta, CarpetaAdapter.CarpetaViewHolder>(CarpetaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarpetaViewHolder {
        val binding = ItemCarpetaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarpetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarpetaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CarpetaViewHolder(private val binding: ItemCarpetaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(carpeta: Carpeta) {
            binding.tvNombreCarpeta.text = carpeta.nombre
            // Por ahora hardcoded, en una implementación real podríamos pasar el conteo
            binding.tvCantidadRecetas.text = "Ver recetas"
            
            binding.root.setOnClickListener { onItemClick(carpeta) }
            binding.root.setOnLongClickListener {
                onEditarClick(carpeta)
                true
            }
            binding.btnEliminarCarpeta.setOnClickListener { onEliminarClick(carpeta) }
        }
    }

    class CarpetaDiffCallback : DiffUtil.ItemCallback<Carpeta>() {
        override fun areItemsTheSame(oldItem: Carpeta, newItem: Carpeta): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Carpeta, newItem: Carpeta): Boolean = oldItem == newItem
    }
}
